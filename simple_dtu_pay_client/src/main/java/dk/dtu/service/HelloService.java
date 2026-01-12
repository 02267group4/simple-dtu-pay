package dk.dtu.service;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

public class HelloService {

    private static final String BASE_URL = "http://localhost:8080/hello";

    public String hello() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(BASE_URL);
        String response = target.request(MediaType.TEXT_PLAIN).get(String.class);
        client.close();
        return response;
    }
}
