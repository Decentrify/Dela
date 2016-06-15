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
import java.util.ArrayList;
import java.util.List;
import se.sics.gvod.mngr.event.HopsTorrentDownloadEvent;
import se.sics.gvod.mngr.util.HDFSResource;
import se.sics.ktoolbox.util.identifiable.Identifier;
import se.sics.ktoolbox.util.identifiable.basic.OverlayIdentifier;
import se.sics.ktoolbox.util.network.KAddress;

/**
 *
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class HopsTorrentDownloadJSON {
    private HDFSResourceJSON resource;
    private String user;
    private int torrentId;
    private List<AddressJSON> partners;
    
    public HopsTorrentDownloadJSON() {}

    public HDFSResourceJSON getResource() {
        return resource;
    }

    public void setResource(HDFSResourceJSON resource) {
        this.resource = resource;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getTorrentId() {
        return torrentId;
    }

    public void setTorrentId(int torrentId) {
        this.torrentId = torrentId;
    }

    public List<AddressJSON> getPartners() {
        return partners;
    }

    public void setPartners(List<AddressJSON> partners) {
        this.partners = partners;
    }

    public static HopsTorrentDownloadEvent.Request resolveFromJSON(HopsTorrentDownloadJSON req) {
        Identifier torrentId = new OverlayIdentifier(Ints.toByteArray(req.torrentId));
        List<KAddress> partners = new ArrayList<>();
        for(AddressJSON partner : req.partners) {
            partners.add(AddressJSON.resolveFromJSON(partner));
        }
        HDFSResource resource = HDFSResourceJSON.resolveFromJSON(req.resource);
        return new HopsTorrentDownloadEvent.Request(resource.hopsIp, resource.hopsPort, resource.dirPath, resource.fileName, req.user, torrentId, partners);
    }
}
