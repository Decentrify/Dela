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

import java.util.Map;
import se.sics.dozy.vod.model.TorrentIdJSON;
import se.sics.dozy.vod.model.hops.util.HDFSEndpointJSON;
import se.sics.dozy.vod.model.hops.util.KafkaEndpointJSON;
import se.sics.ktoolbox.util.identifiable.Identifier;
import se.sics.ktoolbox.util.result.Result;
import se.sics.nstream.hops.hdfs.HDFSEndpoint;
import se.sics.nstream.hops.kafka.KafkaEndpoint;
import se.sics.nstream.hops.library.event.core.HopsTorrentDownloadEvent;
import se.sics.nstream.util.FileExtendedDetails;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public abstract class HTAdvanceDownloadJSON {

    private TorrentIdJSON torrentId;
    private KafkaEndpointJSON kafkaEndpoint;
    private ExtendedDetailsJSON extendedDetails;

    public TorrentIdJSON getTorrentId() {
        return torrentId;
    }

    public void setTorrentId(TorrentIdJSON torrentId) {
        this.torrentId = torrentId;
    }

    public KafkaEndpointJSON getKafkaEndpoint() {
        return kafkaEndpoint;
    }

    public void setKafkaEndpoint(KafkaEndpointJSON kafkaEndpoint) {
        this.kafkaEndpoint = kafkaEndpoint;
    }

    public ExtendedDetailsJSON getExtendedDetails() {
        return extendedDetails;
    }

    public void setExtendedDetails(ExtendedDetailsJSON extendedDetails) {
        this.extendedDetails = extendedDetails;
    }

    protected HopsTorrentDownloadEvent.AdvanceRequest partialResolve(HDFSEndpoint he) {
        Identifier tId = torrentId.resolve();
        KafkaEndpoint ke = kafkaEndpoint.resolve();
        Map<String, FileExtendedDetails> ed = extendedDetails.resolve(he, ke);
        return new HopsTorrentDownloadEvent.AdvanceRequest(tId, Result.success(ed));
    }

    public static class Basic extends HTAdvanceDownloadJSON {

        private HDFSEndpointJSON.Basic hdfsEndpoint;

        public HDFSEndpointJSON.Basic getHdfsEndpoint() {
            return hdfsEndpoint;
        }

        public void setHdfsEndpoint(HDFSEndpointJSON.Basic hdfsEndpoint) {
            this.hdfsEndpoint = hdfsEndpoint;
        }
        
        public HopsTorrentDownloadEvent.AdvanceRequest resolve() {
            HDFSEndpoint he = hdfsEndpoint.resolve();
            return partialResolve(he);
        }
    }
    
    public static class XML extends HTAdvanceDownloadJSON {

        private HDFSEndpointJSON.XML hdfsEndpoint;

        public HDFSEndpointJSON.XML getHdfsEndpoint() {
            return hdfsEndpoint;
        }

        public void setHdfsEndpoint(HDFSEndpointJSON.XML hdfsEndpoint) {
            this.hdfsEndpoint = hdfsEndpoint;
        }
        
        public HopsTorrentDownloadEvent.AdvanceRequest resolve() {
            HDFSEndpoint he = hdfsEndpoint.resolve();
            return partialResolve(he);
        }
    }
}
