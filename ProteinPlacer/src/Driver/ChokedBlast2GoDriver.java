package Driver;

import java.util.ArrayList;
import java.util.List;

import protein.Protein;
import NLP.ChokedWebInterrogator;

/**
 * The Driver for the entire program. Calls the Blast2GoWebInteragator.
 * @author Steve Morse
 * @version 1.0
 */
public class ChokedBlast2GoDriver {
		private static boolean debug = true;
		private static final int THREADPOOL_SIZE = 5;
		
		
	/**
	 * The main method for the entire program
	 * @param args
	 */
	public static void main(String[] args){
		List<Protein> proteinList = new ArrayList<Protein>();	
		//get and process data for text and GO
		ChokedWebInterrogator webInterrogator = new ChokedWebInterrogator(proteinList,THREADPOOL_SIZE,debug);
		webInterrogator.interrogate();
	}//main
}//class