package bigsky.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import bigsky.Contact;
import bigsky.Global;
import bigsky.Logger;

/**
 * Popup that is displayed when you right click a contact name
 * @author Travis Reed
 *
 */
@SuppressWarnings("serial")
public class PopUp extends JPopupMenu {
	private final int returnsNull = 99999;

    JMenuItem editContact;
    JMenuItem startConvo;
    public PopUp(){
        editContact = new JMenuItem("Edit Contact");
        startConvo = new JMenuItem("Open Conversation");
        
        editContact.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String selectedValue = (String)Global.list.getSelectedValue();
				if (selectedValue != null){
					int i = findContactInListModel(selectedValue);
					if (i != returnsNull){
						Contact selectedContactCon = Global.contactAList.get(i);
						EditContact editCon = new EditContact(selectedContactCon, i, selectedValue);
						editCon.getFrmEditContact().setVisible(true);
					}
					else Logger.printOut("Error in Edit Contact");
				}
				else{
					JOptionPane.showMessageDialog(null, "Please select a contact to edit.");
				}	
			}
        });
		startConvo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Conversation.startNewConv();
			}
		});
			
        add(editContact);
        add(startConvo);
    }
    /**
     * Finds a selected contact in the list model
     * @param selectedValue - The name of the contact selected
     * @return - the position of the contact in the array list
     */
    private int findContactInListModel(String selectedValue){
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
		return returnsNull;
	}
}
