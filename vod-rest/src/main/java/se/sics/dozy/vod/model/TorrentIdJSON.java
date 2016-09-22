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

import se.sics.ktoolbox.util.identifiable.BasicBuilders;
import se.sics.ktoolbox.util.identifiable.overlay.OverlayId;
import se.sics.ktoolbox.util.identifiable.overlay.OverlayIdFactory;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class TorrentIdJSON {

    private String val;

    public TorrentIdJSON(String val) {
        this.val = val;
    }

    public TorrentIdJSON() {
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public OverlayId resolve(OverlayIdFactory overlayIdFactory) {
        OverlayId torrentId = overlayIdFactory.id(new BasicBuilders.StringBuilder(val));
        return torrentId;
    }

    public static TorrentIdJSON toJSON(OverlayId torrentId) {
        String sTorrentId = torrentId.toString();
        return new TorrentIdJSON(sTorrentId);
    }

}
