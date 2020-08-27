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

import java.util.Timer;


public class Chat extends RAMutex {
	// for gui
	String      appName;
	JFrame      frame;
	JButton     sendMessage;
	JButton     writeMessage;
	JTextField  messageBox;
	JTextArea   chatBox;
	JLabel      messageLabel;
	JLabel      sendToLabel;
	JLabel      sent;
	JTextField  sendToIds;
	
    public Chat(Linker initComm) {
        super(initComm);
    }
	
    public synchronized void handleMsg(Msg m, int src, String tag) {
		int timeStamp;
		if(tag.equals("chat"))
		{
			//Util.mySleep(7000);
			chatBox.append(" P" + src + " :  " + m.getMessage() + "\n");
		}
		else if (tag.equals("request")) {
			timeStamp = m.getMessageInt();
			c.receiveAction(src, timeStamp);
			if ((myts == Symbols.Infinity) // not interested in CS
					|| (timeStamp < myts)
					|| ((timeStamp == myts) && (src < myId)))
				sendMsg(src, "okay", c.getValue());
			else
				pendingQ.add(src);
		} else if (tag.equals("okay")) {
			numOkay++;
			if (numOkay == N - 1)
				notify(); // okayCS() may be true now
		}
		
    }
	
	public void display(/*Lock lock*/) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel southPanel = new JPanel();
        southPanel.setBackground(Color.green);
        southPanel.setLayout(new GridBagLayout());

        sendToLabel = new JLabel("Send to: ");
        sendToIds = new JTextField(5);
		//sendToIds.TextFieldChangeListener(new sendMessageButtonListener());
		//sendToIds.setEditable(false);
        messageLabel = new JLabel(" Message: ");

        messageBox = new JTextField(20);
		//messageBox.setEditable(false);
        messageBox.requestFocusInWindow();
		
		sent = new JLabel("Message sent!");

        sendMessage = new JButton("Send Message");
		sendMessage.addActionListener(new sendMessageButtonListener(/*lock*/));
		
		writeMessage = new JButton("Write Message");
		writeMessage.addActionListener(new writeMessageButtonListener(/*lock*/));

        chatBox = new JTextArea();
        chatBox.setEditable(false);
        chatBox.setFont(new Font("Serif", Font.PLAIN, 15));
        chatBox.setLineWrap(true);

        mainPanel.add(new JScrollPane(chatBox), BorderLayout.CENTER);
		
		southPanel.add(sendToLabel);
        southPanel.add(sendToIds);
        southPanel.add(messageLabel);
        southPanel.add(messageBox);
		southPanel.add(writeMessage);
        southPanel.add(sendMessage);
		southPanel.add(sent);
		
		
		sendToLabel.setVisible(false);
		sendToIds.setVisible(false);
		messageLabel.setVisible(false);
		messageBox.setVisible(false);
		sendMessage.setVisible(false);
		writeMessage.setVisible(true);
		sent.setVisible(false);		
		
        mainPanel.add(BorderLayout.SOUTH, southPanel);

        frame.add(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setVisible(true);
		
    }

    class sendMessageButtonListener implements ActionListener {
		/*Lock lock;
		
		public sendMessageButtonListener(Lock lock)
		{
			this.lock = lock;
		}*/
		
        public void actionPerformed(ActionEvent event) {
			releaseCS();
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
			//messageBox.requestFocusInWindow();
			sendToLabel.setVisible(false);
			sendToIds.setVisible(false);
			messageLabel.setVisible(false);
			messageBox.setVisible(false);
			sendMessage.setVisible(false);
			writeMessage.setVisible(true);
			

        }
    }
	
	class writeMessageButtonListener implements ActionListener {
		/*Lock lock;
		
		public writeMessageButtonListener(Lock lock)
		{
			this.lock = lock;
		}*/
        public void actionPerformed(ActionEvent event) {
			//System.out.println(myId + " is not in CS");
			requestCS();
			//System.out.println(myId + " is in CS *****");
			writeMessage.setVisible(false);
				
			sendToLabel.setVisible(true);
			sendToIds.setVisible(true);
			messageLabel.setVisible(true);
			messageBox.setVisible(true);
			sendMessage.setVisible(true);
			messageBox.requestFocusInWindow();
			
        }
    }
	
    public static void main(String[] args) throws Exception {
		
        String baseName = args[0];
        int myId = Integer.parseInt(args[1]);
        int numProc = Integer.parseInt(args[2]);
        Linker comm = null;
		//Lock lock = null;
		
		//implement only causal way of alternation of messages
		comm = new CausalLinker(baseName, myId, numProc);
		
		//lock = new RAMutex(comm);
        Chat c = new Chat(comm);
		c.appName = "Chat - Process " + String.valueOf(myId);
		c.frame = new JFrame(c.appName);
		
        for (int i = 0; i < numProc; i++)
            if (i != myId) 
				(new ListenerThread(i, c)).start();
			
		/*while (true) {
                System.out.println(myId + " is not in CS");
                Util.mySleep(2000);
                lock.requestCS();
                Util.mySleep(2000);
                System.out.println(myId + " is in CS *****");
				c.display();
                lock.releaseCS();
        }*/
		
		c.display(/*lock*/);

    }
}
