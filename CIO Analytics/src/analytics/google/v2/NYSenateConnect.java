package analytics.google.v2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NYSenateConnect {
	
	private static final String SENATE_DATA = "http://www.nysenate.gov/senators";
	
	
	private static BufferedReader getReader(String url) throws MalformedURLException, IOException {
		return new BufferedReader(new InputStreamReader(new URL(url).openStream()));
	}
	
	public static List<SenatorObject> SenatorData() throws IOException {
		List<SenatorObject> ret = new ArrayList<SenatorObject>();
		BufferedReader br = getReader(SENATE_DATA);
		Pattern senPattern = Pattern.compile("/senator/([\\w%\\d]+[-]?)*");
		Pattern namePattern = Pattern.compile(">(([\\w]+[-.,]?{0,2}(\\s)?)+)");
		String in;
		SenatorObject tAo = new SenatorObject();
		while((in = br.readLine())!=null) {	
			Matcher senM = senPattern.matcher(in);
			Matcher nameM = namePattern.matcher(in);
			if(senM.find()) {
				if(in.contains("image")) {
					tAo.setUrlPath(in.substring(senM.start(), senM.end()));
				}
				if(nameM.find()) {
					String s = in.substring(nameM.start()+1);
					s = s.split("<")[0];
					String strings[] = s.split(",");
					if(strings.length > 1) {
						if(tAo.getUrlPath() == null) {
							tAo.setFName(((strings[1].charAt(0) == ' ') ? strings[1].replaceFirst(" ", "") : strings[1]));
							tAo.setLName(strings[0]);
							tAo.setUrlPath(in.substring(senM.start()).replaceAll("/contact.*", ""));
						}
						else {
							tAo.setFName(((strings[1].charAt(0) == ' ') ? strings[1].replaceFirst(" ", "") : strings[1]));
							tAo.setLName(strings[0]);
						}
						ret.add(tAo);
						//clears any current data out before next iteration
						tAo = new SenatorObject();

					}
				}
			}
		}
		return ret;
	}
}
