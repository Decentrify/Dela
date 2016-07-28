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

import com.google.common.base.Optional;
import java.util.HashMap;
import java.util.Map;
import org.javatuples.Pair;
import se.sics.dozy.vod.model.TorrentIdJSON;
import se.sics.dozy.vod.model.hops.util.HDFSEndpointJSON;
import se.sics.dozy.vod.model.hops.util.HDFSResourceJSON;
import se.sics.dozy.vod.model.hops.util.KafkaResourceJSON;
import se.sics.ktoolbox.util.identifiable.Identifier;
import se.sics.nstream.hops.HopsFED;
import se.sics.nstream.hops.hdfs.HDFSEndpoint;
import se.sics.nstream.hops.hdfs.HDFSResource;
import se.sics.nstream.hops.kafka.KafkaEndpoint;
import se.sics.nstream.hops.kafka.KafkaResource;
import se.sics.nstream.hops.library.event.core.HopsTorrentUploadEvent;
import se.sics.nstream.util.FileExtendedDetails;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class HTUploadJSON {

    public static class Base {

        private TorrentIdJSON torrentId;
        private HDFSResourceJSON manifestHDFSResource;
        private Map<String, HDFSResourceJSON> hdfsDetails;
        private KafkaEndpoint kafkaEndpoint;
        private Map<String, KafkaResourceJSON> kafkaDetails;

        public TorrentIdJSON getTorrentId() {
            return torrentId;
        }

        public void setTorrentId(TorrentIdJSON torrentId) {
            this.torrentId = torrentId;
        }

        public HDFSResourceJSON getManifestHDFSResource() {
            return manifestHDFSResource;
        }

        public void setManifestHDFSResource(HDFSResourceJSON manifestHDFSResource) {
            this.manifestHDFSResource = manifestHDFSResource;
        }

        public Map<String, HDFSResourceJSON> getHdfsDetails() {
            return hdfsDetails;
        }

        public void setHdfsDetails(Map<String, HDFSResourceJSON> hdfsDetails) {
            this.hdfsDetails = hdfsDetails;
        }

        public KafkaEndpoint getKafkaEndpoint() {
            return kafkaEndpoint;
        }

        public void setKafkaEndpoint(KafkaEndpoint kafkaEndpoint) {
            this.kafkaEndpoint = kafkaEndpoint;
        }
        
        public Map<String, KafkaResourceJSON> getKafkaDetails() {
            return kafkaDetails;
        }

        public void setKafkaDetails(Map<String, KafkaResourceJSON> kafkaDetails) {
            this.kafkaDetails = kafkaDetails;
        }

        protected HopsTorrentUploadEvent.Request partialResolve(HDFSEndpoint hdfsEndpoint) {
            Identifier tId = torrentId.resolve();
            Pair<HDFSEndpoint, HDFSResource> manifest = Pair.with(hdfsEndpoint, manifestHDFSResource.resolve());
            Map<String, FileExtendedDetails> extendedDetails = new HashMap<>();
            for(Map.Entry<String, HDFSResourceJSON> f : hdfsDetails.entrySet()) {
                Pair<HDFSEndpoint, HDFSResource> mainResource = Pair.with(hdfsEndpoint, f.getValue().resolve());
                Optional<Pair<KafkaEndpoint, KafkaResource>> secondaryResource;
                KafkaResourceJSON kafkaResource = kafkaDetails.get(f.getKey());
                if(kafkaResource == null) {
                    secondaryResource = Optional.absent();
                } else {
                    secondaryResource = Optional.of(Pair.with(kafkaEndpoint, kafkaResource.resolve()));
                }
                FileExtendedDetails fed = new HopsFED(mainResource, secondaryResource);
                extendedDetails.put(f.getKey(), fed);
            }
            
            return new HopsTorrentUploadEvent.Request(tId, manifest, extendedDetails);
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

        public HopsTorrentUploadEvent.Request resolve() {
            HDFSEndpoint he = hdfsEndpoint.resolve();
            return partialResolve(he);
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
        
        public HopsTorrentUploadEvent.Request resolve() {
            HDFSEndpoint he = hdfsEndpoint.resolve();
            return partialResolve(he);
        }
    }

}
