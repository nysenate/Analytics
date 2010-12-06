package gov.nysenate.webstats;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.data.analytics.AccountEntry;
import com.google.gdata.data.analytics.AccountFeed;
import com.google.gdata.data.analytics.Aggregates;
import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.data.analytics.DataSource;
import com.google.gdata.data.analytics.Dimension;
import com.google.gdata.data.analytics.Metric;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;


public class WebstatsMain
{
    private static final String DEFAULT_PROPERTIES_FILE_NAME = "webstats.props";
	
	
	public static boolean getProfileInfo(AnalyticsService p_oAnalyticsService, String p_strProfileId)
	{
	    String baseUrl = "https://www.google.com/analytics/feeds/data";
	    AnalyticsService as = p_oAnalyticsService;
	    DataQuery query;
	    
	    //------------------------------------------------------
	    // GA Data Feed
	    //------------------------------------------------------
	    // first build the query
	    try {
	      query = new DataQuery(new URL(baseUrl));
	    }
	    catch (MalformedURLException e) {
	      System.err.println("Malformed URL: " + baseUrl);
	      return false;
	    }
	    query.setIds("ga:" + p_strProfileId);
	    query.setDimensions("ga:date");
	    query.setMetrics("ga:visits,ga:visitors,ga:bounces,ga:timeOnSite,ga:newVisits");
	    query.setSort("-ga:visits");
	    query.setFilters("ga:medium==referral");
	    query.setMaxResults(100);
	    query.setStartDate("2008-01-01");
	    query.setEndDate("2008-12-31");
	    URL url = query.getUrl();
	    System.out.println("\n\n*** Profile Info for id="+p_strProfileId);
	    System.out.println("URL: " + url.toString());

	    // Send our request to the Analytics API and wait for the results to come back
	    DataFeed feed;
	    try {
	      feed = as.getFeed(url, DataFeed.class);
	    }
	    catch (IOException e) {
	      System.err.println("Network error trying to retrieve feed: " + e.getMessage());
	      return false;
	    }
	    catch (ServiceException e) {
	      System.err.println("Analytics API responded with an error message: " + e.getMessage());
	      return false;
	    }

	    outputFeedData(feed);
	    outputFeedDataSources(feed);
	    outputFeedAggregates(feed);
	    outputEntryRowData(feed);
	    
	    String tableData = getFeedTable(feed);
	    System.out.println(tableData);

		return true;
	} // getProfileInfo()
	

	
	// ------------------------------------------------------
	// Format Feed Related Data
	// ------------------------------------------------------
	/**
	 * Output the information specific to the feed.
	 * 
	 * @param {DataFeed} feed Parameter passed back from the feed handler.
	 */
	public static void outputFeedData(DataFeed feed)
	{
		System.out.println("\nFeed Title      = "
				+ feed.getTitle().getPlainText() + "\nFeed ID         = "
				+ feed.getId() + "\nTotal Results   = "
				+ feed.getTotalResults() + "\nStart Index     = "
				+ feed.getStartIndex() + "\nItems Per Page  = "
				+ feed.getItemsPerPage() + "\nStart Date      = "
				+ feed.getStartDate().getValue() + "\nEnd Date        = "
				+ feed.getEndDate().getValue());
	} // outputFeedData()

	  
	  
	/**
	 * Output information about the data sources in the feed. Note: the GA
	 * Export API currently has exactly one data source.
	 * 
	 * @param {DataFeed} feed Parameter passed back from the feed handler.
	 */
	public static void outputFeedDataSources(DataFeed feed)
	{
		DataSource gaDataSource = feed.getDataSources().get(0);
		System.out.println("\nTable Name      = "
				+ gaDataSource.getTableName().getValue()
				+ "\nTable ID        = " + gaDataSource.getTableId().getValue()
				+ "\nWeb Property Id = "
				+ gaDataSource.getProperty("ga:webPropertyId")
				+ "\nProfile Id      = "
				+ gaDataSource.getProperty("ga:profileId")
				+ "\nAccount Name    = "
				+ gaDataSource.getProperty("ga:accountName"));
	} // outputFeedDataSources()

	  
	  
