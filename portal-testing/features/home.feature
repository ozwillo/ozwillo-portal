Feature: Home
  As a regular citizen
  In order to learn about OASIS
  I should peruse the OASIS home page

  Scenario: Simple access
    Given I am not logged in
    When I visit the home page
    Then I should see "Overview" "Application Store" "Data Store" in the main menu
    And I should see news
    And I should see the Comments button

  Scenario: User login
    Given that I am not logged in
    When I visit the home page
    Then I should see "Sign up" "Login" buttons in the navigation bar
    When I click "Login"
    Then I should be on the kernel login page
    When I login as Alice
    Then I should be on the home page
    And I should not see "Sign up" "Login"
    And I should see the logged-in user button with label "A. Legrand"
    When I click the logged-in user button
    Then I should see "Log out" in the user menu
    When I click "Log out"
    Then I should be on the home page
    And I should see "Sign up" "Login" buttons in the navigation bar

# Test objective: the comment system lets users comment anonymously on the home page
# But a name (pseudonym) and email address are required. Email address format is verified.
  Scenario: Comment (not logged in)
    Given that I am not logged in
    When I visit the home page
    And I click the Comments button
    Then I should see existing comments
    And I should see the comment form
    And the comment form should include a name and an email field
    When I enter "lorem ipsum" in the message field
    And I click Send
    Then there should be an error
    And there should be an error message next to the email field
    And there should be an error message next to the name field
    And the message field should contain "lorem ipsum"
    When I enter "test-at-oasis-eu.org" in the email field
    And I enter "Marcus Aurelius" in the name field
    And I click Send
    Then there should be an error
    And there should be an error message next to the email field
    And the name field should contain "Marcus Aurelius"
    And the message field should contain "lorem ipsum"
    When I enter "test@oasis-eu.org" in the email field
    And I click Send
    Then the existing comments should contain an entry from "Marcus Aurelius" dated just now

  Scenario: No empty comment
    Given that I am not logged in
    When I visit the home page
    And I click the Comments button
    And I enter "Julius Caesar" in the name field
    And I enter "julius@war-of-the-gauls.it" in the email field
    And I click Send
    Then there should be an error
    And there should be an error message next to the message field

# Test objective: comment when already logged-in does not ask for email
# and the name field is prefilled with the user's real name
# but it is possible to use a pseudonym
  Scenario: Comment (logged in)
    Given that I am logged in as Alice
    When I visit the home page
    And I click the Comments button
    Then I should see existing comments
    And I should see the comment form
    And the comment form should not include an email field
    And the comment form should include a name field
    And the name field should contain "Alice Legrand"
    When I enter "Not my real name" in the name field
    And I enter "lorem ipsum" in the message field
    And I click Send
    Then the existing comments should contain an entry from "Not my real name" dated just now

