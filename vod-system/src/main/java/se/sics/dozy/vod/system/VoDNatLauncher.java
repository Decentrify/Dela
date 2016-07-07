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
package se.sics.dozy.vod.system;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.caracaldb.MessageRegistrator;
import se.sics.dozy.DozyResource;
import se.sics.dozy.DozySyncComp;
import se.sics.dozy.DozySyncI;
import se.sics.dozy.dropwizard.DropwizardDozy;
import se.sics.dozy.vod.ContentsSummaryREST;
import se.sics.dozy.vod.DozyVoD;
import se.sics.dozy.vod.TorrentExtendedStatusREST;
import se.sics.dozy.vod.VoDEndpointREST;
import se.sics.dozy.vod.hops.HopsTorrentDownloadREST;
import se.sics.dozy.vod.hops.HopsTorrentStopREST;
import se.sics.dozy.vod.hops.HopsTorrentUploadREST;
import se.sics.dozy.vod.hops.helper.HDFSAvroFileCreateREST;
import se.sics.dozy.vod.hops.helper.HDFSConnectionREST;
import se.sics.dozy.vod.hops.helper.HDFSFileCreateREST;
import se.sics.dozy.vod.hops.helper.HDFSFileDeleteREST;
import se.sics.gvod.network.GVoDSerializerSetup;
import se.sics.gvod.stream.mngr.LibraryPort;
import se.sics.gvod.stream.mngr.SystemPort;
import se.sics.gvod.stream.mngr.VoDMngrComp;
import se.sics.gvod.stream.mngr.event.ContentsSummaryEvent;
import se.sics.gvod.stream.mngr.event.TorrentExtendedStatusEvent;
import se.sics.gvod.stream.mngr.event.system.SystemAddressEvent;
import se.sics.gvod.stream.mngr.hops.HopsPort;
import se.sics.gvod.stream.mngr.hops.HopsTorrentPort;
import se.sics.gvod.stream.mngr.hops.helper.event.HDFSAvroFileCreateEvent;
import se.sics.gvod.stream.mngr.hops.helper.event.HDFSConnectionEvent;
import se.sics.gvod.stream.mngr.hops.helper.event.HDFSFileCreateEvent;
import se.sics.gvod.stream.mngr.hops.helper.event.HDFSFileDeleteEvent;
import se.sics.gvod.stream.mngr.hops.torrent.event.HopsTorrentDownloadEvent;
import se.sics.gvod.stream.mngr.hops.torrent.event.HopsTorrentStopEvent;
import se.sics.gvod.stream.mngr.hops.torrent.event.HopsTorrentUploadEvent;
import se.sics.kompics.Channel;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Init;
import se.sics.kompics.Kompics;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.config.ConfigException;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;
import se.sics.ktoolbox.croupier.CroupierSerializerSetup;
import se.sics.ktoolbox.gradient.GradientSerializerSetup;
import se.sics.ktoolbox.netmngr.NetworkMngrSerializerSetup;
import se.sics.ktoolbox.netmngr.event.NetMngrReady;
import se.sics.ktoolbox.overlaymngr.OMngrSerializerSetup;
import se.sics.ktoolbox.util.network.KAddress;
import se.sics.ktoolbox.util.setup.BasicSerializerSetup;
import se.sics.ktoolbox.util.status.Status;
import se.sics.ktoolbox.util.status.StatusPort;
import se.sics.nat.mngr.SimpleNatMngrComp;
import se.sics.nat.stun.StunSerializerSetup;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class VoDNatLauncher extends ComponentDefinition {

    private Logger LOG = LoggerFactory.getLogger(VoDNatLauncher.class);
    private String logPrefix = "";

    //*****************************CONNECTIONS**********************************
    //********************INTERNAL_DO_NOT_CONNECT_TO****************************
    private Positive<StatusPort> otherStatusPort = requires(StatusPort.class);
    //****************************EXTERNAL_STATE********************************
    private KAddress selfAdr;
    //****************************INTERNAL_STATE********************************
    private Component timerComp;
    private Component networkMngrComp;
    private Component vodMngrComp;
    private Component systemSyncIComp;
    private Component hopsHelperSyncIComp;
    private Component hopsTorrentSyncIComp;
    private DropwizardDozy webserver;

    public VoDNatLauncher() {
        LOG.info("{}starting...", logPrefix);

        subscribe(handleStart, control);
        subscribe(handleNetReady, otherStatusPort);

        registerSerializers();
        registerPortTracking();
    }

    private void registerSerializers() {
        MessageRegistrator.register();
        int serializerId = 128;
        serializerId = BasicSerializerSetup.registerBasicSerializers(serializerId);
        serializerId = CroupierSerializerSetup.registerSerializers(serializerId);
        serializerId = GradientSerializerSetup.registerSerializers(serializerId);
        serializerId = OMngrSerializerSetup.registerSerializers(serializerId);
        serializerId = NetworkMngrSerializerSetup.registerSerializers(serializerId);
        serializerId = StunSerializerSetup.registerSerializers(serializerId);
        serializerId = GVoDSerializerSetup.registerSerializers(serializerId);
    }

    private void registerPortTracking() {
    }

    Handler handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            LOG.info("{}starting", logPrefix);

            timerComp = create(JavaTimer.class, Init.NONE);
            setNetworkMngr();

            trigger(Start.event, timerComp.control());
            trigger(Start.event, networkMngrComp.control());
        }
    };

    private void setNetworkMngr() {
        LOG.info("{}setting up network mngr", logPrefix);
        SimpleNatMngrComp.ExtPort netExtPorts = new SimpleNatMngrComp.ExtPort(timerComp.getPositive(Timer.class));
        networkMngrComp = create(SimpleNatMngrComp.class, new SimpleNatMngrComp.Init(netExtPorts));
        connect(networkMngrComp.getPositive(StatusPort.class), otherStatusPort.getPair(), Channel.TWO_WAY);
    }

    ClassMatchedHandler handleNetReady
            = new ClassMatchedHandler<NetMngrReady, Status.Internal<NetMngrReady>>() {
                @Override
                public void handle(NetMngrReady content, Status.Internal<NetMngrReady> container) {
                    LOG.info("{}network mngr ready", logPrefix);
                    selfAdr = content.systemAdr;

                    setVoDMngr();
                    setSystemSyncI();
                    setHopsHelperSyncI();
                    setHopsTorrentSyncI();
                    setWebserver();

                    trigger(Start.event, vodMngrComp.control());
                    trigger(Start.event, systemSyncIComp.control());
                    trigger(Start.event, hopsHelperSyncIComp.control());
                    trigger(Start.event, hopsTorrentSyncIComp.control());

                    startWebserver();
                    LOG.info("{}starting complete...", logPrefix);
                }
            };

    private void setVoDMngr() {
        VoDMngrComp.ExtPort extPorts = new VoDMngrComp.ExtPort(timerComp.getPositive(Timer.class), networkMngrComp.getPositive(Network.class));
        vodMngrComp = create(VoDMngrComp.class, new VoDMngrComp.Init(extPorts, selfAdr));
    }
    
    private void setSystemSyncI() {
        List<Class<? extends KompicsEvent>> resp = new ArrayList<>();
        resp.add(SystemAddressEvent.Response.class);
        systemSyncIComp = create(DozySyncComp.class, new DozySyncComp.Init(SystemPort.class, resp));

        connect(systemSyncIComp.getNegative(Timer.class), timerComp.getPositive(Timer.class), Channel.TWO_WAY);
        connect(systemSyncIComp.getNegative(SystemPort.class), vodMngrComp.getPositive(SystemPort.class), Channel.TWO_WAY);
    }
    
    private void setHopsHelperSyncI() {
        List<Class<? extends KompicsEvent>> resp = new ArrayList<>();
        resp.add(HDFSConnectionEvent.Response.class);
        resp.add(HDFSFileDeleteEvent.Response.class);
        resp.add(HDFSFileCreateEvent.Response.class);
        resp.add(HDFSAvroFileCreateEvent.Response.class);
        hopsHelperSyncIComp = create(DozySyncComp.class, new DozySyncComp.Init(HopsPort.class, resp));
        
        connect(hopsHelperSyncIComp.getNegative(Timer.class), timerComp.getPositive(Timer.class), Channel.TWO_WAY);
        connect(hopsHelperSyncIComp.getNegative(HopsPort.class), vodMngrComp.getPositive(HopsPort.class), Channel.TWO_WAY);
    }
    
    private void setHopsTorrentSyncI() {
        List<Class<? extends KompicsEvent>> resp = new ArrayList<>();
        resp.add(HopsTorrentDownloadEvent.Response.class);
        resp.add(HopsTorrentUploadEvent.Response.class);
        resp.add(HopsTorrentStopEvent.Response.class);
        resp.add(ContentsSummaryEvent.Response.class);
        resp.add(TorrentExtendedStatusEvent.Response.class);
        
        hopsTorrentSyncIComp = create(DozySyncComp.class, new DozySyncComp.Init(HopsTorrentPort.class, resp));

        connect(hopsTorrentSyncIComp.getNegative(Timer.class), timerComp.getPositive(Timer.class), Channel.TWO_WAY);
        connect(hopsTorrentSyncIComp.getNegative(LibraryPort.class), vodMngrComp.getPositive(LibraryPort.class), Channel.TWO_WAY);
    }

    private void setWebserver() {
        Map<String, DozySyncI> synchronousInterfaces = new HashMap<>();
        synchronousInterfaces.put(DozyVoD.systemDozyName, (DozySyncI) systemSyncIComp.getComponent());
        synchronousInterfaces.put(DozyVoD.hopsHelperDozyName, (DozySyncI) hopsHelperSyncIComp.getComponent());
        synchronousInterfaces.put(DozyVoD.hopsTorrentDozyName, (DozySyncI) hopsTorrentSyncIComp.getComponent());

        List<DozyResource> resources = new ArrayList<>();
        resources.add(new VoDEndpointREST());
        resources.add(new ContentsSummaryREST());
        resources.add(new TorrentExtendedStatusREST());
        
        resources.add(new HopsTorrentDownloadREST.Basic());
        resources.add(new HopsTorrentDownloadREST.XML());
        resources.add(new HopsTorrentUploadREST.Basic());
        resources.add(new HopsTorrentUploadREST.XML());
        resources.add(new HopsTorrentStopREST());
        
        resources.add(new HDFSConnectionREST.Basic());
        resources.add(new HDFSConnectionREST.XML());
        resources.add(new HDFSFileDeleteREST());
        resources.add(new HDFSFileCreateREST());
        resources.add(new HDFSAvroFileCreateREST());

        webserver = new DropwizardDozy(synchronousInterfaces, resources);
    }

    private void startWebserver() {
        String webserviceConfig = config().getValue("webservice.server", String.class);
        LOG.info("{}webservices config:{}", logPrefix, webserviceConfig);
        String[] args = new String[]{"server", webserviceConfig};
        try {
            webserver.run(args);
        } catch (ConfigException ex) {
            LOG.error("{}configuration error:{}", logPrefix, ex.getMessage());
            throw new RuntimeException(ex);
        } catch (Exception ex) {
            LOG.error("{}dropwizard error:{}", logPrefix, ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public static void main(String[] args) throws IOException {
        if (Kompics.isOn()) {
            Kompics.shutdown();
        }
        Kompics.createAndStart(VoDNatLauncher.class, Runtime.getRuntime().availableProcessors(), 20); // Yes 20 is totally arbitrary
        try {
            Kompics.waitForTermination();
        } catch (InterruptedException ex) {
            System.exit(1);
        }
    }
}
