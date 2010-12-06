package gov.nysenate.webstats;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import com.google.gdata.data.analytics.DataFeed;


public class DateBasedReport implements Report
{
	private AnalyticsProfile m_oAnalyticsProfile;

	
	public DateBasedReport(AnalyticsProfile p_oAnalyticsProfile)
	{
		m_oAnalyticsProfile = p_oAnalyticsProfile;
	} // DateBasedReport()
	
	

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

        String strDimensions = "ga:date";
        String strMetrics = "ga:visits,ga:visitors,ga:bounces,ga:timeOnSite,ga:newVisits";
        String strHeaders = strDimensions.replaceAll("ga:", "") + "," + strMetrics.replaceAll("ga:", "");

        ps.println(strHeaders);

        AnalyticsQueryParams aqp = new AnalyticsQueryParams("2008-01-01", "2008-06-30", strDimensions, strMetrics);
        DataFeed feed = Utils.getDataFeed(m_oAnalyticsProfile, aqp);
        Utils.printFeedEntries(feed, ps);

        aqp.setDates("2008-07-01", "2008-12-31");
        feed = Utils.getDataFeed(m_oAnalyticsProfile, aqp);
        Utils.printFeedEntries(feed, ps);

        aqp.setDates("2009-01-01", "2009-06-30");
        feed = Utils.getDataFeed(m_oAnalyticsProfile, aqp);
        Utils.printFeedEntries(feed, ps);
        return true;
    } // generateCSV()

} // class DateBasedReport
