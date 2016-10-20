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

import java.util.ArrayList;
import java.util.List;
import se.sics.dozy.vod.model.ElementSummaryJSON;
import se.sics.gvod.mngr.util.ElementSummary;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class HopsContentsSummaryJSON {
    private List<ElementSummaryJSON> contents = new ArrayList<>();

    public HopsContentsSummaryJSON(List<ElementSummaryJSON> contents) {
        this.contents = contents;
    }

    public HopsContentsSummaryJSON() {
    }

    public List<ElementSummaryJSON> getContents() {
        return contents;
    }

    public void setContents(List<ElementSummaryJSON> contents) {
        this.contents = contents;
    }

    public static HopsContentsSummaryJSON resolve(List<ElementSummary> contents) {
        List<ElementSummaryJSON> jsonContents = new ArrayList<>();
        for (ElementSummary es : contents) {
            jsonContents.add(ElementSummaryJSON.resolve(es));
        }
        return new HopsContentsSummaryJSON(jsonContents);
    }
}
