Feature: Language selection
  As a regular citizen
  In order to understand what I'm told
  I want to view text in my native language

  Scenario: Default language selection (no preference)
    Given that I am not logged in
    When I visit the home page
    Then the language button should read "English"

  Scenario: Default language selection (unknown preference)
    Given that I am not logged in
    When I visit the home page with "zh" language header
    Then the language button should read "English"

  Scenario: Default language selection (fr)
    Given I am not logged in
    When I visit the home page with "fr" language header
    Then I should be on the home page
    And the language button should read "Français"

# check that Belgian French is correctly interpreted as French
  Scenario: Default language selection (fr-be)
    Given I am not logged in
    When I visit the home page with "fr-be" language header
    Then I should be on the home page
    And the language button should read "Français"

  Scenario: Manual language selection
    Given I am not logged in
    When I visit the home page with "en" language header
    Then I should be on the home page
    And the language button should read "English"
    When I select language "Italiano"
    Then the language button should read "Italiano"

# Test objective: save language preference in the portal storage, for logged in users only
  Scenario: Persistent language selection
    Given that Alice has no stored language preference
    And that I am logged in as Alice
    When I visit the home page
    Then the language button should read "English"
    When I select language "Italiano"
    And I log out
    Then I should be on the home page
    And the language button should read "English"
    When I log in as Alice
    Then I should be on the home page
    And the language button should read "Italiano"

