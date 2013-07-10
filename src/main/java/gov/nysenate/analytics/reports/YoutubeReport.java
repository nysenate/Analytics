package gov.nysenate.analytics.reports;

import gov.nysenate.analytics.Utils;
import gov.nysenate.analytics.models.NYSenate;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ProtocolException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.ini4j.Profile.Section;

import au.com.bytecode.opencsv.CSVWriter;

public class YoutubeReport extends CSVReport
{
    public static boolean generateCSV(List<NYSenate> nySenateData, Section params)
    {
        // And the NYSenate's "uncut" youtube channel.
        NYSenate uncutObj = new NYSenate();
        uncutObj.fName = "NYSenateUncut";
        uncutObj.youtubeURL = "http://www.youtube.com/user/nysenateuncut";
        nySenateData.add(uncutObj);

        try {

            String webLine;
            CSVWriter writer = getCSVWriter(params);
            writer.writeNext("Date,Channel,Videos,Lifetime Channel Views,Subscribers,Top Viewed Video,Views to Date".split(","));

            // Get today's date
            Date date = new Date();

            // Format as per req
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            String today = formatter.format(date);

            for (NYSenate temp : nySenateData) {
                String totalVideo = "0";
                String lifetimeViews = "0";
                String subscribers = "0";
                String topVideo = "";
                String topVideoViews = "";

                try {
                    if (temp.youtubeURL == null || !("NYSenate".equalsIgnoreCase(temp.fName) || "NYSenateUncut".equalsIgnoreCase(temp.fName)))

                        continue;

                    System.out.println("  " + temp.youtubeURL);
                    BufferedReader br = Utils.getURLWithQueryStringReader(temp.youtubeURL.concat("/videos?sort=p&flow=list&page=1&view=0"));

                    boolean isStatRead = false;
                    boolean isFeedItemFound = false;
                    StringBuffer topVideoBuffer = new StringBuffer();
                    String tempStatVal = "";
                    while ((webLine = br.readLine()) != null) {
                        // Logic to read data from page

                        // assign subscribers values
                        if (isStatRead && webLine.contains("<span class=\"stat-name\">subscribers")) {
                            subscribers = tempStatVal;
                        }
                        // assign subscribers values
                        if (isStatRead && webLine.contains("video views")) {
                            lifetimeViews = tempStatVal;
                        }
                        // statistical data found read it to process when
                        // description will be fetched in next line
                        if (webLine.contains("<span class=\"stat-value\">")) {
                            isStatRead = true;
                            tempStatVal = webLine.substring(webLine.indexOf("<span class=\"stat-value\">") + 25, webLine.indexOf("</span>")).replaceAll(",", "");
                        }
                        else {
                            isStatRead = false;
                            tempStatVal = "";
                        }

                        // all data found for this URL break now to enhance
                        // performance and avoiding overwriting of TopVideo name
                        if (isFeedItemFound && webLine.contains("</li>")) {
                            break;
                        }

                        // fetch first popular video data
                        if (webLine.contains("<li class=\"feed-item-main\">")) {
                            isFeedItemFound = true;
                        }
                        // backup data inside buffer till it will be processed
                        // after breaking loop
                        if (isFeedItemFound) {
                            topVideoBuffer.append(webLine);
                        }
                    }
                    br.close();

                    // read total video
                    BufferedReader brVideo = Utils.getReader(temp.youtubeURL);
                    while ((webLine = brVideo.readLine()) != null) {
                        // total video check and in it
                        if (webLine.contains("<span class=\"blogger-video-count\">1-10 of ")) {
                            totalVideo = webLine.substring(webLine.indexOf("1-10 of ") + 8, webLine.indexOf("</span>")).replaceAll(",", "");
                        }
                    }
                    brVideo.close();

                    // process top video buffered data
                    String topVideoData = topVideoBuffer.toString();
                    if (isFeedItemFound && topVideoData.contains("feed-video-title")) {
                        topVideo = topVideoData.substring(topVideoData.indexOf("<h4>"), topVideoData.indexOf("</h4>"));
                        topVideo = topVideo.substring(topVideo.indexOf(">", 5) + 1, topVideo.indexOf("</")).trim();
                    }

                    // view-count
                    if (isFeedItemFound && topVideoData.contains("<div class=\"metadata\">")) {
                        int bullIndex = topVideoData.indexOf("<span class=\"bull\">");
                        topVideoViews = topVideoData.substring(bullIndex, topVideoData.indexOf("<span class=\"bull\">", bullIndex + 25)).trim();
                        topVideoViews = topVideoViews.substring(topVideoViews.indexOf("<span class=\"view-count\">") + 29, topVideoViews.lastIndexOf(" views ")
                                );
                        topVideoViews = topVideoViews.trim().replaceAll(",", "");

                    }

                    // today's Date,Channel/first name,Videos,Lifetime
                    // ChannelViews,Subscribers,Top Viewed,Views to Date
                    writer.writeNext(new String[] {
                            today,
                            ((temp.fName != null) ? temp.fName : ""),
                            totalVideo,
                            lifetimeViews,
                            subscribers,
                            topVideo,
                            topVideoViews
                    });
                }
                catch (FileNotFoundException e) {
                    System.out.println("BAD URL [not found]: " + temp.youtubeURL);
                }
                catch (ProtocolException e) {
                    System.out.println("BAD URL [too many redirects]: " + temp.youtubeURL);
                }
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
