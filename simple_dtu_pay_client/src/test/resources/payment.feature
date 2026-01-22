Feature: Payment

  Scenario: Successful Payment using the Bank
    Given a customer with name "Alice", CPR "121314-1516", and balance 1000
    And a merchant with name "Bob", CPR "232425-2627", and balance 1000
    And the customer has a valid token
    When the merchant initiates a payment for 100 kr by the customer using the token
    Then the payment is successful
    And the balance of the customer at the bank is 900 kr
    And the balance of the merchant at the bank is 1100 kr

  Scenario: Payment is rejected without a token
    Given a customer with name "Alice", CPR "121314-1516", and balance 1000
    And a merchant with name "Bob", CPR "232425-2627", and balance 1000
    When the merchant initiates a payment for 100 kr by the customer without a token
    Then the payment is rejected
    And the balance of the customer at the bank is 1000 kr
    And the balance of the merchant at the bank is 1000 kr

  Scenario: Payment is rejected when the customer has insufficient funds
    Given a customer with name "Alice", CPR "111111-1121", and balance 50
    And a merchant with name "Bob", CPR "222222-2232", and balance 1000
    And the customer has a valid token
    When the merchant initiates a payment for 100 kr by the customer using the token
    Then the payment is rejected
    And the balance of the customer at the bank is 50 kr
    And the balance of the merchant at the bank is 1000 kr

  Scenario: Successful Payment with a different amount
    Given a customer with name "Alice", CPR "111111-1121", and balance 1000
    And a merchant with name "Bob", CPR "222222-2232", and balance 1000
    And the customer has a valid token
    When the merchant initiates a payment for 250 kr by the customer using the token
    Then the payment is successful
    And the balance of the customer at the bank is 750 kr
    And the balance of the merchant at the bank is 1250 kr