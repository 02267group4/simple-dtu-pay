package dk.dtu.pay.customer.adapter.in.rest;

import dk.dtu.pay.customer.domain.model.Customer;
import dk.dtu.pay.customer.domain.service.CustomerService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.UUID;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @Inject
    CustomerService customerService;

    @POST
    public Response registerCustomer(Customer req) {
        Customer c = customerService.registerCustomer(req);
        return Response.status(201).entity(c).build();
    }

    @DELETE
    @Path("{id}")
    public void deleteCustomer(@PathParam("id") String id) {
        customerService.deleteCustomer(id);
    }

    // Request token issuance (async) — returns requestId
    @POST
    @Path("{id}/tokens")
    public Response requestTokens(@PathParam("id") String id, @QueryParam("count") @DefaultValue("1") int count) {
        String requestId = UUID.randomUUID().toString();
        try {
            customerService.requestTokenIssue(requestId, id, count);
            return Response.status(Response.Status.ACCEPTED)
                    .entity(Map.of("requestId", requestId))
                    .build();
        } catch (CustomerService.UnknownCustomerException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    // Request token list (async) — returns requestId
    @GET
    @Path("{id}/tokens")
    public Response requestTokenList(@PathParam("id") String id) {
        String requestId = UUID.randomUUID().toString();
        try {
            customerService.requestTokenList(requestId, id);
            return Response.status(Response.Status.ACCEPTED)
                    .entity(Map.of("requestId", requestId))
                    .build();
        } catch (CustomerService.UnknownCustomerException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }
}
