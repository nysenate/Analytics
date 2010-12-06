package gov.nysenate.webstats;

import java.io.PrintStream;



public interface Report
{
    public boolean generateCSV();
    
    public boolean generateCSV(PrintStream p_oPrintStream);
} // interface Report
