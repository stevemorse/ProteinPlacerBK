package Driver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import protein.Protein;
import NLP.WebInteragator;

/**
 * The Driver for the entire program.  Outputs serailized proteins to file when processed.
 * @author Steve Morse
 * @version 1.0
 *
 */
public class Driver {
	private static boolean debug = true;
	private static final int THREADPOOL_SIZE = 5;
	//private static File proteinsOutFile = new File("C:\\Users\\Steve\\Desktop\\ProteinPlacer\\proteinsOut.bin");
	
	/**
	 * The main method for the entire program
	 * @param args
	 */
	public static void main(String[] args){
		
		List<Protein> proteinList = new ArrayList<Protein>();
		
		//get and process data for text and GO
		WebInteragator webInteragator = new WebInteragator(proteinList,THREADPOOL_SIZE,debug);
		webInteragator.start();
		try {
			webInteragator.join(); //wind up thread
		} catch (InterruptedException ie) {
			System.err.println("interupted exception on join in driver: " + ie.getMessage());
			ie.printStackTrace();
		}//catch
		
		//output results
		int foundCount = 0;
		int unfoundCount = 0;
			
		ListIterator<Protein> proteinListLiter = proteinList.listIterator();
		while(proteinListLiter.hasNext()){
			Protein currentProtein = proteinListLiter.next();
			System.out.println("Protein:\n" + currentProtein.toString());
			if(currentProtein.isPlacedByText() == true || currentProtein.isPlacedByGOTerms() == true){
				foundCount++;
			}//if
			else{
				unfoundCount++;
			}//else
		}//while proteinListLiter.hasNext
		
		System.out.println("Proteins located by text or GO: " + foundCount);
		System.out.println("Proteins not located by text or GO: " + unfoundCount);
	}//main
}
