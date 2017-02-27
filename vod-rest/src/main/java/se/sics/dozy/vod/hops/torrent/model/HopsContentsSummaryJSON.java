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
package se.sics.dozy.vod.hops.torrent.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import se.sics.dozy.vod.model.ElementSummaryJSON;
import se.sics.gvod.mngr.util.ElementSummary;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class HopsContentsSummaryJSON {

  private Map<Integer, List<ElementSummaryJSON>> contents;

  public HopsContentsSummaryJSON(Map<Integer, List<ElementSummaryJSON>> contents) {
    this.contents = contents;
  }

  public HopsContentsSummaryJSON() {
  }

  public Map<Integer, List<ElementSummaryJSON>> getContents() {
    return contents;
  }

  public void setContents(Map<Integer, List<ElementSummaryJSON>> contents) {
    this.contents = contents;
  }

  public static HopsContentsSummaryJSON resolve(Map<Integer, List<ElementSummary>> contents) {
    Map<Integer, List<ElementSummaryJSON>> jsonContents = new TreeMap<>();
    for (Map.Entry<Integer, List<ElementSummary>> projectSummary : contents.entrySet()) {
      List<ElementSummaryJSON> ps = new LinkedList<>();
      jsonContents.put(projectSummary.getKey(), ps);
      for (ElementSummary es : projectSummary.getValue()) {
        ps.add(ElementSummaryJSON.resolve(es));
      }
    }
    return new HopsContentsSummaryJSON(jsonContents);
  }
}
