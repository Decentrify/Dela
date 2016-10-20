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

import java.util.HashMap;
import java.util.Map;
import org.javatuples.Pair;
import se.sics.dozy.vod.model.hops.util.HDFSResourceJSON;
import se.sics.dozy.vod.model.hops.util.KafkaResourceJSON;
import se.sics.nstream.hops.hdfs.HDFSResource;
import se.sics.nstream.hops.kafka.KafkaResource;

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

    public Pair<Map<String, HDFSResource>, Map<String, KafkaResource>> resolve() {
        Map<String, HDFSResource> hdfsResult = new HashMap<>();
        for(Map.Entry<String, HDFSResourceJSON> e : hdfsDetails.entrySet()) {
            hdfsResult.put(e.getKey(), e.getValue().resolve());
        }
        
        Map<String, KafkaResource> kafkaResult = new HashMap<>();
        for(Map.Entry<String, KafkaResourceJSON> e : kafkaDetails.entrySet()) {
            kafkaResult.put(e.getKey(), e.getValue().resolve());
        }
        
        return Pair.with(hdfsResult, kafkaResult);
    }
}
