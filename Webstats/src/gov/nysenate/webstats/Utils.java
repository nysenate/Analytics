package gov.nysenate.webstats;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.data.analytics.AccountFeed;
import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.data.analytics.Dimension;
import com.google.gdata.data.analytics.Metric;
import com.google.gdata.util.ServiceException;



public class Utils
{
    public static final String ACCOUNT_URL = "https://www.google.com/analytics/feeds/accounts/default";
    public static final String DATA_URL = "https://www.google.com/analytics/feeds/data";
    public static final int MAX_RESULTS = 500;
    
    
    /**
     * Generate a reasonable output filename for a given profile name.
     * @param p_strProfileName The name of the Analytics Profile.
     * @return a filename appropriate for the given profile name
     */
    public static String generateFilename(String p_strProfileName, String p_strExtension)
    {
        String filename = p_strProfileName.replaceFirst("^www\\.", "").replace('.', '_').replace(' ', '_');
        if (p_strExtension != null) {
            filename = filename + "." + p_strExtension;
        }
        return filename;
    } // generateFilename()
    
    
    
    public static PrintStream createOutputFile(String p_strFilename) throws FileNotFoundException
    {
        FileOutputStream fos = new FileOutputStream(p_strFilename, false);
        PrintStream ps = new PrintStream(fos);
        return ps;
    } // createOutputFile()
    
    
    
    public static PrintStream appendOutputFile(String p_strFilename) throws FileNotFoundException
    {
        FileOutputStream fos = new FileOutputStream(p_strFilename, true);
        PrintStream ps = new PrintStream(fos);
        return ps;
    } // appendOutputFile()
    
    
    
    /**
     * @param p_strFileName The file name from which we are reading lines.
     * @return A List of Strings, with each element in the list containing a single line from the file.
     */
    public static List<String> readFileIntoStringList(String p_strFileName)
    {
        BufferedReader bufferedReader = null;
        
        try {
            FileReader fileReader = new FileReader(p_strFileName);
            bufferedReader = new BufferedReader(fileReader);
            List<String> oStringList = new ArrayList<String>();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                oStringList.add(line);
            }
            return oStringList; 
        }
        catch (FileNotFoundException ex) {
            System.err.println(p_strFileName + ": File not found");
            return null;
        }
        catch (IOException ex) {
            return null; 
        }
        finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                }
                catch (IOException ex) {
                }
            }
        }
    } // readFileIntoStringList()
    
    
    
    public static AccountFeed getAccountFeed(AnalyticsService p_oAnalyticsService)
    {
        AnalyticsService as = p_oAnalyticsService;
        String baseUrl = ACCOUNT_URL;
        URL queryUrl;
        try {
            queryUrl = new URL(baseUrl);
        }
        catch (MalformedURLException e) {
            System.err.println("Malformed URL: " + baseUrl);
            return null;
        }
        
        // Send our request to the Analytics API and wait for the results to come back
        AccountFeed accountFeed;
        try {
            accountFeed = as.getFeed(queryUrl, AccountFeed.class);
        }
        catch (IOException e) {
            System.err.println("Network error trying to retrieve feed: " + e.getMessage());
            return null;
        }
        catch (ServiceException e) {
            System.err.println("Analytics API responded with an error message: " + e.getMessage());
            return null;
        }
        return accountFeed;
    } // getAccountFeed()


    
    public static DataFeed getDataFeed(AnalyticsService p_oAnalyticsService, DataQuery p_oDataQuery)
    {
        AnalyticsService as = p_oAnalyticsService;
        DataQuery query = p_oDataQuery;
        URL url = query.getUrl();

        // Send our request to the Analytics API and wait for the results to come back.
        DataFeed feed;
        try {
            feed = as.getFeed(url, DataFeed.class);
        }
        catch (IOException e) {
            System.err.println("Network error trying to retrieve feed: " + e.getMessage());
            return null;
        }
        catch (ServiceException e) {
            System.err.println("Analytics API responded with an error message: " + e.getMessage());
            return null;
        }
        return feed;
    } // getDataFeed()
    
    
    
    public static DataFeed getDataFeed(AnalyticsProfile p_oAnalyticsProfile, AnalyticsQueryParams p_oAnalyticsQueryParams)
    {
        AnalyticsService as = p_oAnalyticsProfile.getAnalyticsService();
        String strId = p_oAnalyticsProfile.getProfileId();
        String strStartDate = p_oAnalyticsQueryParams.getStartDate();
        String strEndDate = p_oAnalyticsQueryParams.getEndDate();
        String strDimensions = p_oAnalyticsQueryParams.getDimensions();
        String strMetrics = p_oAnalyticsQueryParams.getMetrics();
        String strSort = p_oAnalyticsQueryParams.getSort();
        String strFilters = p_oAnalyticsQueryParams.getFilters();
        
        String baseUrl = DATA_URL;
        DataQuery query;
        
        try {
            query = new DataQuery(new URL(baseUrl));
        }
        catch (MalformedURLException ex) {
            System.err.println("Malformed URL: " + baseUrl);
            return null;
        }
        
        query.setIds("ga:"+strId);
        query.setStartDate(strStartDate);
        query.setEndDate(strEndDate);
        query.setMaxResults(MAX_RESULTS);
        
        if (strDimensions != null) {
            query.setDimensions(strDimensions);
        }
        if (strMetrics != null) {
            query.setMetrics(strMetrics);
        }
        if (strSort != null) {
            query.setSort(strSort);
        }
        if (strFilters != null) {
            query.setFilters(strFilters);
        }
        
        return getDataFeed(as, query);
    } // getDataFeed()
    
    
    
    public static void printFeedEntries(DataFeed p_oDataFeed, PrintStream ps)
    {
        DataFeed feed = p_oDataFeed;
        
        if (feed == null) {
            return;
        }
        
        List<DataEntry> entries = feed.getEntries();

        if (entries.size() == 0) {
            System.err.println("There are no entries that match this request");
            return;
        }
        
        DataEntry firstEntry = entries.get(0);
        List<Dimension> dimensions = firstEntry.getDimensions();
        List<Metric> metrics = firstEntry.getMetrics();
        List<String> feedDataNames = new ArrayList<String>();
        
        for (Dimension dimension : dimensions) {
            feedDataNames.add(dimension.getName());
        }
        for (Metric metric : metrics) {
            feedDataNames.add(metric.getName());
        }
        
        for (DataEntry entry : entries) {
            for (String dataName : feedDataNames) {
                ps.print(entry.stringValueOf(dataName) + ",");
            }
            ps.println();
        }
        return;
    } // printFeedEntries()

    
    public static boolean isEmpty(String p_strText)
    {
        if (p_strText == null || p_strText.length() == 0 || p_strText.trim().length() == 0) {
            return true;
        }
        return false;
    } // isEmpty()
    
    
    public static String convertDate(String p_strDate)
    {
        String strDate = p_strDate;
        
        // If the provided date does not start with a digit, assume that it is a date keyword, such as "today".
        if (!Character.isDigit(strDate.charAt(0))) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            
            if (strDate.equals("yesterday")) {
                cal.add(Calendar.DATE, -1);
            }
            else if (strDate.equals("lastweek") || strDate.equals("weekago")) {
                cal.add(Calendar.DATE, -7);
            }
            else if (strDate.equals("lastmonth") || strDate.equals("monthago")) {
                cal.add(Calendar.MONTH, -1);
            }
            // else strDate is assumed to be "today"
            strDate = df.format(cal.getTime());
        }
        return strDate;
    } // convertDate()

} // class Utils
