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
import se.sics.dozy.vod.model.hops.util.HDFSResourceJSON;
import se.sics.dozy.vod.model.hops.util.KafkaResourceJSON;
import se.sics.nstream.hops.HopsFED;
import se.sics.nstream.hops.hdfs.HDFSEndpoint;
import se.sics.nstream.hops.hdfs.HDFSResource;
import se.sics.nstream.hops.kafka.KafkaEndpoint;
import se.sics.nstream.hops.kafka.KafkaResource;
import se.sics.nstream.util.FileExtendedDetails;

/**
 *
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class ExtendedDetailsJSON {

    private Map<String, HDFSResourceJSON> hdfsDetails;
    private Map<String, KafkaResourceJSON> kafkaDetails;

    public Map<String, HDFSResourceJSON> getHdfsDetails() {
        return hdfsDetails;
    }

    public void setHdfsDetails(Map<String, HDFSResourceJSON> hdfsDetails) {
        this.hdfsDetails = hdfsDetails;
    }

    public Map<String, KafkaResourceJSON> getKafkaDetails() {
        return kafkaDetails;
    }

    public void setKafkaDetails(Map<String, KafkaResourceJSON> kafkaDetails) {
        this.kafkaDetails = kafkaDetails;
    }

    public Map<String, FileExtendedDetails> resolve(HDFSEndpoint hdfsEndpoint, KafkaEndpoint kafkaEndpoint) {
        Map<String, FileExtendedDetails> extendedDetails = new HashMap<>();
        for (Map.Entry<String, HDFSResourceJSON> f : hdfsDetails.entrySet()) {
            Pair<HDFSEndpoint, HDFSResource> mainResource = Pair.with(hdfsEndpoint, f.getValue().resolve());
            Optional<Pair<KafkaEndpoint, KafkaResource>> secondaryResource;
            KafkaResourceJSON kafkaResource = kafkaDetails.get(f.getKey());
            if (kafkaResource == null) {
                secondaryResource = Optional.absent();
            } else {
                secondaryResource = Optional.of(Pair.with(kafkaEndpoint, kafkaResource.resolve()));
            }
            FileExtendedDetails fed = new HopsFED(mainResource, secondaryResource);
            extendedDetails.put(f.getKey(), fed);
        }
        return extendedDetails;
    }
}
