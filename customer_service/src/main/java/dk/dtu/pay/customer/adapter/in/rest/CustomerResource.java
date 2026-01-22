package dk.dtu.pay.customer.adapter.in.rest;

import dk.dtu.pay.customer.domain.model.Customer;
import dk.dtu.pay.customer.domain.service.CustomerService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import dk.dtu.pay.customer.adapter.out.request.RequestStore;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.UUID;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @Inject
    CustomerService customerService;

    @Inject
    RequestStore requestStore;

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
    public Response requestTokens(@PathParam("id") String id,
                                  @QueryParam("count") @DefaultValue("1") int count) {

        String requestId = UUID.randomUUID().toString();
        requestStore.start(requestId); // ALWAYS start if requestId is returned

        // validation failure → async rejection
        if (count < 1 || count > 5) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("tokens", List.of()); // must NOT be null
            result.put("error", "Requested token count must be between 1 and 5");

            requestStore.complete(requestId, result);

            return Response.status(Response.Status.ACCEPTED)
                    .entity(Map.of("requestId", requestId))
                    .build();
        }

        try {
            customerService.requestTokenIssue(requestId, id, count);

            return Response.status(Response.Status.ACCEPTED)
                    .entity(Map.of("requestId", requestId))
                    .build();

        } catch (CustomerService.UnknownCustomerException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("tokens", List.of()); // never null
            result.put("error", e.getMessage());

            requestStore.complete(requestId, result);

            return Response.status(Response.Status.ACCEPTED)
                    .entity(Map.of("requestId", requestId))
                    .build();
        }
    }



    // Request token list (async) — returns requestId
    @GET
    @Path("{id}/tokens")
    public Response requestTokenList(@PathParam("id") String id) {
        String requestId = UUID.randomUUID().toString();
        requestStore.start(requestId);

        try {
            customerService.requestTokenList(requestId, id);

        } catch (CustomerService.UnknownCustomerException e) {
            // COMPLETE the async request
            requestStore.complete(requestId, Map.of(
                    "tokens", null,
                    "error", e.getMessage()
            ));
        }

        // ALWAYS return 202 with requestId
        return Response.status(Response.Status.ACCEPTED)
                .entity(Map.of("requestId", requestId))
                .build();
    }


}
