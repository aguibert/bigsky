package bigsky.messaging;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import bigsky.BlueTextRequest;
import bigsky.BlueTextRequest.REQUEST;
import bigsky.BlueTextResponse;
import bigsky.Contact;
import bigsky.Global;
import bigsky.Logger;
import bigsky.TaskBar;
import bigsky.TextMessage;
import bigsky.gui.Conversation;
import bigsky.gui.Notification;
import bigsky.gui.SmallChat;

/**
 * Separate thread that runs for processing most objects that are received 
 * by the PC from the phone.  Also acts as an over-arching class for keeping
 * the primary chat window and quick chat's synchronized.
 * @author Andy Guibert, Jonathan Mielke
 */
public class RequestManager extends Thread
{
	public static boolean sendTexts = true;
	private static Contact blueTextRqContact;
	public void run(){
	
	boolean matchR = false;
		
	try {
	synchronized(this){
	while(true){
		this.wait();
		
		/* Handle BlueTextResponse objects */
		processResponseQueue();
		
		/* Adding chat history from phone */
        if(!Global.phoneTextHistory.isEmpty()){
        	int smallChatNum = 0;
            for(int i=0; i < TaskBar.smallChatWindows.size(); i++){
               	if(blueTextRqContact.getPhoneNumber().equalsIgnoreCase(TaskBar.smallChatWindows.get(i).getFromContact().getPhoneNumber())){
               		smallChatNum = i;
               		break;
                }
            }
            if(smallChatNum == 0){
            	TaskBar.smallChatWindows.add(new SmallChat(TaskBar.me, blueTextRqContact));
               	TaskBar.updateAddTaskbarSmallChatWindows();
            	smallChatNum = TaskBar.smallChatWindows.size() - 1;
            	
            }
                
            sendTexts = false;
                                                                
            while(Global.phoneTextHistory.size() > 0){
                try {
                	Conversation.updateConv(Global.phoneTextHistory.get(Global.phoneTextHistory.size()-1));
                	TaskBar.smallChatWindows.get(smallChatNum).receivedText(Global.phoneTextHistory.get(Global.phoneTextHistory.size()-1));
                } catch (BadLocationException e) {
                	e.printStackTrace();
                    Logger.printOut("Updating in chat history -FAILED");
                }
                        
                Global.phoneTextHistory.remove(Global.phoneTextHistory.size()-1);
            } 
            sendTexts = true;
        }
		
		/* Handle incoming text messages */
        if(!TaskBar.myTextArray.isEmpty()){
        	Logger.printOut("hit manager sending");
			if(TaskBar.savedInfo(Global.NOTIFICATION, Global.ON) && !SmallChat.hasFucusedSmallChat(TaskBar.myTextArray.get(0).getSender().getPhoneNumber())){
				new Notification(TaskBar.myTextArray.get(0));
			}
            matchR = false;
            for(int i=0; i < TaskBar.smallChatWindows.size(); i++){
            	if(TaskBar.myTextArray.get(0).getSender().getPhoneNumber().equalsIgnoreCase(TaskBar.smallChatWindows.get(i).getFromContact().getPhoneNumber())){
            		try {
            			Conversation.updateConv(TaskBar.myTextArray.get(0));
            			TaskBar.smallChatWindows.get(i).receivedText(TaskBar.myTextArray.get(0));
            		} catch (BadLocationException e) {
            			e.printStackTrace();
            			Logger.printOut("Updating a small chat conversation -FAILED");
            		}
            		TaskBar.myTextArray.remove(0);
            		matchR = true;
            		break;
            	}
            }
            if(!matchR){
            	BlueTextRequest rq = new BlueTextRequest(BlueTextRequest.REQUEST.CONTACT_CHAT_HISTORY, TaskBar.myTextArray.get(0).getSender());
            	TaskBar.myTextArray.remove(0);
            	TaskBar.messageHost.sendObject(rq);
            }
        }
		
		/* Handle incoming Contacts */
		while(!TaskBar.incomingContactQueue.isEmpty()){
			Contact ct = TaskBar.incomingContactQueue.remove();
			if (ct.getLastName() == null){
				ct.setLastName("");
			}
			String first_name = ct.getFirstName();
			String perm_last = ct.getLastName();
			Boolean isUnique = true;
			//This loop is designed to remove duplicated names
			int j = 1;
			for (int i = 0; i<Global.contactAList.size();i++){
				Contact con = Global.contactAList.get(i);
				if (con.getFirstName().equals(first_name)){
					if (con.getLastName().equals(ct.getLastName())){
						//If the name already exists and the phone number is identical don't add it
						if (con.getPhoneNumber().equals(ct.getPhoneNumber())){
							isUnique = false;
							break;
						}
						//If the name already exists and there is room to add the second phone to the contact do it.
						else if (con.getSecondPhone().equals("")){
							con.setSecondPhone(ct.getPhoneNumber());
							isUnique = false;
							break;
						}
						String last_name = perm_last + " (" + Integer.toString(j) + ")";
						j++;
						ct.setLastName(last_name);
						i = 0; //set i back to 0 in case there is someone who already uses (1) we may need to use (2)
					}
				}
			}
			if (isUnique){
				addContactToListModel(ct.getFirstName(), ct.getLastName());
				Global.contactAList.add(ct);
			}
		}
	} // end while(true)
	} // end synchronized
	} // end try
	catch (InterruptedException e) {
		e.printStackTrace();
		Logger.printErr("Synchronized method block -FAILED");
	}
	}
	
