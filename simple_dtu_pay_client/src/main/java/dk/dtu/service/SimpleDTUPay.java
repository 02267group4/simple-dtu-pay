package dk.dtu.service;

import dk.dtu.model.Customer;
import dk.dtu.model.Merchant;
import dk.dtu.model.PaymentRequest;
import dk.dtu.model.Payment;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

//test LOL

public class SimpleDTUPay {

    private static final String BASE_URL = "http://localhost:8080";
    private final Client client = ClientBuilder.newClient();
    private final WebTarget target = client.target(BASE_URL);

    public String register(Customer customer) {
        Response response = target.path("customers")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(customer, MediaType.APPLICATION_JSON));

        if (response.getStatus() != 201)
            throw new RuntimeException("Reg failed");
        return response.readEntity(Customer.class).id;
    }

    public String register(Merchant merchant) {
        Response response = target.path("merchants")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(merchant, MediaType.APPLICATION_JSON));

        if (response.getStatus() != 201)
            throw new RuntimeException("Reg failed");
        return response.readEntity(Merchant.class).id;
    }

    public boolean pay(int amount, String customerId, String merchantId) {
        PaymentRequest req = new PaymentRequest(amount, customerId, merchantId);
        Response response = target.path("payments")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(req, MediaType.APPLICATION_JSON));

        if (response.getStatus() == 404) {
            throw new NotFoundException(response.readEntity(String.class));
        }

        return response.getStatus() == 201;
    }

    public List<Payment> getPayments() {
        return target.path("payments")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Payment>>() {
                });
    }

    public void unregisterCustomer(String id) {
        target.path("customers/" + id).request().delete();
    }

    public void unregisterMerchant(String id) {
        target.path("merchants/" + id).request().delete();
    }

    public void close() {
        client.close();
    }
}