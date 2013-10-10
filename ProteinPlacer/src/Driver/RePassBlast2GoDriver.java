package Driver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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
import utils.FileCharReader;
import NLP.LocationLoader;
import NLP.SequenceLoader;
import NLP.SingleGoProteinProcessingThread;

/**
 * The second pass Driver for the entire program.
 * @author Steve Morse
 * @version 1.0
 *
 */
public class RePassBlast2GoDriver {
	private static int InputFileNumber = 9;
	private static double thresholdEValue = 1.0E-30;
	private static final int THREADPOOL_SIZE = 5;
	private static String inSequencesFileBaseString = "/home/steve/Desktop/ProteinPlacer/data/Fasta";
	private static String proteinsOutFileBaseString = "/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_";
	private static String tempOutFileString ="/home/steve/Desktop/ProteinPlacer/data/tempRePassOut";
	private static String blastDataInFileString = "/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_";
	private static File inLocationsOBOFile = new File ("/home/steve/Desktop/ProteinPlacer/cellular_components.obo");
	private static boolean debug = true;
	
	
	/**
	 * The main method for the entire program. Loops through original protein sequences and processes
	 * any that have not already been processed in the first pass.  Manages the threadpool.  
	 * Calls SingleGoProteinProcessingThread.  Loads and processes Go codes and accessions hits.
	 * @param args
	 */
	public static void main(String[] args){
		
		File blastDataInFile = new File(blastDataInFileString + InputFileNumber + "/blastResult_" + InputFileNumber + ".xml");
		File blastAnnotationsInFile = new File(blastDataInFileString + InputFileNumber + "/annot_Seqs_" + InputFileNumber + ".txt");
		
		
		File inSequencesFile = new File (inSequencesFileBaseString + InputFileNumber + ".txt");
		File proteinsInFile = new File(proteinsOutFileBaseString + InputFileNumber + "/proteinsOut_" + InputFileNumber + ".bin");
		File proteinsOutFile = new File(proteinsOutFileBaseString + InputFileNumber + "/proteinsRePassOut_" + InputFileNumber + ".bin");
		File tempOutFile = new File(tempOutFileString + InputFileNumber + ".bin");
		
		List<Protein> sequenceList = new ArrayList<Protein>(); //Initial state before any runs
		List<Protein> proteinList = new ArrayList<Protein>(); //processed proteins
		List<String> sequencesOfProcessedProteins = new ArrayList<String>();
		LocationLoader loader = new LocationLoader();
		Map<String, String> GoAnnotationLocations = new HashMap<String, String>();
		GoAnnotationLocations = loader.loadLocations(inLocationsOBOFile, debug);
		ExecutorService executor = Executors.newFixedThreadPool(THREADPOOL_SIZE);
		//reload sequences
		//read sequence data
		getSequences(inSequencesFile,sequenceList);
		
		ObjectInput input = null;
		InputStream fileStream = null;
		InputStream buffer = null;
		try{
		      fileStream = new FileInputStream(proteinsInFile);
		      buffer = new BufferedInputStream(fileStream);
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
		
		//get accession ids
		Map<String, Map<String, String>> accessionIds = getAccessionIds(blastDataInFile);
		
		//get GO annotations
		Map<String, Map<String, String>> goAnnotations = getGOAnnotations(blastAnnotationsInFile);
				
		
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
		
		//load sequences of processed proteins into string array
		ListIterator<Protein> proteinListLiter = proteinList.listIterator();
		while(proteinListLiter.hasNext()){
			Protein currentProtein = proteinListLiter.next();
			sequencesOfProcessedProteins.add(currentProtein.getSequence());
		}
		
		//loop through sequences checking if processed on first pass, if not run them
		ListIterator<Protein> sequenceListLiter = sequenceList.listIterator();
		Protein currentSequence = null;
		boolean currentDone = true;
		boolean finished = false;
		while(sequenceListLiter.hasNext() && !finished){
			currentSequence = sequenceListLiter.next();
			//load Accession ids from file and associate with protein
			Map<String, String> currentAccessionIds = accessionIds.get(currentSequence.getBlast2GoFileName());
			//trim  Accession ids
			trim(currentAccessionIds, thresholdEValue);
			if(debug){
				if(currentAccessionIds != null){
					System.out.println("number of links: " + currentAccessionIds.size());
				}
				else{
					System.out.println("number of links was null");
				}
			}//if debug
			//load GO annotations
			currentSequence.setAnnotations(goAnnotations.get(currentSequence.getBlast2GoFileName()));
			//if go annotation include a valid cell location, set it
			if(currentSequence.getAnnotations() != null){
				List<String> cellLocationGOCodes = new ArrayList<String>(GoAnnotationLocations.keySet());
				List<String> currentProteinGoCodes = new ArrayList<String>(currentSequence.getAnnotations().keySet());
				ListIterator<String> currentProteinGoCodesLiter = currentProteinGoCodes.listIterator();
				while(currentProteinGoCodesLiter.hasNext()){
					String currentGoCode = 	currentProteinGoCodesLiter.next();
					if(cellLocationGOCodes.contains(currentGoCode) && !currentSequence.isPlacedByGOTerms()){
						currentSequence.setPlacedByGOTerms(true);
						currentSequence.setExpressionPointGOText(currentSequence.getAnnotations().get(currentGoCode));
					}//if(cellLocationGOCodes.contains(currentGoAnchorString)
				}//while currentProteinGoCodesLiter
			}//annotation not null
			Thread oneProteinWorkerThread = null;
			currentDone = true;
			if(!sequencesOfProcessedProteins.contains(currentSequence.getSequence())){
				do{
					try{
						oneProteinWorkerThread = new SingleGoProteinProcessingThread(currentSequence, currentAccessionIds, GoAnnotationLocations, tempOutFile, oosTemp, thresholdEValue, debug);
						executor.execute(oneProteinWorkerThread);
					}
					catch(UnreachableBrowserException ube){
						oneProteinWorkerThread.interrupt();
						currentDone = false;
					} catch(org.openqa.selenium.WebDriverException wbe){
						oneProteinWorkerThread.interrupt();
						currentDone = false;
					}
				}while(!currentDone); //retry current protein thread if browser failure occurs in SingleProteinProcessingThread
			}//if not processed yet
			
			//finished = true; //set for one loop only...test code against first protein only
		}//while sequenceListLiter
			
		executor.shutdown();
	    // Wait until all threads are finish
		while (!executor.isTerminated()) {}
		
		//now merge temp file with orginal proteinsOutFile
		List<Protein> writeproteinList = new ArrayList<Protein>();
		ObjectInput firstPassInput = null;
		InputStream fileInFPStream = null;
		InputStream bufferFP = null;
		try{
			fileInFPStream = new FileInputStream(proteinsInFile);
		    bufferFP = new BufferedInputStream(fileInFPStream);
		    firstPassInput = new ObjectInputStream (bufferFP);
		    while(true){
	    	  Protein writeProtein = (Protein)firstPassInput.readObject();
	    	  writeproteinList.add(writeProtein);
		    }
		} catch(ClassNotFoundException cnfe){
		      System.out.println("class not found: " + cnfe.getMessage());
	    } catch(IOException ioe){
	    	
	    }
		
		System.out.println("proteins in processed list is : " + writeproteinList.size());
		
		ObjectInput RePassInput = null;
		InputStream inRPFileStream = null;
		InputStream bufferRP = null;
		try{
			inRPFileStream = new FileInputStream(tempOutFile);
			bufferRP = new BufferedInputStream(inRPFileStream);
			RePassInput = new ObjectInputStream (bufferRP);
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
			fout = new FileOutputStream(proteinsOutFile);
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
	
	/**
	 * reads and parses the passed file to extract a global map of all accession hits in that
	 * blast 2 go output file.
	 * @param blastDataInFile
	 * @return accessionIds a Map<String, Map<String, String>> where the primary key is the
	 * FastaName of a proteins sequence and the value is a Map of all accession hits and their 
	 * respective eValues for the protein identified by its FastaName.
	 */
	public static Map<String, Map<String, String>> getAccessionIds(File blastDataInFile){
		
		Map<String, Map<String, String>> accessionIds = new HashMap<String, Map<String, String>>();
		Map<String, String> allHitsOfOneQuery = new HashMap<String, String>();
		char[] blastDataBuffer = new char[(int) blastDataInFile.length()];
		System.out.println("in getAccessionIds ");
		//read sequence data from file
		try {
			FileReader sequenceFileReader = new FileReader(blastDataInFile);
			sequenceFileReader.read(blastDataBuffer);
			sequenceFileReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}
		
		String locationsStr = new String(blastDataBuffer);
		//String[] docs = locationsStr.split("<?xml version=\"1.0\"?>");
		String[] docs = locationsStr.split("<?xml version=");
		System.out.println("num docs: " + docs.length);
		for(int docCount = 1; docCount < docs.length; docCount++){
			String currentDoc = docs[docCount];
			//System.out.println("docCount: " + docCount + "\ndoc:\n" + docs[docCount]);
			//extract query name
			int queryBegin = currentDoc.indexOf("<BlastOutput_query-def>") + "<BlastOutput_query-def>".length();
			int queryEnd = currentDoc.indexOf("</BlastOutput_query-def>");
			String query = currentDoc.substring(queryBegin, queryEnd);
			System.out.println("query: " + query);
			allHitsOfOneQuery = parseHits(accessionIds,docs[docCount]);
			accessionIds.put(query, allHitsOfOneQuery);
		}	
		return accessionIds;
	}//getAccessionIds
	
	/**
	 * parses a single searched sequences data entry in a blast to go file's output to
	 * extract all accessions for that searched sequence.
	 * @param doc a string representing all the data in a blast 2 go output file for a
	 * single searched sequence.
	 * @return allHitsOfOneQuery a Map of all accessions for this sequence by
	 * accession key and eValue value.
	 */
	public static Map<String, String> parseHits(Map<String, Map<String, String>> accessionIds, String doc){
		
		Map<String, String> allHitsOfOneQuery = new HashMap<String, String>();
		String[] hits = doc.split("<Hit>");
		System.out.println("hits: " + hits.length);
		if(hits.length > 1){
			for(int hitCount = 1; hitCount < hits.length; hitCount++){
				//System.out.println("hit:\n" + hits[hitCount]);
				int accessionBegin = hits[hitCount].indexOf("<Hit_accession>") + "<Hit_accession>".length();
				int accessionEnd = hits[hitCount].indexOf("</Hit_accession>");
				String accession = hits[hitCount].substring(accessionBegin, accessionEnd);
				int eValueBegin = hits[hitCount].indexOf("<Hsp_evalue>") + "<Hsp_evalue>".length();
				int eValueEnd = hits[hitCount].indexOf("</Hsp_evalue>");
				String eValue = hits[hitCount].substring(eValueBegin, eValueEnd);
				//System.out.println("hit:\n" + hits[hitCount] + "accession: " + accession + " eValue: " + eValue);
				allHitsOfOneQuery.put(accession,eValue);
			}//for hitCount
		}//if (hits.length > 1)
		return allHitsOfOneQuery;
	}//parseHits
	
	/**
	 * Reads, cleans and parses a blast 2 go annotation file to extract a global map of all
	 * go codes and go names and associate them with the sequence they are associated with.
	 * @param blastAnnotationsInFile a blast 2 go annotations file in "sequence" output form
	 * @return goAnnotations A Map<String, Map<String, String>> where the primary key is the
	 * FastaName of a proteins sequence and the value is a Map of all go codes associated
	 * with that sequence by the blast 2 go mapper and annotator as the key and the value 
	 * is the respective go annotation name for that go code.
	 */
	public static Map<String, Map<String, String>> getGOAnnotations(File blastAnnotationsInFile){
		Map<String, Map<String, String>> goAnnotations = new HashMap<String, Map<String, String>>();
		Map<String, String> oneGoAnnotation = null;
		System.out.println("in getGOAnnotations ");
		//read and clean fake whitespace from a sequence data from file
		FileCharReader fcr = new FileCharReader();
		String locationsStr = fcr.read(blastAnnotationsInFile);
		String[] annotations = locationsStr.split("\\n");
		System.out.println("num annotations: " + annotations.length);
		//for(int annotationCount = 1; annotationCount < annotations.length; annotationCount++){
		for(int annotationCount = 1; annotationCount < 100; annotationCount++){
			oneGoAnnotation = new HashMap<String, String>();
			System.out.println("annotation: " + annotations[annotationCount]);
			String annotationsString = annotations[annotationCount];
			int queryNameEnd = annotationsString.indexOf(" ");
			String query = annotationsString.substring(0, queryNameEnd);
			query = query.trim();
			int goBegin = annotationsString.indexOf("GO:");
			String afterGoBegin = annotationsString.substring(goBegin);
			System.out.println("afterGoBegin: " + afterGoBegin + "length: " + afterGoBegin.length());
			int goEnd = afterGoBegin.indexOf(" ");
			String goCode = afterGoBegin.substring(0, goEnd);
			goCode = goCode.trim();
			String goName = afterGoBegin.substring(goEnd);
			goName = goName.trim();
			oneGoAnnotation.put(goCode, goName);
			System.out.println("query: " + query + " goCode: " + goCode + " goName: " + goName);
			if(!goAnnotations.containsKey(query)){
				goAnnotations.put(query, oneGoAnnotation);
			}
			else{
				goAnnotations.get(query).put(goCode, goName);
			}			
		}
		return goAnnotations;
	}//getGOAnnotations

	/**
	 * trims the accessions list for a particular searched sequence down to only
	 * those with an eValue less than the threshold eValue, only these are returned
	 * for further processing.
	 * @param currentAccessionIds the Map of accessions and eValues extracted
	 * from the blast 2 go output for a particular sequence
	 * @param thresholdEValue the cutoff eValue over which we ignore all hits
	 * as possible/probable coincidences.
	 */
	public static void trim(Map<String, String> currentAccessionIds, double thresholdEValue){
		if(currentAccessionIds != null){
			List<String> currentAccessionKeys = new ArrayList<String>(currentAccessionIds.keySet());
			ListIterator<String> currentAccessionKeysLiter = currentAccessionKeys.listIterator();
			while(currentAccessionKeysLiter.hasNext()){
				String currentAccessionKey = currentAccessionKeysLiter.next();
				if(Double.parseDouble(currentAccessionIds.get(currentAccessionKey)) > thresholdEValue){
					currentAccessionIds.remove(currentAccessionKey);
				}//if
			}//while
		}//if not null
	}//trim
	

}
