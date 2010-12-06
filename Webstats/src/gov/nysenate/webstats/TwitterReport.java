package gov.nysenate.webstats;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.URI;
import java.util.List;

import winterwell.jtwitter.Twitter;


public class TwitterReport implements Report
{
    private static final String DEFAULT_OUTFILE_NAME = "nysenate_twitter_stats.csv";
    private List<String> m_lsProfileNames;
    private QueryParams m_oQueryParams;
    private Twitter m_oTwitter;
	
    
	public TwitterReport(List<String> p_lsProfileNames, QueryParams p_oQueryParams)
	{
	    m_lsProfileNames = p_lsProfileNames;
	    m_oQueryParams = p_oQueryParams;
	    m_oTwitter = new Twitter();
	} // TwitterReport()
	
	

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
            Twitter.User twitterUser = m_oTwitter.show(profileName);
            List<Twitter.Status> mentions = m_oTwitter.search("@"+profileName+" since:"+startDate);
            List<Twitter.Status> hashtags = m_oTwitter.search("#"+profileName+" since:"+startDate);
            long profileId = twitterUser.getId();
            long friendsCount = twitterUser.getFriendsCount();
            long followersCount = twitterUser.getFollowersCount();
            long favoritesCount = twitterUser.getFavoritesCount();
            long statusesCount = twitterUser.getStatusesCount();
            int mentionsCount = mentions.size();
            int hashtagsCount = hashtags.size();
            String createdAt = twitterUser.getCreatedAt();
            URI websiteURI = twitterUser.getWebsite();
            String website = websiteURI == null ? "" : websiteURI.toString();
            ps.println(profileName + "," + profileId + "," +
                       friendsCount + "," + followersCount + "," + favoritesCount + "," + statusesCount + "," +
                       mentionsCount + "," + hashtagsCount + "," + createdAt + "," + website);
        }

        if (p_oOutputStream == null) {
            ps.close();
        }
        return true;
    } // generateCSV()

} // class TwitterReport
