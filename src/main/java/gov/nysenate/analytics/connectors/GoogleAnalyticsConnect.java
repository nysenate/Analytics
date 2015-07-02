package gov.nysenate.analytics.connectors;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
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

    private static final String analyticsUrl = "https://www.google.com/analytics/feeds/data";

    public GoogleAnalyticsConnect(String serviceAccountId, File keyFile, String appName) throws AuthenticationException {
        this.as = new AnalyticsService(appName);

        final List<String> scopes = Collections.singletonList(analyticsUrl);
        try {
            this.as.setOAuth2Credentials(getOAuthCredential(serviceAccountId, scopes, keyFile));
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException("Key File error:", e);
        }

        try {
            this.dataURL = new URL(analyticsUrl);
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
            query.setStartDate(params.get("start_date"));
            query.setEndDate(params.get("end_date"));

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

    /**
     * Get oath credentials for the specified scopes using a serviceAccountId and a file containing a key
     */
    private static GoogleCredential getOAuthCredential(String serviceAccountId, List<String> scopes, File keyFile)
            throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        return new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId(serviceAccountId)
                .setServiceAccountScopes(scopes)
                .setServiceAccountPrivateKeyFromP12File(keyFile)
                .build();
    }
}
