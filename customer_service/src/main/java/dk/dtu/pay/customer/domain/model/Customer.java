package dk.dtu.pay.customer.domain.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection                    // keep this for native compilation / reflection
public class Customer {

    private String id;                    // ← private
    private String name;
    private String cprNumber;
    private String bankAccountId;

    // Required for frameworks (Jackson, Hibernate, etc.)
    public Customer() {
    }

    // Better: named / intention-revealing constructor
    public Customer(String name, String cprNumber, String bankAccountId) {
        this.name = name;
        this.cprNumber = cprNumber;
        this.bankAccountId = bankAccountId;
        // you could add basic validation here later
    }

    // Getters (required for most serializers / frameworks)
    public String getId() {
        return id;
    }

    public void setId(String id) {        // useful for repositories
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getCprNumber() {
        return cprNumber;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    // Optional: add setters only if really needed
    // public void setName(String name) { this.name = name; }

    // --- Future improvement examples (add later) ---
    /*
    public boolean hasValidCpr() {
        return cprNumber != null && cprNumber.matches("\\d{6}-\\d{4}");
    }

    public void assignBankAccount(String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Bank account cannot be empty");
        }
        this.bankAccountId = accountId;
    }
    */
}