@fix @order_placement
Feature: FIX Order Placement

  As a FIX automation framework
  I want to send NewOrderSingle messages to the FIX Engine
  So that I can validate the ExecutionReport responses

  Background:
    Given FIX session is connected

  @smoke @buy
  Scenario: Send Buy Order and receive New ExecutionReport
    When I send a NewOrderSingle with symbol "AAPL" quantity 100
    Then I should receive an ExecutionReport
    And ExecutionReport status should be "NEW"
    And ExecutionReport ClOrdID should match the sent order
    And ExecutionReport should contain a valid OrderID
    And ExecutionReport Symbol should be "AAPL"
    And ExecutionReport mandatory fields should be present

  @smoke @sell
  Scenario: Send Sell Order and receive New ExecutionReport
    When I send a NewOrderSingle with symbol "MSFT" side "SELL" quantity 200 price 300.00
    Then I should receive an ExecutionReport
    And ExecutionReport status should be "NEW"
    And ExecutionReport ExecType should be "NEW"
    And ExecutionReport OrderQty should be 200

  @filled
  Scenario: Send Buy Order and verify it gets Filled
    When I send a NewOrderSingle with symbol "GOOGL" side "BUY" quantity 50 price 175.00
    Then I should receive an ExecutionReport
    And ExecutionReport status should be "FILLED"
    And ExecutionReport mandatory fields should be present
    And ExecutionReport ClOrdID should match the sent order

  @partial_fill
  Scenario: Send large order and verify partial fill
    When I send a NewOrderSingle with symbol "TSLA" side "BUY" quantity 1000 price 250.00
    Then I should receive an ExecutionReport
    And ExecutionReport ExecType should be "PARTIAL_FILL"
