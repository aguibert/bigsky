package bigsky.gui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import bigsky.BlueTextRequest;
import bigsky.Contact;
import bigsky.Global;
import bigsky.TaskBar;

/**
 * A New Contact Window - Creates a new contact
 * @author Travis Reed
 *
 */
public class NewContact {

	private JFrame frmNewContact;
	private JLabel lblFirstName;
	private JLabel lblLastName;
	private JTextField txtFirstName;
	private JTextField txtLastName;
	private JTextField txtPhone;
	private JTextField txtSecondPhone;
	private JLabel lblPhone;
	private JLabel lblSecondPhone;
	private JButton btnSubmit;
	private JButton btnCancel;

	/**
	 * Create the new contact window
	 */
	public NewContact() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmNewContact = new JFrame();
		frmNewContact.setTitle("New Contact");
        if (!System.getProperty("os.name").contains("Mac")){
			frmNewContact.setIconImage(Toolkit.getDefaultToolkit().getImage(NewContact.class.getResource("/bigsky/BlueText.gif")));
		}
		frmNewContact.setSize(355, 301);
		frmNewContact.getContentPane().setLayout(null);
		frmNewContact.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmNewContact.setLocationRelativeTo(null);
		
		lblFirstName = new JLabel("First Name:");
		lblFirstName.setBounds(33, 35, 86, 16);
		frmNewContact.getContentPane().add(lblFirstName);
		
		lblLastName = new JLabel("Last Name:");
		lblLastName.setBounds(33, 80, 86, 16);
		frmNewContact.getContentPane().add(lblLastName);
		
		txtFirstName = new JTextField();
		txtFirstName.setBounds(136, 36, 134, 28);
		frmNewContact.getContentPane().add(txtFirstName);
		txtFirstName.setColumns(10);
		
		txtLastName = new JTextField();
		txtLastName.setColumns(10);
		txtLastName.setBounds(136, 81, 134, 28);
		frmNewContact.getContentPane().add(txtLastName);
		
		txtPhone = new JTextField();
		txtPhone.setColumns(10);
		txtPhone.setBounds(136, 122, 134, 28);
		frmNewContact.getContentPane().add(txtPhone);
		
		txtSecondPhone = new JTextField();
		txtSecondPhone.setColumns(10);
		txtSecondPhone.setBounds(136, 171, 134, 28);
		frmNewContact.getContentPane().add(txtSecondPhone);
		
		lblPhone = new JLabel("Phone:");
		lblPhone.setBounds(33, 128, 86, 16);
		frmNewContact.getContentPane().add(lblPhone);
		
		lblSecondPhone = new JLabel("Second Phone:");
		lblSecondPhone.setBounds(33, 177, 106, 16);
		frmNewContact.getContentPane().add(lblSecondPhone);
		
		btnSubmit = new JButton("Submit");
		btnSubmit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String first = txtFirstName.getText();
				String last = txtLastName.getText();
				String phone = txtPhone.getText();
				String secondPhone = txtSecondPhone.getText();
				Contact contactToAdd = validateContact(first, last, phone, secondPhone);
				if (contactToAdd != null){
					Global.contactAList.add(contactToAdd);
					addContactToListModel(first, last);
					frmNewContact.setVisible(false);
				}	
			}
		});
		
		frmNewContact.getRootPane().setDefaultButton(btnSubmit);
		btnSubmit.setBounds(189, 230, 117, 29);
		frmNewContact.getContentPane().add(btnSubmit);
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmNewContact.setVisible(false);
			}
		});
		
		btnCancel.setBounds(33, 230, 117, 29);
		frmNewContact.getContentPane().add(btnCancel);
	}
	
	public JFrame getFrmNewContact(){
		return frmNewContact;
	}
	
	private Contact validateContact(String firstName, String lastName, String phone, String secondPhone){
		if (firstName.equals("")) {
			if (lastName.equals("")) {
				if (phone.equals("")) {
					JOptionPane.showMessageDialog(null, "Please Enter a First Name, Last Name, or Phone Number");
					return null;
				}
				else {
					firstName = phone;
				}
			}
		}
		
		if (phone.equals("")){
			if (secondPhone.equals("")){
				JOptionPane.showMessageDialog(null, "Please Enter a Phone Number");
				return null;
			}
			else {
				phone = secondPhone;
				secondPhone = "";
			}
		}
		else {
			phone = phone.replaceAll("\\D+","");
			if (phone.length() != 10){
				JOptionPane.showMessageDialog(null, "Please Enter all Phone Numbers with 10 digits");
				return null;
			}
		}
		
		if (!secondPhone.equals("")){
			secondPhone = secondPhone.replaceAll("\\D+","");
			if (secondPhone.length() != 10){
				JOptionPane.showMessageDialog(null, "Please Enter all Phone Numbers with 10 digits");
				return null;
			}
		}
		
		for (int i = 0; i<Global.contactAList.size();i++){
			Contact con = Global.contactAList.get(i);
			if (con.getFirstName().equals(firstName)){
				if (con.getLastName().equals(lastName)){
					JOptionPane.showMessageDialog(null, "This name already exists. Please choose a new name");
					return null;
				}
			}
			if (con.getPhoneNumber().equals(phone) || con.getSecondPhone().equals(phone)){
				JOptionPane.showMessageDialog(null, "This phone number already exists on a different contact");
				return null;
			}
			if (!secondPhone.equals("") && (con.getPhoneNumber().equals(secondPhone) || con.getSecondPhone().equals(secondPhone))){
				JOptionPane.showMessageDialog(null, "This secondary phone number already exists on a different contact");
				return null;
			}
		}
		Contact c = new Contact(firstName, lastName, phone, secondPhone);
		
		// Send the contact to the phone so it can be added to the phone's contact list
		TaskBar.messageHost.sendObject(new BlueTextRequest(BlueTextRequest.REQUEST.SUBMIT_NEW_CONTACT, c));
		
		return c;
	}
	
	private boolean addContactToListModel(String firstName, String lastName){
		String newEntry;
		if (!firstName.equals("")){
			if (!lastName.equals("")){
				newEntry = firstName + " " + lastName;
			}
			else {
				newEntry = firstName;
			}
			int j = Global.listModel.size()/2;
			j = getNewPositionBasedOnStringComparision(j, newEntry);
			Global.listModel.add(j, newEntry);
		}
		else if (!lastName.equals("")){
			newEntry = lastName;
			int j = Global.listModel.size()/2;
			j = getNewPositionBasedOnStringComparision(j, newEntry);
			Global.listModel.add(j, newEntry);
			return true;
		}
		return false;
	}
	
	private int getNewPositionBasedOnStringComparision(int j , String newEntry){
		String testEntry = newEntry.toLowerCase();
		String listEntry = (String)Global.listModel.get(j);
		while (testEntry.compareTo(listEntry.toLowerCase()) < 0 & j!=0){
			j = j /2;
			listEntry = (String)Global.listModel.get(j);
		}
		while (testEntry.compareTo(listEntry.toLowerCase()) > 0 & j < Global.listModel.size()-1){
			j++;
			listEntry = (String)Global.listModel.get(j);
		}
		return j;
	}
}
