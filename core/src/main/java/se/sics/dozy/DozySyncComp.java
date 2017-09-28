/*
 * Copyright (C) 2016 Swedish Institute of Computer Science (SICS) Copyright (C)
 * 2016 Royal Institute of Technology (KTH)
 *
 * Dozy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.sics.dozy;

import com.google.common.util.concurrent.SettableFuture;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.Negative;
import se.sics.kompics.PortType;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.id.Identifiable;
import se.sics.kompics.id.Identifier;
import se.sics.kompics.timer.CancelTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class DozySyncComp extends ComponentDefinition implements DozySyncI {

  private static final Logger LOG = LoggerFactory.getLogger(DozySyncComp.class);
  private String logPrefix = "";

  //*******************************CONENCTIONS********************************
  //****************************CONNECT_EXTERNALY*****************************
  private Positive<Timer> timerPort = requires(Timer.class);
  private Positive servicePort;
  //******************************INTERNAL_ONLY*******************************
  private Negative<DozySyncPort> selfPort = provides(DozySyncPort.class);
  //******************************INTERNAL_STATE******************************
  //<reqId, futureResult, timeoutId>
  private Triplet<Identifier, SettableFuture, UUID> pendingJob = null;
  private LinkedList<DozySyncEvent<?>> pending = new LinkedList<>();

  public DozySyncComp(Init init) {
    LOG.info("{}initiating...", logPrefix);

    servicePort = requires(init.portType);

    subscribe(handleStart, control);
    subscribe(handleRequest, selfPort);
    subscribe(handleTimeout, timerPort);
    for (Class<? extends KompicsEvent> responseType : init.responseTypes) {
      LOG.info("{}subscribing handler for:{} on:{}", new Object[]{logPrefix, responseType, servicePort.getPortType().
        getClass().getCanonicalName()});
      Handler responseHandler = new Handler(responseType) {
        @Override
        public void handle(KompicsEvent resp) {
          Identifier respId = ((Identifiable) resp).getId();
          LOG.debug("{}while waiting for:{} received:{}", new Object[]{logPrefix, (pendingJob == null ? null
            : pendingJob.getValue0()), respId});
          if (pendingJob != null && pendingJob.getValue0().equals(respId)) {
            LOG.info("{}received response:{}", logPrefix, resp);
            pendingJob.getValue1().set(DozyResult.ok(resp));
            cancelTimeout(pendingJob.getValue2());
            pendingJob = null;
            if(!pending.isEmpty()) {
              doRequest(pending.removeFirst());
            }
          } else {
            LOG.debug("{}late response:{}", logPrefix, resp);
          }
        }
      };
      subscribe(responseHandler, servicePort);
    }
  }

  @Override
  public boolean isReady() {
    return this.getComponentCore().state().equals(Component.State.ACTIVE);
  }

  @Override
  public <E extends KompicsEvent & Identifiable> DozyResult sendReq(E req, long timeout) {
    SettableFuture<DozyResult> futureResult = SettableFuture.create();
    trigger(new DozySyncEvent(req, futureResult, timeout), selfPort.getPair());
    DozyResult result;
    try {
      result = futureResult.get();
    } catch (InterruptedException ex) {
      result = DozyResult.internalError("dozy problem");
    } catch (ExecutionException ex) {
      result = DozyResult.internalError("dozy problem");
    }
    return result;
  }

  private Handler handleStart = new Handler<Start>() {
    @Override
    public void handle(Start event) {
      LOG.info("{}starting...", logPrefix);
    }
  };

  private Handler handleRequest = new Handler<DozySyncEvent<?>>() {
    @Override
    public void handle(DozySyncEvent<?> event) {
      if (pendingJob != null) {
        pending.add(event);
        return;
      }
      doRequest(event);
    }
  };

  private void doRequest(DozySyncEvent<?> event) {
    LOG.info("{}received request:{}", logPrefix, event.req);
    UUID tId = scheduleRequestTimeout(event.timeout);
    pendingJob = Triplet.with(event.req.getId(), event.result, tId);
    trigger(event.req, servicePort);
  }

  private Handler handleTimeout = new Handler<RequestTimeout>() {
    @Override
    public void handle(RequestTimeout timeout) {
      if (pendingJob != null && pendingJob.getValue2().equals(timeout.getTimeoutId())) {
        LOG.info("{}timed out:{}", logPrefix, pendingJob.getValue0());
        pendingJob.getValue1().set(DozyResult.timeout());
        pendingJob = null;
        if (!pending.isEmpty()) {
          doRequest(pending.removeFirst());
        }
      } else {
        LOG.debug("{}late timeout:{}", logPrefix, timeout.getTimeoutId());
      }
    }
  };

  public static class Init extends se.sics.kompics.Init<DozySyncComp> {

    public final Class<? extends PortType> portType;
    public final List<Class<? extends KompicsEvent>> responseTypes;

    public Init(Class<? extends PortType> portType, List<Class<? extends KompicsEvent>> responseTypes) {
      this.portType = portType;
      this.responseTypes = responseTypes;
    }
  }

  private void cancelTimeout(UUID timeoutId) {
    CancelTimeout cpt = new CancelTimeout(timeoutId);
    trigger(cpt, timerPort);

  }

  private UUID scheduleRequestTimeout(long timeout) {
    ScheduleTimeout spt = new ScheduleTimeout(timeout);
    RequestTimeout sc = new RequestTimeout(spt);
    spt.setTimeoutEvent(sc);
    trigger(spt, timerPort);
    return sc.getTimeoutId();
  }

  private static class RequestTimeout extends Timeout {

    RequestTimeout(ScheduleTimeout st) {
      super(st);
    }

    @Override
    public String toString() {
      return "RequestTimeout<" + getTimeoutId() + ">";
    }
  }
}
