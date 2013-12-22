package bigsky.gui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;

import bigsky.BlueTextRequest;
import bigsky.BlueTextRequest.REQUEST;
import bigsky.Contact;
import bigsky.Global;
import bigsky.Logger;
import bigsky.TaskBar;
import bigsky.TextMessage;
import bigsky.messaging.RequestManager;

/**
 * The main window for our application
 * @author Travis Reed, Jonathan Mielke, Andrew Hartman, Andrew Guibert
 *
 */
public class Conversation {
	
	//added variables from Jon
	private final JTextArea txtrEnterMessageHere = new JTextArea();
	public static ArrayList<JTextPane> textPanes = new ArrayList<JTextPane>();
	public static ArrayList<Contact> currentConvs = new ArrayList<Contact>(); 
	public static ArrayList<Integer> offset = new ArrayList<Integer>();
	private JTextField textField_1;
	private JMenu settings;
	private JMenu notification;
	private JMenu fontSize;
	private static JRadioButtonMenuItem notificationON;
	private static JRadioButtonMenuItem notificationOFF;
	private ButtonGroup notiGroup;
	private JMenu messagePreview;
	private static JRadioButtonMenuItem messagePreviewON;
	private static JRadioButtonMenuItem messagePreviewOFF;
	private ButtonGroup previewMessageGroup;
	private JMenuItem defaultSettings;
	private static BlueTextRequest rq;
	private JFrame frmBluetext;
	private JTextField txtSearch;
	private final int returnsNull = -1;


	public Conversation() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmBluetext = new JFrame();
        if (!System.getProperty("os.name").contains("Mac")){
        	frmBluetext.setIconImage(Toolkit.getDefaultToolkit().getImage(Conversation.class.getResource("/bigsky/BlueText.gif")));
        }
        frmBluetext.setTitle("BlueText");
		frmBluetext.setSize(new Dimension(800,650));
		frmBluetext.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frmBluetext.setLocationRelativeTo(null);
		
