package bigsky;

import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.*;
import bigsky.gui.*;
import bigsky.messaging.MessageHost;
import bigsky.messaging.RequestManager;

public class TaskBar
{
	public static Queue<TextMessage> myTextQueue = new Queue<TextMessage>();
	public static ArrayList<TextMessage> myTextArray = new ArrayList<TextMessage>();
	public static TrayIcon notification = new TrayIcon(new ImageIcon(TaskBar.class.getResource("BlueText.gif"), "tray icon").getImage());
	public static ArrayList<SmallChat> smallChatWindows = new ArrayList<SmallChat>();
	public static Contact me = new Contact("Me", "", "1234567890","");
	public static final TrayIcon trayIcon = createTrayIconImage();
	private static final SystemTray tray = SystemTray.getSystemTray();
	public static MessageHost messageHost = null;
	public static ConcurrentLinkedQueue<Contact> incomingContactQueue = new ConcurrentLinkedQueue<Contact>(); 
	public static ConcurrentLinkedQueue<BlueTextResponse> responseQueue = new ConcurrentLinkedQueue<BlueTextResponse>();
	public static RequestManager textManager = null;
	public static Conversation convo;
	public static ArrayList<TextMessage> outGoingInConv = new ArrayList<TextMessage>();
	public static ArrayList<TextMessage> outGoingInSmall = new ArrayList<TextMessage>();
	public static boolean doNotSend = false;
	public static Menu smallChat;
	public static ArrayList<MenuItem> menuItemArrays = new ArrayList<MenuItem>();
	public static HashMap<ActionListener, MenuItem> menuItemTOactionListener = new HashMap<ActionListener, MenuItem>();

