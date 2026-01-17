package dk.dtu.pay.customer.adapter.in.rest;

import dk.dtu.pay.customer.domain.model.Customer;
import dk.dtu.pay.customer.domain.service.CustomerService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
}