	/**
	 * Output all the metric names and values of the aggregate data.
	 * The aggregate metrics represent values across all of the entries selected
	 *     by the query and not just the rows returned.
	 * @param {DataFeed} feed Parameter passed
	 *     back from the feed handler.
	 */
	public static void outputFeedAggregates(DataFeed feed)
	{
		Aggregates aggregates = feed.getAggregates();
		List<Metric> aggregateMetrics = aggregates.getMetrics();
		for (Metric metric : aggregateMetrics) {
			System.out.println("\nMetric Name  = " + metric.getName()
					+ "\nMetric Value = " + metric.getValue()
					+ "\nMetric Type  = " + metric.getType()
					+ "\nMetric CI    = "
					+ metric.getConfidenceInterval().toString());
		}
	} // outputFeedAggregates()

	  
	  
	/**
	 * Output all the important information from the first entry in the data feed.
	 * @param {DataFeed} feed Parameter passed
	 *     back from the feed handler.
	 */
	public static void outputEntryRowData(DataFeed feed)
	{
		List<DataEntry> entries = feed.getEntries();
		if (entries.size() == 0) {
			System.out.println("No entries found");
			return;
		}
		DataEntry singleEntry = entries.get(0);

		// properties specific to all the entries returned in the feed
		System.out.println("Entry ID    = " + singleEntry.getId());
		System.out.println("Entry Title = " + singleEntry.getTitle().getPlainText());

		// iterate through all the dimensions
		List<Dimension> dimensions = singleEntry.getDimensions();
		for (Dimension dimension : dimensions) {
			System.out.println("Dimension Name  = " + dimension.getName());
			System.out.println("Dimension Value = " + dimension.getValue());
		}

		// iterate through all the metrics
		List<Metric> metrics = singleEntry.getMetrics();
		for (Metric metric : metrics) {
			System.out.println("Metric Name  = " + metric.getName());
			System.out.println("Metric Value = " + metric.getValue());
			System.out.println("Metric Type  = " + metric.getType());
			System.out.println("Metric CI    = " + metric.getConfidenceInterval().toString());
		}
	} // outputEntryRowData()
	
	

	/**
	 * Get the data feed values in the feed as a string.
	 * @param {DataFeed} feed Parameter passed
	 *     back from the feed handler.
	 * @return {String} This returns the contents of the feed.
	 */
	public static String getFeedTable(DataFeed feed)
	{
		List<DataEntry> entries = feed.getEntries();
		if (entries.size() == 0) {
			return "No entries found";
		}
		DataEntry singleEntry = entries.get(0);
		List<Dimension> dimensions = singleEntry.getDimensions();
		List<Metric> metrics = singleEntry.getMetrics();
		List<String> feedDataNames = new ArrayList<String>();
		String feedDataValues = "";

		// put all the dimension and metric names into an array
		for (Dimension dimension : dimensions) {
			feedDataNames.add(dimension.getName());
		}
		for (Metric metric : metrics) {
			feedDataNames.add(metric.getName());
		}

		// put the values of the dimension and metric names into the table
		for (DataEntry entry : entries) {
			for (String dataName : feedDataNames) {
				feedDataValues += "\n" + dataName + "\t= "
						+ entry.stringValueOf(dataName);
			}
			feedDataValues += "\n";
		}
		return feedDataValues;
	} // getFeedTable()


