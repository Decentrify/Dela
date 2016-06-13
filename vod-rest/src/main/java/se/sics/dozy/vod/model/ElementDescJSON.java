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

import com.google.common.primitives.Ints;
import se.sics.gvod.mngr.util.FileInfo;
import se.sics.ktoolbox.util.identifiable.Identifier;
import se.sics.ktoolbox.util.identifiable.basic.OverlayIdentifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class ElementDescJSON {
    private String fileName;
    private int torrentId;
    
    public ElementDescJSON(int identifier, String name) {
        this.torrentId = identifier;
        this.fileName = name;
    }
    
    public ElementDescJSON() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getTorrentId() {
        return torrentId;
    }

    public void setTorrentId(int torrentId) {
        this.torrentId = torrentId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.torrentId;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ElementDescJSON other = (ElementDescJSON) obj;
        if (this.torrentId != other.torrentId) {
            return false;
        }
        return true;
    }
    
    public static ElementDescJSON resolve(Identifier overlayId, FileInfo fileInfo) {
        return new ElementDescJSON(((OverlayIdentifier)overlayId).getInt(), fileInfo.name);
    }
    
    public Identifier resolveTorrentId() {
        return new OverlayIdentifier(Ints.toByteArray(torrentId));
    }
}
