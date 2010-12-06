package gov.nysenate.webstats;

public class QueryParams
{
    private String m_strStartDate;
    private String m_strEndDate;
    
    
    public QueryParams(String p_strStartDate, String p_strEndDate)
    {
        m_strStartDate = Utils.isEmpty(p_strStartDate) ? null : Utils.convertDate(p_strStartDate);
        m_strEndDate = Utils.isEmpty(p_strEndDate) ? null : Utils.convertDate(p_strEndDate);
    } // QueryParams()

    
    
    public String getStartDate()
    {
        return m_strStartDate;
    } // getStartDate()
    
    
    
    public String getEndDate()
    {
        return m_strEndDate;
    } // getEndDate();
    
    
    
    public void setDates(String p_strStartDate, String p_strEndDate)
    {
        m_strStartDate = p_strStartDate;
        m_strEndDate = p_strEndDate;
    } // setDates()
    
    
    
    public void setStartDate(String p_strStartDate)
    {
        m_strStartDate = p_strStartDate;
    } // setStartDate()
    
    
    
    public void setEndDate(String p_strEndDate)
    {
        m_strEndDate = p_strEndDate;
    } // setEndDate()
    
} // class QueryParams
