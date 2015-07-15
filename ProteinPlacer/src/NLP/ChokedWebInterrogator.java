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
import utils.FileCharReader;
import utils.SingleLock;

/**
 * The Driver for the entire program.
 * @author Steve Morse
 * @version 1.0
 *
 */
public class ChokedWebInterrogator{
	private static int InputFileNumber = 0;
	private static double thresholdEValue = 1.0E-30;
	private static String inSequencesFileBaseString = "/home/steve/Desktop/ProteinPlacer/data/Fasta";
	private static String outFileFileBaseString = "/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_";
	private static File inLocationsOBOFile = new File ("/home/steve/Desktop/ProteinPlacer/cellular_components.obo");
	private static String proteinsOutFileBaseString = "/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_";
	private static String proteinDataInFileString = "/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_";
	private boolean debug = true;
	List<Protein> proteinList = new ArrayList<Protein>();
	private int processOneProteinThreads = 0;
	private File inSequencesFile = null;
	private File proteinsOutFile = null;
	private File outFile = null;
	//private File proteinDataInFile = null;
	
	/**
	 * Sole constructor
	 * @param proteinList	The List of proteins to be processed
	 * @param threads	Size of the threadpool	
	 * @param debug	Verbose flag
	 */
	public ChokedWebInterrogator(List<Protein> proteinList, int threads, boolean debug){
		this.debug = debug;
		processOneProteinThreads = threads;
	}
	
