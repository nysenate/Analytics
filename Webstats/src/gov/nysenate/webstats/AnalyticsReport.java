package gov.nysenate.webstats;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import com.google.gdata.data.analytics.DataFeed;


public class AnalyticsReport implements Report
{
	private AnalyticsProfile m_oAnalyticsProfile;
	private AnalyticsQueryParams m_oQueryParams;

	
	public AnalyticsReport(AnalyticsProfile p_oAnalyticsProfile, AnalyticsQueryParams p_oQueryParams)
	{
		m_oAnalyticsProfile = p_oAnalyticsProfile;
		m_oQueryParams = p_oQueryParams;
	} // AnalyticsReport()
	
	

	public boolean generateCSV()
    {
	    return generateCSV(null);
    } // generateCSV()

	
	
    public boolean generateCSV(PrintStream p_oOutputStream)
    {
        PrintStream ps = p_oOutputStream;
        String strProfileId = m_oAnalyticsProfile.getProfileId();
        String strProfileName = m_oAnalyticsProfile.getProfileName();
        
        // If no output stream is open, then formulate a file name and use that file as the output stream.
        if (ps == null) {
            String strFileName = Utils.generateFilename(strProfileName, "csv");
            
            try {
                ps = Utils.createOutputFile(strFileName);
            }
            catch (FileNotFoundException ex) {
                return false;
            }
        }

        String strDimensions = m_oQueryParams.getDimensions();
        String strMetrics = m_oQueryParams.getMetrics();
        
        if (strDimensions == null || strMetrics == null) {
            System.err.println("Must specify metrics and dimensions for an Analytics report");
            return false;
        }
        String strHeaders = strDimensions.replaceAll("ga:", "") + "," + strMetrics.replaceAll("ga:", "");

        ps.println("*** Profile info for " + strProfileName + " (id=" + strProfileId + ")");
        ps.println(strHeaders);

        DataFeed feed = Utils.getDataFeed(m_oAnalyticsProfile, m_oQueryParams);
        Utils.printFeedEntries(feed, ps);
        
        // If the output stream was opened within this method, then close it.
        if (p_oOutputStream == null) {
            ps.close();
        }

        return true;
    } // generateCSV()

} // class AnalyticsReport
