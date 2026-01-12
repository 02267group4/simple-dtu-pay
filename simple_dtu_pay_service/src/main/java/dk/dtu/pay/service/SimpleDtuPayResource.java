package dk.dtu.pay.service;

import dk.dtu.pay.service.model.*;
import dtu.ws.fastmoney.*; // The generated Bank code

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SimpleDtuPayResource {

    private final Map<String, Customer> customers = new ConcurrentHashMap<>();
    private final Map<String, Merchant> merchants = new ConcurrentHashMap<>();
    private final List<Payment> payments = new CopyOnWriteArrayList<>();

    // connection to the Bank
    BankService bank = new BankService_Service().getBankServicePort();

    @POST
    @Path("customers")
    public Response registerCustomer(Customer req) {
        // We assume the client provides Name, CPR, and BankAccount
        Customer c = new Customer(req.name, req.cprNumber, req.bankAccountId);
        c.id = java.util.UUID.randomUUID().toString();
        customers.put(c.id, c);
        return Response.status(201).entity(c).build();
    }

    @POST
    @Path("merchants")
    public Response registerMerchant(Merchant req) {
        Merchant m = new Merchant(req.name, req.cprNumber, req.bankAccountId);
        m.id = java.util.UUID.randomUUID().toString();
        merchants.put(m.id, m);
        return Response.status(201).entity(m).build();
    }

    @POST
    @Path("payments")
    public Response pay(PaymentRequest request) {
        Customer c = customers.get(request.customerId);
        Merchant m = merchants.get(request.merchantId);

        if (c == null) {
            return Response.status(404).entity("customer with id \"" + request.customerId + "\" is unknown").build();
        }
        if (m == null) {
            return Response.status(404).entity("merchant with id \"" + request.merchantId + "\" is unknown").build();
        }

        try {
            // TALK TO THE BANK
            bank.transferMoneyFromTo(
                    c.bankAccountId,
                    m.bankAccountId,
                    BigDecimal.valueOf(request.amount),
                    "DTU Pay: " + request.customerId + " -> " + request.merchantId);

            // If we get here, the bank didn't throw an exception (Money moved)
            Payment payment = new Payment();
            payment.id = java.util.UUID.randomUUID().toString();
            payment.amount = request.amount;
            payment.customerId = request.customerId;
            payment.merchantId = request.merchantId;
            payments.add(payment);

            return Response.status(201).entity(payment).build();

        } catch (BankServiceException_Exception e) {
            // Bank refused (no money, invalid account, etc)
            return Response.status(409).entity("Bank failed: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("payments")
    public List<Payment> getPayments() {
        return List.copyOf(payments);
    }

    // Cleanup endpoints for testing
    @DELETE
    @Path("customers/{id}")
    public void deleteCustomer(@PathParam("id") String id) {
        customers.remove(id);
    }

    @DELETE
    @Path("merchants/{id}")
    public void deleteMerchant(@PathParam("id") String id) {
        merchants.remove(id);
    }
}