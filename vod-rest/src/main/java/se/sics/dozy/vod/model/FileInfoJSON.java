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

import se.sics.gvod.mngr.util.FileInfo;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class FileInfoJSON {

    private String name; //short name
    private String uri; //long name unique resource identifier - path, etc...
    private long size;
    private String description; //short description; 256 char max.

    public FileInfoJSON(String name, String uri, long size, String description) {
        this.name = name;
        this.uri = uri;
        this.size = size;
        this.description = description;
    }

    public FileInfoJSON() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public static FileInfoJSON resolve(FileInfo fileInfo) {
        return new FileInfoJSON(fileInfo.name, fileInfo.uri, fileInfo.size, fileInfo.shortDescription);
    }
    
    public static FileInfo resolve(FileInfoJSON fileInfoJSON) {
        return new FileInfo(fileInfoJSON.name, fileInfoJSON.uri, fileInfoJSON.size, fileInfoJSON.description);
    }
}