    public static void main(String[] args) {
    	
        try {
        	UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        
        new Logger();
        
        // Checks to see if the user setting is to save username and password
        if(savedInfo(Global.save, Global.ON)){
        	TaskBar.putIconInSystemTray();
        	me = Login.setContactMe();
        	try {
				automaticIP();
			} catch (Exception e) {}
			if(messageHost==null){
	   	   		messageHost = new MessageHost();
	   	   		messageHost.start();
	   	   		textManager = new RequestManager();
	   	   		textManager.start();
	        }
        }
        
        else{
        	Login login = new Login();
           	login.setVisible(true);
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	initialize(); 
            }
        });
    }

    private static void initialize() {
        if (!SystemTray.isSupported()) {
            Logger.printOut("SystemTray is not supported");
            return;
        }

        final PopupMenu menu = new PopupMenu();

        //shows full image in taskbar
        trayIcon.setImageAutoSize(true);

        //  menu items
        MenuItem conversation = new MenuItem("Open BlueText");
        smallChat = new Menu("Quick Chat");
        MenuItem logout = new MenuItem("Log out");
        MenuItem exitItem = new MenuItem("Exit");

        //Adding  menu items
        menu.add(conversation);
        menu.add(smallChat);
        menu.add(logout);
        menu.add(exitItem);
        trayIcon.setPopupMenu(menu);

        conversation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	convo.getFrmBluetext().setVisible(true);
            }
        });
        
        logout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
					logout();
            }
        });

        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tray.remove(trayIcon);
                System.exit(0);
            }
        });
    }
    /**
     * Puts the tray icon into the system tray
     */
    public static void putIconInSystemTray(){
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            Logger.printErr("TrayIcon could not be added.");
        }
    }
    
    /**
     * Obtain system tray icon
     * @return tray icon
     */
    protected static TrayIcon createTrayIconImage() {
       TrayIcon tray;
   	   URL imageURL = TaskBar.class.getResource("BlueText.gif");
       Image icon = new ImageIcon(imageURL, "tray icon").getImage();

        if (imageURL == null) {
            Logger.printErr("Resource not found: " + "BlueText.gif");
            return null;
        } else {
        	tray = new TrayIcon(icon);
            return tray;
        }
    }
    
    /**
     *Compares the value of a compare with the saved value
     * @return true if value of compare is the same as saved value
     */
    public static boolean savedInfo(String property, String compare ){

		Properties prop = new Properties();

		try {
			prop.load(new FileInputStream(lastLoggedIn() +".properties"));
		} catch (Exception e) {
			Logger.printOut("No previous login file located.");
		}

		if(compare.equals(prop.getProperty(property))){
			Global.username = lastLoggedIn();
			return true;
		}
		return false;
	}
    
    /**
    *
    * @return the value of what is inside the property given
    */
   public static String savedInfo(String property){

		Properties prop = new Properties();

		try {
			prop.load(new FileInputStream(lastLoggedIn() +".properties"));
		} catch (Exception e) {
			Logger.printOut("No previous login file located.");
		}

		return prop.getProperty(property);
	}
    
    /**
     * Method to load system properties to see which user was logged in last
     * @return String lastloggedin
     */
    public static String lastLoggedIn(){
		
		Properties prop = new Properties();

		try {
			prop.load(new FileInputStream("system.properties"));
		} catch (Exception e) {
			Logger.printOut("No previous system properties located, using defaults.");
		}
		
		return (String) prop.get("lastLoggedIn");
	}
    
    /**
     * Logout of current session.Closes connection between phone and computer
     * Brings to login screen
     * @throws Exception
     */
    public static void logout(){
    	
    	Frame j = new Frame();
    	@SuppressWarnings("static-access")
    	Frame[] frames = j.getFrames();
    	for(int i = 0; i < frames.length; i ++){
    		frames[i].dispose();
    	}
    	
    	tray.remove(trayIcon);
    	
    	if(TaskBar.messageHost != null)
    		TaskBar.messageHost.closeHost(false);
    	    	
    	reLogin();
    	
    	Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(lastLoggedIn() +".properties"));
		} catch (Exception e) {
			Logger.printErr("loading file problem.");
		}		
		prop.setProperty("save", Global.OFF);
		try {
			prop.store(new FileOutputStream(lastLoggedIn() +".properties"),null);
		} catch (Exception e) {
			Logger.printErr("storing file problem");
		}
		
		Logger.closeLogger();
    }
    
    /**
     * After logout this is called for the relogin screen to load 
     */
    public static void reLogin(){
    	
		Login login = new Login();
       	login.setVisible(true);
    }
    
    /**
     * Puts IP address in sql database when user has automatic login
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws UnknownHostException
     */
    public static void automaticIP(){
    	try {
		Class.forName("com.mysql.jdbc.Driver");

		Connection con = DriverManager.getConnection(Global.DATABASE_URL, Global.DATABASE_USERNAME, Global.DATABASE_PASSWORD);
		Statement stmt = con.createStatement();
		String iP =InetAddress.getLocalHost().getHostAddress();	
		stmt.executeUpdate("UPDATE " + Global.DATABASE_TABLENAME + " SET IP_Computer='" + iP + "' WHERE phoneNumber='" + lastLoggedIn() + "';");
    	} catch (Exception e) {
    		Logger.printErr("Automatic login fail\n" + e.getMessage());
    	}
		
    }
    
    /**
     * Adds menus to the taksbar small chat menu when a new tab is opened
     */
    public static void updateAddTaskbarSmallChatWindows(){
		int i  = smallChatWindows.size() - 1;
		MenuItem curMenuItem = new MenuItem(smallChatWindows.get(i).getFromContact().getFirstName() +  " " + smallChatWindows.get(i).getFromContact().getLastName());
		menuItemArrays.add(curMenuItem);
		ActionListener curListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					String name = e.getActionCommand();
					for (int j = 0; j<smallChatWindows.size(); j++){
						Contact from = smallChatWindows.get(j).getFromContact();
						String first = from.getFirstName();
						String last = from.getLastName();
						if (first.equals(name) || last.equals(name) || (first + " " + last).equals(name)){
							smallChatWindows.get(j).getFrmBluetext().setVisible(true);
						}
					}
					
			}
		};
		
		menuItemTOactionListener.put(curListener, curMenuItem);
		curMenuItem.addActionListener(curListener);
		smallChat.add(menuItemArrays.get(i));
	}
}

class Queue<T>{
	protected LinkedList<T> list;

	public Queue(){
		list = new LinkedList<T>();
	}

	public void add(T element){
		list.add(element);
	}

	public T removeFirst(){
		return list.removeFirst();
	}

	public boolean isEmpty(){
		if(list.isEmpty()){
			return true;
		}
		else
			return false;
	}
}
