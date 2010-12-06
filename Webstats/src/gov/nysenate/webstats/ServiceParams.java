package gov.nysenate.webstats;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class ServiceParams
{
    private static final String STDOUT_STREAM_TAG = "<stdout>";
    private String m_strServiceName;
    private String m_strUsername;
    private String m_strPassword;
    private String m_strOutfileName;
    private PrintStream m_oPrintStream;
    
    
    public ServiceParams(String p_strServiceName, String p_strUsername, String p_strPassword, String p_strOutfileName)
           throws FileNotFoundException
    {
        assert !(Utils.isEmpty(p_strServiceName) || Utils.isEmpty(p_strUsername) || Utils.isEmpty(p_strPassword));
        m_strServiceName = p_strServiceName;
        m_strUsername = p_strUsername;
        m_strPassword = p_strPassword;
        m_strOutfileName = Utils.isEmpty(p_strOutfileName) ? null : p_strOutfileName;
        
        if (m_strOutfileName == null) {
            m_oPrintStream = null;
        }
        else if (m_strOutfileName.equals(STDOUT_STREAM_TAG)) {
            m_oPrintStream = System.out;
        }
        else {
            m_oPrintStream = Utils.createOutputFile(m_strOutfileName);
        }
    } // ServiceParams()

    
    
    public String getServiceName()
    {
        return m_strServiceName;
    } // getServiceName()
    
    
    
    public String getUsername()
    {
        return m_strUsername;
    } // getUsername();

    
    
    public String getPassword()
    {
        return m_strPassword;
    } // getPassword()
    
    
    
    public String getOutfileName()
    {
        return m_strOutfileName;
    } // getOutfileName()
    
    
    
    public PrintStream getPrintStream()
    {
        return m_oPrintStream;
    } // getPrintStream()
    
    
    public void closePrintStream()
    {
        if (m_oPrintStream != null) {
            m_oPrintStream.close();
            m_oPrintStream = null;
        }
    } // closePrintStream()
    
} // class ServiceParams
