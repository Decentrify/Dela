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
package se.sics.dozy.vod.model.hops.helper;

import se.sics.dozy.vod.model.hops.util.HDFSResourceJSON;
import se.sics.gvod.stream.mngr.hops.helper.event.HDFSFileDeleteEvent;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class HDFSFileDeleteJSON {
    private HDFSResourceJSON resource;
    
    public HDFSFileDeleteJSON(HDFSResourceJSON resource, String user) {
        this.resource = resource;
    }

    public HDFSFileDeleteJSON() {}

    public HDFSResourceJSON getResource() {
        return resource;
    }

    public void setResource(HDFSResourceJSON hdfs) {
        this.resource = hdfs;
    }

    public static HDFSFileDeleteEvent.Request fromJSON(HDFSFileDeleteJSON json) {
        return new HDFSFileDeleteEvent.Request(HDFSResourceJSON.fromJSON(json.resource));
    }
}
