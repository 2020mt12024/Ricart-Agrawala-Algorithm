import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;
import java.lang.*;
import javax.swing.text.*;
 
public class StartProcess {
	//Declare All static variables
	static JFrame frame;
	static JLabel ra = new JLabel("Ricart-Agrawala algorithm");
	static JLabel logLabel = new JLabel("Log");
	static JLabel statusBar = new JLabel("Distributed Mutual Exclusion");
	static JButton reqCS = new JButton("<html>Request for<br/>Critical<br/>Section</html>");
	static JTextPane logArea = new JTextPane(); 
	static SimpleAttributeSet formatRed = new SimpleAttributeSet();
	static SimpleAttributeSet formatBold = new SimpleAttributeSet();
	static SimpleAttributeSet formatNormal = new SimpleAttributeSet();
	static StyledDocument styleDoc = logArea.getStyledDocument();
	static String[] processList;
	static int ts;
	static int[] reqDeferred;
	static String processName;
	static DatagramSocket dgSocket;
	static DatagramPacket dgPacketSend;
	static DatagramPacket dgPacketReceive;
	static Random rnd = new Random();
	static boolean requestingCS;
	static boolean executingCS;
	static int replyDue;
	public static void main(String[] args) throws InterruptedException {
		processList = new String[] {"A","B","C","D","E","F"};
		ts = 0;
		reqDeferred = new int[] {0,0,0,0,0,0};
		processName = args[0];
		requestingCS = false;
		executingCS = false;
		replyDue = 0;
		frame = new JFrame("Process: "+args[0]+" Port: "+Integer.parseInt(args[1]));
		StyleConstants.setForeground(formatRed, Color.RED);
		StyleConstants.setBold(formatRed, true);
		StyleConstants.setForeground(formatBold, Color.BLACK);
		StyleConstants.setBold(formatBold, true);
		StyleConstants.setForeground(formatNormal, Color.BLACK);
		
		logArea.setEditable(false);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(520,280));
		frame.add(ra, BorderLayout.NORTH);
		frame.add(logLabel, BorderLayout.WEST);
		frame.add(reqCS, BorderLayout.EAST);
		frame.add(new JScrollPane(logArea), BorderLayout.CENTER);
		frame.add(statusBar, BorderLayout.SOUTH);
		frame.setResizable(false);
		frame.setIconImage(new ImageIcon("icon.png").getImage());
		frame.setVisible(true);
			
		try{
			dgSocket = new DatagramSocket((getIndex(processName)+1)*1000);			
		}catch(SocketException ex){
			System.exit(1);
		}
		
		reqCS.addActionListener((ActionEvent evt) -> {			                            
			String msg = "";	
			String sentList = "";
			++ts;
			requestingCS = true;			
			try{ 
				logMsg("Requesting the critical section",formatBold);
				msg="REQUEST-"+ts+","+processName;
				for(int i=0; i < processList.length ;++i){
					if(!processName.equals(processList[i])){
						//Thread.sleep(50+rnd.nextInt(51));
						byte buff[]=msg.getBytes();
						dgPacketSend = new DatagramPacket(buff,buff.length,InetAddress.getLocalHost(),(getIndex(processList[i])+1)*1000);
						dgSocket.send(dgPacketSend);
						sentList += processList[i] + " ";
						++replyDue;
					}					
				}
				logMsg("REQUEST message ("+ts+","+processName+") broadcasted to processes "+sentList,formatNormal);
			}catch(Exception ex){
				logMsg(ex.getMessage(),formatRed);
			}                                  
		});
		new Thread(new Runnable(){
			public void run(){
				try{
					readyToReceivePacket();
				}catch(Exception e){
					
				}
			}
		}).start();			
	}
	public static void readyToReceivePacket() throws InterruptedException{
		while(true){
			try{
				Thread.currentThread().sleep(1000+rnd.nextInt(1001));
				byte buff[]=new byte[128];
				dgPacketReceive = new DatagramPacket(buff,buff.length);
				dgSocket.receive(dgPacketReceive);
				String strMsg = new String(dgPacketReceive.getData());
				strMsg = strMsg.trim();
				String msgType = strMsg.split("-")[0];
				String tsedMsg = strMsg.split("-")[1];
				int tsMsg = Integer.parseInt(tsedMsg.split(",")[0]);
				String senderProcessName = tsedMsg.split(",")[1];
				String msg = "";						
				if(msgType.equals("REQUEST")){
					logMsg("REQUEST message "+strMsg+" received",formatNormal);
					if((!requestingCS || (requestingCS && (tsMsg < ts))) && !executingCS){
						msg = "REPLY-"+ts+","+processName;
						byte buffreply[] = msg.getBytes();
						dgPacketSend = new DatagramPacket(buffreply,buffreply.length,InetAddress.getLocalHost(),(getIndex(senderProcessName)+1)*1000);
						dgSocket.send(dgPacketSend);
						logMsg("REPLY message sent to Process "+senderProcessName,formatNormal);	
					}else{
						reqDeferred[getIndex(senderProcessName)] = 1;
					}
				}
				if(msgType.equals("REPLY")){
					logMsg("REPLY message received. Reply due count: "+ --replyDue,formatNormal);
					if(replyDue == 0){
						logMsg("Entering CS",formatRed);
						new Thread(new Runnable(){
							public void run(){
								try{
									executeCS();
								}catch(Exception e){									
								}
							}
						}).start();	
					}
				}
			}catch(IOException ex){
				logMsg(ex.getMessage(),formatRed);
			}
		}
	}
	public static void executeCS() throws Exception{
		++ts;
		executingCS = true;
		requestingCS = false;
		logArea.setBackground(Color.ORANGE);
		logMsg("Executing in the CS",formatRed);
		Thread.currentThread().sleep(5000+rnd.nextInt(5001));
		logMsg("Exited CS",formatRed);
		logArea.setBackground(Color.WHITE);
		executingCS = false;
		String msg = "";
		for(int j = 0;j < reqDeferred.length; ++j){
			if(reqDeferred[j] == 1){
				msg = "REPLY-"+ts+","+getProcessName(j);
				byte buffreply[] = msg.getBytes();
				dgPacketSend = new DatagramPacket(buffreply,buffreply.length,InetAddress.getLocalHost(),(j+1)*1000);
				dgSocket.send(dgPacketSend);
				logMsg("REPLY message sent to Process "+getProcessName(j),formatNormal);
				reqDeferred[j] = 0;
			}
		}
	}
	public static void logMsg(final String msg, final SimpleAttributeSet attrib) {
		SwingUtilities.invokeLater(() -> {
			try{	
				styleDoc.insertString(styleDoc.getLength(), msg+"\n", attrib);
			}catch(Exception e) { 
				logMsg(e.getMessage(),formatRed);
			}				
		});
	}
	public static int getIndex(String proc) {
		int idx = 0;
		switch(proc){
			case "A":
				idx = 0;break;
			case "B" :
				idx = 1;break;
			case "C" :
				idx = 2;break;
			case "D" :
				idx = 3;break;
			case "E" :
				idx = 4;break;
			case "F" :
				idx = 5;break;
		}
		return idx;
	}
	public static String getProcessName(int idx) {
		String str = "";
		switch(idx){
			case 0:
				str = "A";break;
			case 1:
				str = "B";break;
			case 2:
				str = "C";break;
			case 3:
				str = "D";break;
			case 4:
				str = "E";break;
			case 5:
				str = "F";break;          
		}
		return str;
	}
}