	/**
	 * Obtain website statistics from the Google Analytics service.
	 * @param p_oServiceParams Google Analytics service parameters (eg. username, password, output filename).
	 * @param p_strProfileNames Specific target profile names, or null for all profiles associated with account.
	 * @param p_oQueryParams Google Analytics query parameters (eg. start date, end date, metrics).
	 */
	public static boolean getAnalyticsStats(ServiceParams p_oServiceParams, List<String> p_lsProfileNames,
	                                        AnalyticsQueryParams p_oQueryParams)
	{
	    ServiceParams serviceParams = p_oServiceParams;
	    List<String> profileNameList = p_lsProfileNames;
	    AnalyticsQueryParams queryParams = p_oQueryParams;
	    
        AnalyticsService as = new AnalyticsService("NYSenate-Webstats-1.0");

        //------------------------------------------------------
        // Client Login Authentication
        //------------------------------------------------------
        try {
            as.setUserCredentials(serviceParams.getUsername(), serviceParams.getPassword());
        }
        catch (AuthenticationException e) {
            System.err.println("Authentication failed : " + e.getMessage());
            return false;
        }

        AccountFeed feed = Utils.getAccountFeed(as);
        
        //------------------------------------------------------
        // Format Feed Related Data
        //------------------------------------------------------
        // Print top-level information about the feed
        System.out.println(
                "\nFeed Title     = " + feed.getTitle().getPlainText() + 
                "\nTotal Results  = " + feed.getTotalResults() +
                "\nStart Index    = " + feed.getStartIndex() +
                "\nItems Per Page = " + feed.getItemsPerPage() +
                "\nFeed Id        = " + feed.getId());
        
        
        // Print the feeds' entry data
        for (AccountEntry entry : feed.getEntries()) {
            String curProfileId = entry.getProperty("ga:profileId");
            String curProfileName = entry.getTitle().getPlainText();
            /**********************
            System.out.println(
                    "\nWeb Property Id = " + entry.getProperty("ga:webPropertyId") +
                    "\nAccount Name    = " + entry.getProperty("ga:accountName") +
                    "\nAccount Id      = " + entry.getProperty("ga:accountId") +
                    "\nProfile Name    = " + strProfileName +
                    "\nProfile Id      = " + strProfileId +
                    "\nTable Id        = " + entry.getTableId().getValue());
            ***********************/
            
            /* Process the current profile if no profile names were specified in the properties file (which implies
             * all profiles), or if the current profile name matches any of the profile names from the properties file.
             */
            if (profileNameList.size() == 0 || profileNameList.contains(curProfileName)) {
                AnalyticsProfile ap = new AnalyticsProfile(as, curProfileId, curProfileName);
                AnalyticsReport analyticsReport = new AnalyticsReport(ap, queryParams);
                System.out.println("Producing report for " + curProfileName);
                analyticsReport.generateCSV(serviceParams.getPrintStream());
            }
        }
        
        return true;
	} // getAnalyticsStats()

	

	/**
	 * Obtain statistics from the Twitter service.
	 * @param p_oServiceParams Twitter service parameters, such as username, password, and output filename.
	 */
	public static boolean getTwitterStats(ServiceParams p_oServiceParams, List<String> p_lsProfileNames,
	                                      QueryParams p_oQueryParams)
	{
	    TwitterReport twitterReport = new TwitterReport(p_lsProfileNames, p_oQueryParams);
	    twitterReport.generateCSV(p_oServiceParams.getPrintStream());
	    return true;
	} // getTwitterStats()
	
	
	
