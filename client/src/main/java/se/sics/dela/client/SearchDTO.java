package se.sics.dela.client;

import java.io.Serializable;
import java.util.List;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class SearchDTO {
  public static class ItemDetails implements Serializable {
    private DatasetDTO.Details dataset;
    private List<ClusterDTO.ClusterAddress> bootstrap;

    public ItemDetails() {
    }

    public ItemDetails(DatasetDTO.Details dataset, List<ClusterDTO.ClusterAddress> bootstrap) {
      this.dataset = dataset;
      this.bootstrap = bootstrap;
    }

    public DatasetDTO.Details getDataset() {
      return dataset;
    }

    public void setDataset(DatasetDTO.Details dataset) {
      this.dataset = dataset;
    }

    public List<ClusterDTO.ClusterAddress> getBootstrap() {
      return bootstrap;
    }

    public void setBootstrap(List<ClusterDTO.ClusterAddress> bootstrap) {
      this.bootstrap = bootstrap;
    }
  }
}
