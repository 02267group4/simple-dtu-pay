package dk.dtu.pay.customer.adapter.in.rest;

import dk.dtu.pay.customer.adapter.out.request.RequestStore;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RequestResource {

    @Inject
    RequestStore requestStore;

    @GET
    @Path("{requestId}")
    public Response getRequestResult(@PathParam("requestId") String requestId) {
        System.out.println("Polling RequestStore in customer-service for " + requestId);

        if (!requestStore.isKnown(requestId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        // return the stored envelope object directly (non-null when completed)
        return requestStore.getResult(requestId)
                .map(result -> Response.ok(result).build())
                .orElse(Response.status(Response.Status.ACCEPTED).build());
    }
}
