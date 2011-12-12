package gov.nysenate.analytics.reports;

import gov.nysenate.analytics.Utils;
import gov.nysenate.analytics.structures.NYSenate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ini4j.Profile.Section;

public class FacebookReport {
	
	public static boolean generateCSV(List<NYSenate> nySenateData, Section params) {
		try {
			String webLine;
			Pattern likePattern = Pattern.compile("([0-9,]+)\\\\u003c\\\\/span>\\\\u003c\\\\/div> *like this");
			BufferedWriter bw = new BufferedWriter(new FileWriter(params.get("output_file")));
			bw.write("Date,First,Last,URL,Fans\n");
			for(NYSenate temp:nySenateData) {
				try {
					if(temp.facebookURL == null) continue;

					System.out.println("  "+temp.facebookURL);
					BufferedReader br = Utils.getReader(temp.facebookURL);
					while((webLine = br.readLine()) != null) {
						Matcher likeM = likePattern.matcher(webLine);
						if(likeM.find()) {
							System.out.println("  [Match Found] "+likeM.group(1).replaceAll(",",""));
							temp.friends = likeM.group(1).replaceAll(",", "");
						}
					}
					br.close();

					bw.write(
							params.get("end_date") + ","
					     + ((temp.fName != null) ? temp.fName : "") + ","
						 + ((temp.lName != null) ? temp.lName : "") + ","
						 + ((temp.facebookURL != null) ? temp.facebookURL : "") + ","
						 + ((temp.friends != null) ? temp.friends : "Non-public")
					 );
					bw.newLine();

				} catch (FileNotFoundException e) {
					System.out.println("BAD URL [not found]: "+temp.facebookURL);
				} catch (ProtocolException e) {
					System.out.println("BAD URL [too many redirects]: "+temp.facebookURL);
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
