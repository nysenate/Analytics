import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.data.analytics.AccountEntry;
import com.google.gdata.data.analytics.AccountFeed;
import com.google.gdata.data.analytics.CustomVariable;
import com.google.gdata.data.analytics.Destination;
import com.google.gdata.data.analytics.Engagement;
import com.google.gdata.data.analytics.Goal;
import com.google.gdata.data.analytics.Segment;
import com.google.gdata.data.analytics.Step;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.URL;

/**
 * Sample program demonstrating how to make a data request to the GA Data
 * Export API using client login authorization as well as accessing important
 * data in the account feed.
 */
public class AccountFeedExample {

  private static final String CLIENT_USERNAME = "jared.mi.williams@gmail.com";
  private static final String CLIENT_PASS = "0308426191";

  public AccountFeed accountFeed;

  public static void main(String args[]) {
    AccountFeedExample example;

    try {
      example = new AccountFeedExample();
    } catch (AuthenticationException e) {
      System.err.println("Authentication failed : " + e.getMessage());
      return;
    } catch (IOException e) {
      System.err.println("Network error trying to retrieve feed: " + e.getMessage());
      return;
    } catch (ServiceException e) {
      System.err.println("Analytics API responded with an error message: " + e.getMessage());
      return;
    }

    example.printFeedDetails();
    example.printAdvancedSegments();
    example.printCustomVarForOneEntry();
    example.printGoalsForOneEntry();
    example.printAccountEntries();
  }

  /**
   * Creates a new service object, attempts to authorize using the Client Login
   * authorization mechanism and requests data from the Google Analytics API.
   * @throws AuthenticationException if an error occurs with authorizing with
   *     Google Accounts.
   * @throws IOException if a network error occurs.
   * @throws ServiceException if an error occurs with the Google Analytics API.
   */
  public AccountFeedExample() throws AuthenticationException, IOException, ServiceException {

    // Configure GA API.
    AnalyticsService as = new AnalyticsService("gaExportAPI_acctSample_v2.0");

    // Client Login Authorization.
    as.setUserCredentials(CLIENT_USERNAME, CLIENT_PASS);

    // GA Account Feed query uri.
    String baseUrl = "https://www.google.com/analytics/feeds/accounts/default";
    URL queryUrl = new URL(baseUrl);

    // Send our request to the Analytics API and wait for the results to
    // come back.
    accountFeed = as.getFeed(queryUrl, AccountFeed.class);
  }

  /**
   * Prints the important Google Analytics related data in the Account Feed.
   */
  public void printFeedDetails() {
    System.out.println("\n-------- Important Feed Data --------");
    System.out.println(
        "\nFeed Title     = " + accountFeed.getTitle().getPlainText() +
        "\nTotal Results  = " + accountFeed.getTotalResults() +
        "\nStart Index    = " + accountFeed.getStartIndex() +
        "\nItems Per Page = " + accountFeed.getItemsPerPage() +
        "\nFeed ID        = " + accountFeed.getId());
  }

  /**
   * Prints the advanced segments for this user.
   */
  public void printAdvancedSegments() {
    System.out.println("\n-------- Advanced Segments --------");
    if (!accountFeed.hasSegments()) {
      System.out.println("No advanced segments found");
    } else {
      for (Segment segment : accountFeed.getSegments()) {
        System.out.println(
            "\nSegment Name       = " + segment.getName() +
            "\nSegment ID         = " + segment.getId() +
            "\nSegment Definition = " + segment.getDefinition().getValue());
      }
    }
  }

  /**
   * Prints custom variable information for the first profile that has custom
   * variables configured.
   */
  public void printCustomVarForOneEntry() {
    System.out.println("\n-------- Custom Variables --------");
    if (accountFeed.getEntries().isEmpty()) {
      System.out.println("No entries found.");
    } else {
      // Go through each entry to see if any has a Custom Variable defined.
      for (AccountEntry entry : accountFeed.getEntries()) {
        if (entry.hasCustomVariables()) {
          for (CustomVariable customVariable : entry.getCustomVariables()) {
            System.out.println(
                "\nCustom Variable Index = " + customVariable.getIndex() +
                "\nCustom Variable Name  = " + customVariable.getName() +
                "\nCustom Variable Scope = " + customVariable.getScope());
          }
         }
      }
      System.out.println("\nNo custom variables defined for this user");
    }
  }

