package dk.dtu.pay.service.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Customer {
    public String id;
    public String name;
    public String cprNumber;
    public String bankAccountId;

    public Customer() {
    }

    public Customer(String name, String cprNumber, String bankAccountId) {
        this.name = name;
        this.cprNumber = cprNumber;
        this.bankAccountId = bankAccountId;
    }
}