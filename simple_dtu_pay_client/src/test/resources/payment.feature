Feature: Payment

  Scenario: Successful payment and Token reuse is rejected
    Given a customer with name "Alice", CPR "121314-1516", and balance 1000
    And a merchant with name "Bob", CPR "232425-2627", and balance 1000
    And the customer has a valid token
    When the merchant initiates a payment for 100 kr by the customer using the token
    Then the payment is successful
    And the balance of the customer at the bank is 900 kr
    And the balance of the merchant at the bank is 1100 kr
    When the merchant initiates a payment for 100 kr by the customer using the same token
    Then the payment is rejected
    And the balance of the customer at the bank is 900 kr
    And the balance of the merchant at the bank is 1100 kr



  Scenario: Payment is rejected without a token
    Given a customer with name "Alice", CPR "121314-1516", and balance 1000
    And a merchant with name "Bob", CPR "232425-2627", and balance 1000
    When the merchant initiates a payment for 100 kr by the customer without a token
    Then the payment is rejected
    And the balance of the customer at the bank is 1000 kr
    And the balance of the merchant at the bank is 1000 kr