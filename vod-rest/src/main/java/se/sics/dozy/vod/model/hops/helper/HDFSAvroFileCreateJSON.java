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
//package se.sics.dozy.vod.model.hops.helper;
//
//import se.sics.dozy.vod.model.hops.util.HDFSResourceJSON;
//import se.sics.dozy.vod.model.hops.util.KafkaResourceJSON;
//import se.sics.nstream.hops.library.event.helper.HDFSAvroFileCreateEvent;
//
///**
// * @author Alex Ormenisan <aaor@kth.se>
// */
//public class HDFSAvroFileCreateJSON {
//    private HDFSResourceJSON hdfsResource;
//    private KafkaResourceJSON kafkaResource;
//    private long nrMsgs;
//    
//    public HDFSAvroFileCreateJSON(HDFSResourceJSON hdfsResource, KafkaResourceJSON kafkaResource, long nrMsgs) {
//        this.hdfsResource = hdfsResource;
//        this.kafkaResource = kafkaResource;
//        this.nrMsgs = nrMsgs;
//    }
//
//    public HDFSAvroFileCreateJSON() {}
//
//    public HDFSResourceJSON getHdfsResource() {
//        return hdfsResource;
//    }
//
//    public void setHdfsResource(HDFSResourceJSON hdfsResource) {
//        this.hdfsResource = hdfsResource;
//    }
//
//    public KafkaResourceJSON getKafkaResource() {
//        return kafkaResource;
//    }
//
//    public void setKafkaResource(KafkaResourceJSON kafkaResource) {
//        this.kafkaResource = kafkaResource;
//    }
//    
//    public long getNrMsgs() {
//        return nrMsgs;
//    }
//
//    public void setNrMsgs(long nrMsgs) {
//        this.nrMsgs = nrMsgs;
//    }
//
//    public static HDFSAvroFileCreateEvent.Request fromJSON(HDFSAvroFileCreateJSON json) {
//        return new HDFSAvroFileCreateEvent.Request(HDFSResourceJSON.fromJSON(json.hdfsResource), KafkaResourceJSON.fromJSON(json.kafkaResource), json.nrMsgs);
//    }
//}
