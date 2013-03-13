package gov.nysenate.analytics.reports;

import gov.nysenate.analytics.models.NYSenate;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.List;

import org.ini4j.Profile.Section;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.types.Page;
import com.restfb.types.User;

/**
 * 
 * @author aaakulkarni
 * 
 */
public class FacebookReport
{
    /**
     * 
     * @param nySenateData
     * @param params
     * @return
     */
    public static boolean generateCSV(List<NYSenate> nySenateData, Section params)
    {
        try {
            DefaultFacebookClient client = new DefaultFacebookClient();
            AccessToken accessToken = client.obtainAppAccessToken("471573169552684", "a2dc234abf47c4ce930a4d3cb5d2b017");

            FacebookClient facebookClient = new DefaultFacebookClient(accessToken.getAccessToken());
            BufferedWriter bw = new BufferedWriter(new FileWriter(params.get("output_file")));
            bw.write("Date,First,Last,URL,Fans\n");
            for (NYSenate temp : nySenateData) {
                try {
                    if (temp.facebookURL == null)
                        continue;

                    String pName = "";
                    String partialFBURL = temp.facebookURL.substring(temp.facebookURL.indexOf(".com/"));
                    String[] facebookURLArray = partialFBURL.split("/");

                    if (facebookURLArray.length == 4) {
                        pName = facebookURLArray[3];
                    }
                    else if (facebookURLArray.length == 2) {
                        pName = facebookURLArray[1];
                    }
                    if (pName.indexOf("?") > 0)
                        pName = pName.substring(0, pName.indexOf("?"));

                    System.out.println("accountlocation:: facebook.com/" + pName);
                    Page page = null;
                    String count = "Non-public";
                    try {
                        System.out.println("read as page:: ");
                        page = facebookClient.fetchObject(pName, Page.class);
                        if (null != page) {
                            count = page.getLikes().toString();
                        }
                    }
                    catch (Exception e) {
                        count = "nonPublicPage";
                    }

                    try {
                        if ("Non-public".equalsIgnoreCase(count) || "nonPublicPage".equalsIgnoreCase(count)) {
                            System.out.println("read as User:: ");
                            User user = facebookClient.fetchObject(pName, User.class);
                            if (user != null && user.getMetadata() != null && null != user.getMetadata().getConnections() && null != user.getMetadata().getConnections().getFriends()) {
                                count = user.getMetadata().getConnections().getFriends();
                            }
                            else {
                                Connection<User> myFriends = null;
                                try {
                                    myFriends = facebookClient.fetchConnection(pName.concat("/friends"), User.class);
                                }
                                catch (Exception e) {
                                }

                                if (null != myFriends) {
                                    count = Integer.toString(myFriends.getData().size());
                                }
                                else {
                                    System.out.println(user);
                                    count = "Non-public";
                                }
                            }
                        }

                    }
                    catch (Exception e) {
                        count = "Not-exist";
                    }

                    System.out.println("final count " + count);
                    bw.write(
                            params.get("end_date") + ","
                                    + ((temp.fName != null) ? temp.fName : "") + ","
                                    + ((temp.lName != null) ? temp.lName : "") + ","
                                    + ((temp.facebookURL != null) ? temp.facebookURL : "") + ","
                                    + count
                            );
                    bw.newLine();

                }
                catch (FileNotFoundException e) {
                    System.out.println("BAD URL [not found]: " + temp.facebookURL);
                }
                catch (ProtocolException e) {
                    System.out.println("BAD URL [too many redirects]: " + temp.facebookURL);
                }
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
