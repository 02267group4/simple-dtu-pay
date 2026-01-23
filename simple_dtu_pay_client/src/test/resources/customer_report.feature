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

  Scenario: Customer with no payments has empty report
    Given a customer with name "Bob", CPR "234567-0021", and balance 500
    When the customer requests their report
    Then the customer report is empty

  Scenario: Customer report contains multiple payments
    Given a customer with name "Charlie", CPR "345678-0031", and balance 2000
    And a merchant with name "ShopTwo", CPR "345678-0032", and balance 1000
    And the customer has a valid token
    When the merchant initiates a payment for 100 kr by the customer using the token
    And the customer has a valid token
    And the merchant initiates a payment for 200 kr by the customer using the token
    And the customer has a valid token
    And the merchant initiates a payment for 300 kr by the customer using the token
    And the customer requests their report
    Then the customer report contains 3 payments
    And the customer report contains the payment of 100 kr
    And the customer report contains the payment of 200 kr
    And the customer report contains the payment of 300 kr

  Scenario: Customer report only shows own payments not other customers
    Given a customer with name "Diana", CPR "456789-0041", and balance 1000
    And a merchant with name "ShopThree", CPR "456789-0042", and balance 1000
    And the customer has a valid token
    When the merchant initiates a payment for 250 kr by the customer using the token
    Given a customer with name "Eve", CPR "567890-0051", and balance 1000
    And the customer has a valid token
    When the merchant initiates a payment for 350 kr by the customer using the token
    And the customer requests their report
    Then the customer report contains 1 payments
    And the customer report contains the payment of 350 kr
    And the customer report does not contain a payment of 250 kr

  Scenario: Customer report shows payments to different merchants
    Given a customer with name "Frank", CPR "678901-0061", and balance 3000
    And a merchant with name "ShopFour", CPR "678901-0062", and balance 1000
    And the customer has a valid token
    When the merchant initiates a payment for 400 kr by the customer using the token
    Given a merchant with name "ShopFive", CPR "678901-0063", and balance 1000
    And the customer has a valid token
    When the merchant initiates a payment for 500 kr by the customer using the token
    And the customer requests their report
    Then the customer report contains 2 payments
    And the customer report contains payments to 2 different merchants
