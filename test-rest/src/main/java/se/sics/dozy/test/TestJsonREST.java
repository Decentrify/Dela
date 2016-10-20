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
package se.sics.dozy.test;

import java.util.HashMap;
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
import se.sics.dozy.test.model.TestJSON;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
@Path("/test")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TestJsonREST implements DozyResource {

    private static final Logger LOG = LoggerFactory.getLogger(DozyResource.class);

    @Override
    public void initialize(Map<String, DozySyncI> interfaces) {
    }

    @GET
    public Response getJson() {
        LOG.info("received get TestJSON");

        Map<String, Integer> testValues = new HashMap<>();
        testValues.put("val1", 1);
        testValues.put("val2", 2);
        testValues.put("val3", 3);

        TestJSON testResponse = new TestJSON(testValues);
        LOG.info("answered get TestJSON");
        return Response.status(Response.Status.OK).entity(testResponse).build();
    }
}
