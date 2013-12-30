package bigsky.gui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;

import bigsky.Global;
import bigsky.Logger;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import javax.swing.JLabel;

/**
 * Popup to allow access to facebook pictures
 * @author Andrew Guibert, Andrew Hartman, Travis Reed
 *
 */
public class PopUp_FacebookContacts extends JFrame{
	private static final long serialVersionUID = -5289641047430720306L;
	private JTextField textField;
	private JFrame thisFrame;
	
	public PopUp_FacebookContacts(){
		getContentPane().setFont(new Font("Dialog", Font.PLAIN, 16));
		thisFrame = this;
        if (!System.getProperty("os.name").contains("Mac")){
			setIconImage(Toolkit.getDefaultToolkit().getImage(Login.class.getResource("/bigsky/BlueText.gif")));
		}
		setResizable(false);
		setSize(new Dimension(481, 346));
		this.setLocationRelativeTo(null);
		getContentPane().setLayout(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		// Open browser listener on the "Open Browser" button
		JButton btnOpenBrowser = new JButton("Open Browser");
		btnOpenBrowser.setBounds(154, 71, 121, 23);
		getContentPane().add(btnOpenBrowser);
		btnOpenBrowser.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					java.awt.Desktop.getDesktop().browse(new URI("https://developers.facebook.com/tools/explorer?method=GET&path=me%2Ffriends"));
				} catch (Exception e1) {
					Logger.printErr("Unable to open web page, please go to this website: https://developers.facebook.com/tools/explorer?method=GET&path=me%2Ffriends");
				}
			}
		});
		
		textField = new JTextField();
		textField.setBounds(62, 235, 301, 29);
		getContentPane().add(textField);
		textField.setColumns(10);
		
		// Close and dispose the window
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setBounds(10, 286, 89, 23);
		getContentPane().add(btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				thisFrame.setVisible(false);
				thisFrame.dispose();
			}
		});
		
		JButton btnDone = new JButton("Done");
		btnDone.setBounds(369, 286, 89, 23);
		getContentPane().add(btnDone);
		
		JLabel lblNewLabel = new JLabel("2 : Click \"Get Access Token\"");
		lblNewLabel.setFont(new Font("Franklin Gothic Medium", Font.PLAIN, 16));
		lblNewLabel.setBounds(62, 106, 301, 38);
		getContentPane().add(lblNewLabel);
		
		JLabel lblSelectuserfriends = new JLabel("3: Select \"user_friends\".");
		lblSelectuserfriends.setFont(new Font("Franklin Gothic Medium", Font.PLAIN, 16));
		lblSelectuserfriends.setBounds(62, 142, 301, 38);
		getContentPane().add(lblSelectuserfriends);
		
		JLabel lblPaseAccess = new JLabel("4: Paste access token below:");
		lblPaseAccess.setFont(new Font("Franklin Gothic Medium", Font.PLAIN, 16));
		lblPaseAccess.setBounds(62, 184, 258, 16);
		getContentPane().add(lblPaseAccess);
		
		JLabel lblClickButton = new JLabel("1: Click button to open web browser.");
		lblClickButton.setFont(new Font("Franklin Gothic Medium", Font.PLAIN, 16));
		lblClickButton.setBounds(62, 23, 301, 16);
		getContentPane().add(lblClickButton);
		
		// "Done" button action listener
		btnDone.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String accessToken = textField.getText();
				if(accessToken == null || accessToken.length() < 10){
					Logger.printOut("Please paste access token above.");
				}
				Global.ACCESS_TOKEN = accessToken;
				thisFrame.setVisible(false);
				setFacebookContacts(Global.ACCESS_TOKEN);
			}			
		});		
		
		setVisible(true);
	}
	
	private void setFacebookContacts(String accessString)
	{
		String urlString = "https://graph.facebook.com/me/friends?access_token=" + accessString;
		
		try{
			// Read in facebook contacts using access token
			URL url = new URL(urlString);
	        URLConnection cnt = url.openConnection();
	        BufferedReader br = new BufferedReader(new InputStreamReader(cnt.getInputStream()));
	        String content = br.readLine();
	        br.close();
	        
	        // Parse out the 1 line contacts list to usable properties
	        content = content.replace("{", "")
	        		.replace("\"name\"", "")
		            .replace("\"id\"", "")
		            .replace("},", "")
		            .replace("}", "")
		            .replace("]", "")
	        		.replace(".", " ")
	        		.replace(" ",".")
		            .replace("\"", "")
		            .replace(",:", ":");
	        
	        String[] tokens = content.split("[:\\,]");        
	        Properties prop = new Properties();
	        prop.load(new FileInputStream(Global.username + ".properties"));
	        
	        int i;
	        for(i = 2; i < tokens.length-4; i +=2){
	        	if(prop.getProperty(tokens[i]) == null){
	        		prop.setProperty(tokens[i].toLowerCase(), tokens[i+1]);
	        	}
	        }            	       
	        
	        prop.store(new FileOutputStream(Global.username + ".properties"), null);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
