@fix @order_cancellation
Feature: FIX Order Cancellation

  As a FIX automation framework
  I want to send OrderCancelRequest messages to the FIX Engine
  So that I can validate order cancellation flows

  Background:
    Given FIX session is connected

  @smoke @cancel
  Scenario: Send Buy Order and then cancel it
    When I send a NewOrderSingle with symbol "AAPL" quantity 100
    And I should receive an ExecutionReport
    And ExecutionReport status should be "NEW"
    When I send an OrderCancelRequest for the last order
    Then I should receive an ExecutionReport
    And ExecutionReport status should be "CANCELED"
    And ExecutionReport ClOrdID should match the sent order

  @cancel_filled
  Scenario: Attempt to cancel an already filled order
    When I send a NewOrderSingle with symbol "IBM" side "BUY" quantity 10 price 140.00
    Then ExecutionReport status should be "FILLED"
    When I send an OrderCancelRequest for the last order
    Then I should receive an ExecutionReport
    And the ExecutionReport should indicate the order was rejected
