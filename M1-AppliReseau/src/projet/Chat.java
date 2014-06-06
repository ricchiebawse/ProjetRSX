package projet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Chat extends Thread {
	
	//Classe servant à à recevoir de messages UDP et envoyer des messages UDP en sachant l'ip et le port
	
	private String domain;// Ne sert pas car on envoie de socket sur la même adrese ip
	private int port; //Sert à lier la socket d'écoute
	private String name;
	
	public void run() {
		try {	
			// s o c k e t s u r p o r t l i b r e & a d r e s s e wi l d c a r d l o c a l e
			DatagramSocket socket = new DatagramSocket(port);
			// tab d ' o c t e t s c o r r e s p o n d a n t a l a S t r i n g ` ` He l l o ' '
			//byte[]buf = "Hello".getBytes("ASCII");
			// c r e a t i o n d ' un datagramme c ont enant c e s donnees
			// d e s t i n e au p o r t 3333 de l a machine de nom s e r v e u r

			// e n v o i du datagramme v i a l a s o c k e t
			
			// a l l o c a t i o n & mi se en p l a c e d '1 b u f f e r pour r e c e p .
			byte [ ] buf = new byte [1024];
			byte [] msg;
			DatagramPacket packet= new DatagramPacket(buf,buf.length);
			
			// a f f i c h e : 1024 ( t a i l l e de l a zone de s t o c k a g e )
			// mi se en a t t e n t e de r e c e p t i o n
			while(true){
			socket.receive(packet);
			String txtString = new String(packet.getData(), 0 ,packet.getLength(),"ASCII");
			System.out.println("Message reçu de "+name+" ("+packet.getAddress().toString().substring(1)+":"+port+") : " +txtString);
			/*System.out.println("reponse ? oui ou non\n");
			msg = scan().getBytes("ASCII");
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
	
	public String scan(){
		//Lecture au clavier
		Scanner lectureClavier = new Scanner(System.in);
		String texte = lectureClavier.nextLine();
		return texte;
	}
	
	public void sendMess(String domain, int port) throws SocketException{
		try {
		System.out.println("Tapez votre message à envoyer :");	
		DatagramSocket socket = new DatagramSocket();
		byte [] buf =scan().getBytes("ASCII");
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
	public Chat(String domain, int port, String nameAdv)
	{
		this.domain=domain;
		this.port=port;
		this.name=nameAdv;
		this.start();
	}
	
}
