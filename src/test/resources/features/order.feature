@order
Feature: Order Processing via WebSocket and RabbitMQ

  Background:
    Given the test environment is configured

  @smoke @parallel
  Scenario: Send a new order and receive ACK with final response
    Given I send a new order via WebSocket
    Then I should receive ACK
    And I should verify final response in RabbitMQ

  @parallel
  Scenario: Send a BUY order and verify execution type
    Given I send a new BUY order via WebSocket
    Then I should receive ACK
    And I should verify final response in RabbitMQ
    And the order response should have status "FILLED"
    And the order response should have executionType "MARKET"
