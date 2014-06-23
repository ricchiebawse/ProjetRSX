package projet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ChatUDP extends Thread {
	//Permet de dialoguer en UDP
	
	private int myPort; //Permet de lier la socket d'?coute
	private String myName;
	private String nameOpponent;
	private IHMChatUDP frame;
	private String ipOpponent;
	private int portOpponent;
	
	public void run() {
		//Thread : Ouvre l'IHM de chat et se met EN ATTENTE de reception d'un msg de la part de l'adversaire.
		
		try {	
			//Création d'une socket d'écoute sur son propre port.
			DatagramSocket socket = new DatagramSocket(myPort);
			//Création d'un DatagramPacket prêt à recevoir un message.
			byte[] buf = new byte [1024];
			DatagramPacket packet= new DatagramPacket(buf,buf.length);
			
			//Création de la fenêtre de Chat
			frame = new IHMChatUDP(this);
			frame.setVisible(true);
			
			while(true){
					//Mise en attente de reception d'un message adverse
					socket.receive(packet);
					String txtString = new String(packet.getData(), 0 ,packet.getLength(),"ASCII");
					frame.setTextArea(nameOpponent+" : "+txtString);//on insère le string obtenu dans la JTextArea
					packet.setData(buf,0,buf.length);
			}
		} catch (SocketException e) {
			StaticMethods.consolePrintln(Consts.CONNEXION_OPP_INTERRUPTED);
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}catch (IOException e) {
			e.getMessage();
		}
	}
	
	public void sendMsg(String msg) throws SocketException{
		//Envoie le String "msg" à l'unique adversaire
		
		try {
			//Inscription du msg envoyé dans sa propre JTextArea
			frame.setTextArea(myName+" : "+msg);

			//Envoie d'un packet contenant le string "msg" à l'adversaire (idOpponent, portOpponent)
			DatagramSocket socket = new DatagramSocket();
			byte [] buf =msg.getBytes("ASCII");
			DatagramPacket packet = new DatagramPacket (buf,buf.length,InetAddress.getByName(ipOpponent.substring(1)) ,portOpponent);
			socket.send(packet);
			//Fermeture de la socket
			socket.close();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		run();
	}
	
	public ChatUDP(int port, String nameAdv, String ipOpponent, int portOpponent, String myName)
	{
		this.myPort=port;
		this.nameOpponent=nameAdv;
		this.ipOpponent=ipOpponent;
		this.portOpponent=portOpponent;
		this.myName=myName;
		this.start();
	}
	
	public void endChat(){
		frame.dispose();
		Thread.currentThread().interrupt();
	}
	public String getNameOpponent() {
		return nameOpponent;
	}
}
