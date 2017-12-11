package se.sics.dela.client;

import com.google.gson.Gson;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import se.sics.ktoolbox.util.identifiable.BasicBuilders;
import se.sics.ktoolbox.util.identifiable.overlay.OverlayId;
import se.sics.ktoolbox.util.identifiable.overlay.OverlayIdFactory;
import se.sics.ktoolbox.util.network.KAddress;
import se.sics.nstream.hops.library.event.core.HopsTorrentDownloadEvent;
import se.sics.nstream.hops.storage.hdfs.HDFSEndpoint;
import se.sics.nstream.hops.storage.hdfs.HDFSResource;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class Download {
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
    for(ClusterDTO.ClusterAddress adr : item.getBootstrap()) {
      ClusterDTO.DelaAddress aux = new Gson().fromJson(adr.getDelaTransferAddress(), ClusterDTO.DelaAddress.class);
      KAddress address = aux.resolve();
      partners.add(address);
    }
    return new HopsTorrentDownloadEvent.StartRequest(torrentId, torrentName, projectId, datasetId, hdfsEndpoint, 
      manifestResource, partners);
  }
}
