import java.io.*; 
import java.util.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Chat extends Process {
	// for gui
	String      appName;
	JFrame      frame;
	JButton     sendMessage;
	JTextField  messageBox;
	JTextArea   chatBox;
	JLabel      messageLabel;
	JLabel      sendToLabel;
	JTextField  sendToIds;
	
    public Chat(Linker initComm) {
        super(initComm);
    }
	
    public synchronized void handleMsg(Msg m, int src, String tag){
		chatBox.append(" P" + src + " :  " + m.getMessage() + "\n");
    }
	
	public void display() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel southPanel = new JPanel();
        southPanel.setBackground(Color.green);
        southPanel.setLayout(new GridBagLayout());

        sendToLabel = new JLabel("Send to: ");
        sendToIds = new JTextField(5);
        messageLabel = new JLabel(" Message: ");

        messageBox = new JTextField(20);
        messageBox.requestFocusInWindow();

        sendMessage = new JButton("Send Message");
        sendMessage.addActionListener(new sendMessageButtonListener());

        chatBox = new JTextArea();
        chatBox.setEditable(false);
        chatBox.setFont(new Font("Serif", Font.PLAIN, 15));
        chatBox.setLineWrap(true);

        mainPanel.add(new JScrollPane(chatBox), BorderLayout.CENTER);
		
		southPanel.add(sendToLabel);
        southPanel.add(sendToIds);
        southPanel.add(messageLabel);
        southPanel.add(messageBox);
        southPanel.add(sendMessage);

        mainPanel.add(BorderLayout.SOUTH, southPanel);

        frame.add(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setVisible(true);
    }

    class sendMessageButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
			String chatMsg = messageBox.getText();
			String idsString = sendToIds.getText();
			
            if (chatMsg.length() < 1 || idsString.length() < 1) {
                // do nothing
            } 
			else if (chatMsg.equals("quit")) {
                messageBox.setEditable(false);
				sendToIds.setEditable(false);
            } 
			else {
                chatBox.append(" P" + myId + " :  " + chatMsg + "\n");
                messageBox.setText("");
				sendToIds.setText("");
				
				IntLinkedList destIds = new IntLinkedList();
				
				String[] ids = idsString.split(" ", 0);
				for(int i = 0; i < ids.length; i++){
					int pid = Integer.parseInt(ids[i]);
					if (pid == -1) break;
					else destIds.add(pid);
				}
								
				comm.multicast(destIds, "chat", chatMsg);
            }
            messageBox.requestFocusInWindow();
        }
    }
	
    public static void main(String[] args) throws Exception {
        String baseName = args[0];
        int myId = Integer.parseInt(args[1]);
        int numProc = Integer.parseInt(args[2]);
        Linker comm = null;
		
		//implement only causal way of alternation of messages
		comm = new CausalLinker(baseName, myId, numProc);
		
        Chat c = new Chat(comm);
		c.appName = "Chat - Process " + String.valueOf(myId);
		c.frame = new JFrame(c.appName);
		
        for (int i = 0; i < numProc; i++)
            if (i != myId) (new ListenerThread(i, c)).start();
		
		c.display();

    }
}
