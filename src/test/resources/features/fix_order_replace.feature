@fix @order_replace
Feature: FIX Order Cancel/Replace

  As a FIX automation framework
  I want to send OrderCancelReplaceRequest messages to the FIX Engine
  So that I can validate order modification flows

  Background:
    Given FIX session is connected

  @smoke @replace
  Scenario: Send Buy Order and then replace quantity and price
    When I send a NewOrderSingle with symbol "AAPL" quantity 100
    Then I should receive an ExecutionReport
    And ExecutionReport status should be "NEW"
    When I send an OrderCancelReplaceRequest with new quantity 150 and price 155.00
    Then I should receive an ExecutionReport
    And ExecutionReport status should be "REPLACED"
    And ExecutionReport OrderQty should be 150
    And ExecutionReport mandatory fields should be present

  @replace_cancel
  Scenario: Replace an order then cancel the replacement
    When I send a NewOrderSingle with symbol "MSFT" side "BUY" quantity 200 price 310.00
    Then I should receive an ExecutionReport
    And ExecutionReport status should be "NEW"
    When I send an OrderCancelReplaceRequest with new quantity 300 and price 320.00
    Then I should receive an ExecutionReport
    And ExecutionReport status should be "REPLACED"
    When I send an OrderCancelRequest for the last order
    Then I should receive an ExecutionReport
    And ExecutionReport status should be "CANCELED"
