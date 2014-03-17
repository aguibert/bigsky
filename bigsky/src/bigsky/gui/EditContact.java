package bigsky.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import bigsky.Contact;
import bigsky.Global;

import java.awt.Toolkit;

/**
 * Edit Contact Window - allows contact information to be updated
 * @author Travis Reed
 *
 */
public class EditContact {

	private JFrame frame;
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
	private Contact contactToEdit;
	private int contactArrayNumber;
	private String oldName;
	private String oldFirstName;
	private String oldLastName;

	/**
	 * Create the application.
	 */
	public EditContact(Contact contact, int contactNumber, String selectedValue) {
		contactToEdit = contact;
		contactArrayNumber = contactNumber;
		oldName = selectedValue;
		oldFirstName = contact.getFirstName();
		oldLastName = contact.getLastName();
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
        if (!System.getProperty("os.name").contains("Mac")){
        	frame.setIconImage(Toolkit.getDefaultToolkit().getImage(EditContact.class.getResource("/bigsky/BlueText.gif")));
        }
		frame.setSize(355, 301);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setTitle("Edit Contact");
        frame.setLocationRelativeTo(null);

		
		lblFirstName = new JLabel("First Name:");
		lblFirstName.setBounds(33, 35, 86, 16);
		frame.getContentPane().add(lblFirstName);
		
		lblLastName = new JLabel("Last Name:");
		lblLastName.setBounds(33, 80, 86, 16);
		frame.getContentPane().add(lblLastName);
		
		txtFirstName = new JTextField();
		txtFirstName.setBounds(136, 36, 134, 28);
		frame.getContentPane().add(txtFirstName);
		txtFirstName.setColumns(10);
		txtFirstName.setText(contactToEdit.getFirstName());
		
		txtLastName = new JTextField();
		txtLastName.setColumns(10);
		txtLastName.setBounds(136, 81, 134, 28);
		frame.getContentPane().add(txtLastName);
		txtLastName.setText(contactToEdit.getLastName());
		
		txtPhone = new JTextField();
		txtPhone.setColumns(10);
		txtPhone.setBounds(136, 122, 134, 28);
		frame.getContentPane().add(txtPhone);
		txtPhone.setText(contactToEdit.getPhoneNumber());
		
		txtSecondPhone = new JTextField();
		txtSecondPhone.setColumns(10);
		txtSecondPhone.setBounds(136, 171, 134, 28);
		frame.getContentPane().add(txtSecondPhone);
		txtSecondPhone.setText(contactToEdit.getSecondPhone());
		
		lblPhone = new JLabel("Phone:");
		lblPhone.setBounds(33, 128, 86, 16);
		frame.getContentPane().add(lblPhone);
		
		lblSecondPhone = new JLabel("Second Phone:");
		lblSecondPhone.setBounds(33, 177, 106, 16);
		frame.getContentPane().add(lblSecondPhone);
		
		btnSubmit = new JButton("Submit");
		btnSubmit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Contact validatedContact = validateContact(txtFirstName.getText(), txtLastName.getText(), txtPhone.getText(), txtSecondPhone.getText());
				if (validatedContact != null) {
					Global.contactAList.set(contactArrayNumber, validatedContact);
					Global.listModel.removeElement(oldName);
					addContactToListModel(txtFirstName.getText(), txtLastName.getText());
					frame.setVisible(false);
				}
			}
		});
		
		frame.getRootPane().setDefaultButton(btnSubmit);
		btnSubmit.setBounds(189, 230, 117, 29);
		frame.getContentPane().add(btnSubmit);
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frame.setVisible(false);
			}
		});
		btnCancel.setBounds(33, 230, 117, 29);
		frame.getContentPane().add(btnCancel);
	}
	
	/**
	 * Return the frame
	 * @return the frame for this window
	 */
	public JFrame getFrmEditContact(){
		return frame;
	}
	
	/**
	 * Validate a contact by making sure the name is valid, the phone numbers have 10 digits, and the name/phone numbers are unique
	 * @param firstName - First name to be validated
	 * @param lastName - Last name to be validated
	 * @param phone - Primary phone to be validated
	 * @param secondPhone - Secondary Phone to be validated
	 * @return - A new contact object if validation passes
	 */
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
		if (!(oldFirstName.equals(firstName) && oldLastName.equals(lastName))){
			//check for duplicate naming
			for (int i = 0; i<Global.contactAList.size();i++){
				Contact con = Global.contactAList.get(i);
				if (con.getFirstName().equals(firstName)){
					if (con.getLastName().equals(lastName)){
						JOptionPane.showMessageDialog(null, "This name already exists. Please choose a new name");
						return null;
					}
				}
			}
		}
		
		return new Contact(firstName, lastName, phone, secondPhone);
	}
	
	/**
	 * Adds a specified contact to list model
	 * @param firstName - first name of new contact
	 * @param lastName - last name of new contact
	 * @return
	 */
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
	
	/**
	 * If a contact is edited it may need to move in the list model.
	 * @param j - the current position
	 * @param newEntry - new first name
	 * @return the new position
	 */
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
