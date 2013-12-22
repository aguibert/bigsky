package bigsky.gui;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;

import bigsky.Global;
import bigsky.Logger;
import bigsky.TaskBar;
import bigsky.TextMessage;
import java.awt.Font;
import java.util.ArrayList;

public class Notification {

	private JDialog frame;
	private TextMessage messager;
	private int chatWinNum;
	private float fade = 1.0F;
	private final Timer timer1 = new Timer(50, null);
	private final Timer timer2 = new Timer(10, null);
	private float positionY;
	private static int totalWindows;
	private int windowNum = 0;
	private GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	public static ArrayList<Notification> openNotifications = new ArrayList<Notification>();

	/**
	 * Create the application.
	 */
	public Notification(TextMessage messager){
		this.messager = messager;
		chatWinNum = 0;
		
		totalWindows++;
		windowNum = totalWindows;
		positionY = gd.getDisplayMode().getHeight();
		
		try {
			initialize();
		} catch (BadLocationException e) {
			Logger.printErr("ERROR in Notification Initialize()");
			e.printStackTrace();
		}
		
		frame.setVisible(true);
		 
		 timer1.addActionListener(new ActionListener() {
			 @Override
		     public void actionPerformed(ActionEvent e) {
		        	
				 if (fade < 0.0126F){
					 timer2.stop();
					 timer1.stop();
					 frame.dispose();
					 totalWindows--;
					 for(int i = windowNum - 1; i < openNotifications.size();i++){
						 openNotifications.get(i).windowNum--;
					 }
					 openNotifications.remove(windowNum);
		         }
		         fade = fade - 0.0125F;
		     }
		 });
		 
		 timer1.setInitialDelay(3000);
		 timer1.start();
		 this.animate();
		 openNotifications.add(this);
	}

	/**
	 * Initialize the contents of the frame.
	 * @throws BadLocationException 
	 */
	private void initialize() throws BadLocationException {
		for(int i=0; i < TaskBar.smallChatWindows.size(); i++){
			if(TaskBar.myTextArray.get(0).getSender().getPhoneNumber().equals(TaskBar.smallChatWindows.get(i).getFromContact().getPhoneNumber())){
				chatWinNum = i;
			}
		}
		
		frame = new JDialog();
		frame.setName("New Message");
		frame.setResizable(false);
		frame.setBounds(gd.getDisplayMode().getWidth() - 260, gd.getDisplayMode().getHeight(), 257, 178);
		frame.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		frame.setUndecorated(true);
		frame.getContentPane().setLayout(null);
		frame.setTitle("New Message");
		
		frame.addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(WindowEvent arg0) {
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				totalWindows--;
				for(int i = windowNum - 1; i < openNotifications.size();i++){
					openNotifications.get(i).windowNum--;
				}
				openNotifications.remove(windowNum);
				frame.dispose();
				timer1.restart();
				timer1.stop();
				timer2.stop();
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
			}

			@Override
			public void windowIconified(WindowEvent arg0) {
			}

			@Override
			public void windowOpened(WindowEvent arg0) {
			}
		});
		
		JButton btnQuickChat = new JButton("Quick Chat");
		btnQuickChat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				TaskBar.smallChatWindows.get(chatWinNum).getFrmBluetext().setVisible(true);
				totalWindows--;
				for(int i = windowNum - 1; i < openNotifications.size();i++){
					openNotifications.get(i).windowNum--;
				}
				openNotifications.remove(windowNum);
				frame.dispose();
				timer1.restart();
				timer1.stop();
				timer2.stop();
			}
		});
		btnQuickChat.setBounds(0, 114, 125, 36);
		frame.getContentPane().add(btnQuickChat);
		
		JButton btnMainWindow = new JButton("BlueText");
		btnMainWindow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				TaskBar.convo.getFrmBluetext().setVisible(true);
				Global.conversationPane.setSelectedIndex(chatWinNum);
				totalWindows--;
				for(int i = windowNum - 1; i < openNotifications.size();i++){
					openNotifications.get(i).windowNum--;
				}
				openNotifications.remove(windowNum);
				frame.dispose();
				timer1.restart();
				timer1.stop();
				timer2.stop();
			}
		});
		btnMainWindow.setBounds(125, 114, 125, 36);
		frame.getContentPane().add(btnMainWindow);
		
		JTextPane messagerContent = new JTextPane();
		messagerContent.setEditable(false);
		messagerContent.setBounds(0, 34, 250, 82);
		frame.getContentPane().add(messagerContent);
		if(TaskBar.savedInfo(Global.MESSAGEPREVIEW, Global.ON)){
			messagerContent.getDocument().insertString(0,"- " + messager.getContent(), null);
		}
		else{
			messagerContent.getDocument().insertString(0,"- NEW MESSAGE", null);
		}
		JTextPane messagerName = new JTextPane();
		messagerName.setFont(new Font("Tahoma", Font.BOLD, 16));
		messagerName.setEditable(false);
		messagerName.setBounds(0, 0, 250, 49);
		frame.getContentPane().add(messagerName);
		messagerName.getDocument().insertString(0, messager.getSender().getFirstName() + " " + messager.getSender().getLastName() + ":", null);
		
		frame.addMouseMotionListener(new MouseMotionListener() {
			 public void mouseMoved(MouseEvent e) {
				 fade = 1.0f;
				 timer1.stop();
				 timer1.setInitialDelay(3000);
				 timer1.start();
			 }

			@Override
			public void mouseDragged(MouseEvent arg0) {
			}
		});
	}
	
	/**
	 * starts the animation of notification pop-ups will cause all gaps to be closed once a notification
	 * has expired.  Notifications will remain as close to bottom of screen as possible.
	 */
	private void animate(){
		 timer2.addActionListener(new ActionListener() {
			 @Override
		     public void actionPerformed(ActionEvent e) {
				 if ((positionY > ((gd.getDisplayMode().getHeight() - 50) - (180 * windowNum)))){
					 positionY = positionY - 10;
					 openNotifications.get(windowNum - 1).frame.setLocation(gd.getDisplayMode().getWidth() - 260, (int)positionY);
		         }
				 else if(windowNum != 0 && ((positionY + 10) < ((gd.getDisplayMode().getHeight() - 50) - (180 * windowNum)))){
					 positionY = positionY + 10;
					 openNotifications.get(windowNum - 1).frame.setLocation(gd.getDisplayMode().getWidth() - 260, (int)positionY);
				 }
		     }
		 });
		 timer2.start();
	}
}