	/**
	 * Manages the threadpool of individual protein processing tasks.  Loops through all Proteins loaded until all processed.
	 * Calls SingleGoProteinProcessingThread to process each protein.  Loads and processes Go codes and accessions hits.
	 */
	public void interrogate(){
		
		inSequencesFile = new File (inSequencesFileBaseString + InputFileNumber + ".txt");
		proteinsOutFile = new File(proteinsOutFileBaseString + InputFileNumber + "/proteinsOut_" + InputFileNumber + ".bin");
		outFile = new File(outFileFileBaseString + InputFileNumber + "/outSource_" + InputFileNumber + ".txt");
		File blastDataInFile = new File(proteinDataInFileString + InputFileNumber + "/blastResult_" + InputFileNumber + ".xml");
		File blastAnnotationsInFile = new File(proteinDataInFileString + InputFileNumber + "/annot_Seqs_" + InputFileNumber + ".txt");
		
		Map<String, String> GoAnnotationLocations = new HashMap<String, String>();
		LocationLoader loader = new LocationLoader();
		GoAnnotationLocations = loader.loadLocations(inLocationsOBOFile, debug);		

		//read sequence data from fasta file
		getSequences(inSequencesFile,proteinList);
		
		//SingleProteinProcessingThreads = new SingleProteinProcessingThread[proteinList.size()];
		ExecutorService executor = Executors.newFixedThreadPool(processOneProteinThreads);
		
		//loop through sequences loading data into blast and search
		ListIterator<Protein> proteinListLiter = proteinList.listIterator();
		Protein currentProtein = null;
		
		//get accession ids
		Map<String, Map<String, String>> accessionIds = getAccessionIds(blastDataInFile);
		//get GO annotations
		Map<String, Map<String, String>> goAnnotations = getGOAnnotations(blastAnnotationsInFile);
		
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
			//load Accession ids from file and associate with protein
			Map<String, String> currentAccessionIds = accessionIds.get(currentProtein.getBlast2GoFileName());
			//trim out annotation under eValue
			trim(currentAccessionIds, thresholdEValue);
			//load GO annotations
			currentProtein.setAnnotations(goAnnotations.get(currentProtein.getBlast2GoFileName()));
	
			//if go annotation include a valid cell location, set it
			if(currentProtein.getAnnotations() != null){
				List<String> cellLocationGOCodes = new ArrayList<String>(GoAnnotationLocations.keySet());
				List<String> currentProteinGoCodes = new ArrayList<String>(currentProtein.getAnnotations().keySet());
				ListIterator<String> currentProteinGoCodesLiter = currentProteinGoCodes.listIterator();
				while(currentProteinGoCodesLiter.hasNext()){
					String currentGoCode = 	currentProteinGoCodesLiter.next();
					if(cellLocationGOCodes.contains(currentGoCode) && !currentProtein.isPlacedByGOTerms()){
						currentProtein.setPlacedByGOTerms(true);
						currentProtein.setExpressionPointGOText(currentProtein.getAnnotations().get(currentGoCode));
					}//if(cellLocationGOCodes.contains(currentGoAnchorString)
				}//while currentProteinGoCodesLiter
			}//annotation not null
			Thread oneProteinWorkerThread = null;
			currentDone = true;
			do{
				try{
					if(currentAccessionIds != null && currentAccessionIds.size() >0){
						oneProteinWorkerThread = new ChokedSingleProteinProcessingThread(currentProtein, GoAnnotationLocations, currentAccessionIds,outFile, oos, thresholdEValue, debug);
						executor.execute(oneProteinWorkerThread);
					}//if protein needs processing
					else{
						currentProtein.setProcessed(true);  //fully processed by this point
						SingleLock lock = SingleLock.getInstance();
						synchronized(lock){
							oos.writeObject(currentProtein);
						}
					}//else no further processing required
				}
				catch(UnreachableBrowserException ube){
					oneProteinWorkerThread.interrupt();
					currentDone = false;
				}
				catch(org.openqa.selenium.WebDriverException wbe){
					oneProteinWorkerThread.interrupt();
					currentDone = false;
				}catch (FileNotFoundException fnfe) {
					System.err.println("error opening output file: " + fnfe.getMessage());
					fnfe.printStackTrace();
				} catch (IOException ioe) {
					System.err.println("error opening output stream: " + ioe.getMessage());
					ioe.printStackTrace();
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
	 * @param proteinList	The List of Proteins with their blastx sequences filled.
	 */
	public void getSequences(File inSequencesFile, List<Protein> proteinList){
		SequenceBlast2GoLoader sl = new SequenceBlast2GoLoader();
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
	public Map<String, Map<String, String>> getAccessionIds(File blastDataInFile){
		
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
			allHitsOfOneQuery = parseHits(docs[docCount]);
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
	public Map<String, String> parseHits(String doc){
		
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
	public Map<String, Map<String, String>> getGOAnnotations(File blastAnnotationsInFile){
		Map<String, Map<String, String>> goAnnotations = new HashMap<String, Map<String, String>>();
		Map<String, String> oneGoAnnotation = null;
		
		//read and clean fake whitespace from a sequence data from file
		FileCharReader fcr = new FileCharReader();
		String locationsStr = fcr.read(blastAnnotationsInFile);	
		String[] annotations = locationsStr.split("\\n");
		System.out.println("num annotations: " + annotations.length);
		for(int annotationCount = 1; annotationCount < annotations.length; annotationCount++){
			oneGoAnnotation = new HashMap<String, String>();
			//System.out.println("annotation: " + annotations[annotationCount]);
			String annotationsString = annotations[annotationCount];
			int queryNameEnd = annotationsString.indexOf(" ");
			String query = annotationsString.substring(0, queryNameEnd);
			query = query.trim();
			int goBegin = annotationsString.indexOf("GO:");
			String afterGoBegin = annotationsString.substring(goBegin);
			//System.out.println("afterGoBegin: " + afterGoBegin + "length: " + afterGoBegin.length());
			int goEnd = afterGoBegin.indexOf(" ");
			String goCode = afterGoBegin.substring(0, goEnd);
			goCode = goCode.trim();
			String goName = afterGoBegin.substring(goEnd);
			goName = goName.trim();
			oneGoAnnotation.put(goCode, goName);
			//System.out.println("query: " + query + " goCode: " + goCode + " goName: " + goName);
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
	public void trim(Map<String, String> currentAccessionIds, double thresholdEValue){
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
	
}//class