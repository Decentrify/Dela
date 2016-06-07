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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.javatuples.Pair;
import se.sics.gvod.mngr.event.LibraryContentsEvent;
import se.sics.gvod.mngr.util.FileInfo;
import se.sics.gvod.mngr.util.LibraryElementSummary;
import se.sics.gvod.mngr.util.TorrentInfo;
import se.sics.ktoolbox.util.identifiable.Identifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class LibraryContentsJSON {

    //<file, torrentStatus>
    private List<FileSummaryJSON> contents = new ArrayList<>();

    public LibraryContentsJSON(List<FileSummaryJSON> contents) {
        this.contents = contents;
    }

    public LibraryContentsJSON() {
    }

    public List<FileSummaryJSON> getContents() {
        return contents;
    }

    public void setContents(List<FileSummaryJSON> contents) {
        this.contents = contents;
    }

    public static LibraryContentsJSON resolve(LibraryContentsEvent.Response vodResp) {
        List<FileSummaryJSON> contents = new ArrayList<>();
        for (LibraryElementSummary les : vodResp.content) {
            contents.add(FileSummaryJSON.resolve(les));
        }
        return new LibraryContentsJSON(contents);
    }
}
