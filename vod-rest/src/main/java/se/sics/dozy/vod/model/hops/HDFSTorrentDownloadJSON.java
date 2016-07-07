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

import java.util.ArrayList;
import java.util.List;
import se.sics.dozy.vod.model.AddressJSON;
import se.sics.dozy.vod.model.TorrentIdJSON;
import se.sics.dozy.vod.model.hops.util.HDFSResourceJSON;
import se.sics.dozy.vod.model.hops.util.HopsResourceJSON;
import se.sics.dozy.vod.model.hops.util.KafkaResourceJSON;
import se.sics.gvod.stream.mngr.hops.torrent.event.HopsTorrentDownloadEvent;
import se.sics.ktoolbox.hdfs.HDFSResource;
import se.sics.ktoolbox.hdfs.HopsResource;
import se.sics.ktoolbox.kafka.KafkaResource;
import se.sics.ktoolbox.util.identifiable.Identifier;
import se.sics.ktoolbox.util.network.KAddress;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class HDFSTorrentDownloadJSON {
    private HDFSResourceJSON hdfsResource;
    private KafkaResourceJSON kafkaResource;
    private HopsResourceJSON hopsResource;
    private TorrentIdJSON torrentId;
    private List<AddressJSON> partners;
    
    public HDFSTorrentDownloadJSON() {}

    public HDFSResourceJSON getHdfsResource() {
        return hdfsResource;
    }

    public void setHdfsResource(HDFSResourceJSON hdfsResource) {
        this.hdfsResource = hdfsResource;
    }

    public KafkaResourceJSON getKafkaResource() {
        return kafkaResource;
    }

    public void setKafkaResource(KafkaResourceJSON kafkaResource) {
        this.kafkaResource = kafkaResource;
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

    public List<AddressJSON> getPartners() {
        return partners;
    }

    public void setPartners(List<AddressJSON> partners) {
        this.partners = partners;
    }

    public static HopsTorrentDownloadEvent.Request resolveFromJSON(HDFSTorrentDownloadJSON req) {
        HDFSResource hdfsResource = HDFSResourceJSON.fromJSON(req.hdfsResource);
        KafkaResource kafkaResource = KafkaResourceJSON.fromJSON(req.kafkaResource);
        HopsResource hopsResource = HopsResourceJSON.fromJSON(req.hopsResource);
        Identifier torrentId = TorrentIdJSON.fromJSON(req.torrentId);
        List<KAddress> partners = new ArrayList<>();
        for(AddressJSON partner : req.partners) {
            partners.add(AddressJSON.resolveFromJSON(partner));
        }
        return new HopsTorrentDownloadEvent.Request(hdfsResource, kafkaResource, hopsResource, torrentId, partners);
    }
}
