///*
// * Copyright (C) 2016 Swedish Institute of Computer Science (SICS) Copyright (C)
// * 2016 Royal Institute of Technology (KTH)
// *
// * Dozy is free software; you can redistribute it and/or
// * modify it under the terms of the GNU General Public License
// * as published by the Free Software Foundation; either version 2
// * of the License, or (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
// */
//package se.sics.dozy.vod.system;
//
//import io.hops.kafkautil.HopsKafkaConsumer;
//import se.sics.gvod.hops.kafka.KafkaHelper;
//import se.sics.gvod.hops.kafka.KafkaResource;
//
///**
// *
// * @author Alex Ormenisan <aaor@kth.se>
// */
//public class KafkaConsumer {
//    public static void main(String[] args) {
//        if(args.length != 2) {
//            throw new RuntimeException("expected 2 arg - projectId sessionId");
//        }
//        String projectId = args[0];
//        String sessionId = args[1];
//        String topicName = "testTopic";
//        String brokerEndpoint = "10.0.2.15:9091";
//        String restEndpoint = "http://193.10.67.182:14008";
//        String domain = "193.10.67.182";
//        
//        String keystore = "/tmp/vod/testProject__meb10000__kstore.jks";
//        String truststore = "/tmp/vod/testProject__meb10000__tstore.jks";
//        
//        System.err.println("connecting");
//        KafkaResource kr = new KafkaResource(brokerEndpoint, restEndpoint, domain, sessionId, projectId, topicName, keystore, truststore);
//        HopsKafkaConsumer kc = KafkaHelper.getKafkaConsumer(kr);
//        kc.consume();
//    }
//}
