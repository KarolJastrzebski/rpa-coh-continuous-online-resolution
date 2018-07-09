Feature: Question Round Logic

  Scenario: Create question round from 1 to 2
    Given a standard online hearing is created
    And a standard question
    Given the question round is ' "1" '
    When the post request is sent to create the question
    And the response code is 200
    Given the question round is ' "2" '
    When the post request is sent to create the question
    And the response code is 200

  Scenario: If no jurisdiction question round limit is set then validate question round
    Given a standard online hearing
    And a jurisdiction named ' "Civil directions", with id ' "55" ' with url ' "http://localhost:8080/civildirection" and max question rounds ' "0" ' is created
    And the online hearing jurisdiction is ' "Civil directions" '
    And the post request is sent to create the online hearing
    And a standard question
    Given the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    Given the question round is ' "2" '
    When the post request is sent to create the question
    Then the response code is 200
    Given the question round is ' "4" '
    When the post request is sent to create the question
    Then the response code is 422

  Scenario: Attempt to create the first question as question round 2
    Given a standard online hearing is created
    And a standard question
    Given the question round is ' "2" '
    When the post request is sent to create the question
    Then the response code is 422

  Scenario: Create a question round from 2 to 1 is invalid
    Given a standard online hearing is created
    And a standard question
    Given the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    Given the question round is ' "2" '
    When the post request is sent to create the question
    Then the response code is 200
    Given the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 422

  Scenario: Create question round from 1 to 3 is invalid
    Given a standard online hearing is created
    And a standard question
    And the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    Given the question round is ' "3" '
    When the post request is sent to create the question
    Then the response code is 422

  Scenario: Create question round from 1 to 3 is invalid when the max jurisdiction is 2
    Given a standard online hearing
    And a jurisdiction named ' "Civil directions", with id ' "55" ' with url ' "http://localhost:8080/civildirection" and max question rounds ' "2" ' is created
    And the online hearing jurisdiction is ' "Civil directions" '
    And the post request is sent to create the online hearing
    Given a standard question
    And the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    Given the question round is ' "2" '
    When the post request is sent to create the question
    Then the response code is 200
    Given the question round is ' "3" '
    When the post request is sent to create the question
    Then the response code is 422

  Scenario: Get all question rounds for online hearing and check state is issued
    Given a standard online hearing is created
    And a standard question
    And the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    And the question is updated to issued
    And a standard question
    And the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    And the question is updated to issued
    When the get request is sent to get all question rounds
    Then the response code is 200
    And the number of questions rounds is ' "1" '
    And the number of questions in question round ' "1" ' is ' "2" '
    And the question round ' "1" ' is ' "ISSUED" '

  Scenario: Get all question rounds for online hearing and check state is drafted if one question is draft
    Given a standard online hearing is created
    And a standard question
    And the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    And a standard question
    And the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    And the question is updated to issued
    When the get request is sent to get all question rounds
    Then the response code is 200
    And the number of questions rounds is ' "1" '
    And the number of questions in question round ' "1" ' is ' "2" '
    And the question round ' "1" ' is ' "DRAFTED" '

  Scenario: Get all question rounds for online hearing and each QR has it's own state
    Given a standard online hearing is created
    And a standard question
    And the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    And a standard question
    And the question round is ' "2" '
    When the post request is sent to create the question
    Then the response code is 200
    And the question is updated to issued
    When the get request is sent to get all question rounds
    Then the response code is 200
    And the number of questions rounds is ' "2" '
    And the question round ' "1" ' is ' "DRAFTED" '
    And the question round ' "2" ' is ' "ISSUED" '

  Scenario: Get all question rounds for online hearing and check the previous, current, next & max QRs are correct
    Given a standard online hearing is created
    And a standard question
    And the question round is ' "1" '
    When the post request is sent to create the question
    Then the response code is 200
    And a standard question
    And the question round is ' "2" '
    When the post request is sent to create the question
    Then the response code is 200
    And the question is updated to issued
    When the get request is sent to get all question rounds
    Then the response code is 200
    And the previous question round is ' "1" '
    And the current question round is ' "2" '
    And the next question round is ' "2" '
    And the max question round is ' "2" '