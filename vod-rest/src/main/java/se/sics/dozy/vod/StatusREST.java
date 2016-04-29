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

import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.dozy.DozyResource;
import se.sics.dozy.DozySyncI;
import se.sics.dozy.vod.model.ErrorDescJSON;
import se.sics.dozy.vod.model.SuccessJSON;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
@Path("/status")
@Produces(MediaType.APPLICATION_JSON)
public class StatusREST implements DozyResource {

    private static final Logger LOG = LoggerFactory.getLogger(DozyResource.class);

    private Map<String, DozySyncI> interfaces;

    @Override
    public void setSyncInterfaces(Map<String, DozySyncI> interfaces) {
        this.interfaces = interfaces;
    }

    /**
     * @return Response[{@link se.sics.dozy.vod.model.SuccessJSON}] with OK
     * status or Response[{@link se.sics.dozy.vod.model.ErrorDescJSON type}] in
     * case of error
     */
    @GET
    public Response download() {
        LOG.info("received status request");

        Map.Entry<String, DozySyncI> notReady = null;
        for (Map.Entry<String, DozySyncI> service : interfaces.entrySet()) {
            if (!service.getValue().isReady()) {
                notReady = service;
                break;
            }
        }
        if (notReady != null) {
            LOG.debug("service:{} not ready", notReady.getKey());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(new ErrorDescJSON(notReady.getKey() + " not ready")).build();
        }

        LOG.debug("all services are up and running");
        return Response.status(Response.Status.OK).entity(new SuccessJSON()).build();
    }
}
