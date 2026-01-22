package dk.dtu.pay.manager.adapter.in.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.client.ClientBuilder;

@Path("/requests")
public class RequestProxyResource {

    @GET
    @Path("{id}")
    public Response get(@PathParam("id") String id) {
        return ClientBuilder.newClient()
                .target("http://customer-service:8081/requests/" + id)
                .request()
                .get();
    }
}
