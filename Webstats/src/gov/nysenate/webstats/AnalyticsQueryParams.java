package gov.nysenate.webstats;

public class AnalyticsQueryParams extends QueryParams
{
    private String m_strDimensions;
    private String m_strMetrics;
    private String m_strSort;
    private String m_strFilters;
    
    
    public AnalyticsQueryParams(String p_strStartDate, String p_strEndDate,
                                String p_strDimensions, String p_strMetrics,
                                String p_strSort, String p_strFilters)
    {
        super(p_strStartDate, p_strEndDate);
        m_strDimensions = Utils.isEmpty(p_strDimensions) ? null : p_strDimensions;
        m_strMetrics = Utils.isEmpty(p_strMetrics) ? null : p_strMetrics;
        m_strSort = Utils.isEmpty(p_strSort) ? null : p_strSort;
        m_strFilters = Utils.isEmpty(p_strFilters) ? null : p_strFilters;
    } // AnalyticsQueryParams()
    
    
    
    public AnalyticsQueryParams(String p_strStartDate, String p_strEndDate,
                                String p_strDimensions, String p_strMetrics)
    {
        this(p_strStartDate, p_strEndDate, p_strDimensions, p_strMetrics, null, null);
    } // AnalyticsQueryParams()
    
    
    
    public AnalyticsQueryParams(String p_strStartDate, String p_strEndDate)
    {
        this(p_strStartDate, p_strEndDate, null, null, null, null);
    } // AnalyticsQueryParams()
    
    
    
    public String getDimensions()
    {
        return m_strDimensions;
    } // getDimensions()
    
    
    
    public String getMetrics()
    {
        return m_strMetrics;
    } // getMetrics()
    
    
    
    public String getSort()
    {
        return m_strSort;
    } // getSort()
    
    
    
    public String getFilters()
    {
        return m_strFilters;
    } // getFilters()    
    
} // class AnalyticsQueryParams
