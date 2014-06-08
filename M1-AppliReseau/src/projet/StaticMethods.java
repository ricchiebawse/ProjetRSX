package projet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public final class StaticMethods {
	
	public static String receiveString(BufferedReader entree)
	{
		//Retourne le String re�u via le BufferedReader entree.
		String text=null;
		try {
			text = entree.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return text;
	}

	public static void sendString(String text, PrintWriter sortie)
	{
		//Envoie d'un string via le PrintWriter sortie.
		sortie.println(text);
		
	}
	
	public static void consolePrintln(String m)
	{
		//Afiche en console le String m
		System.out.println(m);
	}
	
	public static String getKeyboarding(){
		//Retourne le String saisi au clavier
		Scanner lectureClavier = new Scanner(System.in);
		String texte = lectureClavier.nextLine();
		return texte;
	}
}
