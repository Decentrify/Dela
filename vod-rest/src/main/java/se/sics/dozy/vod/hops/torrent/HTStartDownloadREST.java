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
package se.sics.dozy.vod.hops.torrent;

import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.dozy.DozyResource;
import se.sics.dozy.DozyResult;
import se.sics.dozy.DozySyncI;
import se.sics.dozy.vod.DozyVoD;
import se.sics.dozy.vod.hops.torrent.model.HTStartDownloadJSON;
import se.sics.dozy.vod.model.ErrorDescJSON;
import se.sics.dozy.vod.model.SuccessJSON;
import se.sics.dozy.vod.util.ResponseStatusMapper;
import se.sics.ktoolbox.util.identifiable.overlay.OverlayIdFactory;
import se.sics.nstream.hops.library.event.core.HopsTorrentDownloadEvent;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class HTStartDownloadREST implements DozyResource {

    //TODO Alex - make into config?
    public static long timeout = 30000;

    private static final Logger LOG = LoggerFactory.getLogger(DozyResource.class);

    private DozySyncI vodTorrentI = null;
    protected OverlayIdFactory torrentIdFactory;

    public HTStartDownloadREST(OverlayIdFactory torrentIdFactory) {
        this.torrentIdFactory = torrentIdFactory;
    }

    @Override
    public void initialize(Map<String, DozySyncI> interfaces) {
        vodTorrentI = interfaces.get(DozyVoD.hopsTorrentDozyName);
        if (vodTorrentI == null) {
            throw new RuntimeException("no sync interface found for vod REST API");
        }
    }

    protected Response download(HopsTorrentDownloadEvent.StartRequest request) {
        LOG.trace("received download torrent request:{}", request.torrentId);

        if (!vodTorrentI.isReady()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(new ErrorDescJSON("vod not ready")).build();
        }

        LOG.debug("waiting for download:{}<{}> response", request.torrentId, request.eventId);
        DozyResult result = vodTorrentI.sendReq(request, timeout);
        Pair<Response.Status, String> wsStatus;
        Object entityResult = null;
        if (result.ok()) {
            if (result.getValue() instanceof HopsTorrentDownloadEvent.StartSuccess) {
                DozyResult<HopsTorrentDownloadEvent.StartSuccess> r = (DozyResult<HopsTorrentDownloadEvent.StartSuccess>) result;
                wsStatus = ResponseStatusMapper.resolveHopsTorrentDownload1(r);
                entityResult = new SuccessJSON();
            } else if (result.getValue() instanceof HopsTorrentDownloadEvent.StartFailed) {
                DozyResult<HopsTorrentDownloadEvent.StartFailed> r = (DozyResult<HopsTorrentDownloadEvent.StartFailed>) result;
                wsStatus = ResponseStatusMapper.resolveHopsTorrentDownload2(r);
                entityResult = new ErrorDescJSON(wsStatus.getValue1());
            } else {
                throw new RuntimeException("todo - new type of msg?");
            }
        } else {
            wsStatus = ResponseStatusMapper.resolveDozyError(result);
            entityResult = new ErrorDescJSON(wsStatus.getValue1());
        }

        LOG.info("download:{}<{}> status:{} details:{}", new Object[]{request.torrentId, request.eventId, wsStatus.getValue0(), wsStatus.getValue1()});
        if (wsStatus.getValue0().equals(Response.Status.OK)) {
            return Response.status(Response.Status.OK).entity(entityResult).build();
        } else {
            return Response.status(wsStatus.getValue0()).entity(entityResult).build();
        }
    }

    @Path("/torrent/hops/download/start/basic")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    /**
     * consumes - HTStartDownloadJSON.Basic produces - SuccessJSON
     */
    public static class Basic extends HTStartDownloadREST {

        public Basic(OverlayIdFactory torrentIdFactory) {
            super(torrentIdFactory);
        }
        
        @POST
        public Response downloadBasic(HTStartDownloadJSON.Basic req) {
            return download(req.resolve(torrentIdFactory));
        }
    }

    @Path("/torrent/hops/download/start/xml")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    /**
     * consumes - HTStartDownloadJSON.XML produces - SuccessJSON
     */
    public static class XML extends HTStartDownloadREST {

        public XML(OverlayIdFactory torrentIdFactory) {
            super(torrentIdFactory);
        }
        
        @POST
        public Response downloadBasic(HTStartDownloadJSON.XML req) {
            return download(req.resolve(torrentIdFactory));
        }
    }
}
