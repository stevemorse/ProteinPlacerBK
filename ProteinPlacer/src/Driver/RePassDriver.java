package Driver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
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
import NLP.LocationLoader;
import NLP.SequenceLoader;
import NLP.SingleProteinProcessingThread;
import NLP.WebInteragator;

public class RePassDriver {
	private static boolean debug = true;
	private static final int THREADPOOL_SIZE = 5;
	private static double thresholdEValue = 1.0E-30;
	private static File inSequencesFile = new File ("C:\\Users\\Steve\\Desktop\\ProteinPlacer\\VelvetAssembly.fasta");
	private static File inLocationsOBOFile = new File ("C:\\Users\\Steve\\Desktop\\ProteinPlacer\\cellular_components.obo");
	private static File outFile = new File("C:\\Users\\Steve\\Desktop\\ProteinPlacer\\data\\proteinsRePassOut0.bin");
	private static File tempOutFile = new File("C:\\Users\\Steve\\Desktop\\ProteinPlacer\\data\\tempRePassOut.bin");
	private static File proteinsInFile = new File("C:\\Users\\Steve\\Desktop\\ProteinPlacer\\data\\proteinsOut0.bin");
	
	/**
	 * The main method for the entire program
	 * @param args
	 */
	public static void main(String[] args){
		
		List<Protein> sequenceList = new ArrayList<Protein>();
		List<Protein> proteinList = new ArrayList<Protein>();
		LocationLoader loader = new LocationLoader();
		Map<String, String> GoAnnotationLocations = new HashMap<String, String>();
		GoAnnotationLocations = loader.loadLocations(inLocationsOBOFile, debug);
		ExecutorService executor = Executors.newFixedThreadPool(THREADPOOL_SIZE);
		//reload sequences
		//read sequence data
		getSequences(inSequencesFile,sequenceList);
		
		ObjectInput input = null;
		try{
		      InputStream fileStream = new FileInputStream(proteinsInFile);
		      InputStream buffer = new BufferedInputStream(fileStream);
		      input = new ObjectInputStream (buffer);
		      while(true){
		    	  Protein currentProtein = (Protein)input.readObject();
		    	  proteinList.add(currentProtein);
		      }
		} catch(ClassNotFoundException cnfe){
		      System.out.println("class not found: " + cnfe.getMessage());
	    } catch(IOException ioe){
	    	
	    }
		
		System.out.println("number of proteins is: " + proteinList.size());
		
		
		//set up temp object output stream
		FileOutputStream foutTemp;
		ObjectOutputStream oosTemp = null;
		try {
			foutTemp = new FileOutputStream(tempOutFile);
			oosTemp = new ObjectOutputStream(foutTemp);
		} catch (FileNotFoundException fnfe) {
			System.err.println("error opening output file: " + fnfe.getMessage());
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("error opening output stream: " + ioe.getMessage());
			ioe.printStackTrace();
		}
		
		//loop through sequences checking of processed on first pass, if not run them
		ListIterator<Protein> sequenceListLiter = sequenceList.listIterator();
		ListIterator<Protein> proteinListLiter = proteinList.listIterator();
		Protein currentSequence = null;
		Protein currentProtein = null;
		boolean currentDone = true;
		boolean finished = false;
		
		currentProtein = proteinListLiter.next(); //get first of the already run protein objects
		while(sequenceListLiter.hasNext() && !finished){
			currentSequence = sequenceListLiter.next();
			if(currentSequence.getSequence().compareTo(currentProtein.getSequence()) != 0 || !currentProtein.isProcessed()){
				Thread oneProteinWorkerThread = null;
				currentDone = true;
				Protein proteinToRun = null;
				if(currentSequence.getSequence().compareTo(currentProtein.getSequence()) != 0){
					proteinToRun = currentSequence; //this one was never run and should not have a match in proteins run
				}
				else if(!currentProtein.isProcessed()){
					proteinToRun = currentProtein;
					currentProtein = proteinListLiter.next(); //if sequences match but this one need re-processing
				}
				else{
					System.err.println("protein to run is invalid!");
				}
				do{
					try{
					oneProteinWorkerThread = new SingleProteinProcessingThread(proteinToRun, GoAnnotationLocations, tempOutFile, oosTemp, thresholdEValue, debug);
					executor.execute(oneProteinWorkerThread);
					}
					catch(UnreachableBrowserException ube){
						oneProteinWorkerThread.interrupt();
						currentDone = false;
					}
				}while(!currentDone); //retry current protein thread if browser failure occurs in SingleProteinProcessingThread
			}
			else{
				currentProtein = proteinListLiter.next();//matched and processed...get next
			}
			//finished = true; //set for one loop only...test code against first protein only
		}//while sequenceListLiter
			
		executor.shutdown();
	    // Wait until all threads are finish
		while (!executor.isTerminated()) {}
		
		//now merge temp file with orginal proteinsOutFile
		List<Protein> writeproteinList = new ArrayList<Protein>();
		ObjectInput firstPassInput = null;
		try{
		      InputStream fileStream = new FileInputStream(proteinsInFile);
		      InputStream buffer = new BufferedInputStream(fileStream);
		      firstPassInput = new ObjectInputStream (buffer);
		      while(true){
		    	  Protein writeProtein = (Protein)firstPassInput.readObject();
		    	  writeproteinList.add(writeProtein);
		      }
		} catch(ClassNotFoundException cnfe){
		      System.out.println("class not found: " + cnfe.getMessage());
	    } catch(IOException ioe){
	    	
	    }
		
		System.out.println("proteins in first pass list is : " + writeproteinList.size());
		
		ObjectInput RePassInput = null;
		try{
		      InputStream fileStream = new FileInputStream(tempOutFile);
		      InputStream buffer = new BufferedInputStream(fileStream);
		      RePassInput = new ObjectInputStream (buffer);
		      while(true){
		    	  Protein writeProtein = (Protein)RePassInput.readObject();
		    	  writeproteinList.add(writeProtein);
		      }
		} catch(ClassNotFoundException cnfe){
		      System.out.println("class not found: " + cnfe.getMessage());
	    } catch(IOException ioe){
	    	
	    }
		
		System.out.println("proteins in first and repass list is : " + writeproteinList.size());
		
		FileOutputStream fout;
		ObjectOutputStream oos = null;
		try {
			fout = new FileOutputStream(outFile);
			oos = new ObjectOutputStream(fout);
		} catch (FileNotFoundException fnfe) {
			System.err.println("error opening output file: " + fnfe.getMessage());
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("error opening output stream: " + ioe.getMessage());
			ioe.printStackTrace();
		}
		
		ListIterator<Protein> proteinWriteLiter = writeproteinList.listIterator();
		while(proteinWriteLiter.hasNext()){
			Protein currentWriteProtein = proteinWriteLiter.next();
			try {
				oos.writeObject(currentWriteProtein);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}//while
		
		try {
			oosTemp.close();
		} catch (IOException ioe) {
			System.err.println("error closing file: " + ioe.getMessage());
			ioe.printStackTrace();
		}
		
		tempOutFile.delete();
		
	}//main
	
	/**
	 * Loads all protein sequences from a file into a List of Proteins.
	 * @param inSequencesFile	The file from which to load the protein sequences.
	 * @param proteinList	The List of Proteins
	 */
	public static void getSequences(File inSequencesFile, List<Protein> proteinList){
		
		SequenceLoader sl = new SequenceLoader();
		sl.getSequences(inSequencesFile, proteinList);
	}//getSequences
	
}
