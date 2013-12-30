package bigsky.gui;

import javax.swing.JFrame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Properties;
import java.io.FileOutputStream;

import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextPane;

import bigsky.Contact;
import bigsky.Global;
import bigsky.Logger;
import bigsky.TaskBar;
import bigsky.TextMessage;
import bigsky.messaging.RequestManager;

import java.awt.Toolkit;
import java.io.FileInputStream;

public class SmallChat  {

	private JFrame frmBluetext;
	public final JTextField textField = new JTextField();
	private ArrayList<TextMessage> myTextHistory = new ArrayList<TextMessage>();
	
	private JButton send;
	private JScrollPane scrollPane;
	private JTextPane textPane;
	private DefaultCaret caret;
	private int winLocationY;
	
	private int offset = 0;
	private int textCount = -1;
	private static int windowNum = 0;
	private int winNum = 0;
	
	private Contact me;
	private Contact you;
	private TextMessage sent;
	private JTextField textField_1;
	protected JMenu settings;
	private JMenu messagePreview;
	private JMenu notification;
	private JMenu fontSize;
	protected static JRadioButtonMenuItem notificationON;
	protected static JRadioButtonMenuItem notificationOFF;
	protected static JRadioButtonMenuItem messagePreviewON;
	protected static JRadioButtonMenuItem messagePreviewOFF;
	private ButtonGroup notiGroup;
	private JMenuBar menuBar;
	private ButtonGroup previewMessageGroup;
    private JMenuItem defaultSettings;

