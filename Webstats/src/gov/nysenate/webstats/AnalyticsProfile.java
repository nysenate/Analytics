package gov.nysenate.webstats;

import com.google.gdata.client.analytics.AnalyticsService;

public class AnalyticsProfile
{
    private AnalyticsService m_oAnalyticsService;
    private String m_strProfileId;
    private String m_strProfileName;
    
    
    public AnalyticsProfile(AnalyticsService p_oAnalyticsService, String p_strProfileId, String p_strProfileName)
    {
        m_oAnalyticsService = p_oAnalyticsService;
        m_strProfileId = p_strProfileId;
        m_strProfileName = p_strProfileName;
    } // AnalyticsProfile()
    
    
    
    public AnalyticsService getAnalyticsService()
    {
        return m_oAnalyticsService;
    } // getAnalyticsService()
    
    
    
    public String getProfileId()
    {
        return m_strProfileId;
    } // getProfileId()
    
    
    
    public String getProfileName()
    {
        return m_strProfileName;
    } // getProfileName()
} // class AnalyticsProfile
