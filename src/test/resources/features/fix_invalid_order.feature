@fix @invalid_order
Feature: FIX Invalid Order Scenarios

  As a FIX automation framework
  I want to send invalid or malformed FIX orders to the FIX Engine
  So that I can validate that the engine correctly rejects them

  Background:
    Given FIX session is connected

  @smoke @reject
  Scenario: Send order with zero quantity and expect rejection
    When I send a NewOrderSingle with symbol "AAPL" side "BUY" quantity 0 price 150.00
    Then I should receive an ExecutionReport
    And the ExecutionReport should indicate the order was rejected

  @reject_negative_price
  Scenario: Send order with negative price and expect rejection
    When I send a NewOrderSingle with symbol "GOOGL" side "BUY" quantity 100 price -10.00
    Then I should receive an ExecutionReport
    And the ExecutionReport should indicate the order was rejected

  @reject_unknown_symbol
  Scenario: Send order with unknown symbol and expect rejection
    When I send a NewOrderSingle with symbol "XXXX_INVALID" side "BUY" quantity 100 price 50.00
    Then I should receive an ExecutionReport
    And the ExecutionReport should indicate the order was rejected
    And ExecutionReport mandatory fields should be present
