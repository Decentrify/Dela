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
package se.sics.dozy.vod.util;

import org.javatuples.Pair;
import se.sics.dozy.DozyResult;
import se.sics.gvod.mngr.event.HopsContentsEvent;
import se.sics.gvod.mngr.event.HopsTorrentDownloadEvent;
import se.sics.gvod.mngr.event.HopsTorrentUploadEvent;
import se.sics.gvod.mngr.event.LibraryAddEvent;
import se.sics.gvod.mngr.event.LibraryContentsEvent;
import se.sics.gvod.mngr.event.LibraryElementGetEvent;
import se.sics.gvod.mngr.event.TorrentDownloadEvent;
import se.sics.gvod.mngr.event.TorrentStopEvent;
import se.sics.gvod.mngr.event.TorrentUploadEvent;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class ResponseStatusMapper {

    public static Pair<javax.ws.rs.core.Response.Status, String> resolveLibraryContents(DozyResult<LibraryContentsEvent.Response> result) {
        return resolve(result, (result.hasValue() ? result.getValue().result : null));
    }

    public static Pair<javax.ws.rs.core.Response.Status, String> resolveLibraryElement(DozyResult<LibraryElementGetEvent.Response> result) {
        return resolve(result, (result.hasValue() ? result.getValue().result : null));
    }
    
    public static Pair<javax.ws.rs.core.Response.Status, String> resolveLibraryAdd(DozyResult<LibraryAddEvent.Response> result) {
        return resolve(result, (result.hasValue() ? result.getValue().result : null));
    }

    public static Pair<javax.ws.rs.core.Response.Status, String> resolveTorrentUpload(DozyResult<TorrentUploadEvent.Response> result) {
        return resolve(result, (result.hasValue() ? result.getValue().result : null));
    }

    public static Pair<javax.ws.rs.core.Response.Status, String> resolveTorrentDownload(DozyResult<TorrentDownloadEvent.Response> result) {
        return resolve(result, (result.hasValue() ? result.getValue().result : null));
    }

    public static Pair<javax.ws.rs.core.Response.Status, String> resolveTorrentStop(DozyResult<TorrentStopEvent.Response> result) {
        return resolve(result, (result.hasValue() ? result.getValue().result : null));
    }
    
    public static Pair<javax.ws.rs.core.Response.Status, String> resolveHopsTorrentUpload(DozyResult<HopsTorrentUploadEvent.Response> result) {
        return resolve(result, (result.hasValue() ? result.getValue().result : null));
    }
    
    public static Pair<javax.ws.rs.core.Response.Status, String> resolveHopsTorrentDownload(DozyResult<HopsTorrentDownloadEvent.Response> result) {
        return resolve(result, (result.hasValue() ? result.getValue().result : null));
    }
    
    public static Pair<javax.ws.rs.core.Response.Status, String> resolveHopsContents(DozyResult<HopsContentsEvent.Response> result) {
        return resolve(result, (result.hasValue() ? result.getValue().result : null));
    }

    public static Pair<javax.ws.rs.core.Response.Status, String> resolve(se.sics.dozy.DozyResult dozyResult, se.sics.gvod.mngr.util.Result vodResult) {
        switch (dozyResult.status) {
            case OK:
                break; // continue and check vod response status
            case INTERNAL_ERROR:
                return Pair.with(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR, dozyResult.getDetails() == null ? "dozy problem" : dozyResult.getDetails());
            case OTHER:
                return Pair.with(javax.ws.rs.core.Response.Status.SEE_OTHER, dozyResult.getDetails() == null ? "dozy problem" : dozyResult.getDetails());
            case TIMEOUT:
                return Pair.with(javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE, "vod unavailable");
            default:
                return Pair.with(javax.ws.rs.core.Response.Status.SEE_OTHER, "undefined");
        }
        
        if(vodResult == null) {
            return Pair.with(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR, "dozy status logic problem");
        }
        switch (vodResult.status) {
            case SUCCESS:
                return Pair.with(javax.ws.rs.core.Response.Status.OK, vodResult.description);
            case TIMEOUT:
                return Pair.with(javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE, vodResult.description);
            case INTERNAL_FAILURE:
                return Pair.with(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR, vodResult.description);
            case BAD_REQUEST:
                return Pair.with(javax.ws.rs.core.Response.Status.BAD_REQUEST, vodResult.description);
            default:
                return Pair.with(javax.ws.rs.core.Response.Status.SEE_OTHER, "undefined");
        }
    }
}