	/**
	 * Initializes a quick chat window
	 * @param me user Contact
	 * @param you Contact the user communicates with
	 */
	public SmallChat(Contact me, Contact you) {
		this.me = TaskBar.me;
		this.you = you;
		initialize();
		winNum = windowNum;
		windowNum++;
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		//final SystemTray tray = SystemTray.getSystemTray();
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		
		send = new JButton("Send");
		
		frmBluetext = new JFrame();
        if (!System.getProperty("os.name").contains("Mac")){
			frmBluetext.setIconImage(Toolkit.getDefaultToolkit().getImage(SmallChat.class.getResource("/bigsky/BlueText.gif")));
		}
		frmBluetext.getRootPane().setDefaultButton(send);
		frmBluetext.setResizable(false);
		frmBluetext.getContentPane().setBackground(Color.DARK_GRAY);
		frmBluetext.setTitle(you.getFirstName() + " " + you.getLastName());
		if((gd.getDisplayMode().getHeight() - 385 * (winNum + 1)) > gd.getDisplayMode().getHeight()){
			winLocationY = gd.getDisplayMode().getHeight();
		}
		else{
			winLocationY = gd.getDisplayMode().getHeight() - 385 * (winNum + 1);
		}
		frmBluetext.setBounds(gd.getDisplayMode().getWidth() - 243, winLocationY, 236, 340);
		frmBluetext.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmBluetext.getContentPane().setLayout(null);
		
		frmBluetext.getContentPane().add(send);
		
		
		
		textField.setBounds(0, 289, 230, 23);
		frmBluetext.getContentPane().add(textField);
		textField.setColumns(10);
		
		textPane = new JTextPane();
		textPane.setFont(new Font("Franklin Gothic Medium", Font.PLAIN, 12));
		textPane.setEditable(false);
		
		scrollPane = new JScrollPane(textPane);
		scrollPane.setBounds(0, 24, 230, 264);
		frmBluetext.getContentPane().add(scrollPane);
		scrollPane.setViewportView(textPane);
		caret = (DefaultCaret)textPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		//Beginning of Settings Menu Bar
		menuBar = new JMenuBar();
		menuBar.setBounds(169, 0, 60, 23);
		frmBluetext.getContentPane().add(menuBar);
		
		settings = new JMenu("Settings");
		menuBar.add(settings);
		
		//Notifications menu
		notification = new JMenu("Notifications");
		settings.add(notification);
				
		notificationON = new JRadioButtonMenuItem("On");
		notificationOFF = new JRadioButtonMenuItem("Off");
		
		notification.add(notificationON);
		notification.add(notificationOFF);
		// Adding buttons to group so only 1 radio button can be selected at any times
		notiGroup = new ButtonGroup();
		notiGroup.add(notificationON);
		notiGroup.add(notificationOFF);
		
		//preview message Menu
		messagePreview = new JMenu("Preview Message");
		settings.add(messagePreview);
			
		messagePreviewON = new JRadioButtonMenuItem("On");
		messagePreviewOFF = new JRadioButtonMenuItem("Off");
		
		messagePreview.add(messagePreviewON);
		messagePreview.add(messagePreviewOFF);
				
		//Adding buttons to group so that only 1 radio button can be selected at any time.
		previewMessageGroup = new ButtonGroup();
		previewMessageGroup.add(messagePreviewON);
		previewMessageGroup.add(messagePreviewOFF);
		
		//Choose font size of small chat				
		fontSize = new JMenu("Font Size");
		settings.add(fontSize);
		
		textField_1 = new JTextField();
		fontSize.add(textField_1);
		textField_1.setColumns(10);
		
		//default settings
		defaultSettings = new JMenuItem("Default Settings");
		settings.add(defaultSettings);
		
		checkButtons();
		
		send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sent = new TextMessage(me, you, textField.getText());
				try {			
					updateConv(sent);
				} catch (BadLocationException e1) {
					e1.printStackTrace();
					Logger.printOut("updateConv in SmallChat - FAILED");
				} 
				textField.setText("");
			}
		});
		
		//Notification setting
		notificationON.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(notificationON.isSelected()){
					Login.saveInProp(Global.username,Global.NOTIFICATION, Global.ON);
					notificationOFF.setSelected(false);
					Conversation.selectNotificationOn();
				}				
			}
		});
		//Notification setting
		notificationOFF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(notificationOFF.isSelected()){
					Login.saveInProp(Global.username,Global.NOTIFICATION, Global.OFF);
					notificationON.setSelected(false);
					Conversation.selectNotificationOff();
				}						
			}
		});
		//MessagePreview setting
		messagePreviewON.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(messagePreviewON.isSelected()){
					Login.saveInProp(Global.username,Global.MESSAGEPREVIEW, Global.ON);
					messagePreviewOFF.setSelected(false);
					Conversation.selectPreviewOn();
				}				
			}
		});
		//MessagePreview setting
			messagePreviewOFF.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(messagePreviewOFF.isSelected()){
							Login.saveInProp(Global.username,Global.MESSAGEPREVIEW, Global.OFF);
							messagePreviewON.setSelected(false);
							Conversation.selectPreviewOff();
						}						
					}
				});
		
		//Font Size setting
		textField_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (textField_1.getText().matches("[0-9]+")){
					textPane.setFont(new Font("Franklin Gothic Medium", Font.PLAIN, Integer.valueOf(textField_1.getText())));
					Login.saveInProp(Global.username,Global.smallChatFontSize,textField_1.getText().toString());
					updateSmallChatFontSize();
				}
			}
		});
		
		//Default Setting listener
		defaultSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				defaultSettings();
				updateSmallChatFontSize();
				}
		});
	}
	
	/**
	 * Checks(selects) the buttons that are set in the preference file
	 */
	private void checkButtons(){
		Properties prop = new Properties();
	
		try {
			prop.load(new FileInputStream(Global.username +".properties"));
		} catch (Exception e) {
			Logger.printErr("file load problem.");
		}
			
		if(prop.getProperty(Global.NOTIFICATION).equals(Global.ON)){
			notificationON.setSelected(true);
		}
		else if(prop.getProperty(Global.NOTIFICATION).equals(Global.OFF)){
			notificationOFF.setSelected(true);
		}
		
		if(prop.getProperty(Global.MESSAGEPREVIEW).equals(Global.ON)){
			messagePreviewON.setSelected(true);
		}
		else if(prop.getProperty(Global.MESSAGEPREVIEW).equals(Global.OFF)){
			messagePreviewOFF.setSelected(true);
		}
	}
	
	/**
	 * Allows user to reset the settings of the specified window to that of manufacturers
	 */
	private void defaultSettings(){
		Properties prop = new Properties();
		
		try {
			prop.load(new FileInputStream(Global.username +".properties"));
		} catch (Exception e) {
			Logger.printErr("file load problem.");
		}
		
		prop.setProperty("save", Global.OFF);
		prop.setProperty(Global.MESSAGEPREVIEW,Global.ON);
		prop.setProperty(Global.NOTIFICATION,Global.ON);
		prop.setProperty(Global.smallChatFontSize, "12");	
		
		try{
			prop.store(new FileOutputStream(Global.username + ".properties"), null);
		}
		catch(Exception e1){
			Logger.printErr("Problem saving default settings in small chat");
		}
		textPane.setFont(new Font("Franklin Gothic Medium", Font.PLAIN, Integer.valueOf(TaskBar.savedInfo(Global.conversationFontSize))));
	}
	
	/**
	 * updates conversation based on text message received or sent.  Logic inside determines how to update
	 * @param text text message that will be updated
	 * @throws BadLocationException
	 */
	protected void updateConv(TextMessage text) throws BadLocationException{
		boolean check = false;
		int temp = 0;
		
		String person1 = text.getSender().getFirstName() + ":";
		
		for(int i = person1.length(); i < 17;i++){
			person1 = person1 + " ";
		}
		
		//checks if user is sender
		if(!text.getContent().trim().isEmpty() && text.getSender().getPhoneNumber().equalsIgnoreCase(me.getPhoneNumber())){
			textPane.getDocument().insertString(offset, person1 + text.getContent() + "\n\n", null);
			offset += (person1 + text.getContent() + "\n\n").length();
			textCount++;
			myTextHistory.add(text);
			you.setSecondPhone("");
			text.setReceiver(you);
			
			if(!TaskBar.doNotSend && RequestManager.sendTexts){
				TaskBar.outGoingInSmall.add(text);
			}
			
			if(TaskBar.outGoingInSmall.size() != 0 && RequestManager.sendTexts){
				for(int i = 0; i < Conversation.currentConvs.size();i++){
					if(TaskBar.outGoingInSmall.get(0).getReceiver().getPhoneNumber().equalsIgnoreCase(Conversation.currentConvs.get(i).getPhoneNumber())){
						TaskBar.doNotSend = true;
						Conversation.updateConv(text);
						temp = Conversation.offset.get(i);
						Conversation.textPanes.get(i).getDocument().insertString(Conversation.offset.get(i), person1 + text.getContent() + "\n\n", null);
						temp += (person1 + text.getContent() + "\n\n").length();
						Conversation.offset.set(i, temp);
						TaskBar.outGoingInSmall.remove(0);
						TaskBar.doNotSend = false;
						check = true;
						break;
					}
				}
				if(check == false && RequestManager.sendTexts){
					TaskBar.doNotSend = true;
					
					JTextPane textPane = new JTextPane();
					DefaultCaret caretC = (DefaultCaret)textPane.getCaret();
					caretC.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
					textPane.setFont(new Font("Franklin Gothic Medium", Font.PLAIN, 12));
					textPane.setEditable(false);
					Conversation.textPanes.add(textPane);
					JScrollPane scroll = new JScrollPane(textPane);
					Global.conversationPane.addTab(text.getReceiver().getFirstName() + " " + text.getReceiver().getLastName(), null, scroll, null);
					Conversation.initTabComponent(Global.conversationPane.getTabCount()-1);
					Global.conversationPane.setSelectedIndex(Global.conversationPane.getTabCount()-1);
					Conversation.offset.add(new Integer(0));
					Conversation.currentConvs.add(text.getReceiver());
					
					Conversation.updateConv(text);
					TaskBar.outGoingInSmall.remove(0);
					TaskBar.doNotSend = false;
				}
			}

			if(!TaskBar.doNotSend && RequestManager.sendTexts){
				TaskBar.messageHost.sendObject(text);
			}

		}
		
			
		if(!text.getContent().trim().isEmpty() && text.getSender().getPhoneNumber().equals(you.getPhoneNumber())){
			textPane.getDocument().insertString(offset, person1 + text.getContent() + "\n\n", null);
			offset += (person1 + text.getContent() + "\n\n").length();
		}
		
		//attempt to scroll on creation
		textPane.setAutoscrolls(true);
		scrollPane.scrollRectToVisible(new Rectangle(0,textPane.getBounds(null).height,1,1));
	}
	
	/**
	 * returns the user as Contact object
	 * @return user contact
	 */
	public Contact getLocalContact(){
		return me;
	}
	
	/**
	 * returns the Contact the user is communicating with
	 * @return Contact user is chating with
	 */
	public Contact getFromContact(){
		return you;
	}
	
	/**
	 * updates the SmallChat window from the received text
	 * @param text the text message received
	 * @throws BadLocationException
	 */
	public void receivedText(TextMessage text) throws BadLocationException{
		updateConv(text);
	}
	
	/**
	 * returns number of texts
	 * @return number of texts
	 */
	public int getMyTextCount(){
		return textCount;
	}
	
	/**
	 * returns text history
	 * @return text history
	 */
	public ArrayList<TextMessage> getMyHistory(){
		return myTextHistory;
	}
	
	/**
	 * returns this windows index
	 * @return window index
	 */
	public int getChatNumber(){
		return winNum;
	}
	
	/**
	 * returns the frame component
	 * @return frame component
	 */
	public JFrame getFrmBluetext() {
		return frmBluetext;
	}
	
	/**
	 * Checks if a small chat window with a specific phone number has focus
	 * @param phone number to be checked
	 * @return true if SmallChat window with phone number has focus, false else
	 */
	public static boolean hasFucusedSmallChat(String phone){
		for(int i = 0; i < TaskBar.smallChatWindows.size();i++){
			if(TaskBar.smallChatWindows.get(i).getFromContact().getPhoneNumber().equalsIgnoreCase(phone) && TaskBar.smallChatWindows.get(i).getFrmBluetext().isFocused()){
				return true;
			}
		}
		return false;
	}
	/**
	 * All of these below update help update
	 * smallchats settings when they are 
	 * changed in the conversation window
	 * 
	 */
	public static void selectPreviewOn(){
		messagePreviewON.setSelected(true);
		messagePreviewOFF.setSelected(false);
	}
	public static void selectPreviewOff(){
		messagePreviewOFF.setSelected(true);
		messagePreviewON.setSelected(false);
	}
	public static void selectNotificationOn(){
		notificationON.setSelected(true);
		notificationOFF.setSelected(false);
	}
	public static void selectNotificationOff(){
		notificationOFF.setSelected(true);
		notificationON.setSelected(false);
	}
	public JTextPane getTextPane(){
		return textPane;
	}
	private void updateSmallChatFontSize(){
		for(int i = 0; i < TaskBar.smallChatWindows.size(); i++){
			TaskBar.smallChatWindows.get(i).getTextPane().setFont(new Font("Franklin Gothic Medium", Font.PLAIN, Integer.valueOf(TaskBar.savedInfo(Global.smallChatFontSize))));
		}
	}
}
