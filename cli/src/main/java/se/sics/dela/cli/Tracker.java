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
import java.util.function.Consumer;
import se.sics.dela.cli.dto.SearchServiceDTO;
import se.sics.ktoolbox.httpsclient.WebClient;
import se.sics.ktoolbox.httpsclient.WebResponse;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class Tracker {

  public static class Target {

    public static String HOPS = "https://hops.site:443/hops-site/api";
    public static String BBC5 = "https://bbc5.sics.se:43080/hops-site/api";
    public static String BBC5_TEST = "https://localhost:52300/hops-site/api";
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

    public static Consumer<SearchServiceDTO.Item[]> searchResultConsolePrinter() {
      return new Consumer<SearchServiceDTO.Item[]>() {
        @Override
        public void accept(SearchServiceDTO.Item[] items) {
          for (SearchServiceDTO.Item item : items) {
            System.out.println(item.getPublicDSId() + " - " + item.getDataset().getName());
          }
          System.out.flush();
        }
      };
    }
  }

  public static class Ops {

    public static SearchServiceDTO.Item[] search(String target, String term) {
      try (WebClient client = WebClient.httpsInstance()) {
        SearchServiceDTO.Params searchParam = new SearchServiceDTO.Params(term);
        WebResponse resp;
        resp = client
          .setTarget(target)
          .setPath(Path.search())
          .setPayload(searchParam)
          .doPost();
        SearchServiceDTO.SearchResult pageResult = resp.readContent(SearchServiceDTO.SearchResult.class);

        resp = client
          .setPath(Path.searchResult(pageResult.getSessionId(), 0, pageResult.getNrHits()))
          .setPayload(null)
          .doGet();
        SearchServiceDTO.Item[] result = parseSearchResult(resp);

        return result;
      }
    }

    public static SearchServiceDTO.ItemDetails datasetDetails(String target, String publicDSId) {
      try (WebClient client = WebClient.httpsInstance()) {
        WebResponse resp = client
          .setTarget(target)
          .setPath(Path.datasetDetails(publicDSId))
          .doGet();
        SearchServiceDTO.ItemDetails result = resp.readContent(SearchServiceDTO.ItemDetails.class);
        return result;
      }
    }

    private static SearchServiceDTO.Item[] parseSearchResult(WebResponse wResult) {
      String sResult = wResult.response.readEntity(String.class);
      SearchServiceDTO.Item[] result = new Gson().fromJson(sResult, SearchServiceDTO.Item[].class);
      return result;
    }
  }
}
