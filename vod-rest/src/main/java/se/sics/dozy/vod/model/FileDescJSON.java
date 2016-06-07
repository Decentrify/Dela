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
import se.sics.ktoolbox.util.identifiable.Identifier;
import se.sics.ktoolbox.util.identifiable.basic.IntIdentifier;
import se.sics.ktoolbox.util.identifiable.basic.OverlayIdentifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class FileDescJSON {
    private String name;
    private int identifier;
    
    public FileDescJSON(int identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }
    
    public FileDescJSON() {
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.identifier;
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
        final FileDescJSON other = (FileDescJSON) obj;
        if (this.identifier != other.identifier) {
            return false;
        }
        return true;
    }
    
    public static FileDescJSON resolve(Identifier overlayId, FileInfo fileInfo) {
        return new FileDescJSON(((OverlayIdentifier)overlayId).getInt(), fileInfo.name);
    }
}
