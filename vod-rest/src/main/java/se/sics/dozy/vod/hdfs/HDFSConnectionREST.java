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
package se.sics.dozy.vod.hdfs;

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
import se.sics.dozy.vod.DozyVoD;
import se.sics.dozy.vod.model.ErrorDescJSON;
import se.sics.dozy.vod.model.HDFSConnectionJSON;
import se.sics.dozy.vod.model.SuccessJSON;
import se.sics.dozy.vod.util.ResponseStatusMapper;
import se.sics.gvod.mngr.event.system.HopsConnectionEvent;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
@Path("/hdfs/connection")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HDFSConnectionREST implements DozyResource {

    //TODO Alex - make into config?
    public static long timeout = 5000;

    private static final Logger LOG = LoggerFactory.getLogger(DozyResource.class);

    private DozySyncI vodSystemI = null;

    @Override
    public void setSyncInterfaces(Map<String, DozySyncI> interfaces) {
        vodSystemI = interfaces.get(DozyVoD.systemDozyName);
        if (vodSystemI == null) {
            throw new RuntimeException("no sync interface found for vod REST API");
        }
    }

    @PUT
    public Response download(HDFSConnectionJSON req) {
        LOG.trace("received hdfs connection check request");

        if (!vodSystemI.isReady()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(new ErrorDescJSON("vod not ready")).build();
        }

        HopsConnectionEvent.Request request = new HopsConnectionEvent.Request(HDFSConnectionJSON.resolveFromJSON(req));
        LOG.debug("waiting for hdfs connection check response:{}", request.eventId);
        DozyResult<HopsConnectionEvent.Response> result = vodSystemI.sendReq(request, timeout);
        Pair<Response.Status, String> wsStatus = ResponseStatusMapper.resolveHopsConnection(result);
        LOG.info("hdfs connection check:{} status:{} details:{}", new Object[]{request.eventId, wsStatus.getValue0(), wsStatus.getValue1()});
        if (wsStatus.getValue0().equals(Response.Status.OK)) {
            return Response.status(Response.Status.OK).entity(new SuccessJSON()).build();
        } else {
            return Response.status(wsStatus.getValue0()).entity(new ErrorDescJSON(wsStatus.getValue1())).build();
        }
    }
}