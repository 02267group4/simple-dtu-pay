package dk.dtu.pay.customer.adapter.in.rest;

import dk.dtu.pay.service.AppContext;
import dk.dtu.pay.customer.domain.model.Customer;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @POST
    public Response registerCustomer(Customer req) {
        Customer c = AppContext.customerService.registerCustomer(req);
        return Response.status(201).entity(c).build();
    }

    // Cleanup endpoint for testing (same behavior: void)
    @DELETE
    @Path("{id}")
    public void deleteCustomer(@PathParam("id") String id) {
        AppContext.customerService.deleteCustomer(id);
    }
}
