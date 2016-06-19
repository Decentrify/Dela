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
package se.sics.dozy.vod.model;

import se.sics.ktoolbox.hops.managedStore.storage.util.HDFSResource;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class HDFSResourceJSON {
    private String hopsIp;
    private int hopsPort;
    private String dirPath;
    private String fileName;
    
    public HDFSResourceJSON(String hopsIp, int hopsPort, String dirPath, String fileName) {
        this.hopsIp = hopsIp;
        this.hopsPort = hopsPort;
        this.dirPath = dirPath;
        this.fileName = fileName;
    }
    
    public HDFSResourceJSON() {}

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

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    
    public static HDFSResourceJSON resolveToJSON(HDFSResource file) {
        return new HDFSResourceJSON(file.hopsIp, file.hopsPort, file.dirPath, file.fileName);
    }
    
    public static HDFSResource resolveFromJSON(HDFSResourceJSON file) {
        return new HDFSResource(file.hopsIp, file.hopsPort, file.dirPath, file.fileName);
    }
}
