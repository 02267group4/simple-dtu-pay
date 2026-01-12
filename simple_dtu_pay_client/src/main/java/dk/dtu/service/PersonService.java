package dk.dtu.service;

import dk.dtu.exception.PersonServiceException;
import dk.dtu.model.Person;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class PersonService {

    private static final String BASE_URL = "http://localhost:8080/person";
    private final Client client = ClientBuilder.newClient();
    private final WebTarget target = client.target(BASE_URL);

    public Person getPerson() {
        Response response = target.request(MediaType.APPLICATION_JSON).get();
        if (response.getStatus() == 200) {
            return response.readEntity(Person.class);
        } else {
            throw handleError(response);
        }
    }

    public void updatePerson(Person person) {
        Response response = target.request(MediaType.APPLICATION_JSON)
                                 .put(Entity.entity(person, MediaType.APPLICATION_JSON));
        if (response.getStatus() != 204) {  // PUT succes = 204 No Content
            throw handleError(response);
        }
        response.close();
    }

    private PersonServiceException handleError(Response response) {
        String errorMessage = "Unknown error";
        if (response.hasEntity()) {
            errorMessage = response.readEntity(String.class);
        }
        response.close();
        return new PersonServiceException("HTTP " + response.getStatus() + ": " + errorMessage);
    }

    public void close() {
        client.close();
    }
}
