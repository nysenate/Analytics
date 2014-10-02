package gov.nysenate.analytics;

import gov.nysenate.analytics.models.NYSenate;
import gov.nysenate.analytics.models.Source;
import gov.nysenate.analytics.reports.ExcelReport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import com.google.gdata.data.analytics.DataEntry;


public class Utils
{
  // Use some reflection to sum up the given attribute.
  // I still can't believe there are no java built-ins for this....
  public static int getTotal(Collection<?> list, Class<?> ObjectClass, String fieldname)
  {
    try {
      int i = 0;
      Field field = ObjectClass.getField(fieldname);
      for (Object so : list)
        i += field.getInt(so);
      return i;
    }
    catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
  } // getTotal()


  // creates a new BufferedReader for a given url
  public static BufferedReader getReader(String url) throws MalformedURLException, IOException
  {
    return new BufferedReader(new InputStreamReader(new URL(url).openStream(), "UTF-8"));
  } // getReader()


  // creates a new BufferedReader for a given url
  public static BufferedReader getURLWithQueryStringReader(String url) throws MalformedURLException, IOException
  {
    HttpURLConnection httpUrlConnection = (HttpURLConnection) (new URL(url)).openConnection();
    return new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream(), "UTF-8"));
  } // getURLWithQueryStringReader()


  public static List<NYSenate> getSenateData() throws IOException, InterruptedException
  {
    List<NYSenate> ret = new ArrayList<NYSenate>();
    BufferedReader br = Utils.getReader("http://www.nysenate.gov/senators");
    Pattern senPattern = Pattern.compile("<a href=\\\"(/senator/[\\w\\-]+/?|http://www.kemphannon.com)\\\">([^<>]+)</a>");
    Pattern facebookPattern = Pattern.compile("https?://(www.)?(new.)?facebook.com");
    Pattern flickrPattern = Pattern.compile("https?://(www.)?flickr.com");
    Pattern twitterPattern = Pattern.compile("https?://(www.)?twitter.com");
    Pattern youtubePattern = Pattern.compile("https?://(www.)?youtube.com");

    String in, s;
    NYSenate tSObj = null;
    while ((in = br.readLine()) != null) {
      Matcher senM = senPattern.matcher(in);
      Matcher facebookM = facebookPattern.matcher(in);
      Matcher flickrM = flickrPattern.matcher(in);
      Matcher twitterM = twitterPattern.matcher(in);
      Matcher youtubeM = youtubePattern.matcher(in);

      if (senM.find()) {
        if (tSObj == null) {
          tSObj = new NYSenate();
          tSObj.nysenateURL = senM.group(1);
        }
        if (senM.group(2).startsWith("Senate District")) {
          tSObj = null;
          continue;
        }
        String strings[] = senM.group(2).split(",");

        strings[1] = strings[1].trim();
        strings[0] = strings[0].trim();

        tSObj.fName = strings[1];
        tSObj.lName = strings[0];
      }
      if (in.contains("social_buttons") && tSObj != null) {
        if (facebookM.find()) {
          s = in.substring(facebookM.start());
          s = s.split("\"")[0];
          tSObj.facebookURL = s;
        }
        if (flickrM.find()) {
          s = in.substring(flickrM.start());
          s = s.split("\"")[0];
          tSObj.flickrURL = s;

        }
        if (twitterM.find()) {
          s = in.substring(twitterM.start());
          s = s.split("\"")[0];
          tSObj.twitterURL = s;

        }
        if (youtubeM.find()) {
          s = in.substring(youtubeM.start());
          s = s.split("\"")[0];
          tSObj.youtubeURL = s;
        }
        ret.add(fixForNYSenateGov(tSObj));
        tSObj = null;
      }

    }

    // Add special case for NYSenate.
    NYSenate tObj = new NYSenate();
    tObj.fName = "NYSenate";
    tObj.nysenateURL = "/";
    tObj.twitterURL = "http://www.twitter.com/nysenate";
    tObj.facebookURL = "http://www.facebook.com/NYsenate";
    tObj.youtubeURL = "http://www.youtube.com/user/NYSenate";
    ret.add(tObj);

    return ret;
  } // getSenateData()


  /**
   * Takes a list of data entries and aggregates the pageViews, bounces, and timeOnPage by
   * source.
   * 
   * @param entries
   * @param pathMatch
   * @return
   */
  public static List<Source> combineDataFeedBySource(List<DataEntry> entries, String pathMatch)
  {
    Map<String, Source> map = new HashMap<String, Source>();
    for (DataEntry de : entries) {
      String path = de.stringValueOf("ga:pagePath");

      if (pathMatch == null || path.contains(pathMatch)) {
        String source = ((pathMatch == null) ? path : de.stringValueOf("ga:source"));
        int pageViews = new Integer(de.stringValueOf("ga:pageviews"));
        int bounces = new Integer(de.stringValueOf("ga:bounces"));
        double timeOnPage = new Double(de.stringValueOf("ga:timeOnPage"));

        if (map.containsKey(source)) {
          Source sourceObject = map.get(source);
          sourceObject.pageviews += pageViews;
          sourceObject.bounces += bounces;
          sourceObject.timeOnPage += timeOnPage;
        }
        else {
          map.put(source, new Source(source, pageViews, bounces, timeOnPage));
        }
      }
    }
    return new ArrayList<Source>(map.values());
  } // combineDataFeedBySource()


  /**
   * Truncates a list of sources by grouping all sources after `sourceLimit` into
   * a new "other" source.
   * 
   * @param sources
   *      The list of sources
   * @param sourceLimit
   *      The number of sources to allow before grouping under "other"
   * @return
   */
  public static List<Source> groupOthers(List<Source> sources, int sourceLimit)
  {
    int bounces = 0;
    int pageviews = 0;
    double time = 0;

    for (int i = sourceLimit; i < sources.size(); i++) {
      Source so = sources.get(i);
      bounces += so.bounces;
      pageviews += so.pageviews;
      time += so.timeOnPage;
    }

    List<Source> ret = sources.subList(0, Math.min(sourceLimit, sources.size()));
    ret.add(new Source("other", pageviews, bounces, time));
    return ret;
  } // groupOthers()


  public static NYSenate fixForNYSenateGov(NYSenate senator)
  {
    if (senator.lName.equals("Hannon")) {
      senator.nysenateURL = "/senator/kemp-hannon";
    }
    return senator;
  } // fixForNYSenateGov()


  public static void emailExcel(Ini config) throws Exception
  {
    String startDate = config.get("", "start_date");
    String endDate = config.get("", "end_date");

    Section emailSection = config.get("email");
    String sender = emailSection.get("sender");
    String recipients = emailSection.get("recipients");
    String message_body = emailSection.get("message_body");
    String subject = emailSection.get("subject")+" - "+startDate+" to "+endDate;
    String attachments = ExcelReport.getFileName(config);
    // String attachments = emailSection.get("attachments");
    String error_message = null;
    String smtp_server = emailSection.get("hostname");
    String port = emailSection.get("port");
    String user = emailSection.get("user");
    String pass = emailSection.get("pass");
    int error = send(sender, recipients, subject, message_body, attachments,
                     smtp_server, port, user, pass);
  } // emailExcel()


  // Sender, Recipient, CCRecipient, and BccRecipient are comma-
  // separated lists of addresses;
  //   body can span multiple CR/LF-separated lines;
  //   attachments is a ///-separated list of file names;
  public static int send(String from, String to, String subject, String body,
                         String attachments, String smtpServer,
                         String port, String user, String pass)
  {
    // Error status;
    int errorStatus = 0;

    // create some properties and get the default Session;
    Properties props = System.getProperties();
    props.put("mail.smtp.port", port);
    props.put("mail.smtp.user", user);
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.host", smtpServer);
    Session session = Session.getDefaultInstance(props, null);

    try {
      // create a message;
      MimeMessage msg = new MimeMessage(session);

      // extracts the senders and adds them to the message;
      // Sender is a comma-separated list of e-mail addresses as
      // per RFC822;
      {
        InternetAddress[] theAddresses =
            InternetAddress.parse(from);
        msg.addFrom(theAddresses);
      }
      // extract the recipients and assign them to the message;
      // Recipient is a comma-separated list of e-mail addresses
      // as per RFC822;
      InternetAddress[] theAddresses = InternetAddress.parse(to);
      msg.addRecipients(Message.RecipientType.TO, theAddresses);
      msg.setSubject(subject);

      // create the Multipart to be added the parts to;
      Multipart mp = new MimeMultipart();

      // create and fill the first message part;
      MimeBodyPart mbp = new MimeBodyPart();
      mbp.setText(body);
      // attach the part to the multipart;
      mp.addBodyPart(mbp);


      // attach the files to the message;
      if (null != attachments) {
        int startIndex = 0, posIndex = 0;
        while (-1 != (posIndex = attachments.indexOf("///", startIndex))) {
          // create and fill other message parts;
          mbp = new MimeBodyPart();
          FileDataSource fds =
              new FileDataSource(attachments.substring(startIndex, posIndex));
          mbp.setDataHandler(new DataHandler(fds));
          mbp.setFileName(fds.getName());
          mp.addBodyPart(mbp);
          posIndex += 3;
          startIndex = posIndex;
        }

        // last, or only, attachment file;
        if (startIndex < attachments.length()) {
          mbp = new MimeBodyPart();
          FileDataSource fds =
              new FileDataSource(attachments.substring(startIndex));
          mbp.setDataHandler(new DataHandler(fds));
          mbp.setFileName(fds.getName());
          mp.addBodyPart(mbp);
        }
      }

      // add the Multipart to the message;
      msg.setContent(mp);

      // set the Date: header;
      msg.setSentDate(new Date());

      Transport t = session.getTransport("smtp");
      t.connect(user, pass);
      t.sendMessage(msg, theAddresses);
    }
    catch (MessagingException msgException) {
      String errorMessage = msgException.toString();
      Exception theException = null;
      if ((theException = msgException.getNextException()) != null) {
        errorMessage = errorMessage+"\n"+theException.toString();
      }
      System.out.println(errorMessage);
      errorStatus = 1;
    }
    return errorStatus;
  } // send()

} // class Utils
