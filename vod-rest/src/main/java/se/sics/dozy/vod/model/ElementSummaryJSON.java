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
package se.sics.dozy.vod.model;

import se.sics.gvod.mngr.util.ElementSummary;
import se.sics.gvod.mngr.util.TorrentExtendedStatus;
import se.sics.ktoolbox.util.identifiable.basic.OverlayIdentifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class ElementSummaryJSON {

    private String fileName;
    private int torrentId;
    private String torrentStatus;
    
    public ElementSummaryJSON(String name, int torrentId, String status) {
        this.fileName = name;
        this.torrentId = torrentId;
        this.torrentStatus = status;
    }

    public ElementSummaryJSON() {
    }

    public String getName() {
        return fileName;
    }

    public void setName(String name) {
        this.fileName = name;
    }

    public int getIdentifier() {
        return torrentId;
    }

    public void setIdentifier(int identifier) {
        this.torrentId = identifier;
    }

    public String getStatus() {
        return torrentStatus;
    }

    public void setStatus(String status) {
        this.torrentStatus = status;
    }

    public static ElementSummaryJSON resolve(ElementSummary les) {
        return new ElementSummaryJSON(les.name, ((OverlayIdentifier)les.torrentId).getInt(), les.status.name());
    }
}
