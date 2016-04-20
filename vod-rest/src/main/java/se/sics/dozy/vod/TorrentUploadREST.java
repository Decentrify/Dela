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
package se.sics.dozy.vod;

import com.google.common.util.concurrent.SettableFuture;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
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
import se.sics.dozy.vod.model.FileInfoJSON;
import se.sics.dozy.vod.util.ResponseStatusMapper;
import se.sics.gvod.mngr.event.TorrentUploadEvent;
import se.sics.ktoolbox.util.identifiable.basic.IntIdentifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
@Path("/torrent/upload")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TorrentUploadREST implements DozyResource {
    //TODO Alex - make into config?
    public static long timeout = 5000;

    private static final Logger LOG = LoggerFactory.getLogger(DozyResource.class);

    private DozySyncI vod = null;

    @Override
    public void setSyncInterfaces(Map<String, DozySyncI> interfaces) {
        vod = interfaces.get(DozyVoD.dozyName);
        if (vod == null) {
            throw new RuntimeException("no sync interface found for vod REST API");
        }
    }

    @PUT
    public Response pendingUpload(FileInfoJSON fileInfo) {
        LOG.info("received upload torrent request:{}", fileInfo.getName());

        if (!vod.isReady()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("vod not ready").build();
        }

        TorrentUploadEvent.Request request = new TorrentUploadEvent.Request(fileInfo.getName(), new IntIdentifier(fileInfo.getOverlayId()));
        try {
            SettableFuture<DozyResult> futureResult = vod.sendReq(request, timeout);
            LOG.debug("waiting for upload:{}<{}> response", request.fileName, request.eventId);
            DozyResult<TorrentUploadEvent.Response> result = futureResult.get();
            Pair<Response.Status, String> wsStatus = ResponseStatusMapper.resolveTorrentUpload(result);
            LOG.info("upload:{}<{}> status:{} details:{}", new Object[]{request.eventId, request.fileName, wsStatus.getValue0(), wsStatus.getValue1()});
            if (wsStatus.getValue0().equals(Response.Status.OK)) {
                return Response.status(Response.Status.OK).entity(result.getValue().req.fileName).build();
            } else {
                return Response.status(wsStatus.getValue0()).entity(wsStatus.getValue1()).build();
            }
        } catch (InterruptedException ex) {
            LOG.error("upload:{}<{}> status:{}", new Object[]{request.eventId, request.fileName, Response.Status.INTERNAL_SERVER_ERROR});
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("dozy problem").build();
        } catch (ExecutionException ex) {
            LOG.error("upload:{}<{}> status:{}",new Object[]{request.eventId, request.fileName, Response.Status.INTERNAL_SERVER_ERROR});
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("dozy problem").build();
        }
    }
}
