Feature: Token

  Scenario: Customer requests token issuance
    Given a customer with name "Alice", CPR "111111-1111", and balance 1000
    When the customer requests 3 tokens
    Then a request id is returned

  Scenario: Customer requests token list
    Given a customer with name "Bob", CPR "222222-2222", and balance 0
    When the customer requests their token list
    Then a request id is returned

  Scenario: Token service issues a token
    Given a customer with name "Charlie", CPR "333333-3333", and balance 0
    When the test creates a token for the customer
    Then a token is returned
