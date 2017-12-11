package se.sics.dela.client;

import com.google.gson.Gson;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import javax.ws.rs.client.WebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ComponentProxy;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.timer.CancelPeriodicTimeout;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;
import se.sics.ktoolbox.util.identifiable.BasicBuilders;
import se.sics.ktoolbox.util.identifiable.overlay.OverlayId;
import se.sics.ktoolbox.util.identifiable.overlay.OverlayIdFactory;
import se.sics.ktoolbox.util.network.KAddress;
import se.sics.nstream.hops.library.HopsTorrentPort;
import se.sics.nstream.hops.library.event.core.HopsTorrentDownloadEvent;
import se.sics.nstream.hops.storage.hdfs.HDFSEndpoint;
import se.sics.nstream.hops.storage.hdfs.HDFSResource;
import se.sics.nstream.library.event.torrent.TorrentExtendedStatusEvent;
import se.sics.nstream.library.util.TorrentState;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class Download {

  private static Logger LOG = LoggerFactory.getLogger(Launcher.class);

  public static Function<State, State> setupDownload = new Function<State, State>() {
    @Override
    public State apply(State s) {
      LOG.info("download dataset:{}", s.publicDSId);
      SearchDTO.ItemDetails result = Hopssite.getDatasetDetails(s.hopssite, s.publicDSId);
      HopsTorrentDownloadEvent.StartRequest req = Download.startRequest(s, result);
      LOG.info("bootstrap:{}", req.partners);
      s.proxy.trigger(req, s.torrentPort);
      scheduleDownloadStatus(s);
      return s;
    }
  };

  private static HopsTorrentDownloadEvent.StartRequest startRequest(State s, SearchDTO.ItemDetails item) {
    String torrentName = "";
    Integer projectId = 0;
    Integer datasetId = 0;
    HDFSEndpoint hdfsEndpoint = null;
    String dirPath = s.libDir + File.separator + s.publicDSId;
    HDFSResource manifestResource = new HDFSResource(dirPath, "manifest.json");
    List<KAddress> partners = new LinkedList<>();
    for (ClusterDTO.ClusterAddress adr : item.getBootstrap()) {
      ClusterDTO.DelaAddress aux = new Gson().fromJson(adr.getDelaTransferAddress(), ClusterDTO.DelaAddress.class);
      KAddress address = aux.resolve();
      partners.add(address);
    }
    return new HopsTorrentDownloadEvent.StartRequest(s.torrentId, torrentName, projectId, datasetId, hdfsEndpoint,
      manifestResource, partners);
  }

  private static void scheduleDownloadStatus(State s) {
    SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(s.downloadStatusPeriod, s.downloadStatusPeriod);
    StatusTimeout st = new StatusTimeout(spt);
    spt.setTimeoutEvent(st);
    s.setStatusTimer(st.getTimeoutId());
    s.proxy.trigger(spt, s.timerPort);
  }

  private static void cancelDownloadStatus(State s) {
    if (s.statusTimer.isPresent()) {
      CancelPeriodicTimeout cpd = new CancelPeriodicTimeout(s.statusTimer.get());
      s.proxy.trigger(cpd, s.timerPort);
      s.resetStatusTimer();
    }
  }

  public static class StatusTimeout extends Timeout {

    public StatusTimeout(SchedulePeriodicTimeout st) {
      super(st);
    }
  }

  public static Handler handleTorrentResult = new Handler<HopsTorrentDownloadEvent.TorrentResult>() {
    @Override
    public void handle(HopsTorrentDownloadEvent.TorrentResult event) {
      LOG.info("manifest");
    }
  };

  public static Handler getHandleDownloadStatus(final State s) {
    return new Handler<StatusTimeout>() {
      @Override
      public void handle(StatusTimeout t) {
        TorrentExtendedStatusEvent.Request req = new TorrentExtendedStatusEvent.Request(s.torrentId);
        s.pendingReq = req;
        s.proxy.trigger(req, s.torrentPort);
      }
    };
  }

  public static Handler getHandleDownloadStatusResponse(final State s) {
    return new Handler<TorrentExtendedStatusEvent.Response>() {
      @Override
      public void handle(TorrentExtendedStatusEvent.Response event) {
        if (s.pendingReq != null) {
          LOG.info("download:{}", new Object[]{event.result.getValue().getPercentageComplete()});
          s.pendingReq = null;
          if(event.result.getValue().torrentStatus.equals(TorrentState.UPLOADING)) {
            cancelDownloadStatus(s);
          }
        }
      }
    };
  }

  public static class State {

    //***************************************************SETUP**********************************************************
    private ComponentProxy proxy;
    private Positive<HopsTorrentPort> torrentPort;
    private Positive<Timer> timerPort;
    private OverlayIdFactory torrentIdFactory;
    private WebTarget hopssite;
    private String libDir;
    private long downloadStatusPeriod;

    private String publicDSId;
    private OverlayId torrentId;
    //***************************************************WORKING********************************************************
    private Optional<UUID> statusTimer = Optional.empty();
    //WAITING FIXES
    private TorrentExtendedStatusEvent.Request pendingReq; //TODO Alex fix broadcasting issue 
    //***************************************************SETUP**********************************************************

    public State setConnection(ComponentProxy proxy, Positive<HopsTorrentPort> torrentPort,
      Positive<Timer> timerPort) {
      this.proxy = proxy;
      this.torrentPort = torrentPort;
      this.timerPort = timerPort;
      return this;
    }

    public State setTorrentIdFactory(OverlayIdFactory torrentIdFactory) {
      this.torrentIdFactory = torrentIdFactory;
      return this;
    }

    public State setHopssite(WebTarget hopssite) {
      this.hopssite = hopssite;
      return this;
    }

    public State setLibDir(String libDir) {
      this.libDir = libDir;
      return this;
    }

    public State setDownloadStatusPeriod(long duration) {
      this.downloadStatusPeriod = duration;
      return this;
    }

    public State download(String publicDSId) {
      this.publicDSId = publicDSId;
      this.torrentId = torrentIdFactory.id(new BasicBuilders.StringBuilder(publicDSId));
      return this;
    }

    //***************************************************WORKING********************************************************
    private State setStatusTimer(UUID statusTimer) {
      this.statusTimer = Optional.of(statusTimer);
      return this;
    }

    private State resetStatusTimer() {
      this.statusTimer = Optional.empty();
      return this;
    }
  }
}
