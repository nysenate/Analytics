package gov.nysenate.analytics.reports;

import java.io.IOException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.ini4j.Profile.Section;

import au.com.bytecode.opencsv.CSVWriter;

import com.livestream.api.generated.Channel;
import com.livestream.api.generated.Rss;

/**
 * Handles reports with `report_type=livestream` and writes them to the `output_file` as
 * a CSV file in the following format:
 * 
 * > Date,Channel,Viewer Minutes this Month,Total Viewer Minutes,URL
 * > ...
 * > ...
 * > ,Total,#month,#alltime
 * 
 * Channels to report are stored as a comma separated list in the `channels` parameter.
 * 
 * @author GraylinKim
 * 
 */
public class LivestreamReport extends CSVReport
{
    public static final String apiUrlStart = "http://x";
    public static final String apiUrlEnd = "x.api.channel.livestream.com/2.0/info.xml";

    public static void generateCSV(Section params) throws IOException
    {
        int monthTotal = 0, allTimeTotal = 0;
        CSVWriter writer = getCSVWriter(params);
        writer.writeNext("Date,Channel,Viewer Minutes this Month,Total Viewer Minutes,URL".split(","));
        try {
            Unmarshaller jcm = JAXBContext.newInstance("com.livestream.api.generated").createUnmarshaller();
            for (String channelName : params.get("channels").split(",")) {
                try {
                    URL url = new URL(apiUrlStart + channelName + apiUrlEnd);
                    System.out.println(url);

                    Channel channel = ((Rss) jcm.unmarshal(url)).getChannel();
                    monthTotal += channel.getViewerMinutesThisMonth();
                    allTimeTotal += channel.getTotalViewerMinutes();
                    writer.writeNext(new String[] {
                            params.get("end_date"),
                            channel.getTitle().replaceAll(",", " "),
                            String.valueOf(channel.getViewerMinutesThisMonth()),
                            String.valueOf(channel.getTotalViewerMinutes()),
                            channel.getLink()
                    });
                }
                catch (IOException | JAXBException e) {
                    System.err.println("Unable to retrieve data for " + channelName + " :" + e.getMessage());
                    e.printStackTrace(System.err);
                }
            }
        }
        catch (JAXBException e) {
            System.err.println("Unable to create JAXB context: " + e.getMessage());
            e.printStackTrace(System.err);
        }
        finally {
            writer.writeNext(new String[] {});
            writer.writeNext((",Total," + monthTotal + "," + allTimeTotal).split(","));
            writer.close();
        }
    }
}
