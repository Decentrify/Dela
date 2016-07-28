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
package se.sics.dozy.vod.model.hops.util;

import se.sics.nstream.hops.hdfs.HDFSEndpoint;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public interface HDFSEndpointJSON {

    public HDFSEndpoint resolve();
    
    public static class Basic implements HDFSEndpointJSON {

        private String hopsIp;
        private int hopsPort;
        private String user;

        public Basic() {
        }

        public Basic(String hopsIp, int hopsPort, String user) {
            this.hopsIp = hopsIp;
            this.hopsPort = hopsPort;
            this.user = user;
        }

        public String getHopsIp() {
            return hopsIp;
        }

        public void setHopsIp(String hopsIp) {
            this.hopsIp = hopsIp;
        }

        public int getHopsPort() {
            return hopsPort;
        }

        public void setHopsPort(int hopsPort) {
            this.hopsPort = hopsPort;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        @Override
        public HDFSEndpoint resolve() {
            return new HDFSEndpoint(hopsIp, hopsPort, user);
        }
    }

    public static class XML implements HDFSEndpointJSON {
        private String hdfsXMLPath;
        private String user;

        public XML() {}
        
        public XML(String hdfsXMLPath, String user) {
            this.hdfsXMLPath = hdfsXMLPath;
            this.user = user;
        }

        public String getHdfsXMLPath() {
            return hdfsXMLPath;
        }

        public void setHdfsXMLPath(String hdfsXMLPath) {
            this.hdfsXMLPath = hdfsXMLPath;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        @Override
        public HDFSEndpoint resolve() {
            return new HDFSEndpoint(hdfsXMLPath, user);
        }
    }
}
