package gov.nysenate.analytics;

import gov.nysenate.analytics.structures.NYSenate;
import gov.nysenate.analytics.structures.Source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.gdata.data.analytics.DataEntry;


public class Utils
{
	
	// Use some reflection to sum up the given attribute. 
	// I still can't believe there are no java built-ins for this....
	public static int getTotal(Collection<?> list, Class<?> ObjectClass, String fieldname) {
		try {
			int i = 0;
			Field field = ObjectClass.getField(fieldname);
			for(Object so:list)
				i+=field.getInt(so);
			return i;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	//creates a new BufferedReader for a given url
	public static BufferedReader getReader(String url) throws MalformedURLException, IOException {
		return new BufferedReader(new InputStreamReader(new URL(url).openStream()));
	}

	public static List<NYSenate> SenatorData() throws IOException, InterruptedException {
		List<NYSenate> ret = new ArrayList<NYSenate>();
		BufferedReader br = Utils.getReader("http://www.nysenate.gov/senators");
		Pattern senPattern = Pattern.compile("<a href=\\\"(/senator/[\\w\\-]+/?|http://www.kemphannon.com)\\\">([^<>]+)</a>");
		Pattern facebookPattern = Pattern.compile("https?://(www.)?(new.)?facebook.com");
		Pattern flickrPattern = Pattern.compile("https?://(www.)?flickr.com");
		Pattern twitterPattern = Pattern.compile("https?://(www.)?twitter.com");
		Pattern youtubePattern = Pattern.compile("https?://(www.)?youtube.com");
		
		String in, s;
		NYSenate tSObj = null;
		while((in = br.readLine())!=null) {	
			Matcher senM = senPattern.matcher(in);
			Matcher facebookM = facebookPattern.matcher(in);
			Matcher flickrM = flickrPattern.matcher(in);
			Matcher twitterM = twitterPattern.matcher(in);
			Matcher youtubeM = youtubePattern.matcher(in);
			
			if(senM.find()) {
				if(tSObj == null) {
					tSObj = new NYSenate();
					tSObj.nysenateURL = senM.group(1);
				}
				String strings[] = senM.group(2).split(",");
				
				strings[1] = strings[1].trim();
				strings[0] = strings[0].trim();
				
				tSObj.fName = strings[1];
				tSObj.lName = strings[0];
			}
			if(in.contains("social_buttons") && tSObj != null) {
				if(facebookM.find()) {
					s = in.substring(facebookM.start());
					s = s.split("\"")[0];
					tSObj.facebookURL = s;
				}
				if(flickrM.find()) {
					s = in.substring(flickrM.start());
					s = s.split("\"")[0];
					tSObj.flickrURL = s;

				}
				if(twitterM.find()) {
					s = in.substring(twitterM.start());
					s = s.split("\"")[0];
					tSObj.twitterURL = s;

				}
				if(youtubeM.find()) {
					s = in.substring(youtubeM.start());
					s = s.split("\"")[0];
					tSObj.youtubeURL = s;
				}
				ret.add(fixForNYSenateGov(tSObj));
				tSObj = null;
			}
			
		}
		return ret;
	}
    
	public static NYSenate fixForNYSenateGov(NYSenate senator) {
		if (senator.lName.equals("Alesi")) {
			senator.twitterURL = "http://twitter.com/senatoralesi";
			
		} else if (senator.lName.equals("Bonacic")) {
			senator.twitterURL = "http://twitter.com/johnbonacic";
			senator.facebookURL = "http://www.facebook.com/JohnBonacic";
			
		} else if (senator.lName.equals("Fuschillo")) {
			senator.twitterURL = "http://twitter.com/SenFuschillo";
			senator.facebookURL = "http://www.facebook.com/senatorfuschillo";
			
		} else if (senator.lName.equals("Hannon")) {
			senator.nysenateURL = "http://nysenate.gov/senator/kemp-hannon";
			
		} else if (senator.lName.equals("Lanza")) {
			senator.twitterURL = "http://twitter.com/senatorlanza";
			
		} else if (senator.lName.equals("Oppenheimer")) {
			senator.twitterURL = "http://twitter.com/SenatorSuzi";
			
		} else if (senator.lName.equals("Rivera")) {
			senator.twitterURL = "http://twitter.com/NYSenatorRivera";
			
		} else if (senator.lName.equals("Savino")) {
			senator.twitterURL = "http://twitter.com/dianesavino";
			
		} else if (senator.lName.equals("Valesky")) {
			senator.twitterURL = "http://twitter.com/SenDavidValesky";
			
		} else if (senator.lName.equals("Young")) {
			senator.twitterURL = "http://twitter.com/SenatorYoung";
		}
		return senator;
	}
	
	public static List<Source> combineDataFeedBySource(List<DataEntry> entries, String pathMatch) {
		Map<String,Source> map = new HashMap<String,Source>();
		for(DataEntry de:entries) {
			String path = de.stringValueOf("ga:pagePath");			
			
			if(pathMatch == null || path.contains(pathMatch)) { 
				
				String s = ((pathMatch == null) ? path : de.stringValueOf("ga:source"));
				int view = new Integer(de.stringValueOf("ga:pageviews"));
				int b = new Integer(de.stringValueOf("ga:bounces"));
				double t = new Double(de.stringValueOf("ga:timeOnPage"));			
				
				if(map.containsKey(s)) {
					Source so = map.get(s);
					so.pageviews += view;
					so.bounces += b;
					so.time += t;
					map.remove(s);
					map.put(s, so);
				}
				else {
					Source so = new Source(s, view, b, t);				
					map.put(so.source, so);
				}
			}					
		}
		return Lists.newArrayList(map.values());
	}

	public static List<Source> groupOthers(List<Source> lst, int count) {
		List<Source> ret = new ArrayList<Source>();

		int i = 0, bounces = 0, pageviews = 0;
		double time = 0;
		for(Source so:lst) {
			if(i < count) {
				ret.add(so);				
				i++;
			}
			else {
				bounces += so.bounces;
				pageviews += so.pageviews;
				time += so.time;
			}			
		}
		ret.add(new Source("other",pageviews,bounces,time));
		
		return ret;
	}
	
} // class Utils
