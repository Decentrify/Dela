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

        private String ip;
        private int port;
        private String user;

        public Basic() {
        }

        public Basic(String hopsIp, int hopsPort, String user) {
            this.ip = hopsIp;
            this.port = hopsPort;
            this.user = user;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        
        
        @Override
        public HDFSEndpoint resolve() {
            return new HDFSEndpoint(ip, port, user);
        }
    }

    public static class XML implements HDFSEndpointJSON {
        private String xmlPath;
        private String user;

        public XML() {}
        
        public XML(String xmlPath, String user) {
            this.xmlPath = xmlPath;
            this.user = user;
        }

        public String getXmlPath() {
            return xmlPath;
        }

        public void setXmlPath(String xmlPath) {
            this.xmlPath = xmlPath;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        @Override
        public HDFSEndpoint resolve() {
            return new HDFSEndpoint(xmlPath, user);
        }
    }
}
