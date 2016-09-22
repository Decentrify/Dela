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

import se.sics.dozy.vod.model.TorrentIdJSON;
import se.sics.dozy.vod.model.hops.util.HDFSEndpointJSON;
import se.sics.dozy.vod.model.hops.util.HDFSResourceJSON;
import se.sics.ktoolbox.util.identifiable.overlay.OverlayId;
import se.sics.ktoolbox.util.identifiable.overlay.OverlayIdFactory;
import se.sics.nstream.hops.hdfs.HDFSEndpoint;
import se.sics.nstream.hops.hdfs.HDFSResource;
import se.sics.nstream.hops.library.event.core.HopsTorrentUploadEvent;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class HTUploadJSON {

    public static class Base {

        private TorrentIdJSON torrentId;
        private String torrentName;
        private HDFSResourceJSON manifestHDFSResource;

        public TorrentIdJSON getTorrentId() {
            return torrentId;
        }

        public void setTorrentId(TorrentIdJSON torrentId) {
            this.torrentId = torrentId;
        }

        public String getTorrentName() {
            return torrentName;
        }

        public void setTorrentName(String torrentName) {
            this.torrentName = torrentName;
        }
        
        public HDFSResourceJSON getManifestHDFSResource() {
            return manifestHDFSResource;
        }

        public void setManifestHDFSResource(HDFSResourceJSON manifestHDFSResource) {
            this.manifestHDFSResource = manifestHDFSResource;
        }

        protected HopsTorrentUploadEvent.Request partialResolve(HDFSEndpoint hdfsEndpoint, OverlayIdFactory overlayIdFactory) {
            OverlayId tId = torrentId.resolve(overlayIdFactory);
            HDFSResource mr = manifestHDFSResource.resolve();
            return new HopsTorrentUploadEvent.Request(tId, torrentName, hdfsEndpoint, mr);
        }
    }

    public static class Basic extends Base {

        private HDFSEndpointJSON.Basic hdfsEndpoint;

        public HDFSEndpointJSON.Basic getHdfsEndpoint() {
            return hdfsEndpoint;
        }

        public void setHdfsEndpoint(HDFSEndpointJSON.Basic hdfsEndpoint) {
            this.hdfsEndpoint = hdfsEndpoint;
        }

        public HopsTorrentUploadEvent.Request resolve(OverlayIdFactory overlayIdFactory) {
            HDFSEndpoint he = hdfsEndpoint.resolve();
            return partialResolve(he, overlayIdFactory);
        }
    }
    
    public static class XML extends Base {
        private HDFSEndpointJSON.XML hdfsEndpoint;

        public HDFSEndpointJSON.XML getHdfsEndpoint() {
            return hdfsEndpoint;
        }

        public void setHdfsEndpoint(HDFSEndpointJSON.XML hdfsEndpoint) {
            this.hdfsEndpoint = hdfsEndpoint;
        }
        
        public HopsTorrentUploadEvent.Request resolve(OverlayIdFactory overlayIdFactory) {
            HDFSEndpoint he = hdfsEndpoint.resolve();
            return partialResolve(he, overlayIdFactory);
        }
    }

}
