package projet;

import java.awt.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Arbitre extends Thread {
	
	private ServerSocket s;
	private static Socket soc=null;
	private static Socket soc2=null;
	private static String joueur1;  // Nom joueur 1 de la dernière partie commencée
	private static String joueur2; // Nom joueur 2 de la dernière partie commencée

	public String recevoir(BufferedReader entree)
	{
		String texte=null;
		try {
			texte = entree.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return texte;
	}

	public void emettre(String texte, PrintWriter sortie)
	{
		sortie.println(texte);
	}
	
	public boolean fin(int p1, int p2, PrintWriter sortie1, PrintWriter sortie2)
	{
		//Fonction qui envoie résultats du match si match est terminé
		//Pour l'instant pour gagner -> victoire = 3 mais ça serait bien de le mettre en attribut de la classe afin de l'instancier dans le constructeur
		int point_pour_gagner=3;
		
		if(p2>=point_pour_gagner)
		{
			emettre("victoire",sortie2);
			emettre("defaite",sortie1);
			
			return true;
		}
		if(p1>=point_pour_gagner){
			emettre("victoire",sortie1);
			emettre("defaite",sortie2);
			
			return true;
		}
		return false;
		
	}
	
	public boolean abandon(String str1, String str2, PrintWriter sortie1, PrintWriter sortie2)
	{
		//Fonction qui envoie résultats du match si un joueur a abandonné
				if(str1.equals("abandon")){
					emettre("victoire",sortie2);
					emettre("abandon",sortie1);
					
					return true;
				}
				if(str2.equals("abandon")){
					emettre("victoire",sortie1);
					emettre("abandon",sortie2);
					
					return true;
				}
				return false;
	}
	public void diffuserResultat(int result,int p1, int p2, PrintWriter sortie1, PrintWriter sortie2)
	{
		//Fonction qui envoie aux joueurs les résultats du tour de jeu 
		
		if(result==3)
		{
			//FAIT VITE, A OPTIMISER SI POSSIBLE -> Demande au joueur qui a nimm de réitérer sa commande ?
			emettre("probleme"+"/ points joueur :"+p1,sortie1);
			emettre("probleme"+"/ points joueur :"+p2,sortie2);
		}
		if(result==2)
		{
			emettre("perdu "+"/ points joueur :"+p1,sortie1);
			emettre("gagne"+"/ points joueur :"+p2,sortie2);
		}
		if(result==1)
		{
			emettre("gagne"+"/ points joueur :"+p1,sortie1);
			emettre("perdu"+"/ points joueur :"+p2,sortie2);
		}
		if(result==0)
		{
			emettre("nul"+"/ points joueur :"+p1,sortie1);
			emettre("nul"+"/ points joueur :"+p2,sortie2);
		}
	}
	public void run() {
		try {
			BufferedReader entree1 = new BufferedReader (new InputStreamReader (soc.getInputStream()));
			PrintWriter sortie1 = new PrintWriter (soc.getOutputStream(), true);
			BufferedReader entree2 = new BufferedReader (new InputStreamReader (soc2.getInputStream()));
			PrintWriter sortie2 = new PrintWriter (soc2.getOutputStream(), true);
			soc=null;
			soc2=null;
			int p1=0;
			int p2=0;
			
			
			while(true) {
				String str1 = recevoir(entree1);
				String str2 = recevoir(entree2);
				
				//Test si un joueur veut abandonné la partie
				if(abandon(str1,str2,sortie1,sortie2))
					break; //Fin de partie donc on sort de la boucle while true.
				
				int result = jeu(str1,str2); //Gére un tour de jeu
				
				//Met à jour score des joueurs.
				if(result==2)
					p2++;
				else if(result==1)
					p1++;
				
				if(fin(p1,p2,sortie1,sortie2))//Si un joueur atteint le max de point, match terminé
					break;//Fin de partie donc on sort de la boucle while true.
				diffuserResultat(result,p1,p2,sortie1,sortie2); //Diffuse résultat du tour aux joueurs
			}

			entree1.close();
			entree2.close();
			sortie2.close();
			sortie1.close();
		} catch (IOException e) {
			e.getMessage();
		}
	}	
	
	public Arbitre(Socket socket) {
		if(soc==null){
			this.soc = socket;
			BufferedReader entreetmp = null;
			try {
				//Permet de récupérer le nom du joueur 1
				//FAIT A LA VA VITE, A OPTIMISER SI POSSIBLE
				entreetmp = new BufferedReader (new InputStreamReader (soc.getInputStream()));
				this.joueur1=recevoir(entreetmp);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			this.soc2=socket;
			BufferedReader entreetmp2 = null;
			try {
				//Permet de récupérer le nom du joueur 2
				//FAIT A LA VA VITE, A OPTIMISER SI POSSIBLE
				entreetmp2 = new BufferedReader (new InputStreamReader (soc2.getInputStream()));
				this.joueur2=recevoir(entreetmp2);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public int jeu(String a, String b){
		// Code du jeu en question : Pierre feuille ciseau
	if(a.equals("pierre"))
	{
		if(b.equals("pierre"))
		{
			return 0;
		}
		if(b.equals("feuille"))
		{
			return 2;
		}
		if(b.equals("ciseau"))
		{
			return 1;
		}
	}
	if(a.equals("feuille"))
	{
		if(b.equals("pierre"))
		{
			return 1;
		}
		if(b.equals("feuille"))
		{
			return 0;
		}
		if(b.equals("ciseau"))
		{
			return 2;
		}
	}
	if(a.equals("ciseau"))
	{
		if(b.equals("pierre"))
		{
			return 2;
		}
		if(b.equals("feuille"))
		{
			return 1;
		}
		if(b.equals("ciseau"))
		{
			return 0;
		}
	}
	return 3;// Signifie qu'il y a une erreur dans ce que un des joueurs a entré
	}
	public static void main(String[] args) {
		int i = 0;
		System.out.println("Serveur multijoueurs de jeu de pierre feuille ciseau");
		try {		
			ServerSocket s = new ServerSocket(8080);
			
			while(i < 10) {
				Socket socket = s.accept();
				Arbitre smt = new Arbitre(socket);
				
				if((soc!=null) && (soc2!=null))
				{
					//Permet d'envoyer le nom de l'aversaire au joueur sans entrer dans le run
					//FAIT VITE, A OPTIMISER SI POSSIBLE
					PrintWriter sortietmp = new PrintWriter (soc.getOutputStream(), true);
					PrintWriter sortietmp2 = new PrintWriter (soc2.getOutputStream(), true);
					sortietmp.println(joueur2);
					sortietmp2.println(joueur1);
					
					//Lance le run qui se chargera de gérer un match
					smt.start();
					System.out.println("Un match en cours, Identifiant du Thread :"+smt.getId());
				}
				++i; // Pas plus de 10 connexions simultanées -> donc 5 parties de pierre feuille ciseau
			}
			
			
			s.close();
		} catch (IOException e) {
			e.getMessage();
		}
	}
}
