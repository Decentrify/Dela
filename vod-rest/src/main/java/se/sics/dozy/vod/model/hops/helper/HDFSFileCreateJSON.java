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
//import se.sics.nstream.hops.library.event.helper.HDFSFileCreateEvent;
//
///**
// * @author Alex Ormenisan <aaor@kth.se>
// */
//public class HDFSFileCreateJSON {
//    private HDFSResourceJSON resource;
//    private long fileSize;
//    
//    public HDFSFileCreateJSON(HDFSResourceJSON resource, long fileSize) {
//        this.resource = resource;
//        this.fileSize = fileSize;
//    }
//
//    public HDFSFileCreateJSON() {}
//    
//    public HDFSResourceJSON getResource() {
//        return resource;
//    }
//
//    public void setResource(HDFSResourceJSON resource) {
//        this.resource = resource;
//    }
//
//    public long getFileSize() {
//        return fileSize;
//    }
//
//    public void setFileSize(long fileSize) {
//        this.fileSize = fileSize;
//    }
//    
//    public static HDFSFileCreateEvent.Request fromJSON(HDFSFileCreateJSON json) {
//        return new HDFSFileCreateEvent.Request(HDFSResourceJSON.fromJSON(json.resource), json.fileSize);
//    }
//}
