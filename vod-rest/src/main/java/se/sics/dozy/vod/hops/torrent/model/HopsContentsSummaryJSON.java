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
import se.sics.nstream.mngr.util.ElementSummary;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class HopsContentsSummaryJSON {

  public static class Basic {

    private Map<Integer, List<ElementSummaryJSON>> contents;

    public Basic(Map<Integer, List<ElementSummaryJSON>> contents) {
      this.contents = contents;
    }

    public Basic() {
    }

    public Map<Integer, List<ElementSummaryJSON>> getContents() {
      return contents;
    }

    public void setContents(Map<Integer, List<ElementSummaryJSON>> contents) {
      this.contents = contents;
    }
  }

  public static class Hops {

    private ContentsElement[] contents;

    public Hops(ContentsElement[] contents) {
      this.contents = contents;
    }

    public Hops() {
    }

    public ContentsElement[] getContents() {
      return contents;
    }

    public void setContents(ContentsElement[] contents) {
      this.contents = contents;
    }
  }

  public static Hops resolveHops(Map<Integer, List<ElementSummary>> contents) {
    ContentsElement[] jsonContents = new ContentsElement[contents.size()];
    int i = 0;
    for (Map.Entry<Integer, List<ElementSummary>> projectSummary : contents.entrySet()) {
      ElementSummaryJSON[] ps = new ElementSummaryJSON[projectSummary.getValue().size()];
      jsonContents[i++] = new ContentsElement(projectSummary.getKey(), ps);
      int j = 0;
      for (ElementSummary es : projectSummary.getValue()) {
        ps[j++] = ElementSummaryJSON.resolve(es);
      }
    }
    return new Hops(jsonContents);
  }
  
  public static Basic resolveBasic(Map<Integer, List<ElementSummary>> contents) {
      Map<Integer, List<ElementSummaryJSON>> jsonContents = new TreeMap<>();
      for (Map.Entry<Integer, List<ElementSummary>> projectSummary : contents.entrySet()) {
        List<ElementSummaryJSON> ps = new LinkedList<>();
        jsonContents.put(projectSummary.getKey(), ps);
        for (ElementSummary es : projectSummary.getValue()) {
          ps.add(ElementSummaryJSON.resolve(es));
        }
      }
      return new Basic(jsonContents);
    }

  public static class ContentsElement {

    public Integer projectId;
    public ElementSummaryJSON[] projectContents;

    private ContentsElement() {
    }

    public ContentsElement(Integer projectId, ElementSummaryJSON[] projectContents) {
      this.projectId = projectId;
      this.projectContents = projectContents;
    }

    @Override
    public String toString() {
      return "ContentsElement{" + "projectId=" + projectId + ", projectContents=" + projectContents + '}';
    }
  }
}
