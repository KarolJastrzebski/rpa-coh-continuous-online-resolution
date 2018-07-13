Feature: Questions feature

  Scenario: Submit a question
    Given a standard online hearing is created
    And a standard question
    When the post request is sent to create the question
    Then the response code is 200

  Scenario: Retrieve a question
    Given a standard online hearing is created
    And a standard question
    And the post request is sent to create the question
    When the get request is sent to retrieve the submitted question
    Then the response code is 200
    And the question id matches
    And the question state name is DRAFTED
    And the question state timestamp is today

  Scenario: No questions to retrieve for online hearing
    Given a standard online hearing is created
    When the get request is sent to retrieve all questions
    Then the response code is 200
    And the response contains 0 questions

  Scenario: Submit multiple questions
    Given a standard online hearing is created
    And a standard question
    And the post request is sent to create the question
    And a standard question
    And the post request is sent to create the question
    When the get request is sent to retrieve all questions
    Then the response code is 200
    And the response contains 2 questions

  Scenario: Edit the question body
    Given a standard online hearing is created
    And a standard question
    When the post request is sent to create the question
    Then the response code is 200
    Given the question body is edited to ' "some new question text?" '
    When the put request to update the question is sent
    Then the response code is 200
    When the get request is sent to retrieve the submitted question
    Then the response code is 200
    And the question body is ' "some new question text?" '

  Scenario: Edit the question header
    Given a standard online hearing is created
    And a standard question
    When the post request is sent to create the question
    Then the response code is 200
    Given the question header is edited to ' "some new header text?" '
    When the put request to update the question is sent
    Then the response code is 200
    When the get request is sent to retrieve the submitted question
    Then the response code is 200
    And the question header is ' "some new header text?" '

  Scenario: Edit the question state to submitted
    Given a standard online hearing is created
    And a standard question
    When the post request is sent to create the question
    Then the response code is 200
    Given the question state is edited to ' "SUBMITTED" '
    When the put request to update the question is sent
    Then the response code is 200
    When the get request is sent to retrieve the submitted question
    Then the response code is 200
    And the question state name is SUBMITTED