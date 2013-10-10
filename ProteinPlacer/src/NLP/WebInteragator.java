package NLP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openqa.selenium.remote.UnreachableBrowserException;

import protein.Protein;

/**
 * Loads the Proteins into a List and handles the processing of the List of proteins.
 * Uses a threadpool to control the volume of individual threads spawned to process each protein sequence individually.
 * @author Steve Morse
 * @version 1.0
 *
 */
public class WebInteragator extends Thread{
	
	private static int InputFileNumber = 0;
	private static double thresholdEValue = 1.0E-30;
	//private static File inSequencesFile = new File ("C:\\Users\\Steve\\Desktop\\ProteinPlacer\\seq.txt");
	//private static File inSequencesFile = new File ("C:\\Users\\Steve\\Desktop\\ProteinPlacer\\VelvetAssembly.fasta");
	private static String inSequencesFileBaseString = "C:\\Users\\Steve\\Desktop\\ProteinPlacer\\data\\Fasta";
	private static File inLocationsOBOFile = new File ("C:\\Users\\Steve\\Desktop\\ProteinPlacer\\cellular_components.obo");
	private static File outFile = new File("C:\\Users\\Steve\\Desktop\\ProteinPlacer\\outSource.txt");
	private static String proteinsOutFileBaseString = "C:\\Users\\Steve\\Desktop\\ProteinPlacer\\data\\proteinsOut";
	private boolean debug = true;
	List<Protein> proteinList = new ArrayList<Protein>();
	private int processOneProteinThreads = 0;
	private File inSequencesFile = null;
	private File proteinsOutFile = null;
	/**
	 * Sole constructor
	 * @param proteinList	The List of proteins to be processed
	 * @param threads	Size of the threadpool	
	 * @param debug	Verbose flag
	 */
	public WebInteragator(List<Protein> proteinList, int threads, boolean debug){
		this.debug = debug;
		processOneProteinThreads = threads;
	}
	
	/**
	 * Manages the threadpool of individual protein processing tasks.  Loops through all Proteins loaded until all processed.
	 */
	public void run(){
		
		inSequencesFile = new File (inSequencesFileBaseString + InputFileNumber + ".txt");
		proteinsOutFile = new File(proteinsOutFileBaseString + InputFileNumber + ".bin");
		
		Map<String, String> GoAnnotationLocations = new HashMap<String, String>();
		LocationLoader loader = new LocationLoader();
		GoAnnotationLocations = loader.loadLocations(inLocationsOBOFile, debug);		

		//read sequence data
		getSequences(inSequencesFile,proteinList);
		
		//SingleProteinProcessingThreads = new SingleProteinProcessingThread[proteinList.size()];
		ExecutorService executor = Executors.newFixedThreadPool(processOneProteinThreads);
		
		//loop through sequences loading data into blast and search
		ListIterator<Protein> proteinListLiter = proteinList.listIterator();
		Protein currentProtein = null;
		
		FileOutputStream fout;
		ObjectOutputStream oos = null;
		try {
			fout = new FileOutputStream(proteinsOutFile);
			oos = new ObjectOutputStream(fout);
		} catch (FileNotFoundException fnfe) {
			System.err.println("error opening output file: " + fnfe.getMessage());
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("error opening output stream: " + ioe.getMessage());
			ioe.printStackTrace();
		}
		
		boolean currentDone = true;
		boolean finished = false;
		while(proteinListLiter.hasNext() && !finished){
			currentProtein = proteinListLiter.next();
			//Runnable oneProteinWorkerThread;
			Thread oneProteinWorkerThread = null;
			currentDone = true;
			do{
				try{
				oneProteinWorkerThread = new SingleProteinProcessingThread(currentProtein, GoAnnotationLocations, outFile, oos, thresholdEValue, debug);
				executor.execute(oneProteinWorkerThread);
				}
				catch(UnreachableBrowserException ube){
					oneProteinWorkerThread.interrupt();
					currentDone = false;
				}
			}while(!currentDone); //retry current protein thread if browser failure occurs in SingleProteinProcessingThread
			//finished = true; //set for one loop only...test code against first protein only
		}//while sequenceListLiter
			
		executor.shutdown();
	    // Wait until all threads are finish
	    while (!executor.isTerminated()) {}
	    
	    try {	
	    	oos.close();
		} catch (FileNotFoundException fnfe) {
			System.err.println("error opening output file: " + fnfe.getMessage());
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("error opening output stream: " + ioe.getMessage());
			ioe.printStackTrace();
		}
	}//process
	
	/**
	 * Loads all protein sequences from a file into a List of Proteins.
	 * @param inSequencesFile	The file from which to load the protein sequences.
	 * @param proteinList	The List of Proteins
	 */
	public void getSequences(File inSequencesFile, List<Protein> proteinList){
		
		SequenceLoader sl = new SequenceLoader();
		sl.getSequences(inSequencesFile, proteinList);
	}//getSequences
	
}//class
