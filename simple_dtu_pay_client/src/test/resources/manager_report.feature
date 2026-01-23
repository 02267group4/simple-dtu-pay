Feature: Manager Report

  # Scenario 1: Basic report retrieval
  Scenario: Manager retrieves a report of payments
    Given a customer with name "Alice", CPR "230126-1346", and balance 1000
    And a merchant with name "ShopOne", CPR "230126-1347", and balance 1000
    And the customer has a valid token
    When the merchant initiates a payment for 200 kr by the customer using the token
    And the payment is successful
    And the manager requests the report
    Then the report contains the payment of 200 kr

  # Scenario 2: COMPLETED payment status
  Scenario: Manager retrieves report with COMPLETED payment status
    Given a customer with name "Customer2", CPR "230126-1348", and balance 2000
    And a merchant with name "MerchantC", CPR "230126-1349", and balance 1000
    And the customer has a valid token
    When the merchant initiates a payment for 500 kr by the customer using the token
    And the payment is successful
    And the manager requests the report
    Then the report contains a payment with status "COMPLETED"
    And the report contains the payment of 500 kr

  # Scenario 3: FAILED payment status (token reuse)
  Scenario: Manager retrieves report with FAILED payment status
    Given a customer with name "Customer3", CPR "230126-1350", and balance 2000
    And a merchant with name "MerchantD", CPR "230126-1351", and balance 1000
    And the customer has a valid token
    When the merchant initiates a payment for 100 kr by the customer using the token
    And the payment is successful
    When the merchant initiates a payment for 100 kr by the customer using the same token
    And the payment is rejected
    And the manager requests the report
    Then the report contains a payment with status "FAILED"

  # Scenario 4: Required payment fields validation
  Scenario: Manager report contains all required payment fields
    Given a customer with name "Customer4", CPR "230126-1352", and balance 2000
    And a merchant with name "MerchantG", CPR "230126-1353", and balance 1000
    And the customer has a valid token
    When the merchant initiates a payment for 250 kr by the customer using the token
    And the payment is successful
    And the manager requests the report
    Then all payments in the report have required fields

  # Scenario 5: Unique payment IDs
  Scenario: Manager report contains unique payment IDs
    Given a customer with name "Customer5", CPR "230126-1354", and balance 2000
    And a merchant with name "MerchantH", CPR "230126-1355", and balance 1000
    And the customer has a valid token
    When the merchant initiates a payment for 100 kr by the customer using the token
    And the payment is successful
    And the manager requests the report
    Then all payment IDs in the report are unique

  # Scenario 6: Report immediately after payment creation
  Scenario: Manager requests report immediately after payment creation
    Given a customer with name "Customer6", CPR "230126-1356", and balance 2000
    And a merchant with name "MerchantJ", CPR "230126-1357", and balance 1000
    And the customer has a valid token
    When the merchant initiates a payment for 400 kr by the customer using the token
    And the manager requests the report
    Then the report contains the payment of 400 kr
    And the report contains a payment with status "PENDING" or "COMPLETED"

  # Scenario 7: Multiple report requests consistency
  Scenario: Multiple simultaneous manager report requests return consistent results
    Given a customer with name "Customer7", CPR "230126-1358", and balance 2000
    And a merchant with name "MerchantM", CPR "230126-1359", and balance 1000
    And the customer has a valid token
    When the merchant initiates a payment for 600 kr by the customer using the token
    And the payment is successful
    When the manager requests the report
    And the manager requests the report again
    Then both reports contain the payment of 600 kr
    And both reports have the same number of payments

  # Scenario 8: Find payment by ID
  Scenario: Manager can find specific payment by ID in report
    Given a customer with name "Customer8", CPR "230126-1360", and balance 2000
    And a merchant with name "MerchantI", CPR "230126-1361", and balance 1000
    And the customer has a valid token
    When the merchant initiates a payment for 350 kr by the customer using the token
    And the payment is successful
    And the manager requests the report
    Then the report contains the payment by its ID
