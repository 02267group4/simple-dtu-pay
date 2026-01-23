Feature: Token

  Scenario: Customer requests too many tokens at once
    Given a customer with name "Alice", CPR "121394-1516", and balance 1000
    When the customer requests 7 tokens
    Then the token request is rejected


  Scenario: Customer requests tokens in valid sequence and retrieves list
    Given a customer with name "Bob", CPR "232925-2627", and balance 0
    When the customer requests 1 token
    Then the token request is accepted
    When the customer requests 3 tokens
    Then the token request is accepted
    When the customer requests their token list
    Then list of 4 tokens are returned


  Scenario: Customer cannot request more tokens when already holding tokens
    Given a customer with name "Charlie", CPR "343936-3738", and balance 0
    When the customer requests 2 tokens
    Then the token request is accepted
    When the customer requests 1 token
    Then the token request is rejected
    When the customer requests their token list
    Then list of 2 tokens are returned
