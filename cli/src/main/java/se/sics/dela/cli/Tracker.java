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
package se.sics.dela.cli;

import com.google.gson.Gson;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.function.Consumer;
import se.sics.dela.cli.dto.JsonResponse;
import se.sics.dela.cli.dto.SearchServiceDTO;
import se.sics.dela.cli.util.ManagedClientException;
import se.sics.dela.cli.util.UnknownClientException;
import se.sics.ktoolbox.httpsclient.WebClient;
import se.sics.ktoolbox.httpsclient.WebResponse;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class Tracker {

  public static class Target {

    public static String HOPS = "https://hops.site:51081/hops-site/api";
    public static String BBC5 = "https://bbc5.sics.se:43080/hops-site/api";
    public static String BBC5_TEST = "https://bbc5.sics.se:52300/hops-site/api";
  }

  public static class Path {

    public static String search() {
      return "public/dataset/search";
    }

    public static String searchResult(String sessionId, int startItem, int nrItems) {
      return "public/dataset/search/" + sessionId + "/page/" + startItem + "/" + nrItems;
    }

    public static String datasetDetails(String publicDSId) {
      return "public/dataset/" + publicDSId + "/details";
    }
  }

  public static class Printers {

    public static Consumer<SearchServiceDTO.Item[]> searchResultConsolePrinter(final PrintWriter out) {
      return new Consumer<SearchServiceDTO.Item[]>() {
        @Override
        public void accept(SearchServiceDTO.Item[] items) {
          out.printf("%20s", "DatasetId");
          out.printf(" | DatasetName\n");
          for (SearchServiceDTO.Item item : items) {
            datasetIdFormat(out, item.getPublicDSId());
            out.printf(" | %s", item.getDataset().getName());
            out.printf("\n");
          }
        }
      };
    }

    private static void datasetIdFormat(PrintWriter out, String datasetId) {
      if (datasetId.length() < 20) {
        out.printf("%20s", datasetId);
      } else if (datasetId.length() < 50) {
        out.printf("%50s", datasetId);
      } else if (datasetId.length() < 100) {
        out.printf("%100s", datasetId);
      } else {
        out.printf(datasetId);
      }
    }
  }

  public static class Ops {

    public static SearchServiceDTO.Item[] search(String target, String term) throws UnknownClientException {
      try (WebClient client = WebClient.httpsInstance()) {
        SearchServiceDTO.Params searchParam = new SearchServiceDTO.Params(term);
        WebResponse resp;

        try {
          resp = client
            .setTarget(target)
            .setPath(Path.search())
            .setPayload(searchParam)
            .doPost();
          if (!resp.statusOk()) {
            Optional<JsonResponse> errorDesc = getErrorDesc(resp);
            if (errorDesc.isPresent()) {
              throw new UnknownClientException(errorDesc.get().getErrorMsg());
            } else {
              throw new UnknownClientException("tracker communication failed with status:" + resp.response.getStatus());
            }
          }
          SearchServiceDTO.SearchResult pageResult = resp.readContent(SearchServiceDTO.SearchResult.class);
          resp = client
            .setPath(Path.searchResult(pageResult.getSessionId(), 0, pageResult.getNrHits()))
            .setPayload(null)
            .doGet();
          if (!resp.statusOk()) {
            Optional<JsonResponse> errorDesc = getErrorDesc(resp);
            if (errorDesc.isPresent()) {
              throw new UnknownClientException(errorDesc.get().getErrorMsg());
            } else {
              throw new UnknownClientException("tracker communication failed with status:" + resp.response.getStatus());
            }
          }
          SearchServiceDTO.Item[] result = parseSearchResult(resp);
          return result;
        } catch (UnknownClientException ex) {
          throw ex;
        } catch (Exception ex) {
          throw new UnknownClientException(ex);
        }
      }
    }

    public static SearchServiceDTO.ItemDetails datasetDetails(String target, String publicDSId) throws
      UnknownClientException, ManagedClientException {
      try (WebClient client = WebClient.httpsInstance()) {
        WebResponse resp = client
          .setTarget(target)
          .setPath(Path.datasetDetails(publicDSId))
          .doGet();
        if (!resp.statusOk()) {
          Optional<JsonResponse> errorDesc = getErrorDesc(resp);
          if (errorDesc.isPresent()) {
            if (errorDesc.get().getErrorMsg().equals("no dataset")) {
              throw new ManagedClientException("wrong dataset id - no such dataset on tracker");
            } else {
              throw new UnknownClientException(errorDesc.get().getErrorMsg());
            }
          } else {
            throw new UnknownClientException("tracker communication failed with status:" + resp.response.getStatus());
          }
        }
        SearchServiceDTO.ItemDetails result = resp.readContent(SearchServiceDTO.ItemDetails.class);
        return result;
      } catch (UnknownClientException | ManagedClientException ex) {
        throw ex;
      } catch (Exception ex) {
        throw new UnknownClientException(ex);
      }
    }

    private static SearchServiceDTO.Item[] parseSearchResult(WebResponse wResult) {
      String sResult = wResult.response.readEntity(String.class);
      SearchServiceDTO.Item[] result = new Gson().fromJson(sResult, SearchServiceDTO.Item[].class);
      return result;
    }

    private static Optional<JsonResponse> getErrorDesc(WebResponse resp) {
      try {
        JsonResponse errorDetails = resp.readErrorDetails(JsonResponse.class);
        return Optional.of(errorDetails);
      } catch (IllegalStateException ex) {
        return Optional.empty();
      }
    }
  }
}
