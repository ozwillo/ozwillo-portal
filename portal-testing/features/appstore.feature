Feature: App Store
  As a regular citizen
  In order to find and subscribe to Ozwillo applications
  I want to access an application store

# NB the scenarios suppose that the application "inscription sur liste électorale à Valence" exists, and that Alice has not subscribed to it
# NB implement a REST call to manually unsubscribe a user from a specific application
# implementation note here: the "score" is number of "helpful" - number of "unhelpful" + 10
# flagged is number of times someone has flagged a review as spam
# reviews are shown in decreasing order of score, then decreasing order of date
# reviews with score 0 or less, or flagged 2 or more, are hidden
# we will have admin tasks (in the Portal admin app) to reset scores/flags and also view flags
# note we need to keep track of who reviews a review too...

  Background:
    Given that Alice is unsubscribed from the test application
    And the following reviews for the test application:
      | author      | date        | rating    | review                | score | flagged |
      | Brian Deer  | 2012-12-06  | 3         | Lorem ipsum           | 10    | 0       |
      | Roger Vega  | 2012-09-21  | 4         | Dolor sit amet        | 1     | 0       |
      | John Cleese | 2011-02-12  | 0         | Buy Viagra online     | 10    | 1       |


# test objective: check that one can search while not being logged in
# check that install requires login
#  Scenario: Access from home page
#    Given that I am not logged in
#    When I visit the home page
#    And I click "Application Store" in the main menu
#    Then I should be on the appstore page
#    When I type "liste électorale valence" in the search box
#    And I check "Citizens"
#    And I uncheck "Public Bodies" "Companies"
#    And I click "Find"
#    Then the results list should contain "Inscription sur liste électorale à Valence"
#    And in "Inscription sur liste électorale à Valence" app result the official indicator is present
#    And in "Inscription sur liste électorale à Valence" app result the free badge is present
#    When I click "Inscription sur liste électorale à Valence" app result
#    Then the app detail block should contain "Inscription sur liste électorale à Valence"
#    When I click "Install" in the app detail block
#    Then I should be on the login page
#
  Scenario: Access from dashboard
    Given that I am logged in as Alice
    When I visit the dashboard
    And I click the plus icon
    Then I should be on the appstore page


# test objective: subscribe and view the app on the dashboard
#  Scenario: Subscribe to an app
#    Given that I am logged in as Alice
#    When I search the app store for "liste électorale valence"
#    And I click "Install" in the app detail block
#    Then I should be on the Dashboard
#    And the Dashboard should contain "Inscription sur liste électorale à Valence"
#    When I click "Inscription sur liste électorale à Valence"
#    Then I should be in Citizen Kin for Valence
# from there the user can create a request to register as a voter - but that's another app's tests

  Scenario: Review app reviews
    Given that I am logged in as Alice
    When I search the app store for "liste électorale valence"
    Then I should see 3 reviews
    When I downvote review number 3
    Then I should see 2 reviews
    When I upvote review number 1
    Then I should not see the voting tools for review 1
    When I flag review number 2
    Then I should see 1 review

# test objective: when writing a new review it goes at the top (since we have no reviews with score >10 in the test data), and one cannot vote for/against/flag one's own reviews
  Scenario: Write an app review
    Given that I am logged in as Alice
    When I search the app store for "liste électorale valence"
    And I click "Write a review"
    And I submit a review
    Then I should see 4 reviews
    And review 1 should be by "Alice Legrand"
    And I should not see the voting tools for review 1

