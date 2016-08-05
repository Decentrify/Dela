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
import se.sics.nstream.hops.kafka.KafkaEndpoint;
import se.sics.nstream.hops.kafka.KafkaHelper;
import se.sics.nstream.hops.kafka.KafkaResource;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class KafkaConsumer {
    public static void main(String[] args) {
        if(args.length != 3) {
            throw new RuntimeException("expected 3 arg - projectId topicName sessionId");
        }
        String projectId = args[0];
        String sessionId = args[1];
        String topicName = args[2];
        String brokerEndpoint = "10.0.2.15:9091";
        String restEndpoint = "http://bbc1.sics.se:14003";
        String domain = "bbc1.sics.se";
        
        String keystore = "/tmp/newDestination__meb10000__kstore.jks";
        String truststore = "/tmp/newDestination__meb10000__tstore.jks";
        
        System.err.println("connecting");
        KafkaEndpoint ke = new KafkaEndpoint(brokerEndpoint, restEndpoint, domain, projectId, keystore, truststore);
        KafkaResource kr = new KafkaResource(sessionId, topicName);
        HopsKafkaConsumer kc = KafkaHelper.getKafkaConsumer(ke, kr);
        kc.consume();
    }
}
