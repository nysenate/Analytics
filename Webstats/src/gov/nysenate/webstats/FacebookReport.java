package gov.nysenate.webstats;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

import com.google.code.facebookapi.FacebookJsonRestClient;


public class FacebookReport implements Report
{
    private static final String DEFAULT_OUTFILE_NAME = "nysenate_facebook_stats.csv";
    private List<String> m_lsProfileNames;
    private QueryParams m_oQueryParams;
    private FacebookJsonRestClient m_oFacebook;
	
    
	public FacebookReport(List<String> p_lsProfileNames, QueryParams p_oQueryParams)
	{
	    m_lsProfileNames = p_lsProfileNames;
	    m_oQueryParams = p_oQueryParams;
	    m_oFacebook = new FacebookJsonRestClient("", "");
	} // FacebookReport()
	
	

	public boolean generateCSV()
    {
	    return generateCSV(null);
    } // generateCSV()

	
	
    public boolean generateCSV(PrintStream p_oOutputStream)
    {
        PrintStream ps = p_oOutputStream;
        String startDate = m_oQueryParams.getStartDate();
        
        if (ps == null) {
            try {
                ps = Utils.createOutputFile(DEFAULT_OUTFILE_NAME);
            }
            catch (FileNotFoundException ex) {
                return false;
            }
        }
        
        ps.println("profileName,id,friends,followers,favorites,statuses,mentions,hashtags,created,website");
        for (String profileName : m_lsProfileNames) {
            long profileId = 0;
            long friendsCount = 0;
            long followersCount = 0;
            long favoritesCount = 0;
            long statusesCount = 0;
            ps.println(profileName + "," + profileId + "," + friendsCount + "," + followersCount +
                                     "," + favoritesCount + "," + statusesCount);
        }

        if (p_oOutputStream == null) {
            ps.close();
        }
        return true;
    } // generateCSV()

} // class FacebookReport
