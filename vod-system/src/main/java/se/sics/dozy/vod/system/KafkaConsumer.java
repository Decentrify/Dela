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
package se.sics.dozy.vod.system;

import io.hops.kafkautil.HopsKafkaConsumer;
import se.sics.ktoolbox.kafka.KafkaHelper;
import se.sics.ktoolbox.kafka.KafkaResource;

/**
 *
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class KafkaConsumer {
    public static void main(String[] args) {
        if(args.length != 1) {
            throw new RuntimeException("expected 1 arg");
        }
        String sessionId = args[0];
        String projectId = "10";
        String topicName = "testTopic";
        String schemaName = "testSchema";
        String brokerEndpoint = "10.0.2.15:9091";
        String restEndpoint = "http://bbc1.sics.se:14003/hopsworks/api/project";
        String domain = "bbc1.sics.se";
        
        String keystore = "/tmp/vod/tester123__meb10000__kstore.jks";
        String truststore = "/tmp/vod/tester123__meb10000__tstore.jks";
        
        System.err.println("connecting");
        KafkaResource kr = new KafkaResource(brokerEndpoint, restEndpoint, domain, sessionId, projectId, topicName, schemaName, keystore, truststore);
        HopsKafkaConsumer kc = KafkaHelper.getKafkaConsumer(kr);
        kc.consume();
        System.err.println("running");
    }
}
