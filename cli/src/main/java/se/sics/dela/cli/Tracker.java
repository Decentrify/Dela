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

import java.io.PrintWriter;
import java.util.function.BiFunction;
import se.sics.dela.cli.dto.tracker.SearchServiceDTO;
import se.sics.ktoolbox.webclient.WebClient;
import se.sics.ktoolbox.webclient.WebResponse;
import se.sics.ktoolbox.util.trysf.Try;
import static se.sics.ktoolbox.util.trysf.TryHelper.tryFSucc0;
import static se.sics.dela.cli.util.ExHelper.simpleTrackerExMapper;

public class Tracker {

  public static class Target {

    public static String HOPS = "https://hops.site:51081/hops-site/api";
    public static String BBC5 = "https://bbc5.sics.se:43080/hops-site/api";
    public static String BBC5_TEST = "https://bbc5.sics.se:52300/hops-site/api";

    public static String used() {
      return HOPS;
    }
  }

  public static class Path {

    public static String delaVersion() {
      return "/public/cluster/dela/version";
    }

    public static String search() {
      return "/public/dataset/search";
    }

    public static String searchResult(String sessionId, int startItem, int nrItems) {
      return "/public/dataset/search/" + sessionId + "/page/" + startItem + "/" + nrItems;
    }

    public static String datasetDetails(String publicDSId) {
      return "/public/dataset/" + publicDSId + "/details";
    }
  }

  public static class Printer {

    public static BiFunction<PrintWriter, SearchServiceDTO.Item[], PrintWriter> itemsPrinter() {
      return (PrintWriter out, SearchServiceDTO.Item[] items) -> {
        out.printf("%20s", "DatasetId");
        out.printf(" | DatasetName\n");
        for (SearchServiceDTO.Item item : items) {
          datasetIdFormat(out, item.getPublicDSId());
          out.printf(" | %s", item.getDataset().getName());
          out.printf("\n");
        }
        return out;
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

  public static class Rest {

    public static Try<String> delaVersion() {
      try (WebClient client = WebClient.httpsInstance()) {
        Try<String> result = client
          .setTarget(Target.used())
          .setPath(Path.delaVersion())
          .tryGet()
          .flatMap(WebResponse.readContent(String.class, simpleTrackerExMapper()));
        return result;
      }
    }

    public static Try<SearchServiceDTO.Item[]> trackerSearch(String term) {
      try (WebClient client = WebClient.httpsInstance()) {
        SearchServiceDTO.Params searchParam = new SearchServiceDTO.Params(term);
        WebResponse resp;

        Try<SearchServiceDTO.SearchResult> pageResult = client
          .setTarget(Target.used())
          .setPath(Path.search())
          .setPayload(searchParam)
          .tryPost()
          .flatMap(WebResponse.readContent(SearchServiceDTO.SearchResult.class, simpleTrackerExMapper()));
        if (pageResult.isFailure()) {
          return (Try.Failure) pageResult;
        }
        Try<SearchServiceDTO.Item[]> itemsResults = client
          .setTarget(Target.used())
          .setPath(Path.searchResult(pageResult.get().getSessionId(), 0, pageResult.get().getNrHits()))
          .setPayload(null)
          .tryGet()
          .flatMap(WebResponse.readContent(SearchServiceDTO.Item[].class, simpleTrackerExMapper()));
        return itemsResults;
      }
    }

    public static <O> BiFunction<O, Throwable, Try<SearchServiceDTO.ItemDetails>>
      trackerDatasetDetails(String publicDSId) {
      return tryFSucc0(() -> {
        try (WebClient client = WebClient.httpsInstance()) {
          Try<SearchServiceDTO.ItemDetails> result = client
            .setTarget(Target.used())
            .setPath(Path.datasetDetails(publicDSId))
            .setPayload(null)
            .tryGet()
            .flatMap(WebResponse.readContent(SearchServiceDTO.ItemDetails.class, simpleTrackerExMapper()));
          return result;
        }
      });
    }
  }
}