	/**
	 * Obtain statistics from the Facebook service.
	 * @param p_oServiceParams Facebook service parameters, such as username, password, and output filename.
	 */
	public static boolean getFacebookStats(ServiceParams p_oServiceParams, List<String> p_lsProfileNames,
	                                       QueryParams p_oQueryParams)
	{
	    return true;
	} // getFacebookStats()
	
	
	/**
	 * @param args command line arguments
	 */
	public static void main(String args[])
	{
	    String strPropFilename = DEFAULT_PROPERTIES_FILE_NAME;
	    
	    // Process command line args
	    for (int i = 0; i < args.length; i++) {
	        if (args[i].equals("-c")) {
	            strPropFilename = args[++i];
	        }
	        else {
	            System.err.println(args[i] + ": Invalid argument");
	            System.exit(1);
	        }
	    }
	    
	    // Load configuration parameters from the properties file.
	    Properties props = new Properties();
	    try {
	        props.load(new FileReader(strPropFilename));
	    }
	    catch (IOException ex) {
	        System.err.println("Unable to load properties file: " + strPropFilename);
	        System.exit(1);
	    }
	    
	    // The webstats.ids property is a comma-separated list of ids that reference the various services.
	    String statIdList = props.getProperty("webstats.ids");
	    
	    if (statIdList == null || statIdList.isEmpty()) {
	        System.err.println("Property webstats.ids is required in " + strPropFilename);
	        System.exit(1);
	    }
	    
	    String[] statIds = statIdList.split(",");
	    
	    // Iterate over the list of statistics services.
	    for (String statId : statIds) {
	        System.out.println("\n\nProcessing webstat id=" + statId);
	        String propPrefix = "webstats." + statId;
	        String serviceName = props.getProperty(propPrefix + ".service.name");
	        String username = props.getProperty(propPrefix + ".login.username");
	        String password = props.getProperty(propPrefix + ".login.password");
            String outfileName = props.getProperty(propPrefix + ".outfile.name");
	        String profileNames = props.getProperty(propPrefix + ".profile.names");
	        String profileFileName = props.getProperty(propPrefix + ".profile.filename");
            String startDate = props.getProperty(propPrefix + ".query.startdate");
            String endDate = props.getProperty(propPrefix + ".query.enddate");
            String dimensions = props.getProperty(propPrefix + ".query.dimensions");
            String metrics = props.getProperty(propPrefix + ".query.metrics");
            String sort = props.getProperty(propPrefix + ".query.sort");
            String filters = props.getProperty(propPrefix + ".query.filters");

            ServiceParams serviceParams = null;
            try {
                serviceParams = new ServiceParams(serviceName, username, password, outfileName);
            }
            catch (AssertionError ex) {
    	        System.err.println(statId + ": Service name and username/password must be specified; skipping");
    	        continue;
    	    }
            catch (FileNotFoundException ex) {
                System.err.println(statId + ": Unable to open output file: " + outfileName + "; skipping");
                continue;
            }
    	    
            // Get list of target profiles from the profile.filename parameter.
            List<String> profileNameList = new ArrayList<String>();
            if (!Utils.isEmpty(profileFileName)) {
                profileNameList = Utils.readFileIntoStringList(profileFileName);
                if (profileNameList == null) {
                    System.err.println(statId + ": Profile filename [" + profileFileName + "] does not exist; skipping");
                    continue;
                }
            }
            
            // Get more target profiles from the comma-separated list specified in the profile.names parameter.
    	    if (!Utils.isEmpty(profileNames)) {
    	        String[] profileNameArray = profileNames.split(",");
    	        List<String> profileNameSublist = Arrays.asList(profileNameArray);
    	        profileNameList.addAll(profileNameSublist);
    	    }
    	    
    	    if (serviceName.equals("analytics")) {
    	        AnalyticsQueryParams aqp = new AnalyticsQueryParams(startDate, endDate, dimensions, metrics, sort, filters);
    	        getAnalyticsStats(serviceParams, profileNameList, aqp);
    	    }
    	    else if (serviceName.equals("twitter")) {
    	        QueryParams qp = new QueryParams(startDate, endDate);
    	        getTwitterStats(serviceParams, profileNameList, qp);
    	    }
    	    else if (serviceName.equals("facebook")) {
    	        QueryParams qp = new QueryParams(startDate, endDate);
    	        getFacebookStats(serviceParams, profileNameList, qp);
    	    }
    	    else {
    	        System.err.println(statId + ": Service '" + serviceName + "' is not supported");
    	        continue;
    	    }
    	    
    	    serviceParams.closePrintStream();
    	  
	    }
	    System.exit(0);
	} // main()
} // class WebstatsMain
