package dk.dtu.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Merchant {
    public String id;
    public String name;
    public String cprNumber;
    public String bankAccountId;

    public Merchant() {
    }

    public Merchant(String name, String cprNumber, String bankAccountId) {
        this.name = name;
        this.cprNumber = cprNumber;
        this.bankAccountId = bankAccountId;
    }
}