package bigsky;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.Timestamp;

public class Logger {
	
	private static File logFile = null;
	private static PrintStream ps = null;
	private static boolean noError = true;
	
	public Logger()
	{
		String timeString = new Timestamp(new java.util.Date().getTime()).toString();
		timeString = timeString.replace(' ', '-').replace(':', '-');
		
		logFile = new File(timeString + ".log");
		try {
			ps = new PrintStream(logFile);
		} catch (FileNotFoundException e) {
			return;
		}
		
		System.setErr(ps);
		System.setOut(ps);
	}

	public static void printErr(String err)
	{
		noError = false;
		
		String timeString = new Timestamp(new java.util.Date().getTime()).toString();

		StringBuffer sb = new StringBuffer("[");
		sb.append(timeString);
		sb.append(']');
		sb.append("    ");
		StackTraceElement ste = Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length - 1];
		sb.append(String.format("%-50s", ste.getClassName() + '.' + ste.getMethodName() + '@' + ste.getLineNumber()));
		sb.append(err);
		
		System.err.println(sb.toString());
	}
	
	public static void printOut(String out)
	{
		String timeString = new Timestamp(new java.util.Date().getTime()).toString();

		StringBuffer sb = new StringBuffer("[");
		sb.append(timeString);
		sb.append(']');
		sb.append("    ");
		StackTraceElement ste = Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length - 1];
		sb.append(String.format("%-50s", ste.getClassName() + '.' + ste.getMethodName() + '@' + ste.getLineNumber()));
		sb.append(out);
		
		System.out.println(sb.toString());
	}
	
	public static void closeLogger()
	{
		ps.close();
		
		if(noError){
			logFile.delete();
		}
		else{
			logFile.setReadOnly();
		}
	}
}
