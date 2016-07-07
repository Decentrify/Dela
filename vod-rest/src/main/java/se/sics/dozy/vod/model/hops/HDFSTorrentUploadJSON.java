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
package se.sics.dozy.vod.model.hops;

import se.sics.dozy.vod.model.TorrentIdJSON;
import se.sics.dozy.vod.model.hops.util.HDFSResourceJSON;
import se.sics.dozy.vod.model.hops.util.HopsResourceJSON;
import se.sics.gvod.stream.mngr.hops.torrent.event.HopsTorrentUploadEvent;
import se.sics.ktoolbox.hdfs.HDFSResource;
import se.sics.ktoolbox.hdfs.HopsResource;
import se.sics.ktoolbox.util.identifiable.Identifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class HDFSTorrentUploadJSON {

    private HDFSResourceJSON hdfsResource;
    private HopsResourceJSON hopsResource;
    private TorrentIdJSON torrentId;
    
    public HDFSTorrentUploadJSON() {}

    public HDFSResourceJSON getHdfsResource() {
        return hdfsResource;
    }

    public void setHdfsResource(HDFSResourceJSON hdfsResource) {
        this.hdfsResource = hdfsResource;
    }

    public HopsResourceJSON getHopsResource() {
        return hopsResource;
    }

    public void setHopsResource(HopsResourceJSON hopsResource) {
        this.hopsResource = hopsResource;
    }

    public TorrentIdJSON getTorrentId() {
        return torrentId;
    }

    public void setTorrentId(TorrentIdJSON torrentId) {
        this.torrentId = torrentId;
    }
    
    public static HopsTorrentUploadEvent.Request resolveFromJSON(HDFSTorrentUploadJSON req) {
        Identifier torrentId = TorrentIdJSON.fromJSON(req.torrentId);
        HDFSResource hdfsResource = HDFSResourceJSON.fromJSON(req.hdfsResource);
        HopsResource hopsResource = HopsResourceJSON.fromJSON(req.hopsResource);
        return new HopsTorrentUploadEvent.Request(hdfsResource, hopsResource, torrentId);
    }
}
