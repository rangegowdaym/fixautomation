@fix @duplicate_order
Feature: FIX Duplicate Order Scenarios

  As a FIX automation framework
  I want to test duplicate order handling in the FIX Engine
  So that I can verify idempotency and error handling for repeated orders

  Background:
    Given FIX session is connected

  @smoke @duplicate
  Scenario: Send identical NewOrderSingle twice and verify second is rejected or acknowledged
    When I send a NewOrderSingle with symbol "AAPL" quantity 100
    Then I should receive an ExecutionReport
    And ExecutionReport status should be "NEW"
    And ExecutionReport ClOrdID should match the sent order

  @order_status
  Scenario: Send OrderStatusRequest after placing an order
    When I send a NewOrderSingle with symbol "AAPL" quantity 100
    Then I should receive an ExecutionReport
    And ExecutionReport status should be "NEW"
    When I send an OrderStatusRequest for the last order
    Then I should receive an ExecutionReport
    And ExecutionReport mandatory fields should be present

  @multiple_orders
  Scenario Outline: Place multiple orders for different symbols
    When I send a NewOrderSingle with symbol "<symbol>" side "<side>" quantity <qty> price <price>
    Then I should receive an ExecutionReport
    And ExecutionReport status should be "NEW"
    And ExecutionReport Symbol should be "<symbol>"

    Examples:
      | symbol | side | qty | price  |
      | AAPL   | BUY  | 100 | 150.00 |
      | MSFT   | BUY  | 200 | 300.00 |
      | GOOGL  | SELL | 50  | 175.00 |
      | AMZN   | BUY  | 75  | 185.00 |
