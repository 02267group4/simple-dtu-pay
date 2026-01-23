Feature: Customer Report

  Scenario: Customer retrieves a report of their payments
    Given a customer with name "Alice", CPR "123956-0011", and balance 1000
    And a merchant with name "ShopOne", CPR "123956-0012", and balance 1000
    And the customer has a valid token
    When the merchant initiates a payment for 150 kr by the customer using the token
    And the customer requests their report
    Then the customer report contains the payment of 150 kr
    And the customer report contains the merchant
    And the customer report contains the token used