		JMenuBar menuBar = new JMenuBar();
		frmBluetext.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mnu_new_contact = new JMenuItem("New Contact");
		mnu_new_contact.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openNewContactWindow();
			}
		});
		mnFile.add(mnu_new_contact);
		
		settings = new JMenu("Settings");
		mnFile.add(settings);
		
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
		
		//Status Menu
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
		
		//Choose font size of conversation chat				
		fontSize = new JMenu("Font Size");
		settings.add(fontSize);
		
		textField_1 = new JTextField();
		fontSize.add(textField_1);
		textField_1.setColumns(10);
		
		//default settings button
		defaultSettings = new JMenuItem("Default Settings");
		settings.add(defaultSettings);
		
		//Notification setting
		notificationON.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(notificationON.isSelected()){
					Login.saveInProp(Global.username,Global.NOTIFICATION, Global.ON);
					notificationOFF.setSelected(false);
					SmallChat.selectNotificationOn();
				}				
			}
		});
		//Notification setting
				notificationOFF.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(notificationOFF.isSelected()){
							Login.saveInProp(Global.username,Global.NOTIFICATION, Global.OFF);
							notificationON.setSelected(false);
							SmallChat.selectNotificationOff();
						}						
					}
				});
		//MessagePreview setting
		messagePreviewON.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(messagePreviewON.isSelected()){
					Login.saveInProp(Global.username,Global.MESSAGEPREVIEW, Global.ON);
					messagePreviewOFF.setSelected(false);
					SmallChat.selectPreviewOn();

				}				
			}
		});
		//MessagePreview setting
		messagePreviewOFF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(messagePreviewOFF.isSelected()){
					Login.saveInProp(Global.username,Global.MESSAGEPREVIEW, Global.OFF);
					messagePreviewON.setSelected(false);
					SmallChat.selectPreviewOff();

				}						
			}
		});
		
		//Font Size setting
		textField_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (textField_1.getText().matches("[0-9]+")){
					updateTabFonts();
					Login.saveInProp(Global.username,Global.conversationFontSize,textField_1.getText().toString());
				}
			}
		});
		
		//Default Setting listener
		defaultSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				defaultSettings();
				updateTabFonts();
			}
		});

		checkButtons();

		JMenuItem mnu_new_conversation = new JMenuItem("New Conversation");
		mnu_new_conversation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startNewConv();
			}
		});

		mnFile.add(mnu_new_conversation);

		JMenuItem mnu_logout = new JMenuItem("Log Out");
		mnu_logout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					TaskBar.logout();
				} catch (Exception e1) {}
			}
		});
		
		JMenuItem mntmImportFromFacebook = new JMenuItem("Import from Facebook");
		mnFile.add(mntmImportFromFacebook);
		mnFile.add(mnu_logout);
		mntmImportFromFacebook.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new PopUp_FacebookContacts();
			}
		});

		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);
		
		JMenuItem mntmEditContact = new JMenuItem("Edit Contact");
		mntmEditContact.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				editContactAction();
			}
		});
		
		mnEdit.add(mntmEditContact);

		JMenu mnView = new JMenu("View");
		menuBar.add(mnView);
		
		JMenuItem mntmImportContacts = new JMenuItem("Import Contacts");
		mntmImportContacts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sortListModel();
				JOptionPane.showMessageDialog(null, "Contacts updated. Happy conversations!");
			}
		});
		
		mnView.add(mntmImportContacts);

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		JMenu mnHidden = new JMenu("                                                                                                                                         ");
		mnHidden.setEnabled(false);
		mnHidden.setFocusable(false);
		menuBar.add(mnHidden);

		JMenuItem mntmAboutBluetext = new JMenuItem("About BlueText");
		mntmAboutBluetext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AboutDialog about = new AboutDialog();
				about.getFrame().setVisible(true);
			}
		});
		
		mnHelp.add(mntmAboutBluetext);
		updateBatteryIndicator(Global.battery_remaining);
		menuBar.add(Global.batteryIndicator);
		Global.batteryIndicator.setArmed(false);
		Global.batteryIndicator.setFocusable(false);

		JPanel panel = new JPanel();
		frmBluetext.getContentPane().add(panel);
		panel.setLayout(null);

		txtSearch = new JTextField();
		txtSearch.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String searchTerm = txtSearch.getText();
				searchContact(searchTerm);
			}
		});
		txtSearch.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				txtSearch.setText("");
			}
		});

		txtSearch.setBounds(14, 16, 163, 29);
		panel.add(txtSearch);
		txtSearch.setText("Search");
		txtSearch.setColumns(10);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(14, 225, 188, 306);
		panel.add(scrollPane);
		Global.list.addMouseListener(new PopClickListener());
		Global.list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				String selectedContact = (String)Global.list.getSelectedValue();
				if (selectedContact.contains("Create Contact")) {
					openNewContactWindow();
				}
			}
		});

		Global.list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		Global.list.setFont(new Font("Franklin Gothic Medium", Font.PLAIN,16));
		scrollPane.setViewportView(Global.list);

		JPanel conversationPanel = new JPanel();
		conversationPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		conversationPanel.setBounds(212, 26, 504, 435);
		
		Global.conversationPane.setBounds(226, 0, 490, 35);
		conversationPanel.add(Global.conversationPane);
	
		panel.add(conversationPanel);
		conversationPanel.setLayout(new CardLayout(0, 0));

		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				TextMessage text = new TextMessage(TaskBar.me,currentConvs.get(Global.conversationPane.getSelectedIndex()),txtrEnterMessageHere.getText());
            	try {
					updateConv(text);
				} catch (BadLocationException e) {
					e.printStackTrace();
					Logger.printErr("updateConv in Conversation - FAILED");
				}
            	txtrEnterMessageHere.setText("");
			}
		});
		btnSend.setBounds(599, 536, 117, 29);
		panel.add(btnSend);
		
		txtrEnterMessageHere.setText("New Message...");
		txtrEnterMessageHere.setBounds(212, 472, 504, 93);
		panel.add(txtrEnterMessageHere);
		txtrEnterMessageHere.setFont(new Font("Franklin Gothic Medium", Font.PLAIN, Integer.valueOf(TaskBar.savedInfo(Global.conversationFontSize))));
		
		txtrEnterMessageHere.addKeyListener(new KeyAdapter()
	    {
	        public void keyPressed(KeyEvent evt)
	        {
	            if(evt.getKeyCode() == KeyEvent.VK_ENTER)
	            {
	            	TextMessage text = new TextMessage(TaskBar.me,currentConvs.get(Global.conversationPane.getSelectedIndex()),txtrEnterMessageHere.getText());
	            	try {
						updateConv(text);
					} catch (BadLocationException e) {
						e.printStackTrace();
						Logger.printErr("updateConv in Conversation - FAILED");
					}
	            	txtrEnterMessageHere.setText("");
	            }
	        }
	        public void keyReleased(KeyEvent evt){
	        	if(evt.getKeyCode() == KeyEvent.VK_ENTER){
	        		txtrEnterMessageHere.setText("");
	        	}
	        }
	    });
		
		txtrEnterMessageHere.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				txtrEnterMessageHere.setText("");
			}
		});
		

		JButton btn_select_contact = new JButton("Start New Convo");
		btn_select_contact.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(!Global.list.isSelectionEmpty()){
					startNewConv();
				}
			}
		});
		btn_select_contact.setBounds(16, 536, 186, 29);
		panel.add(btn_select_contact);
		
		JButton btnImportContacts = new JButton("");
		btnImportContacts.setToolTipText("Import Contacts.");
		btnImportContacts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sortListModel();
			}
		});
		btnImportContacts.setIcon(new ImageIcon(Conversation.class.getResource("/bigsky/gui/user.png")));
		btnImportContacts.setBackground(Color.WHITE);
		btnImportContacts.setBounds(175, 16, 27, 29);
		panel.add(btnImportContacts);
		Global.defaultContactImage = new ImageIcon(this.getClass().getResource("default-profile.jpg"));
		Global.contactThumbnail.setIcon(Global.defaultContactImage);
		Global.contactThumbnail.setBounds(17, 47, 188, 166);
		panel.add(Global.contactThumbnail);
	}

	/**
	 * Bring up the Edit Contact Page
	 */
	private void editContactAction(){
		String selectedValue = (String)Global.list.getSelectedValue();
		if (selectedValue != null){
			int i = findContactInListModel(selectedValue);
			if (i != returnsNull){
				Contact selectedContactCon = Global.contactAList.get(i);
				EditContact editCon = new EditContact(selectedContactCon, i, selectedValue);
				editCon.getFrmEditContact().setVisible(true);
			}
			else Logger.printErr("Error in Edit Contact");
		}
		else{
			JOptionPane.showMessageDialog(null, "Please select a contact to edit.");
		}
	}

	/**
	 * Bring up the New Contact Window
	 */
	private void openNewContactWindow(){
		NewContact newCon = new NewContact();
		newCon.getFrmNewContact().setVisible(true);
	}
	
	/**
	 * Modify the list model to only display items that match the requirements of the searchTerm
	 * @param searchTerm - The text entered into the search bar
	 */
	@SuppressWarnings("unchecked")
	private void searchContact(String searchTerm){
		Global.listModel.removeAllElements();
		if (!searchTerm.equals("")){
			for (int i = 0; i < Global.contactAList.size(); i++){
				Contact con = Global.contactAList.get(i);
				String first = con.getFirstName();
				String last = con.getLastName();
				if (first.toLowerCase().contains(searchTerm.toLowerCase())) {
						addContactToListModel(first, last);
				}
				else if (last.toLowerCase().contains(searchTerm.toLowerCase())) {
					addContactToListModel(first, last);
				}
				else if ((first.toLowerCase() + " " + last.toLowerCase()).contains(searchTerm.toLowerCase())) {
					addContactToListModel(first, last);
				}
			}
		}
		else {
			for (int i = 0; i < Global.contactAList.size()-1; i++){
				Contact con = Global.contactAList.get(i);
				String firstName = con.getFirstName();
				String lastName = con.getLastName();
				addContactToListModel(firstName, lastName);
				sortListModel();
			}
		}
		if (Global.listModel.size() < 12){
			Global.listModel.addElement("Create Contact");
		}
	}
	
	/**
	 * Adds a contact to the global list model
	 * @param firstName - First name of contact to be added
	 * @param lastName - Last name of contact to be added
	 */
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
	 * Finds a contact from the list Model and returns the position of that contact in the ArrayList
	 * @param selectedValue - String of the contact name from the list model
	 * @return the position of that contact in the ArrayList
	 */
	private static int findContactInListModel(String selectedValue){
		for (int i=0;i<Global.contactAList.size();i++){
			Contact con = Global.contactAList.get(i);
			if (con.getFirstName().equals(selectedValue)){
				return i;
			}
			else if (con.getLastName().equals(selectedValue)){
				return i;
			}
			else if ((con.getFirstName() + " " + con.getLastName()).equals(selectedValue)){
				return i;
			}
		}
		//Contact was not found
		return -1;
	}

	public JFrame getFrmBluetext() {
		return frmBluetext;
	}
	
	/**
	 * Sort List Model Alphabetically by last first name
	 */
	@SuppressWarnings("unchecked")
	private void sortListModel(){
		String[] tempList = new String[Global.listModel.size()];
		for (int i=0; i<Global.listModel.size(); i++) {
			tempList[i] = (String)Global.listModel.get(i);
		}
		Global.listModel.removeAllElements();
		Arrays.sort(tempList, 0, tempList.length, String.CASE_INSENSITIVE_ORDER);
		for (int i=0; i<tempList.length;i++){
			Global.listModel.addElement(tempList[i]);
		}
	}
	
	/**
	 * Create a conversation tab
	 * @param contact - Contact on other end of conversation
	 */
	public static void createTab(Contact contact){
		JTextPane textPane = new JTextPane();
		DefaultCaret caret = (DefaultCaret)textPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		textPane.setFont(new Font("Franklin Gothic Medium", Font.PLAIN, Integer.valueOf(TaskBar.savedInfo(Global.conversationFontSize))));
		textPane.setEditable(false);
		textPanes.add(textPane);
		JScrollPane scroll = new JScrollPane(textPane);
		Global.conversationPane.addTab((String)Global.list.getSelectedValue(), null, scroll, null);
		initTabComponent(Global.conversationPane.getTabCount()-1);
		Global.conversationPane.setSelectedIndex(Global.conversationPane.getTabCount()-1);
		offset.add(new Integer(0));
	}
	
	/**
	 * Start a new conversation
	 */
	public static void startNewConv(){
		boolean match = false;
		String selectedValue = (String)Global.list.getSelectedValue();
		int j = findContactInListModel(selectedValue);
		Contact selectedContactCon = Global.contactAList.get(j);

		for(int i = 0; i < currentConvs.size(); i++){
			if(selectedContactCon.getFirstName().equals(currentConvs.get(i).getFirstName()) &&
					selectedContactCon.getPhoneNumber().equals(currentConvs.get(i).getPhoneNumber())){
				match = true;
				break;
			}
		}
		if(!match){
			currentConvs.add(selectedContactCon);
			createTab(selectedContactCon);
			
			rq = new BlueTextRequest(BlueTextRequest.REQUEST.CONTACT_CHAT_HISTORY, selectedContactCon);
			TaskBar.messageHost.sendObject(rq);
			
			setThumbnailPicture(selectedContactCon);	
		}
	}
	
	/**
	 * Gets facebook id from properties file
	 * @param c
	 * @return
	 */
	private static String getFacebookID(Contact c){
		String contactName = null;
		try{
			Properties prop = new Properties();
			contactName = (c.getFirstName()+ '.' + c.getLastName()).replace(' ', '.').toLowerCase();
			prop.load(new FileInputStream(Global.username + ".properties"));
			
			return prop.getProperty(contactName);			
		} catch(Exception e){
			return null;
		}
	}
	
	/**
	 * Set the thumbnail picture for a given Contact c.
	 * The contact image can be:
	 * <LI> from Facebook
	 * <LI> from phone contacts list
	 * <LI> default image
	 * @param c
	 */
	public static void setThumbnailPicture(Contact c)
	{
		if(c == null){
			Global.contactThumbnail.setIcon(Global.defaultContactImage);
			return;
		}
		
		if(Global.contactTOimageIcon.get(c.getPhoneNumber()) != null){
			// If we have the image stored to memory, use that and return
			Global.contactThumbnail.setIcon(Global.contactTOimageIcon.get(c.getPhoneNumber()));
			return;
		}
			
		String facebookID = getFacebookID(c);
		
		// First try to get picture from facebook
		try{
			if(facebookID == null)
				throw new Exception("Unable to find facebook id in username.properties file");
			
			URL url = new URL("http://graph.facebook.com/" + facebookID + "/picture?type=large");
			BufferedImage bi = ImageIO.read(url);
			ImageIcon img = new ImageIcon(bi.getScaledInstance(180, 180, Image.SCALE_SMOOTH));
			Global.contactTOimageIcon.put(c.getPhoneNumber(), img);
			Global.contactThumbnail.setIcon(img);
		}catch(Exception e){
			Global.contactTOimageIcon.put(c.getPhoneNumber(), Global.defaultContactImage);
			
			// If that doesn't work, ask for it from the phone
			// Send a REQUEST for contact's picture
			BlueTextRequest req1 = new BlueTextRequest(REQUEST.CONTACT_PICTURE, c);
			TaskBar.messageHost.sendObject(req1);
		}
	}
	
	/**
	 * Update image with Battery Indicator
	 * @param newPercentage - Percentage of battery remaining. Passed from phone
	 */
	public static void updateBatteryIndicator(int newPercentage){
		if (newPercentage >= 85){
			Global.batteryIndicator.setIcon(new ImageIcon(Conversation.class.getResource("/bigsky/gui/battery_discharging_100.png")));
		}
		else if (newPercentage >=60){
			Global.batteryIndicator.setIcon(new ImageIcon(Conversation.class.getResource("/bigsky/gui/battery_discharging_075.png")));
		}
		else if (newPercentage >= 35){
			Global.batteryIndicator.setIcon(new ImageIcon(Conversation.class.getResource("/bigsky/gui/battery_discharging_050.png")));
		}
		else {
			Global.batteryIndicator.setIcon(new ImageIcon(Conversation.class.getResource("/bigsky/gui/battery_discharging_025.png")));
		}
		Global.battery_remaining = newPercentage;
		String batteryString = Global.battery_remaining.toString() + "%";
		Global.batteryIndicator.setText((batteryString));
	}
	
	/**
	 * updates conversation of Conversation window with text received or sent
	 * @param text message that will update the window
	 * @throws BadLocationException
	 */
	public static void updateConv(TextMessage text) throws BadLocationException{
				
		int current = 0;
		int temp = 0;
		Contact you = null;
		boolean check1 = false;
		boolean check2 = false;
		boolean check3 = false;
		String person1 = text.getSender().getFirstName() + ":";
		
		for(int i = person1.length(); i < 17;i++){
			person1 = person1 + " ";
		}
		if(Global.conversationPane.getTabCount()!=0){
			current = Global.conversationPane.getSelectedIndex();
			// added if check
			if(offset.size() <= current){
				current = offset.size()-1;
				Logger.printErr("Error in updateConv()");
			}
				
			temp = offset.get(current);
		}
		
		//Checks if the user is the sender
		if(!text.getContent().trim().isEmpty() && text.getSender().getPhoneNumber().equalsIgnoreCase(TaskBar.me.getPhoneNumber())){
			if(!TaskBar.doNotSend){
				textPanes.get(current).getDocument().insertString(offset.get(current), person1 + text.getContent() + "\n\n", null);
				temp += (person1 + text.getContent() + "\n\n").length();
				offset.set(current, temp);
			}
			
			if(!TaskBar.doNotSend && RequestManager.sendTexts){
				TaskBar.outGoingInConv.add(text);
			}
			
			if(TaskBar.outGoingInConv.size() != 0){
				for(int i = 0; i < TaskBar.smallChatWindows.size();i++){
					if(TaskBar.outGoingInConv.get(0).getReceiver().getPhoneNumber().equals(TaskBar.smallChatWindows.get(i).getFromContact().getPhoneNumber()) && RequestManager.sendTexts){
						TaskBar.doNotSend = true;
						TaskBar.smallChatWindows.get(i).receivedText(text);
						TaskBar.outGoingInConv.remove(0);
						TaskBar.doNotSend = false;
						check3 = true;
						break;
					}
				}
				if(check3 == false && RequestManager.sendTexts){
					TaskBar.smallChatWindows.add(new SmallChat(text.getSender(), text.getReceiver()));
					TaskBar.updateAddTaskbarSmallChatWindows();
					TaskBar.doNotSend = true;
					
					TaskBar.smallChatWindows.get(current).receivedText(text);
					
					TaskBar.outGoingInConv.remove(0);
					TaskBar.doNotSend = false;
				}
			}
			
			if(!TaskBar.doNotSend && RequestManager.sendTexts){
				TaskBar.messageHost.sendObject(text);
			}
			
			check1 = true;
		}
		else{
			for(int i = 0; i < currentConvs.size();i++){
				if(currentConvs.get(i).getPhoneNumber().equalsIgnoreCase(text.getSender().getPhoneNumber())){
					you = currentConvs.get(i);
					check2 = true;
					current = i;
					break;
				}
			}
		}	
		if(!text.getContent().trim().isEmpty() && check2){
			temp = offset.get(current);
			textPanes.get(current).getDocument().insertString(temp, person1 + text.getContent() + "\n\n", null);
			temp += (person1 + text.getContent() + "\n\n").length();
			offset.set(current, temp);
		}
		else if(!text.getContent().trim().isEmpty() && you == null && !check1){
			JTextPane textPane = new JTextPane();
			DefaultCaret caret = (DefaultCaret)textPane.getCaret();
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
			textPane.setFont(new Font("Franklin Gothic Medium", Font.PLAIN, Integer.valueOf(TaskBar.savedInfo(Global.conversationFontSize))));
			textPane.setEditable(false);
			textPanes.add(textPane);
			JScrollPane scroll = new JScrollPane(textPane);
			Global.conversationPane.addTab(text.getSender().getFirstName() + " " + text.getSender().getLastName(), null, scroll, null);
			initTabComponent(Global.conversationPane.getTabCount()-1);
			Global.conversationPane.setSelectedIndex(Global.conversationPane.getTabCount()-1);
			offset.add(new Integer(0));
			current = offset.size() - 1;
			temp = offset.get(current);
			currentConvs.add(text.getSender());
			textPanes.get(current).getDocument().insertString(temp, person1 + text.getContent() + "\n\n", null);
			temp += (person1 + text.getContent() + "\n\n").length();
			offset.set(current, temp);
		}
	}
	
	/**
	 * Checks(selects) the buttons that are set in the preference file
	 */
	private  void checkButtons(){
		Properties prop = new Properties();	
		try {
			prop.load(new FileInputStream(Global.username +".properties"));
		} catch (Exception e) {
			e.printStackTrace();
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
			e.printStackTrace();
		}
		
		prop.setProperty("save", Global.OFF);
		prop.setProperty(Global.MESSAGEPREVIEW,Global.ON);
		prop.setProperty(Global.NOTIFICATION,Global.ON);
		prop.setProperty(Global.conversationFontSize, "12");	

		try{
			prop.store(new FileOutputStream(Global.username + ".properties"), null);
		}
		catch(Exception e1){
			e1.printStackTrace();
			Logger.printErr("Problem saving default settings in small chat");
		}
		
		updateTabFonts();
	}
	
	/**
	 * This updates the textpanes fonts immediately
	 */
	private void updateTabFonts(){
		int tabCount  = textPanes.size();
		for(int i = 0 ; i < tabCount ; i++){
			textPanes.get(i).setFont(new Font("Franklin Gothic Medium", Font.PLAIN, Integer.valueOf(TaskBar.savedInfo(Global.conversationFontSize))));
		}
	}
	
	/**
	 * initializes the close button on a tab at index i
	 * @param i index of tab
	 */
	public static void initTabComponent(int i) {
		Global.conversationPane.setTabComponentAt(i, new ButtonTabComponent(Global.conversationPane));
		Global.conversationPane.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent arg0) {
				int j = Global.conversationPane.getSelectedIndex();
				if(j >= currentConvs.size())
					j = currentConvs.size()-1;
				if(j < 0){
					setThumbnailPicture(null);
				}
				else{
					setThumbnailPicture(currentConvs.get(j));
				}
			}
		});
	}  
	
	/**
	 * removes tab from conversationPane
	 * @param i index of tab being removed
	 */
	public static void removeTab(int i){		
		TaskBar.smallChatWindows.get(i).getFrmBluetext().dispose();
    	Conversation.offset.remove(i);
    	Conversation.textPanes.remove(i);
		Conversation.currentConvs.remove(i);
		String name = TaskBar.smallChatWindows.get(i).getFromContact().getFirstName() + " " + TaskBar.smallChatWindows.get(i).getFromContact().getLastName();
		TaskBar.smallChatWindows.remove(i);
		int menuitemlength = TaskBar.menuItemArrays.size();
		for(int j = 0; j < menuitemlength; j++){	
			String array = TaskBar.menuItemArrays.get(j).getLabel();
			if(array.equalsIgnoreCase(name)){
				TaskBar.smallChat.remove(TaskBar.menuItemArrays.get(j));
				TaskBar.menuItemArrays.remove(j);
				break;
			}
		}
	}
	
	/**
	 * All of these below update help update
	 * smallchats settings when they are 
	 * changed in the SmallChat window
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
}
