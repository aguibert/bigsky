package bigsky;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Logging class for BlueText 
 * @author Andrew
 */
public class Logger{
	
	private static boolean loggingEnabled;
	private static File logFile = null;
	private static PrintStream ps = null;
	private static boolean noError = true;
	private static boolean isOpenLogger = false;
	private static PrintStream normalOut = null;
	private static PrintStream normalErr = null;
	private static final int SEVERITY_STD = 1;
	private static final int SEVERITY_ERR = 0;
	
	/**
	 * Constructs a new logger instance for the run if 
	 * one does not already exist.  Sets System.out and
	 * System.err to the log file.
	 */
	public Logger()
	{
		Logger.loggingEnabled = Global.loggingEnabled;
		if(!Logger.loggingEnabled || isOpenLogger)
			return;
		
		isOpenLogger = true;
		
		String timeString = new Timestamp(new java.util.Date().getTime()).toString();
		timeString = timeString.replace(' ', '-').replace(':', '-').replace('.', '-');
		
		logFile = new File(timeString + ".log");
		try {
			ps = new PrintStream(logFile);
		} catch (FileNotFoundException e) {
			return;
		}
		
		normalOut = System.out;
		normalErr = System.err;
		System.setErr(ps);
		System.setOut(ps);
		
		System.out.println("# Log file created at: " + timeString);
		try {
			System.out.print("# For user: ");
			if(TaskBar.me.getFirstName().equalsIgnoreCase("me")){
				System.out.println(Inet4Address.getLocalHost().getHostAddress());
			}
			else{
				TaskBar.me.toString();
			}
		} catch (UnknownHostException e) {}
		
	}

	/**
	 * Logs an error message.
	 * @param err The error to be written to log file.
	 */
	public static void printErr(String err)
	{
		if(!loggingEnabled)
			return;
		
		noError = false;
				
		System.err.println(formatLogString(err, SEVERITY_ERR));
	}
	
	/**
	 * Logs a normal output message.
	 * @param out The message to be written to log file.
	 */
	public static void printOut(String out)
	{		
		if(!loggingEnabled)
			return;
		
		System.out.println(formatLogString(out, SEVERITY_STD));
	}
	
	private static String formatLogString(String msg, int severity)
	{
		String timeString = new Timestamp(new java.util.Date().getTime()).toString();

		StringBuffer sb = new StringBuffer(String.format("%-27s", '[' + timeString + ']'));
		if(severity == SEVERITY_ERR){
			sb.append("ERR  ");
		}
		else{
			sb.append("OUT  ");
		}
		StackTraceElement ste = Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length - 1];
		sb.append(String.format("%-50s", ste.getClassName() + '.' + ste.getMethodName() + '@' + ste.getLineNumber()));
		sb.append(msg);
		
		// Also echo output to standard console
		if(severity == SEVERITY_ERR){
			normalErr.println(sb.toString());
		}
		else{
			normalOut.println(sb.toString());
		}
		
		return sb.toString();
	}
	
	/**
	 * Closes the log file.
	 * If there were no errors, delete the log file.
	 * If there were errors, email the log file to 
	 * blueTextLogger@gmail.com so we can look at it later.
	 */
	public static void closeLogger()
	{
		if(!loggingEnabled)
			return;
		
		ps.close();
		
		// If there is no error, safe to delete log file
		if(noError){
			logFile.delete();
		}
		// If there was an error, email the log file to us
		else{
			logFile.setReadOnly();
			new EmailLogFile(logFile).run();
		}
		
		Logger.isOpenLogger = false;
	}
}

class EmailLogFile extends Thread
{
	private final File log;
    private static final String USER_NAME = "bluetextlogging";
    private static final String PASSWORD = "ISUteam01";
    private static final String RECIPIENT = "bluetextlogging@gmail.com";
    
    public EmailLogFile(File logFile)
    {
    	log = logFile;
    }
    
	public void run()
	{
        try {
        	// Set general information about the message
	        String from = USER_NAME;
	        String pass = PASSWORD;
	        String[] to = { RECIPIENT }; // list of recipient email addresses
	        String subject = "Error log from: ";
	        if(TaskBar.me == null || TaskBar.me.getFirstName() == null || TaskBar.me.getFirstName().equalsIgnoreCase("me")){
	        	try{
	        		subject += Inet4Address.getLocalHost().getHostAddress();
	        	} catch (Exception e){
	        		subject += TaskBar.me.toString();
	        	}
	        }
	        else{
	        	subject += TaskBar.me.toString();
	        }
	        
	        // Set the message contents
	        StringBuffer sb = new StringBuffer("Username: ");
	        sb.append(Global.username);
	        sb.append('\n');
	        sb.append(TaskBar.me.toString());
	        String body = sb.toString();
	        MimeBodyPart stringBodyPart = new MimeBodyPart();
	        stringBodyPart.setText(body);
	        
	        // Set system properties for messaging
	        Properties props = System.getProperties();
	        String host = "smtp.gmail.com";
	        props.put("mail.smtp.starttls.enable", "true");
	        props.put("mail.smtp.host", host);
	        props.put("mail.smtp.user", from);
	        props.put("mail.smtp.password", pass);
	        props.put("mail.smtp.port", "587");
	        props.put("mail.smtp.auth", "true");
	        
            // Attach the log file to the email
	        Session session = Session.getDefaultInstance(props);
	        MimeMessage message = new MimeMessage(session);
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            DataSource ds = new FileDataSource(log.getAbsolutePath());
            messageBodyPart.setDataHandler(new DataHandler(ds));
            messageBodyPart.setFileName(log.getName());
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(stringBodyPart);
            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);

            message.setSubject(subject);
            //message.setText(body);
            message.setFrom(new InternetAddress(from));
            InternetAddress[] toAddress = new InternetAddress[to.length];

            // To get the array of addresses
            for( int i = 0; i < to.length; i++ ) {
                toAddress[i] = new InternetAddress(to[i]);
            }
            for( int i = 0; i < toAddress.length; i++) {
                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
            }
            
            // Send the message
            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, pass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (Exception ex){
        	// nothing we can do about exceptions at this point
        }
	}
}

