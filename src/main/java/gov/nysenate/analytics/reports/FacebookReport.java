package gov.nysenate.analytics.reports;

import gov.nysenate.analytics.models.NYSenate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ini4j.Profile.Section;

import au.com.bytecode.opencsv.CSVWriter;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.types.Page;
import com.restfb.types.User;

/**
 * Handles reports with `report_type=facebook` and writes them to the `output_file` as
 * a CSV file in the following format:
 * 
 * > Date,First Name,Last Name,URL,Fans
 * > ...
 * > ...
 * 
 * Uses the Facebook API authenticated with the `app_key` and the `app_secret` values from
 * the configuration file to retrieve the data.
 * 
 * @author aaakulkarni, GraylinKim
 * 
 */
public class FacebookReport extends CSVReport
{
    public static Pattern userPattern = Pattern.compile("(?:https?://)?(?:www\\.)?facebook.com/.*?([-._\\w]+)(\\?.*)?$", Pattern.CASE_INSENSITIVE);

    public static void generateCSV(List<NYSenate> nySenateData, Section params) throws IOException
    {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        CSVWriter writer = getCSVWriter(params);
        writer.writeNext("Date,First Name,Last Name,Account Type,Fans,URL,".split(","));

        DefaultFacebookClient client = new DefaultFacebookClient();
        AccessToken accessToken = client.obtainAppAccessToken("471573169552684", "a2dc234abf47c4ce930a4d3cb5d2b017");
        FacebookClient facebookClient = new DefaultFacebookClient(accessToken.getAccessToken());

        for (NYSenate senateObject : nySenateData) {
            String url = senateObject.facebookURL;
            if (url == null || url.isEmpty())
                continue;

            Exception exception = null;
            String facebook_id = "";
            String account_type = "unknown";
            String popularity = "unavailable";
            Matcher userMatcher = userPattern.matcher(url);

            if (!userMatcher.find()) {
                System.err.println("  BAD URL: " + url);
            }
            else {
                facebook_id = userMatcher.group(1);
                System.out.println("  Fetching: facebook.com/" + facebook_id);

                try {
                    // See if we have a page url. It is important not to confirm account type until
                    // after verifying that we have access to likes. For some reason facebook will
                    // return a bastardized Page object even when requesting users.
                    Page page = facebookClient.fetchObject(facebook_id, Page.class);
                    if (page != null) {
                        popularity = page.getLikes().toString();
                        account_type = "page";
                    }
                }
                catch (Exception e) {
                    exception = e;
                }

                if (!account_type.equals("page")) {
                    try {
                        // If it turned out the url wasn't to a page it must be a person. This is
                        // where we start running into issues with the Senator's privacy settings.
                        // Because the app doesn't make requests on behalf of a Facebook user it
                        // treats us as if we are logged out of their web app. Many senator user
                        // accounts are open only to Facebook members and not to the public.
                        User user = facebookClient.fetchObject(facebook_id, User.class);
                        if (user != null) {
                            account_type = "user";
                            if (user.getMetadata() != null && user.getMetadata().getConnections() != null) {
                                popularity = user.getMetadata().getConnections().getFriends();
                            }
                        }
                    }
                    catch (Exception e) {
                        exception = e;
                    }
                }
            }

            if (account_type.equalsIgnoreCase("unknown")) {
                // If the user hasn't been identified with either account type then we're not
                // sure what is going on. Either the account doesn't actually exist or the privacy
                // settings are turned up so high that we can't tell via public access.
                System.out.println("    Error: unable to fetch data for - " + url);
                if (exception != null) {
                    exception.printStackTrace(System.out);
                }
            }
            else if (popularity.isEmpty()) {
                // If we have an account type but no popularity measure then it means that the
                // privacy settings have hidden it from us
                System.out.println("    Non-public user - " + url);
                popularity = "nonpublic";
            }

            writer.writeNext(new String[] {
                    currentDate,
                    ((senateObject.fName != null) ? senateObject.fName : ""),
                    ((senateObject.lName != null) ? senateObject.lName : ""),
                    account_type,
                    popularity,
                    ((senateObject.facebookURL != null) ? senateObject.facebookURL : "")
            });
        }
        writer.close();
    }
}
