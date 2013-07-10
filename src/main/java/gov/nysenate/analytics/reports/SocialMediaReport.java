package gov.nysenate.analytics.reports;

import gov.nysenate.analytics.models.NYSenate;

import java.io.IOException;
import java.util.List;

import org.ini4j.Profile.Section;

import au.com.bytecode.opencsv.CSVWriter;

public class SocialMediaReport extends CSVReport
{
    public static boolean generateCSV(List<NYSenate> nySenateData, Section params)
    {
        try {
            CSVWriter writer = getCSVWriter(params);
            writer.writeNext("Date,First,Last,Twitter,Youtube,Facebook,Flickr".split(","));
            for (NYSenate temp : nySenateData) {
                writer.writeNext(new String[] {
                        params.get("end_date"),
                        ((temp.fName != null) ? temp.fName : ""),
                        ((temp.lName != null) ? temp.lName : ""),
                        ((temp.twitterURL != null) ? temp.twitterURL : ""),
                        ((temp.youtubeURL != null) ? temp.youtubeURL : ""),
                        ((temp.facebookURL != null) ? temp.facebookURL : ""),
                        ((temp.flickrURL != null) ? temp.flickrURL : "")
                });
            }
            writer.close();
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
