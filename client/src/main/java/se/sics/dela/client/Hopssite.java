package se.sics.dela.client;

import com.google.gson.Gson;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class Hopssite {
  private static Logger LOG = LoggerFactory.getLogger(Hopssite.class);
  
  public static String getDelaVersion(WebTarget hopssite) {
    WebTarget webTarget = hopssite.path(ClusterService.delaVersion());
    try {
      LOG.debug("path:{}", new Object[]{webTarget.getUri().toString()});
      Response response = webTarget.request(MediaType.APPLICATION_JSON).get();
      if (response.getStatus() == Response.Status.OK.getStatusCode()) {
        return response.readEntity(String.class);
      } else {
        throw new RuntimeException("problem contacting:" + webTarget.getUri().toString());
      }
    } catch (ProcessingException ex) {
      throw new RuntimeException("problem contacting:" + webTarget.getUri().toString());
    }
  }
  
  public static SearchDTO.ItemDetails getDatasetDetails(WebTarget hopssite, String publicDSId) {
    WebTarget webTarget = hopssite.path(DatasetService.datasetDetails(publicDSId));
    try {
      LOG.debug("path:{}", new Object[]{webTarget.getUri().toString()});
      Response response = webTarget.request(MediaType.APPLICATION_JSON).get();
      if (response.getStatus() == Response.Status.OK.getStatusCode()) {
        String resultValue = response.readEntity(String.class);
        LOG.debug("result:{}", resultValue);
        return new Gson().fromJson(resultValue, SearchDTO.ItemDetails.class);
      } else {
        throw new RuntimeException("problem contacting:" + webTarget.getUri().toString());
      }
    } catch (ProcessingException ex) {
      throw new RuntimeException("problem contacting:" + webTarget.getUri().toString());
    }
  }

  public static class ClusterService {

    public static String delaVersion() {
      return "public/cluster/dela/version";
    }
  }

  public static class DatasetService {

    public static String datasetDetails(String publicDSId) {
      return "public/dataset/" + publicDSId + "/details";
    }
  }
}
