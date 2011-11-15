package analytics.social;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Constant.Constants;


public class NYSenateAnalytics implements Constant.Constants  {
	private static final String SENATE_DATA = "http://www.nysenate.gov/senators";
	private static final String SOCIAL_F = "scratch/social.csv";
	private static final String FACEBOOK_F = "scratch/facebook.csv";
	
	public NYSenateAnalytics() throws IOException {
		List<NYSenateObject> senObjLst = SenatorData();
		
		createSocialCSV(senObjLst, Constants.DATE);
		createFacebookCSV(senObjLst, Constants.DATE);
	}
	
	public void createFacebookCSV(List<NYSenateObject> senObjLst, String date) throws IOException  {
		
		NYSenateObject tObj = new NYSenateObject();
		tObj.setFName("NYSenate");
		tObj.setFacebookURL("http://www.facebook.com/NYsenate");
		senObjLst.add(tObj);

		new File(FACEBOOK_F).delete();
		BufferedWriter br = null;
		try {
			br = new BufferedWriter(new FileWriter(FACEBOOK_F));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		br.write("Date,First,Last,URL,Fans\n");
		for(NYSenateObject temp:senObjLst) {
			try {
				if(temp.getFacebookURL() != null) {
					System.out.println(temp.getFacebookURL());
					temp.setFriends(getFriends(temp.getFacebookURL()));
					if(temp.getFriends() != null) {
						System.out.println(temp.getFriends());
						br.write(date + "," + temp.toFacebookString()+"\n");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		br.close();
	}
	
	public void createSocialCSV(List<NYSenateObject> senObjLst, String date) throws IOException  {
		new File(SOCIAL_F).delete();
		BufferedWriter br = null;
		try {
			br = new BufferedWriter(new FileWriter(SOCIAL_F));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		br.write("Date,First,Last,Twitter,Youtube,Facebook,Flickr\n");
		for(NYSenateObject temp:senObjLst) {
			br.write(date + "," + temp.toAnalyticsString()+"\n");
		}
		br.close();
	}
	
	//creates a new BufferedReader for a given url
	private BufferedReader getReader(String url) throws MalformedURLException, IOException {
		return new BufferedReader(new InputStreamReader(new URL(url).openStream()));
	}
	
	/*
	 * File structure: "(lastname),(firstname + middle),(generational title),(facebookURL)" 
	 */
	public String getFriends(String facebookUrl) throws Exception {
		if(facebookUrl != null) { //if Senator has a facebook URL
			BufferedReader br = getReader(facebookUrl);
			String webLine; //string being read from url
			Boolean toggle = false; //true if match has been found
			while((webLine = br.readLine()) != null && !toggle) {
				//237\u003c\/span>\u003c\/div>people like this
				//1,598</span></div>people like this
				Pattern likePattern = Pattern.compile("((\\d)+(,?(\\d)+)?)((</span>)?</div>| ) ?like this");
				Matcher likeM = likePattern.matcher(webLine);
				
				String matchString = "";

				if(likeM.find()) {
					matchString = new String(likeM.group(1));
					toggle = true;
				}
				
				if(toggle) { //if match has been found assemble CSV
					return matchString.replaceAll(",","");
				}
			}
			if(!toggle) { //if no match was found assemble CSV
				return null;
			}
			br.close();
		}
		else { //if no facebookURL is available assemble CSV
			return null;
		}
		return null;
	}
	public List<NYSenateObject> SenatorData() throws IOException {
		List<NYSenateObject> ret = new ArrayList<NYSenateObject>();
		BufferedReader br = getReader(SENATE_DATA);
		Pattern senPattern = Pattern.compile("/senator/([\\w%\\d]+[-]?)*/contact");
		Pattern namePattern = Pattern.compile(">(([\\w]+[-.,]?{0,2}(\\s)?)+)");
		Pattern facebookPattern = Pattern.compile("http://www.(new.)?facebook.com");
		Pattern flickrPattern = Pattern.compile("http://(www.)?flickr.com");
		Pattern twitterPattern = Pattern.compile("http://(www.)?twitter.com");
		Pattern youtubePattern = Pattern.compile("http://(www.)?youtube.com");
		
		String in;
		NYSenateObject tSObj = null;
		while((in = br.readLine())!=null) {	
			Matcher senM = senPattern.matcher(in);
			Matcher nameM = namePattern.matcher(in);
			Matcher facebookM = facebookPattern.matcher(in);
			Matcher flickrM = flickrPattern.matcher(in);
			Matcher twitterM = twitterPattern.matcher(in);
			Matcher youtubeM = youtubePattern.matcher(in);
			String s;
			if(senM.find()) {
				if(nameM.find()) {
					if(tSObj == null) {
						tSObj = new NYSenateObject();
					}
					else {
						
					}
					s = in.substring(nameM.start()+1);
					s = s.split("<")[0];
					String strings[] = s.split(",");
					
					strings[1] = strings[1].trim();
					strings[0] = strings[0].trim();
					
					tSObj.setFName(strings[1]);
					tSObj.setLName(strings[0]);
				}
				
			}
			if(in.contains("social_buttons")) {
				if(facebookM.find()) {
					s = in.substring(facebookM.start());
					s = s.split("\"")[0];
					tSObj.setFacebookURL(s);
				}
				if(flickrM.find()) {
					s = in.substring(flickrM.start());
					s = s.split("\"")[0];
					tSObj.setFlickrURL(s);

				}
				if(twitterM.find()) {
					s = in.substring(twitterM.start());
					s = s.split("\"")[0];
					tSObj.setTwitterURL(s);

				}
				if(youtubeM.find()) {
					s = in.substring(youtubeM.start());
					s = s.split("\"")[0];
					tSObj.setYoutubeURL(s);
				}
				ret.add(tSObj);
				tSObj = null;
				tSObj = new NYSenateObject();			
			}
			
		}
		return ret;
	}
}
