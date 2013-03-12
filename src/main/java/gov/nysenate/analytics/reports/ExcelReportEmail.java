package gov.nysenate.analytics.reports;



import java.util.Date;
import java.util.Map;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

public class ExcelReportEmail {
	public static void emailExcel(Ini config) throws Exception {
		  for(Map.Entry<String,Section> entry : config.entrySet()) {

				//Skip non-report related blocks
				if(!entry.getKey().startsWith("email")) continue;

				//Get the report type and log the start of processing

				String sender =  entry.getValue().get("sender");
				String recipients = entry.getValue().get("recipients");
				String message_body = entry.getValue().get("message_body");
				String subject = entry.getValue().get("subject");
				String attachments = entry.getValue().get("attachments");
				String error_message = null;
				String smtp_server = entry.getValue().get("hostname");
				String port = entry.getValue().get("port");
				String user = entry.getValue().get("user");
				String pass = entry.getValue().get("pass");
				int error = Send(sender,recipients,subject,message_body,attachments,
						error_message,smtp_server,port,user,pass);

				if(error == 1){
					System.out.println(error_message);
				}

		  }

	}

     // Sender, Recipient, CCRecipient, and BccRecipient are comma-
     // separated lists of addresses;
     // Body can span multiple CR/LF-separated lines;
     // Attachments is a ///-separated list of file names;
     public static int Send(String from,
                            String to,
                            String Subject,
                            String Body,
                            String Attachments,
                            String ErrorMessage,
                            String  SMTPServer,
                            String port,
                            String user,
                            String pass) {

        // Error status;
        int ErrorStatus = 0;



        // create some properties and get the default Session;
        Properties props = System.getProperties();
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.user", user);
        props.put("mail.smtp.auth","true");
        props.put("mail.smtp.host", SMTPServer);
         Session session = Session.getDefaultInstance(props, null);

         try {
            // create a message;
            MimeMessage msg = new MimeMessage(session);

            // extracts the senders and adds them to the message;
            // Sender is a comma-separated list of e-mail addresses as
            // per RFC822;
            {
               InternetAddress[] TheAddresses =
                                        InternetAddress.parse(from);
               msg.addFrom(TheAddresses);
            }
//System.err.println("debug1");
          // extract the recipients and assign them to the message;
            // Recipient is a comma-separated list of e-mail addresses
            // as per RFC822;
            //{
               InternetAddress[] TheAddresses =
                                     InternetAddress.parse(to);

              msg.addRecipients(Message.RecipientType.TO,
                                 TheAddresses);
         //   }
       //     System.err.println("debug2");
           // subject field;
            msg.setSubject(Subject);

            // create the Multipart to be added the parts to;
            Multipart mp = new MimeMultipart();

            // create and fill the first message part;
            {
               MimeBodyPart mbp = new MimeBodyPart();
               mbp.setText(Body);

               // attach the part to the multipart;
               mp.addBodyPart(mbp);
            }
         //   System.err.println("debug3");
            // attach the files to the message;
            if (null != Attachments) {
               int StartIndex = 0, PosIndex = 0;
               while (-1 != (PosIndex = Attachments.indexOf("///",
                                                      StartIndex))) {
                  // create and fill other message parts;
                  MimeBodyPart mbp = new MimeBodyPart();
                  FileDataSource fds =
                  new FileDataSource(Attachments.substring(StartIndex,
                                                           PosIndex));
                  mbp.setDataHandler(new DataHandler(fds));
                  mbp.setFileName(fds.getName());
                  mp.addBodyPart(mbp);
                  PosIndex += 3;
                  StartIndex = PosIndex;
               }
           //    System.err.println("debug4");
               // last, or only, attachment file;
               if (StartIndex < Attachments.length()) {
                  MimeBodyPart mbp = new MimeBodyPart();
                  FileDataSource fds =
                new FileDataSource(Attachments.substring(StartIndex));
                  mbp.setDataHandler(new DataHandler(fds));
                  mbp.setFileName(fds.getName());
                  mp.addBodyPart(mbp);
               }
            }
         //   System.err.println("debug5");
            // add the Multipart to the message;
            msg.setContent(mp);

            // set the Date: header;
            msg.setSentDate(new Date());
          //  System.err.println("debug6");

            Transport t = session.getTransport("smtp");
            t.connect(user,pass);
            t.sendMessage(msg,TheAddresses);
         //   System.err.println("debug7");
         } catch (MessagingException MsgException) {
              ErrorMessage = MsgException.toString();
              Exception TheException = null;
              if ((TheException = MsgException.getNextException()) !=
                                                                 null)
                 ErrorMessage = ErrorMessage + "\n" +
                                   TheException.toString();
             ErrorStatus = 1;
         }
         return ErrorStatus;
     }
  }


