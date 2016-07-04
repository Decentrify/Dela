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

import se.sics.ktoolbox.hdfs.HDFSResource;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class HDFSXMLResourceJSON {
    private String hdfsXMLPath;
    private String user;
    private String dirPath;
    private String fileName;
    
    public HDFSXMLResourceJSON(String hdfsXMLPath, String user, String dirPath, String fileName) {
        this.hdfsXMLPath = hdfsXMLPath;
        this.user = user;
        this.dirPath = dirPath;
        this.fileName = fileName;
    }
    
    public HDFSXMLResourceJSON() {}

    public String getHdfsXMLPath() {
        return hdfsXMLPath;
    }

    public void setHdfsXMLPath(String hdfsXMLPath) {
        this.hdfsXMLPath = hdfsXMLPath;
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public static HDFSResource fromJSON(HDFSXMLResourceJSON file) {
        return new HDFSResource(file.hdfsXMLPath, file.user, file.dirPath, file.fileName);
    }
}
