package gov.nysenate.analytics.reports;

import gov.nysenate.analytics.structures.NYSenate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.ini4j.Profile.Section;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;


public class TwitterReport
{
    public static boolean generateCSV(List<NYSenate> nySenateData, Section params) {
    	try {
	    	Twitter twitter = new Twitter();
	    	BufferedWriter bw = new BufferedWriter(new FileWriter(new File(params.get("output_file"))));
	        bw.write("profileName,id,friends,followers,favorites,statuses,mentions,hashtags,created,website");
	        bw.newLine();
	        
	        for (NYSenate senator : nySenateData) {
	        	try {
	        		if(senator.twitterURL != null) {
		        		String[] urlParts = senator.twitterURL.split("/");
		        		String profileName = urlParts[urlParts.length-1];
			        	System.out.println("  Processing: "+profileName);
			            Twitter.User twitterUser = twitter.show(profileName);
			            List<Twitter.Status> mentions = twitter.search("@"+profileName+" since:"+params.get("start_date"));
			            List<Twitter.Status> hashtags = twitter.search("#"+profileName+" since:"+params.get("start_date"));
			            
			            //This can sometimes be null
			            URI website = twitterUser.getWebsite();
			            
			            bw.write(
		        			profileName + "," +
		            		twitterUser.getId() + "," +
		            		twitterUser.getFriendsCount() + "," +
		            		twitterUser.getFollowersCount() + "," +
		            		twitterUser.getFavoritesCount() + "," +
		            		twitterUser.getStatusesCount() + "," +
		                    mentions.size() + "," +
		            		hashtags.size() + "," +
		                    twitterUser.getCreatedAt() + "," +
		            		((website == null) ? "" : website.toString())
		        		);
			            bw.newLine();
	        		}
	        	} catch( TwitterException e) {
	        		System.out.println("  Twitter Error: "+e.getMessage());
	        	}
	        }
	        bw.close();
	        return true;
	    } catch( IOException e ) {
	    	e.printStackTrace();
	    }
    	return false;
    }

} // class TwitterReport
