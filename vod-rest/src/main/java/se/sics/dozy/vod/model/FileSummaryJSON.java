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

import se.sics.gvod.mngr.util.FileInfo;
import se.sics.gvod.mngr.util.LibraryElementSummary;
import se.sics.gvod.mngr.util.TorrentInfo;
import se.sics.ktoolbox.util.identifiable.Identifier;
import se.sics.ktoolbox.util.identifiable.basic.IntIdentifier;
import se.sics.ktoolbox.util.identifiable.basic.OverlayIdentifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class FileSummaryJSON {

    private String name;
    private int identifier;
    private String status;
    
    public FileSummaryJSON(String name, int identifier, String status) {
        this.name = name;
        this.identifier = identifier;
        this.status = status;
    }

    public FileSummaryJSON() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static FileSummaryJSON resolve(LibraryElementSummary les) {
        return new FileSummaryJSON(les.name, ((OverlayIdentifier)les.overlayId.get()).getInt(), les.status.name());
    }
}
