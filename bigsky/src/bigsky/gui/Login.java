package bigsky.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Inet4Address;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import bigsky.Contact;
import bigsky.Global;
import bigsky.Logger;
import bigsky.TaskBar;
import bigsky.messaging.MessageHost;
import bigsky.messaging.RequestManager;

@SuppressWarnings("serial")
public class Login extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JPasswordField passwordField_1;
	private JLabel promptRegister;
	private JLabel wrongInfo;
	private JRadioButton saveInfo;
	private boolean hit = false;

	/**
	 * Create the frame.
	 */
	public Login() {
		new Logger();
		
        if (!System.getProperty("os.name").contains("Mac")){
        	setIconImage(Toolkit.getDefaultToolkit().getImage(Login.class.getResource("/bigsky/BlueText.gif")));
        }
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
        JLabel image = new JLabel();
        contentPane = (JPanel) getContentPane();
        setSize(new Dimension(576, 320));
        ImageIcon icon = new ImageIcon(this.getClass().getResource("Login_Image.png"));
        image.setIcon(icon);       
        contentPane.add(image, java.awt.BorderLayout.CENTER);
        this.setLocationRelativeTo(null);

		textField = new JTextField();
		textField.setForeground(Color.WHITE);
		textField.setBackground(Color.GRAY);
		textField.setBounds(220, 130, 165, 28);
		image.add(textField);
		
		passwordField_1 = new JPasswordField();
		passwordField_1.setForeground(Color.WHITE);
		passwordField_1.setBackground(Color.GRAY);
		passwordField_1.setBounds(220, 180, 165, 28);
		image.add(passwordField_1);
		
		JLabel lblPhoneNumber = new JLabel("Phone Number");
		lblPhoneNumber.setForeground(Color.WHITE);
		lblPhoneNumber.setBounds(126, 135, 98, 16);
		image.add(lblPhoneNumber);
		
		JLabel lblPassword = new JLabel("Password");
		lblPassword.setBounds(126, 185, 98, 16);
		lblPassword.setForeground(Color.WHITE);
		image.add(lblPassword);
		
		JButton login = new JButton("Login");
		login.setBounds(200, 258, 90, 29);
		image.add(login);
		contentPane.getRootPane().setDefaultButton(login);
		
		JButton register = new JButton("Register");
		register.setBounds(315, 258, 90, 29);
		image.add(register);
		
		wrongInfo = new JLabel("Wrong username or password");
		wrongInfo.setForeground(Color.RED);
		wrongInfo.setBounds(236, 233, 215, 14);
		image.add(wrongInfo);
		wrongInfo.setVisible(false);
		
		promptRegister = new JLabel("Would you like to register?");
		promptRegister.setForeground(Color.RED);
		promptRegister.setBounds(259, 233, 192, 14);
		image.add(promptRegister);
		promptRegister.setVisible(false);
		
		saveInfo = new JRadioButton("Save Username/Password");
		saveInfo.setBackground(Color.WHITE);
		saveInfo.setForeground(Color.WHITE);
		saveInfo.setBounds(220, 213, 212, 23);
		image.add(saveInfo);
		saveInfo.setOpaque(false);

		login.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
            	try {
					if(login()){
						dispose();
						TaskBar.putIconInSystemTray();
						if(TaskBar.messageHost==null){   
				   	   		TaskBar.messageHost = new MessageHost();
				   	   		TaskBar.messageHost.start();
				   	   		TaskBar.textManager = new RequestManager();
				   	   		TaskBar.textManager.start();
				        }			
						LoginInfo();
						systemPrefs();
						if(hit){
							saveInProp(getUsername(), "save", Global.ON);
						}
					}
					else{
						throw new Exception();
					}
				} catch (Exception e1) {
					Logger.printOut("Login checks fail");}			
            }
        });
		
		register.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	dispose();
            	Register reg = new Register();
            	reg.setVisible(true);
            }
        });	
		
		//save password and username button
		saveInfo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	  
            	hit = true;
            }
        });	
	}
	
	/**
	 * gets the password typed in password field
	 * @param Jpass
	 * @return returns password field as a string
	 */
	private String getPassword(JPasswordField pass){
		String password = "";
		char [] word = pass.getPassword();
		for(int i = 0; i < word.length; i++){
			password = password + word[i];
		}
		return password;	
	}
	
	/**
	 * retrieves string from textfield of login screen
	 * @return string from textfield
	 */
	private String getUsername(){
		String user = textField.getText();
		//takes out all not  digits
		user = user.replaceAll("\\D+","");
		Global.username = user.trim();
		return  Global.username;	
	}
	
	
	//returns row number of users database row
	public boolean login(){
		try{
		Class.forName("com.mysql.jdbc.Driver");
		Connection con = DriverManager.getConnection(Global.DATABASE_URL, Global.DATABASE_USERNAME, Global.DATABASE_PASSWORD);
		Statement stmt = con.createStatement();
		String iP =Inet4Address.getLocalHost().getHostAddress();		
		
		ResultSet rs = con.createStatement().executeQuery("select * from " + Global.DATABASE_TABLENAME + " where phoneNumber='" + getUsername() + "'");
	
		if(rs.next() == false){
			promptRegister.setVisible(true);
			rs.close();		
			con.close();
			return false;
		}
		if(rs.getString("password") == null){
			promptRegister.setVisible(true);
			rs.close();		
			con.close();
			return false;			
		}
		else if(!getUsername().equals(rs.getString("phonenumber")) || !getPassword(passwordField_1).equals(rs.getString("password"))){
			wrongInfo.setVisible(true);
			rs.close();		
			con.close();
			return false;
		}
		else if(!getUsername().equals(rs.getString("phonenumber")) && !getPassword(passwordField_1).equals(rs.getString("password"))){
			promptRegister.setVisible(true);
			rs.close();		
			con.close();
			return false;
		}

		TaskBar.me = new Contact(rs.getString("firstName"),rs.getString("lastName"), rs.getString("phoneNumber"),null);

		stmt.executeUpdate("UPDATE " + Global.DATABASE_TABLENAME + " SET IP_Computer='" + iP + "' WHERE phoneNumber='" + getUsername() + "';");
		
		rs.close();		
		con.close();
		}catch(Exception e){
			Logger.printErr("Login fail" + e.getMessage());
		}
		
		return true;
	}
	
	/**
	 * initializes the preference file
	 */
	public void LoginInfo(){
		Properties prop = new Properties();
		
		try {
			prop.load(new FileInputStream(getUsername() +".properties"));
		}catch (Exception e) {
			prop.setProperty("username", getUsername());
			prop.setProperty("password", getPassword(passwordField_1));
			prop.setProperty("save", Global.OFF);
			prop.setProperty(Global.MESSAGEPREVIEW,Global.ON);
			prop.setProperty(Global.NOTIFICATION,Global.ON);
			prop.setProperty(Global.smallChatFontSize, "12");
			prop.setProperty(Global.conversationFontSize, "12");
			
			try {
				prop.store(new FileOutputStream(getUsername() + ".properties"),null);
				
			} catch (Exception e1) {
				Logger.printErr("file load problem.");
			}
		}
	}
	
	/**
	 * Create Contact for phone
	 * @return me contact
	 */
	 public static Contact setContactMe(){
	    	Contact me = null;
    	try{
	    	Class.forName("com.mysql.jdbc.Driver");
	    	Connection con = DriverManager.getConnection(Global.DATABASE_URL, Global.DATABASE_USERNAME, Global.DATABASE_PASSWORD);
			
			ResultSet rs = con.createStatement().executeQuery("select * from " + Global.DATABASE_TABLENAME + " where phoneNumber='" + Global.username + "'");
	    	
			if(rs.next()){
				me = new Contact(rs.getString("firstName"),rs.getString("lastName"), rs.getString("phoneNumber"),null);
			}
			
			rs.close();		
			con.close();
		}catch(Exception e){
			Logger.printErr("Login fail" + e.getMessage());
		}
		
    	return me;
    }

	/**
	 * Generic method to store items in users preference file
	 * @param user
	 * @param property
	 * @param value
	 */
	public static void saveInProp(String user, String property, String value){
		
		Properties prop = new Properties();

		try {
			prop.load(new FileInputStream(user +".properties"));
		} catch (Exception e) {
			Logger.printErr("File load problem.");
		}
		
		prop.setProperty(property, value);
	
		try {
			prop.store(new FileOutputStream(user+".properties"),null);
		} catch (Exception e) {
			Logger.printErr("file store problem.");
		}
		
	}
	
	/**
	 * Sets up the system preference file
	 */
	public void systemPrefs(){
		
		Properties prop = new Properties();	
		
		try {
			prop.load(new FileInputStream("system.properties"));
			
		} catch (Exception e) {}
		
		prop.setProperty("lastLoggedIn", getUsername());
		try{
			prop.store(new FileOutputStream("system.properties"), null);
		}catch(Exception e1){
			Logger.printErr("System store file problem");				
		}
	}
}
