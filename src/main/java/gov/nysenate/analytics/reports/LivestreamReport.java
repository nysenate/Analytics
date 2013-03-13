package gov.nysenate.analytics.reports;

import gov.nysenate.analytics.CSVReport;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ProtocolException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;

import org.ini4j.Profile.Section;

import com.livestream.api.generated.Channel;
import com.livestream.api.generated.Rss;

public class LivestreamReport extends CSVReport
{
    public static final String apiUrlStart = "http://x";
    public static final String apiUrlEnd = "x.channel-api.livestream-api.com/2.0/info";

    public static boolean generateCSV(Section params)
    {
        try {
            JAXBContext jc = JAXBContext.newInstance("com.livestream.api.generated");

            // Get the file started with a simple header

            BufferedWriter bw = getOutputWriter(params);
            bw.write("Date,Channel,Minutes this Month,Total Minutes");
            bw.newLine();

            int monthTotal = 0, allTimeTotal = 0;
            for (String channelName : params.get("channels").split(",")) {
                try {
                    URL url = new URL(apiUrlStart + channelName + apiUrlEnd);
                    System.out.println(url);

                    Channel channel = ((Rss) jc.createUnmarshaller().unmarshal(url)).getChannel();
                    monthTotal += channel.getViewerMinutesThisMonth();
                    allTimeTotal += channel.getTotalViewerMinutes();

                    bw.write(
                            params.get("end_date") + ","
                                    + channel.getTitle().replaceAll(",", " ") + ","
                                    + channel.getViewerMinutesThisMonth() + ","
                                    + channel.getTotalViewerMinutes()
                            );
                    bw.newLine();
                }
                catch (FileNotFoundException e) {
                    System.out.println("BAD URL [not found]: " + apiUrlStart + channelName + apiUrlEnd);
                }
                catch (ProtocolException e) {
                    System.out.println("BAD URL [too many redirects]: " + apiUrlStart + channelName + apiUrlEnd);
                }
                catch (UnmarshalException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
            bw.write("\n,Total," + monthTotal + "," + allTimeTotal);
            bw.close();
            return true;
        }
        catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
}
