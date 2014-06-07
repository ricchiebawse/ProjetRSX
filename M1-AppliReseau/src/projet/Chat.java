package projet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Chat extends Thread {
	//Permet de dialoguer en UDP
	
	private int port; //Permet de lier la socket d'écoute
	private String nameOpponent;
	
	public void run() {
		//Thread : se met EN ATTENTE de reception d'un msg de la part de l'adversaire.
		try {	
			DatagramSocket socket = new DatagramSocket(port);

			byte[] buf = new byte [1024];
			DatagramPacket packet= new DatagramPacket(buf,buf.length);
			
			while(true){
				socket.receive(packet);
				String txtString = new String(packet.getData(), 0 ,packet.getLength(),"ASCII");
				StaticMethods.consolePrintln("Message recu de "+nameOpponent+" ("+packet.getAddress().toString().substring(1)+":"+port+") : " +txtString);
				
				/*StaticMethods.consolePrintln("reponse ? oui ou non\n");
				msg = StaticMethods.scan().getBytes("ASCII");
				packet.setData(msg);
				//AUCUN SENS NORMALMENT
				packet.setAddress(packet.getAddress());
				packet.setPort(packet.getPort());
				socket.send(packet);*/
				
				
				packet.setData(buf,0,buf.length);
			}
		} catch (IOException e) {
			e.getMessage();
		}
	}
	
	public void sendMsg(String domain, int port) throws SocketException{
		//Envoie d'un msg ˆ l'adresse domain sur le port port.
		try {
			StaticMethods.consolePrintln("Tapez votre message a envoyer :");	
			DatagramSocket socket = new DatagramSocket();
			byte [] buf =StaticMethods.getKeyboarding().getBytes("ASCII");
			DatagramPacket packet = new DatagramPacket (buf,buf.length,InetAddress.getByName(domain.substring(1)) ,port);
			socket.send(packet);
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
	
	public Chat(int port, String nameAdv)
	{
		this.port=port;
		this.nameOpponent=nameAdv;
		this.start();
	}
	
}
