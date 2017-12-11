package se.sics.dela.client;

import com.google.gson.Gson;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import javax.ws.rs.client.WebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ComponentProxy;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.ktoolbox.util.identifiable.BasicBuilders;
import se.sics.ktoolbox.util.identifiable.overlay.OverlayId;
import se.sics.ktoolbox.util.identifiable.overlay.OverlayIdFactory;
import se.sics.ktoolbox.util.network.KAddress;
import se.sics.nstream.hops.library.HopsTorrentPort;
import se.sics.nstream.hops.library.event.core.HopsTorrentDownloadEvent;
import se.sics.nstream.hops.storage.hdfs.HDFSEndpoint;
import se.sics.nstream.hops.storage.hdfs.HDFSResource;
import se.sics.nstream.library.event.torrent.TorrentExtendedStatusEvent;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class Download {

  private static Logger LOG = LoggerFactory.getLogger(Launcher.class);

  public static HopsTorrentDownloadEvent.StartRequest startRequest(OverlayIdFactory datasetIdFactory,
    String publicDSId, SearchDTO.ItemDetails item, String libPath) {
    OverlayId torrentId = datasetIdFactory.id(new BasicBuilders.StringBuilder(publicDSId));
    String torrentName = "";
    Integer projectId = 0;
    Integer datasetId = 0;
    HDFSEndpoint hdfsEndpoint = null;
    String dirPath = libPath + File.separator + publicDSId;
    HDFSResource manifestResource = new HDFSResource(dirPath, "manifest.json");
    List<KAddress> partners = new LinkedList<>();
    for (ClusterDTO.ClusterAddress adr : item.getBootstrap()) {
      ClusterDTO.DelaAddress aux = new Gson().fromJson(adr.getDelaTransferAddress(), ClusterDTO.DelaAddress.class);
      KAddress address = aux.resolve();
      partners.add(address);
    }
    return new HopsTorrentDownloadEvent.StartRequest(torrentId, torrentName, projectId, datasetId, hdfsEndpoint,
      manifestResource, partners);
  }

  public static Function<State, State> setupDownload = new Function<State, State>() {
    @Override
    public State apply(State s) {
      LOG.info("download dataset:{}", s.publicDSId);
      SearchDTO.ItemDetails result = Hopssite.getDatasetDetails(s.hopssite, s.publicDSId);
      HopsTorrentDownloadEvent.StartRequest req
        = Download.startRequest(s.torrentIdFactory, s.publicDSId, result, s.libDir);
      LOG.info("bootstrap:{}", req.partners);
      s.proxy.trigger(req, s.torrentPort);
      return s;
    }
  };

  public static Handler handleDownloadStatus = new Handler<TorrentExtendedStatusEvent.Response>() {
    @Override
    public void handle(TorrentExtendedStatusEvent.Response event) {
      LOG.info("download:{}", event.result.getValue().getPercentageComplete());
    }
  };

  public static class State {

    //***************************************************SETUP**********************************************************
    private ComponentProxy proxy;
    private Positive<HopsTorrentPort> torrentPort;
    private OverlayIdFactory torrentIdFactory;
    private WebTarget hopssite;
    private String publicDSId;
    private String libDir;
    //***************************************************WORKING********************************************************
    
    //***************************************************SETUP**********************************************************
    public State setConnection(ComponentProxy proxy, Positive<HopsTorrentPort> torrentPort) {
      this.proxy = proxy;
      this.torrentPort = torrentPort;
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
    
    public State download(String publicDSId) {
      this.publicDSId = publicDSId;
      return this;
    }
  }
}
