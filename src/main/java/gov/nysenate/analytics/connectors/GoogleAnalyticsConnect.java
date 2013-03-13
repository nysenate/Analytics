package gov.nysenate.analytics.connectors;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.ini4j.Profile.Section;

import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class GoogleAnalyticsConnect
{
    private final URL dataURL;
    private final AnalyticsService as;

    /**
     * 
     * @param config
     *            Required Keys: user, pass, app_name
     * 
     * @throws AuthenticationException
     */
    public GoogleAnalyticsConnect(Map<String, String> config) throws AuthenticationException
    {
        this(config.get("user"), config.get("pass"), config.get("app_name"));
    }

    public GoogleAnalyticsConnect(String user, String pass, String appName) throws AuthenticationException
    {
        this.as = new AnalyticsService(appName);
        this.as.setUserCredentials(user, pass);

        try {
            this.dataURL = new URL("https://www.google.com/analytics/feeds/data");
        }
        catch (MalformedURLException e) {
            System.err.println("Malformed data url. This is hardcoded and shouldn't be possible.");
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }

    public DataFeed getDataFeed(Section params)
    {
        try {
            DataQuery query = new DataQuery(dataURL);

            if (params.get("max_results") != null)
                query.setMaxResults(Integer.parseInt(params.get("max_results")));
            else
                query.setMaxResults(10000); // The max it can handle

            query.setIds("ga:" + params.get("id"));
            query.setStartDate(params.get("end_date"));
            query.setEndDate(params.get("start_date"));

            if (params.containsKey("dimensions"))
                query.setDimensions(params.get("dimensions"));
            if (params.containsKey("metrics"))
                query.setMetrics(params.get("metrics"));
            if (params.containsKey("sort"))
                query.setSort(params.get("sort"));
            if (params.containsKey("filters"))
                query.setFilters(params.get("filters"));

            return as.getFeed(query, DataFeed.class);

        }
        catch (IOException e) {
            System.err.println("Network error trying to retrieve feed: " + e.getMessage());
        }
        catch (ServiceException e) {
            System.err.println("Analytics API responded with an error message: " + e.getMessage());
            System.out.println(e.getResponseBody());
        }
        return null;
    }
}
