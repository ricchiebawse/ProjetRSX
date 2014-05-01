package projet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Joueur extends Thread {
	
	private String name;
	private Socket soc;
	BufferedReader entree;
	PrintWriter sortie;
	int tour;
	String adversaire;
	
	
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
		afficher("\n recherche de connexion avec l'autre joueur. \n");
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
		System.out.print(m);
	}
	public void run() {
		try {	
			connexion("localhost", 8080);
			if(soc==null)
			{
				afficher("\n Connexion a échouée \n");
			}
			else
			{
				afficher("Connextion effectuée avec " + adversaire+ "\n");
				afficher("Bon match !\n");
				tour=1;
				while(true) {	
						afficher("\n Tour n° "+ tour);
						afficher("\n Quelle choix \n");
						String texte = scan();/* demande Ã  l'utilisateur la saisie de son choix*/ 
						emettre(texte);
						String rep = recevoir();
						afficher("\nMon Choix --> " + texte );
						afficher("\t Résultat --> " + rep);
						//C'est pas encore géré, mais on pourrais recevoir de l'arbitre le choix que mon adversaire a fait et les points de mon adversaire
						tour++;
						
						//Si le serveursocket m'envoie un de ces messages, match terminé
						if(rep.equals("defaite")||rep.equals("victoire")||rep.equals("abandon")){
							resultat(rep);
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
	public Joueur() {
		afficher("\n Quelle est votre nom?\n");
		String name = scan();
		afficher("\n Bienvenue "+ name +" !\n");
		this.name=name;
	}
	
	public void resultat(String res){
		if((res.equals("abandon")))
			afficher("\n Vous avez abandonné la partie \n");
		else if((res.equals("defaite")))
			System.out.println("\n Vous avez perdu la partie \n");
		else
			System.out.println("\n Vous avez gagné la partie \n");
	}

	public static void main(String[] args){
		//System.out.println("Je suis un client !");
		Joueur cli = new Joueur();
		cli.start();
	}
}