  /**
   * Prints all the goal information for one profile.
   */
  public void printGoalsForOneEntry() {
    System.out.println("\n-------- Goal Configuration --------");
    if (accountFeed.getEntries().isEmpty()) {
      System.out.println("No entries found.");
    } else {
      // Go through each entry to see if any have Goal information.
      for (AccountEntry entry : accountFeed.getEntries()) {
        if (entry.hasGoals()) {
          for (Goal goal : entry.getGoals()) {
            // Print common information for all Goals in this profile.
            System.out.println("\n----- Goal -----");
            System.out.println(
                "\nGoal Number = " + goal.getNumber() +
                "\nGoal Name   = " + goal.getName() +
                "\nGoal Value  = " + goal.getValue() +
                "\nGoal Active = " + goal.getActive());
            if (goal.hasDestination()) {
              printDestinationGoal(goal.getDestination());
            } else if (goal.hasEngagement()) {
              printEngagementGoal(goal.getEngagement());
            }
          }
          return;
        }
      }
    }
  }

  /**
   * Prints the important information for destination goals including all the
   * configured steps if they exist.
   * @param destination the destination goal configuration.
   */
  public void printDestinationGoal(Destination destination) {
    System.out.println("\n\t----- Destination Goal -----");
    System.out.println(
        "\n\tExpression      = " + destination.getExpression() +
        "\n\tMatch Type      = " + destination.getMatchType() +
        "\n\tStep 1 Required = " + destination.getStep1Required() +
        "\n\tCase Sensitive  = " + destination.getCaseSensitive());

    // Print goal steps.
    if (destination.hasSteps()) {
      System.out.println("\n\t----- Destination Goal Steps -----");
      for (Step step : destination.getSteps()) {
        System.out.println(
            "\n\tStep Number = " + step.getNumber() +
            "\n\tStep Name   = " + step.getName() +
            "\n\tStep Path   = " + step.getPath());
      }
    }
  }

  /**
   * Prints the important information for Engagement Goals.
   * @param engagement The engagement goal configuration.
   */
  public void printEngagementGoal(Engagement engagement) {
    System.out.println("\n\t----- Engagement Goal -----");
    System.out.println(
        "\n\tGoal Type       = " + engagement.getType() +
        "\n\tGoal Comparison = " + engagement.getComparison() +
        "\n\tGoal Threshold  = " + engagement.getThresholdValue());
  }

  /**
   * Prints the important Google Analytics related data in each Account Entry.
   */
  public void printAccountEntries() {
    System.out.println("\n-------- First 1000 Profiles In Account Feed --------");
    if (accountFeed.getEntries().isEmpty()) {
      System.out.println("No entries found.");
    } else {
      for (AccountEntry entry : accountFeed.getEntries()) {
        System.out.println(
          "\nWeb Property Id = " + entry.getProperty("ga:webPropertyId") +
          "\nAccount Name    = " + entry.getProperty("ga:accountName") +
          "\nAccount ID      = " + entry.getProperty("ga:accountId") +
          "\nProfile Name    = " + entry.getTitle().getPlainText() +
          "\nProfile ID      = " + entry.getProperty("ga:profileId") +
          "\nTable Id        = " + entry.getTableId().getValue() +
          "\nCurrency        = " + entry.getProperty("ga:currency") +
          "\nTimeZone        = " + entry.getProperty("ga:timezone") +
          (entry.hasCustomVariables() ? "\nThis profile has custom variables" : "") +
          (entry.hasGoals() ? "\nThis profile has goals" : ""));
      }
    }
  }
}