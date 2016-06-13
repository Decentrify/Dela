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

import com.google.common.primitives.Ints;
import java.util.Map;
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
import se.sics.dozy.vod.model.ErrorDescJSON;
import se.sics.dozy.vod.model.ElementDescJSON;
import se.sics.dozy.vod.model.TorrentExtendedStatusJSON;
import se.sics.dozy.vod.util.ResponseStatusMapper;
import se.sics.gvod.mngr.event.TorrentExtendedStatusEvent;
import se.sics.ktoolbox.util.identifiable.Identifier;
import se.sics.ktoolbox.util.identifiable.basic.OverlayIdentifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
@Path("/library/torrentStatus")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TorrentExtendedStatusREST implements DozyResource {

    //TODO Alex - make into config?
    public static long timeout = 5000;

    private static final Logger LOG = LoggerFactory.getLogger(DozyResource.class);

    private DozySyncI vodLibraryI = null;

    @Override
    public void setSyncInterfaces(Map<String, DozySyncI> interfaces) {
        vodLibraryI = interfaces.get(DozyVoD.libraryDozyName);
        if (vodLibraryI == null) {
            throw new RuntimeException("no sync interface found for vod REST API");
        }
    }

    /**
     * @param fileDesc {@link se.sics.dozy.vod.model.ElementDescJSON type}
     * @return Response[{@link se.sics.dozy.vod.model.TorrentExtendedStatusJSON type}]
 with OK value or
 Response[{@link se.sics.dozy.vod.model.ErrorDescJSON type}] in case of
     * error
     */
    @PUT
    public Response getTorrentExtendedStatus(ElementDescJSON fileDesc) {
        LOG.info("received torrents extended status request");
        if (!vodLibraryI.isReady()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(new ErrorDescJSON("vod not ready")).build();
        }

        Identifier overlayId = new OverlayIdentifier(Ints.toByteArray(fileDesc.getTorrentId()));
        TorrentExtendedStatusEvent.Request request = new TorrentExtendedStatusEvent.Request(overlayId);
        LOG.debug("waiting for torrents extended status:{} response", request.eventId);
        DozyResult<TorrentExtendedStatusEvent.Response> result = vodLibraryI.sendReq(request, timeout);
        Pair<Response.Status, String> wsStatus = ResponseStatusMapper.resolveElementStatus(result);
        LOG.info("torrents extended status:{} status:{} details:{}", new Object[]{request.eventId, wsStatus.getValue0(), wsStatus.getValue1()});
        if (wsStatus.getValue0().equals(Response.Status.OK)) {
            return Response.status(Response.Status.OK).entity(TorrentExtendedStatusJSON.resolveToJson(result.getValue().value)).build();
        } else {
            return Response.status(wsStatus.getValue0()).entity(new ErrorDescJSON(wsStatus.getValue1())).build();
        }
    }
}
