package gov.nysenate.analytics.connectors;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.ini4j.Profile.Section;

import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.data.analytics.AccountFeed;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class GoogleAnalyticsConnect {

	private final Section config;
	private AnalyticsService as;

	public String getDateString(String date_type, Section params) {
		return params.containsKey(date_type) ? params.get(date_type) : config.get(date_type);
	}

	public GoogleAnalyticsConnect(Section config) {
		this.config = config;

		try {
			as = new AnalyticsService(config.get("app_name"));
			as.setUserCredentials(config.get("user"), config.get("pass"));
		}
		catch (AuthenticationException e) {
			System.err.println("Authentication failed : " + e.getMessage());
			System.exit(0);
	    }
	}

	public AccountFeed getAccountFeed() {
		try {
			return as.getFeed(new URL(config.get("account_url")), AccountFeed.class);
		} catch (MalformedURLException e) {
	        System.err.println("Malformed URL: " + config.get("account_url"));
	    } catch (IOException e) {
            System.err.println("Network error trying to retrieve feed: " + e.getMessage());
        } catch (ServiceException e) {
            System.err.println("Analytics API responded with an error message: " + e.getMessage());
        }
		return null;
	}

	public DataFeed getDataFeed(Section params) {
		try {
			DataQuery query = new DataQuery(new URL(config.get("data_url")));

			if(params.get("max_results") != null)
				query.setMaxResults(Integer.parseInt(params.get("max_results")));
			else
			    query.setMaxResults(10000); // The max it can handle

			query.setIds("ga:"+params.get("id"));
			query.setStartDate(this.getDateString("start_date", params));
			query.setEndDate(this.getDateString("end_date", params));

			if(params.containsKey("dimensions"))
				query.setDimensions(params.get("dimensions"));
			if(params.containsKey("metrics"))
				query.setMetrics(params.get("metrics"));
			if(params.containsKey("sort"))
				query.setSort(params.get("sort"));
			if(params.containsKey("filters"))
				query.setFilters(params.get("filters"));

			return as.getFeed(query, DataFeed.class);

		} catch (MalformedURLException e) {
			System.err.println("Malformed URL: " + config.get("data_url"));
		} catch (IOException e) {
            System.err.println("Network error trying to retrieve feed: " + e.getMessage());
        } catch (ServiceException e) {
            System.err.println("Analytics API responded with an error message: " + e.getMessage());
            System.out.println(e.getResponseBody());
        }
		return null;
	}
}