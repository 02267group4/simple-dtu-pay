package dk.dtu.pay.customer.adapter.in.rest;

import dk.dtu.pay.customer.domain.model.Customer;
import dk.dtu.pay.customer.domain.model.PaymentDTO;
import dk.dtu.pay.customer.domain.service.CustomerService;
import dk.dtu.pay.customer.adapter.out.messaging.RabbitMQCustomerReportPublisher;
import dk.dtu.pay.customer.adapter.out.request.CustomerReportStore;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import dk.dtu.pay.customer.adapter.out.request.RequestStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @Inject
    CustomerService customerService;

    @Inject
    RequestStore requestStore;

    @Inject
    RabbitMQCustomerReportPublisher reportPublisher;

    @Inject
    CustomerReportStore reportStore;

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
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "Requested token count must be between 1 and 5");
            errorResult.put("tokens", List.of());
            requestStore.complete(requestId, errorResult);

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
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            errorResult.put("tokens", List.of());
            requestStore.complete(requestId, errorResult);

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
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            errorResult.put("tokens", List.of());
            requestStore.complete(requestId, errorResult);
        }

        // ALWAYS return 202 with requestId
        return Response.status(Response.Status.ACCEPTED)
                .entity(Map.of("requestId", requestId))
                .build();
    }

    // ---------- customer report over RabbitMQ ----------

    @GET
    @Path("/{id}/report")
    public Response getCustomerReport(@PathParam("id") String customerId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<List<PaymentDTO>> future = reportStore.createPending(correlationId);

        try {
            // send request event to payment service
            reportPublisher.publish(correlationId, customerId);

            // wait (max 5 seconds) for async reply
            List<PaymentDTO> payments = future.get(5, TimeUnit.SECONDS);
            return Response.ok(payments).build();

        } catch (Exception e) {
            reportStore.remove(correlationId);
            return Response.status(Response.Status.GATEWAY_TIMEOUT)
                    .entity("Timed out while waiting for customer report")
                    .build();
        }
    }

}
