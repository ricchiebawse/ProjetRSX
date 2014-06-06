package projet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Joueur extends Thread {
	
	//Classe joueur qui sert à se connecter à un serveur de jeu en TCP
	//Rendre classe générique!
	
	private String domain;//Domain auquel on se connecte pour joueur, IP du serveur
	private int port;//Port du serveur
	private String name;
	private Socket soc;
	BufferedReader entree;
	PrintWriter sortie;
	int tour;
	/* A modifier pour gérer plusieurs adversaires (liste)*/
	String adversaire;//Nom de l'adversaire
	String ipadv="";//IP de l'adversaire
	int portadv;//Port de l'adversaire
	
	
	public void connexion(String domain, int port)
	{
		//Fonction qui se charge de se connecter à un serveursocket (à l'arbitre dans notre cas)
		soc = null;
		try {
			soc = new Socket(domain, port);
			entree = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			sortie = new PrintWriter(soc.getOutputStream(),true);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sortie.println(name); // Envoie du nom du joueur à l'arbitre afin qu'il le diffuse à l'autre joueur
		afficher("recherche de connexion avec l'autre joueur...");
		adversaire = recevoir(); // Attente du nom de l'adversaire que l'arbitre va me donner 
		//Tant que l'arbitre ne me donne pas le nom de l'abversaire, je suis en attente
		
	}
	
	public String recevoir()
	{
		//Permet de recevoir un msg d'un serveursocket
		String texte=null;
		try {
			texte = entree.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return texte;
	}

	public void emettre(String texte)
	{
		//Permet d'emettre un msg à un serveursocket
		sortie.println(texte);
	}
	
	public String scan(){
		//Lecture au clavier
		Scanner lectureClavier = new Scanner(System.in);
		String texte = lectureClavier.nextLine();
		return texte;
	}
	public void afficher(String m)
	{
		System.out.println(m);
	}
	public void run() {
		try {	
			connexion(this.domain, this.port);
			if(soc==null)
			{
				afficher("Connexion a échouée");
			}
			else
			{
				afficher("Connextion effectuée avec " + adversaire);
				afficher("Bon match !");
				//Chat : classe servant à chatter avec un adversaire (pas encore multiadversaire)
				Chat chat = new Chat(soc.getLocalAddress().toString(), soc.getLocalPort(),adversaire);
				tour=1;
				while(true) {
					afficher("Menu : 1) jouer (par defaut)\n2) chatter");
					String choix = scan();
					if(choix.equals("2"))
					{
						Chatter(chat);
					}
					else
					{
						/*afficher("\n Tour n° "+ tour);
						afficher("\n Quelle choix \n");
						texte = scan();// demande Ã  l'utilisateur la saisie de son choix
						emettre(texte);
						String rep = recevoir();
						afficher("\nMon Choix --> " + texte );
						afficher("\t Résultat --> " + rep+"\n");
						//C'est pas encore géré, mais on pourrais recevoir de l'arbitre le choix que mon adversaire a fait et les points de mon adversaire
						tour++;
						
						//Si le serveursocket m'envoie un de ces messages, match terminé
						if(rep.equals("defaite")||rep.equals("victoire")||rep.equals("abandon")){
							resultat(rep);
							break;
						}*/
						String rep = Tour();
						if(EndMatch(rep))
							break;
						
					
				}
			}
			entree.close();
			sortie.close();
			soc.close();
			}

		} catch (IOException e) {
			e.getMessage();
		}
	}
	
	public String Tour(){
		afficher("Tour n° "+ tour);
		afficher("Veuillez entrer soit :\npierre\nfeuille\nciseau\nlezard\nspoke");
		String texte = scan();/* demande Ã  l'utilisateur la saisie de son choix*/ 
		emettre(texte);
		String rep = recevoir();
		afficher("Mon Choix --> " + texte );
		afficher("\t Résultat --> " + rep);
		//C'est pas encore géré, mais on pourrais recevoir de l'arbitre le choix que mon adversaire a fait et les points de mon adversaire
		tour++;
		return rep;

	}
	
	public boolean EndMatch(String rep){
		//Si le serveursocket m'envoie un de ces messages, match terminé
		if(rep.equals("defaite")||rep.equals("victoire")||rep.equals("abandon")){
			resultat(rep);
			return true;
		}
		return false;
	}
	
	public void Chatter(Chat chat){ 
		/* A modifier (avec modifiation des attributs de la classe) pour gérer une partie avec plusieurs adversaires*/
		if(ipadv.equals("")){
			/*Dans ce cas la, on pas encore reçu les donnees de l'adversaire donc on doit demander au serveur de nous les envoyer*/
			emettre("info");
			ipadv = recevoir();
			portadv = Integer.parseInt(recevoir());
			afficher("Données de l'adversaire reçu.");
			/*afficher(ipadv+"\n");
			afficher(String.valueOf(portadv)+"\n");*/
			//texte = scan();
		}
		try {
			chat.sendMess(ipadv, portadv);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public Joueur() {
		afficher("Quelle est votre nom?");
		String name = scan();
		afficher("Bienvenue "+ name +" !");
		this.name=name;
		this.domain="localhost";
		this.port=8080;
	}
	public Joueur(String domain, int port) {
		afficher("Quelle est votre nom?");
		String name = scan();
		afficher("Bienvenue "+ name +" !");
		this.name=name;
		this.domain=domain;
		this.port=port;
	}
	public void resultat(String res){
		if((res.equals("abandon")))
			afficher("Vous avez abandonné la partie");
		else if((res.equals("defaite")))
			System.out.println("Vous avez perdu la partie");
		else
			System.out.println("Vous avez gagné la partie");
	}

	public static void main(String[] args){
		Joueur cli = new Joueur("localhost", 8080);
		cli.start();
	}
}