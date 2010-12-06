// Copyright 2009 Google Inc. All Rights Reserved.

/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.data.analytics.Aggregates;
import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.data.analytics.DataSource;
import com.google.gdata.data.analytics.Dimension;
import com.google.gdata.data.analytics.Metric;
import com.google.gdata.data.analytics.Segment;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample program demonstrating how to make a data request to the GA Data Export
 * using client login authorization as well as accessing important data in the
 * feed.
 */
public class DataFeedExample {

	private static final String CLIENT_USERNAME = "jared.mi.williams@gmail.com";
	private static final String CLIENT_PASS = "0308426191";
	private static final String TABLE_ID = "ga:16456134";

	public DataFeed feed;

	public static void main(String[] args) {
		DataFeedExample example;

		try {
			example = new DataFeedExample();
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

		example.printFeedData();
		example.printFeedDataSources();
		example.printFeedAggregates();
		example.printSegmentInfo();
		example.printDataForOneEntry();

		System.out.println(example.getEntriesAsTable());
	}

	/**
	 * Creates a new service object, attempts to authorize using the Client Login
	 * authorization mechanism and requests data from the Google Analytics API.
	 * @throws AuthenticationException if an error occurs with authorizing with
	 *     Google Accounts.
	 * @throws IOException if a network error occurs.
	 * @throws ServiceException if an error occurs with the Google Analytics API.
	 */
	public DataFeedExample() throws AuthenticationException, IOException, ServiceException {

		// Configure GA API.
		AnalyticsService as = new AnalyticsService("gaExportAPI_acctSample_v2.0");

		// Client Login Authorization.
		as.setUserCredentials(CLIENT_USERNAME, CLIENT_PASS);

		// GA Data Feed query uri.
		String baseUrl = "https://www.google.com/analytics/feeds/data";
		DataQuery query = new DataQuery(new URL(baseUrl));
		query.setIds(TABLE_ID);
		query.setDimensions("ga:source,ga:medium");
		query.setMetrics("ga:visits,ga:bounces");
		query.setSegment("gaid::-11");
		query.setFilters("ga:medium==referral");
		query.setSort("-ga:visits");
		query.setMaxResults(5);
		query.setStartDate("2010-04-01");
		query.setEndDate("2010-04-25");
		URL url = query.getUrl();
		System.out.println("URL: " + url.toString());

		// Send our request to the Analytics API and wait for the results to
		// come back.
		feed = as.getFeed(url, DataFeed.class);
	}

	/**
	 * Prints the important Google Analytics relates data in the Data Feed.
	 */
	public void printFeedData() {
		System.out.println("\n-------- Important Feed Information --------");
		System.out.println(
				"\nFeed Title      = " + feed.getTitle().getPlainText() +
				"\nFeed ID         = " + feed.getId() +
				"\nTotal Results   = " + feed.getTotalResults() +
				"\nSart Index      = " + feed.getStartIndex() +
				"\nItems Per Page  = " + feed.getItemsPerPage() +
				"\nStart Date      = " + feed.getStartDate().getValue() +
				"\nEnd Date        = " + feed.getEndDate().getValue());
	}

	/**
	 * Prints the important information about the data sources in the feed.
	 * Note: the GA Export API currently has exactly one data source.
	 */
	public void printFeedDataSources() {
		DataSource gaDataSource = feed.getDataSources().get(0);
		System.out.println("\n-------- Data Source Information --------");
		System.out.println(
				"\nTable Name      = " + gaDataSource.getTableName().getValue() +
				"\nTable ID        = " + gaDataSource.getTableId().getValue() +
				"\nWeb Property Id = " + gaDataSource.getProperty("ga:webPropertyId") +
				"\nProfile Id      = " + gaDataSource.getProperty("ga:profileId") +
				"\nAccount Name    = " + gaDataSource.getProperty("ga:accountName"));
	}

	/**
	 * Prints all the metric names and values of the aggregate data. The
	 * aggregate metrics represent the sum of the requested metrics across all
	 * of the entries selected by the query and not just the rows returned.
	 */
	public void printFeedAggregates() {
		System.out.println("\n-------- Aggregate Metric Values --------");
		Aggregates aggregates = feed.getAggregates();
		for (Metric metric : aggregates.getMetrics()) {
			System.out.println(
					"\nMetric Name  = " + metric.getName() +
					"\nMetric Value = " + metric.getValue() +
					"\nMetric Type  = " + metric.getType() +
					"\nMetric CI    = " + metric.getConfidenceInterval().toString());
		}
	}

	/**
	 * Prints segment information if the query has an advanced segment defined.
	 */
	public void printSegmentInfo() {
		if (feed.hasSegments()) {
			System.out.println("\n-------- Advanced Segments Information --------");
			for (Segment segment : feed.getSegments()) {
				System.out.println(
						"\nSegment Name       = " + segment.getName() +
						"\nSegment ID         = " + segment.getId() +
						"\nSegment Definition = " + segment.getDefinition().getValue());
			}
		}
	}

	/**
	 * Prints all the important information from the first entry in the
	 * data feed.
	 */
	public void printDataForOneEntry() {
		System.out.println("\n-------- Important Entry Information --------\n");
		if (feed.getEntries().isEmpty()) {
			System.out.println("No entries found");
		} else {
			DataEntry singleEntry = feed.getEntries().get(0);

			// Properties specific to all the entries returned in the feed.
			System.out.println("Entry ID    = " + singleEntry.getId());
			System.out.println("Entry Title = " + singleEntry.getTitle().getPlainText());

			// Iterate through all the dimensions.
			for (Dimension dimension : singleEntry.getDimensions()) {
				System.out.println("Dimension Name  = " + dimension.getName());
				System.out.println("Dimension Value = " + dimension.getValue());
			}

			// Iterate through all the metrics.
			for (Metric metric : singleEntry.getMetrics()) {
				System.out.println("Metric Name  = " + metric.getName());
				System.out.println("Metric Value = " + metric.getValue());
				System.out.println("Metric Type  = " + metric.getType());
				System.out.println("Metric CI    = " + metric.getConfidenceInterval().toString());
			}
		}
	}

	/**
	 * Get the data feed values in the feed as a string.
	 * @return {String} This returns the contents of the feed.
	 */
	public String getEntriesAsTable() {
		if (feed.getEntries().isEmpty()) {
			return "No entries found";
		}
		DataEntry singleEntry = feed.getEntries().get(0);
		List<String> feedDataNames = new ArrayList<String>();
		StringBuffer feedDataValues = new StringBuffer("\n-------- All Entries In A Table --------\n");

		// Put all the dimension and metric names into an array.
		for (Dimension dimension : singleEntry.getDimensions()) {
			feedDataNames.add(dimension.getName());
		}
		for (Metric metric : singleEntry.getMetrics()) {
			feedDataNames.add(metric.getName());
		}

		// Put the values of the dimension and metric names into the table.
		for (DataEntry entry : feed.getEntries()) {
			for (String dataName : feedDataNames) {
				feedDataValues.append(String.format("\n%s \t= %s",
						dataName, entry.stringValueOf(dataName)));
			}
			feedDataValues.append("\n");
		}
		return feedDataValues.toString();
	}
}