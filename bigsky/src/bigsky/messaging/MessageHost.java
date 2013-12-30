package bigsky.messaging;

import java.net.*;
import java.io.*;

import bigsky.BlueTextRequest;
import bigsky.BlueTextRequest.REQUEST;
import bigsky.BlueTextResponse;
import bigsky.Contact;
import bigsky.Logger;
import bigsky.TaskBar;
import bigsky.TextMessage;
import bigsky.gui.Conversation;
import bigsky.gui.LoadScreen;

/**
 * Thread that runs continuously for reading objects that have been transmitted
 * by the phone. It is important that the while loop completes as quickly as
 * possible when an object is received through the stream, and not much time is
 * wasted processing the object. Best practice is to hand the object off to the
 * TextMessageManager (which runs on a separate thread) and then notify the
 * thread. Doing so transfers responsibility of processing the object to the
 * other thread and frees up this thread so that it may continue and read in
 * more objects right away.
 * 
 * @author Andy Guibert
 */
class ReadThread implements Runnable 
{
	Thread t;
	Socket client = null;

	ReadThread(Socket client) {
		this.client = client;
		t = new Thread(this);
		t.start();
	}

	public void run() {
		Contact user = TaskBar.me;
		ObjectInputStream br = null;
		try {
			br = new ObjectInputStream(client.getInputStream());
			while (br != null) {
				Object streamObject = br.readObject();
				if(streamObject instanceof Contact)
				{
					Contact ct = (Contact) streamObject;
					TaskBar.incomingContactQueue.add(ct);
					synchronized(TaskBar.textManager){
						TaskBar.textManager.notify();
					}
				}
				else if(streamObject instanceof TextMessage)
				{
					TextMessage txtMessage = (TextMessage) streamObject;
					txtMessage.setReceiver(user);
					Logger.printOut("Client: " + txtMessage.getContent());					
					TaskBar.myTextArray.add(txtMessage);					
					synchronized(TaskBar.textManager){
						TaskBar.textManager.notify();
					}
				}
				else if(streamObject instanceof BlueTextResponse)
				{
					BlueTextResponse response = (BlueTextResponse) streamObject;
					TaskBar.responseQueue.add(response);
					synchronized(TaskBar.textManager){
						TaskBar.textManager.notify();
					}
				}
				else{
					Logger.printErr("ERROR: Unknown object sent through stream");
				}
			}
		} catch (Exception e) {
			if(TaskBar.messageHost != null){
				TaskBar.messageHost.closeHost(true);
			}
		}
	}
}

/**
 * Thread that runs for managing the PC's communication with the the phone. This
 * thread sets up the ClientConn thread and holds authority for closing
 * communication channels and anything that is written to the phone using
 * sendObject().
 * 
 * @author Andrew
 */
public class MessageHost extends Thread{
	
	public ReadThread conn = null;
	private ObjectOutputStream ps2 = null;
	private ServerSocket socket = null;
	private boolean alreadyCleaningUp = false;
	
	public void run(){
		
		LoadScreen load = new LoadScreen();
		try{
			// Establish socket and connection with PC
			socket = new ServerSocket(1300);
			load.setVisible(true);
			Socket client = socket.accept();
			
			// Start thread to read information sent by phone
			conn = new ReadThread(client);
			
			// Close splash screen and create main window
			load.dispose();
			TaskBar.convo = new Conversation();
			TaskBar.convo.getFrmBluetext().setVisible(true);
			
			// Establish output stream
			ps2 = new ObjectOutputStream(client.getOutputStream());
			
			// Send initial request for phone battery percentage
			sendObject(new BlueTextRequest(REQUEST.BATTERY_PERCENTAGE, null));
		} catch(Exception e){
			e.printStackTrace();
			Logger.printErr("Caught exception while setting up MessageHost" + e.getMessage());
			closeHost(true);
		}
	}
	
	/**
	 * Closes all communication channels between the PC and phone and prepares
	 * BlueText for logging in again.
	 * 
	 * @param callLogout
	 *            If the caller would like to call TaskBar.logout() after the
	 *            method completes, taking the user back to the login screen.
	 */
	public void closeHost(boolean callLogout){
		
		Logger.printOut("MessageHost's ClientConn is now closing");
		
		if(alreadyCleaningUp){
			return;
		}
		
		alreadyCleaningUp = true;
		
		if(conn != null){
			if(conn.client != null){
				try {
					conn.client.close();
				} catch (IOException e) {}
				conn.client = null;
			}
			conn = null;
		}
		if(ps2 != null){
			try {
				ps2.close();
			} catch (IOException e) {}
			ps2 = null;
		}
		if(socket !=null){
			try {
				socket.close();
			} catch (IOException e) {}
			socket = null;
		}
		TaskBar.messageHost = null;
		
		RequestManager.yield();
		TaskBar.textManager = null;
		
		if(callLogout){
			TaskBar.logout();
		}
	}
	
	/**
	 * Sends an object to the phone through the MessageHost's ObjectOutputStream.
	 * @param o The object to be sent to the phone 
	 */
	public synchronized void sendObject(Object o)
	{
		try{
			ps2.writeObject(o);
			// Always remember to flush
			ps2.flush();
		} catch(Exception e){
			e.printStackTrace();
			Logger.printErr("Got exception in MessageHost.sendObject(): " + e.getMessage());
		}
	}
}
