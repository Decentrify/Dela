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

import java.io.UnsupportedEncodingException;
import se.sics.ktoolbox.util.identifiable.Identifier;
import se.sics.ktoolbox.util.identifiable.basic.SimpleByteIdentifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class TorrentIdJSON {
    private String val;
    
    public TorrentIdJSON(String val) {
        this.val = val;
    }
    
    public TorrentIdJSON() {}

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public static TorrentIdJSON toJSON(Identifier torrentId) {
        try {
            String sTorrentId = new String(((SimpleByteIdentifier)torrentId).id, "UTF-8");
            return new TorrentIdJSON(sTorrentId);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("logic error", ex);
        }
    }
    
    public static Identifier fromJSON(TorrentIdJSON jsonTorrentId) {
        try {
            byte[] bTorrentId = jsonTorrentId.val.getBytes("UTF-8");
            Identifier torrentId = new SimpleByteIdentifier(bTorrentId);
            return torrentId;
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("logic error", ex);
        }
    }
}