	@SuppressWarnings("unchecked")
	private void addContactToListModel(String firstName, String lastName){
		if (!firstName.equals("")){
			String newEntry = firstName + " " + lastName;
			Global.listModel.addElement(newEntry);
		}
		else if (lastName.equals("")){
			String newEntry = lastName;
			Global.listModel.addElement(newEntry);
		}
	}
	
	/**
	 * Looks at TaskBar.responseQueue which is an ArrayList of BlueTextResponses.
	 * Currently supports the following response objects:
	 * <LI>REQUEST.BATTERY_PERCENTAGE
	 * <LI>REQUEST.CONTACT_CHAT_HISTORY
	 * <LI>REQUEST.CONTACT_PICTURE
	 */
	private void processResponseQueue()
	{
		while(!TaskBar.responseQueue.isEmpty())
		{
			BlueTextResponse resp = TaskBar.responseQueue.remove();
			REQUEST req = resp.getOriginalRequest().getRequest();
			
			if(REQUEST.BATTERY_PERCENTAGE == req)
			{
				Logger.printOut("Updating battery percentage to: " + resp.getBatteryLevel());
				Conversation.updateBatteryIndicator(resp.getBatteryLevel());
			}
			else if(REQUEST.CONTACT_CHAT_HISTORY == req){
				blueTextRqContact = resp.getOriginalRequest().getContact();
				Global.phoneTextHistory = resp.getChatHistory();
				if(!Global.phoneTextHistory.isEmpty() && Global.phoneTextHistory.get(Global.phoneTextHistory.size()-1).getSender().getPhoneNumber().equalsIgnoreCase(TaskBar.me.getPhoneNumber())){
					Global.phoneTextHistory.add(new TextMessage(blueTextRqContact,TaskBar.me,"hey"));
				}
				if(Global.phoneTextHistory.isEmpty()){
					Global.phoneTextHistory.add(new TextMessage(TaskBar.me, blueTextRqContact, "BlueText - Start Conversation: (this message is not sent)"));
				}
				if(!TaskBar.myTextArray.isEmpty()){
					Global.phoneTextHistory.add(TaskBar.myTextArray.get(0));
					TaskBar.myTextArray.remove(0);
				}
				 
			}
			else if(REQUEST.CONTACT_PICTURE == req){
				
				Contact requestedContact = resp.getOriginalRequest().getContact();
				Object imageResource = resp.getImageResource();
				if(imageResource instanceof String && ((String)imageResource).equalsIgnoreCase("NO_IMG")){
					//Global.contactTOimageIcon.put(requestedContact, Global.defaultContactImage);
				}
				else if(imageResource instanceof String){
					// Deprecated code path, just use default image here
					//Global.contactTOimageIcon.put(requestedContact, Global.defaultContactImage);
				}
				else if(imageResource instanceof byte[]){
					// If a byte[] was returned by the phone, then the user
					// actually has a contact picture
					try{
						ImageIcon img = new ImageIcon((byte[]) imageResource);
						InputStream in = new ByteArrayInputStream((byte[]) imageResource);
						BufferedImage bi = ImageIO.read(in);
						img = new ImageIcon(bi.getScaledInstance(180, 180, Image.SCALE_SMOOTH));
						Global.contactTOimageIcon.put(requestedContact.getPhoneNumber(), img);
					} catch(Exception e){
						e.printStackTrace();
						//Global.contactTOimageIcon.put(requestedContact, Global.defaultContactImage);
					}
				}		
				Conversation.setThumbnailPicture(requestedContact);
			}
			else{
				Logger.printErr("WARNING: an unknown response was received from the phone.");
			}
		} // End while loop
	}	
}
