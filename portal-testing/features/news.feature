Feature: News
  As a regular citizen
  In order to keep informed about Ozwillo
  I want to read news

  Background:
    Given the test news set is loaded
# test set contains 2 news item, the first one dated 04/07/2014 and containing Mary-Poppinsesque text in the text but not in the headline
# also the news item is translated in French
# NB the test assumes English is the global default language (when no Accept-Language header is provided)

  Scenario: Home News
    When I visit the home page
    Then I should see 2 news items
    And the first news item should be dated "04/07/2014"
  
  Scenario: Read more
    When I visit the home page
    And I read more on the first news item
    Then I should be on a news page
# "be on a news page" = url contains /news/date/title
    And the news text should contain "supercalifragilisticexpialidocious"
    When I select language "Français"
    Then the news text should contain "supercalifragilisticexpiadélicieux"
