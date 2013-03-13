package gov.nysenate.analytics.reports;

import gov.nysenate.analytics.models.NYSenate;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import org.ini4j.Profile.Section;

public class SocialMediaReport extends CSVReport
{
    public static boolean generateCSV(List<NYSenate> nySenateData, Section params)
    {
        try {
            BufferedWriter bw = getOutputWriter(params);
            bw.write("Date,First,Last,Twitter,Youtube,Facebook,Flickr\n");
            for (NYSenate temp : nySenateData) {
                bw.write(params.get("end_date") + "," + temp.toAnalyticsString() + "\n");
            }
            bw.close();
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
