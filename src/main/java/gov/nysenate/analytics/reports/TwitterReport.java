package gov.nysenate.analytics.reports;

import gov.nysenate.analytics.models.NYSenate;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.ini4j.Profile.Section;

import winterwell.jtwitter.OAuthSignpostClient;
import winterwell.jtwitter.Status;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;
import winterwell.jtwitter.User;
import au.com.bytecode.opencsv.CSVWriter;

public class TwitterReport extends CSVReport
{
    public static boolean generateCSV(List<NYSenate> nySenateData, Section params)
    {
        try {
            Twitter twitter = new Twitter(
                    "SenateAnalytics",
                    new OAuthSignpostClient(
                            params.get("consumer_key"),
                            params.get("consumer_secret"),
                            params.get("access_key"),
                            params.get("access_secret")
                    )
                    );

            CSVWriter writer = getCSVWriter(params);
            writer.writeNext("profileName,id,friends,followers,favorites,statuses,mentions,hashtags,created,website".split(","));

            for (NYSenate senator : nySenateData) {
                try {
                    if (senator.twitterURL != null) {
                        String[] urlParts = senator.twitterURL.split("/");
                        String profileName = urlParts[urlParts.length - 1];
                        System.out.println("  Processing: " + profileName);
                        User twitterUser = twitter.users().getUser(profileName);
                        List<Status> mentions = twitter.search("@" + profileName + " since:" + params.get("start_date"));
                        List<Status> hashtags = twitter.search("#" + profileName + " since:" + params.get("start_date"));

                        // This can sometimes be null
                        URI website = twitterUser.getWebsite();

                        writer.writeNext(new String[] {
                                profileName,
                                String.valueOf(twitterUser.getId()),
                                String.valueOf(twitterUser.getFriendsCount()),
                                String.valueOf(twitterUser.getFollowersCount()),
                                String.valueOf(twitterUser.getFavoritesCount()),
                                String.valueOf(twitterUser.getStatusesCount()),
                                String.valueOf(mentions.size()),
                                String.valueOf(hashtags.size()),
                                twitterUser.getCreatedAt().toString(),
                                ((website == null) ? "" : website.toString())
                        });
                    }
                }
                catch (TwitterException e) {
                    System.out.println("  Twitter Error: " + e.getMessage());
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
} // class TwitterReport
