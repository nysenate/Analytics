package gov.nysenate.webstats;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import com.google.gdata.data.analytics.DataFeed;


public class PageBasedReport implements Report
{
	private AnalyticsProfile m_oAnalyticsProfile;

	
	public PageBasedReport(AnalyticsProfile p_oAnalyticsProfile)
	{
		m_oAnalyticsProfile = p_oAnalyticsProfile;
	} // PageBasedReport()
	
	

	public boolean generateCSV()
    {
	    String strProfileId = m_oAnalyticsProfile.getProfileId();
	    String strProfileName = m_oAnalyticsProfile.getProfileName();
	    String strFileName = Utils.generateFilename(strProfileName, "csv");
        
	    try {
            FileOutputStream fos = new FileOutputStream(strFileName);
            PrintStream ps = new PrintStream(fos);
            ps.println("*** Profile info for " + strProfileName + " (id=" + strProfileId + ")");
            return generateCSV(ps);
        }
        catch (FileNotFoundException ex) {
            return false;
        }
    } // generateCSV()

	
	
    public boolean generateCSV(PrintStream p_oOutputStream)
    {
        PrintStream ps = p_oOutputStream;

        String strDimensions = "ga:pagePath";
        String strMetrics = "ga:pageviews,ga:uniquePageviews,ga:bounces,ga:newVisits";
        String strFilters = "ga:pagePath=~^/senator/[^/]+$";
        String strHeaders = strDimensions.replaceAll("ga:", "") + "," + strMetrics.replaceAll("ga:", "");

        ps.println(strHeaders);

        AnalyticsQueryParams aqp = new AnalyticsQueryParams("2009-01-01", "2009-06-30", strDimensions, strMetrics, null, strFilters);
        DataFeed feed = Utils.getDataFeed(m_oAnalyticsProfile, aqp);
        Utils.printFeedEntries(feed, ps);
        return true;
    } // generateCSV()


} // class DateBasedReport
