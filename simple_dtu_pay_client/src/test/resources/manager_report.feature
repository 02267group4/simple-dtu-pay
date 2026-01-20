Feature: Manager Report

  Scenario: Manager retrieves a report of payments
    Given a customer with name "Alice", CPR "123456-0001", and balance 1000
    And a merchant with name "ShopOne", CPR "123456-0002", and balance 1000
    And the customer has a valid token
    When the merchant initiates a payment for 200 kr by the customer using the token
    And the manager requests the report
    Then the report contains the payment of 200 kr