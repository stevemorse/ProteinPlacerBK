package NLP;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.remote.UnreachableBrowserException;

import ThreadPools.PriorityRunnable;
import ThreadPools.PriorityFuture;
import ThreadPools.TheSinglePriorityThreadPool;
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
	private static int inputFileNumber = 0;
	private static double thresholdEValue = 1.0E-30;
	private static final String inSequencesFileBaseString = "/home/steve/Desktop/ProteinPlacer/data/Fasta";
	private static String outFileBaseString = "/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_";
	private static String outTextFileBaseString = "/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_";
	private static File inLocationsOBOFile = new File ("/home/steve/Desktop/ProteinPlacer/cellular_components.obo");
	private static String proteinsOutFileBaseString = "/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_";
	private static String proteinDataInFileString = "/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_";
	private boolean debug = true;
	List<Protein> proteinList = new ArrayList<Protein>();
	//private int processOneProteinThreads = 0;
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
	public ChokedWebInterrogator(List<Protein> proteinList, int threads, int inputFileNumber, boolean debug){
		this.debug = debug;
		//processOneProteinThreads = threads;
		this.inputFileNumber = inputFileNumber;
		
	}
	
	/**
	 * Manages the threadpool of individual protein processing tasks.  Loops through all Proteins loaded until all processed.
	 * Calls SingleGoProteinProcessingThread to process each protein.  Loads and processes Go codes and accessions hits.
	 */
	public void interrogate(){
		
		inSequencesFile = new File (inSequencesFileBaseString + inputFileNumber + ".txt");
		proteinsOutFile = new File(proteinsOutFileBaseString + inputFileNumber + "/proteinsOut_" + inputFileNumber + ".bin");
		outFile = new File(outFileBaseString + inputFileNumber + "/outSource_" + inputFileNumber + ".txt");
		File threadLogFile = new File(proteinsOutFileBaseString + inputFileNumber + "/ThreadLog_" + inputFileNumber + ".txt");
		File textOutFile = new File(outTextFileBaseString + inputFileNumber + "/textOutOfProtiens"
				+ "_" + inputFileNumber + ".txt");
		File blastDataInFile = new File(proteinDataInFileString + inputFileNumber + "/blastResult_" + inputFileNumber + ".xml");
		File blastAnnotationsInFile = new File(proteinDataInFileString + inputFileNumber + "/annot_Seqs_" + inputFileNumber + ".txt");
		
		Map<String, String> goAnnotationLocations = new HashMap<String, String>();
		LocationLoader loader = new LocationLoader();
		goAnnotationLocations = loader.loadLocations(inLocationsOBOFile, debug);
		TheSinglePriorityThreadPool.getInstance();

		//read sequence data from fasta file
		getSequences(inSequencesFile,proteinList);
		
		//SingleProteinProcessingThreads = new SingleProteinProcessingThread[proteinList.size()];
		//ExecutorService executor = Executors.newFixedThreadPool(processOneProteinThreads);
		
		//loop through sequences loading data into blast and search
		ListIterator<Protein> proteinListLiter = proteinList.listIterator();
		Protein currentProtien = null;
		
		//get accession ids
		Map<String, Map<String, String>> accessionIds = getAccessionIds(blastDataInFile);
		//get GO annotations
		Map<String, Map<String, String>> goAnnotations = getGOAnnotations(blastAnnotationsInFile);
		
		
		PrintWriter threadLogWriter = null;
		FileOutputStream fout = null;
		ObjectOutputStream oos = null;
		try {
			fout = new FileOutputStream(proteinsOutFile);
			oos = new ObjectOutputStream(fout);
			threadLogWriter = new PrintWriter(new FileWriter(threadLogFile));
			threadLogWriter.close();
		} catch (FileNotFoundException fnfe) {
			System.err.println("error opening output file: " + fnfe.getMessage());
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("error opening output stream: " + ioe.getMessage());
			ioe.printStackTrace();
		}
		
		boolean currentDone = true;
		boolean finished = false;
		int protienCount = 0;
		while(proteinListLiter.hasNext() && !finished){
			currentProtien = proteinListLiter.next();
			protienCount++;
			//load Accession ids from file and associate with protein
			Map<String, String> currentAccessionIds = accessionIds.get(currentProtien.getBlast2GoFileName());
			//trim out annotation under eValue
			trim(currentAccessionIds, thresholdEValue);
			//load GO annotations
			currentProtien.setAnnotations(goAnnotations.get(currentProtien.getBlast2GoFileName()));
			currentDone = true;
			do{
				try{
					synchronized(threadLogFile){
						try {
							threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
						} catch (IOException ioe) {
							System.out.println("error opening file for write " +ioe.getMessage());
							ioe.printStackTrace();
						}//catch
						int numAccesions = 0;
						if(currentAccessionIds != null){
							numAccesions = currentAccessionIds.size();
						}
						else{
							numAccesions = -1;
						}
						threadLogWriter.println("processin protein: " + protienCount + " of: " + proteinList.size() + " with " + numAccesions + "accessions");
						threadLogWriter.close();
					}//synchronized
					
					if(currentAccessionIds != null && currentAccessionIds.size() >0){
						boolean firstAccession = true;
						Iterator<String> currentAccessionIdsIter = currentAccessionIds.keySet().iterator();
						while(currentAccessionIdsIter.hasNext()){
							//oneProteinWorkerThread = new ChokedSingleGoProteinProcessingThread(currentProtein, GoAnnotationLocations, currentAccessionIds,outFile, oos, thresholdEValue, debug);
							//executor.execute(oneProteinWorkerThread);
							String accession = currentAccessionIdsIter.next();
							String eValueStr = currentAccessionIds.get(accession);
							double eValue = Double.parseDouble(eValueStr);
							if(eValue < thresholdEValue){
								//process presumed non-random hit
								StringBuilder region = new StringBuilder("");
								//theAnchorLinkThreadGateway.getAnchorLinkThread(currentProtien, accession, region, goAnnotationLocations, textOutFile, firstAccession, finished);
								TheSinglePriorityThreadPool.getAnchorLinkThread(currentProtien, accession, region, 
										goAnnotationLocations, textOutFile, threadLogFile, firstAccession, debug);
								firstAccession = false;
								currentProtien.getAllFoundRegionsInText().put(accession,region.toString());
							}//if eValue of current accession (probability of hit being random) is lower than the threshold probability (eValue)
						}//while there are accessions for this protein that have not been data mined
					}//if protein has at least one accession id and therefore needs processing
					
					//output protein objects whether it was processed or not
					currentProtien.setProcessed(true);  //fully processed by this point
					/*
					SingleLock lock = SingleLock.getInstance();
					synchronized(lock){
						oos.writeObject(currentProtien);
					}//synchronized
					*/
					
				}
				catch(UnreachableBrowserException ube){
					currentDone = false;
				}//catch ube
				catch(org.openqa.selenium.WebDriverException wbe){
					currentDone = false;
				
				}//catch block
			}while(!currentDone); //retry current protein thread if browser failure occurs in SingleAnchorThreadRunner
			
			//finished = true; //set for one loop only...test code against first protein only
		}//while sequenceListLiter
		
	    try {	
	    	oos.close();
	    	fout.close();
		} catch (FileNotFoundException fnfe) {
			System.err.println("error closing output file: " + fnfe.getMessage());
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("error closing output stream: " + ioe.getMessage());
			ioe.printStackTrace();
		}
	    
	    //need to spawn SingleGoAnchor threads from running tasks b/f shutdown
	    TheSinglePriorityThreadPool.getInstance().invokeAll();
	    
	    //shut it down
	    TheSinglePriorityThreadPool.getInstance().shutdown();
	    
	    System.out.println("threadpool is shutdown = " + TheSinglePriorityThreadPool.getInstance().isShutDown());	   
	   
	    //wait while it shuts down
	    while(!TheSinglePriorityThreadPool.getInstance().awaitTermination(1000L, TimeUnit.SECONDS)){
	    	System.out.println("wait 1000 seconds in second loop for all tasks to terminate");
	    }//while not all threads terminated
	    
	    cleanNotMatched(proteinList);
	    
	    setGoLocations(proteinList, goAnnotationLocations);
	    
	    windupStats(outFile,threadLogFile);
	    
	    writeProteinObjectsToFile(proteinList);
	    
	    synchronized(threadLogFile){
			try {
				threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
			} catch (IOException ioe) {
				System.out.println("error opening file for write " +ioe.getMessage());
				ioe.printStackTrace();
			}//catch
			threadLogWriter.println("ChockedWebInterrogator terminates normally");
			threadLogWriter.close();
		}//synchronized
	}//method interrogate

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
			System.out.println("trimmed query: " + query);
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
	
	/**
	 * ensures "NOT MATCED" constructor string was properly removed where matched sequences were found
	 * @param proteinList
	 */
	private void cleanNotMatched(List<Protein> proteinList){
		ListIterator<Protein> proteinListLiter = proteinList.listIterator();
		Protein currentProtien = null;
		while(proteinListLiter.hasNext()){
			currentProtien = proteinListLiter.next();
			synchronized(currentProtien){
			if(currentProtien.getProteinSequences().size() > 1 && 
					currentProtien.getProteinSequences().get(0).compareToIgnoreCase("NOT MATCHED") ==0){
					//clean not matched if it's still there
					currentProtien.getProteinSequences().remove(0);//remove "NOT MATCHED" constructor string
					currentProtien.getProteinSequences().removeAll(Collections.singleton(null)); //fixs for ArrayList remove() shift to left bug
					currentProtien.getProteinSequences().removeAll(Collections.singleton("NOT MATCHED"));
					currentProtien.getProteinSequences().removeAll(Collections.singleton(""));
				}//if
			}//Synchronized	
		}//while(proteinListLiter.hasNext()
	}//cleanNotMatched
	
	private void setGoLocations(List<Protein> proteinList, Map<String, String> goAnnotationLocations){
		//if go annotations include a valid cell location, set it
		ListIterator<Protein> proteinListLiter = proteinList.listIterator();
		Protein currentProtien = null;
		while(proteinListLiter.hasNext()){
			currentProtien = proteinListLiter.next();
			if(currentProtien.getAnnotations() != null){
				List<String> cellLocationGOCodes = new ArrayList<String>(goAnnotationLocations.keySet());
				List<String> currentProteinGoCodes = new ArrayList<String>(currentProtien.getAnnotations().keySet());
				ListIterator<String> currentProteinGoCodesLiter = currentProteinGoCodes.listIterator();
				while(currentProteinGoCodesLiter.hasNext()){
					String currentGoCode = 	currentProteinGoCodesLiter.next();
					if(cellLocationGOCodes.contains(currentGoCode) && !currentProtien.isPlacedByGOTerms()){
						currentProtien.setPlacedByGOTerms(true);
						currentProtien.setExpressionPointGOText(currentProtien.getAnnotations().get(currentGoCode));
					}//if(cellLocationGOCodes.contains(currentGoAnchorString)
				}//while currentProteinGoCodesLiter
			}//annotation not null
		}//while proteinListLiter
	}//setGoLocation
	
	/**
	 * produces final output of effects of protein data gathering operations
	 * @param outFile where to print the proteins
	 * @param threadLogFile where to print the effects of thread operations
	 */
	private void windupStats(File outFile, File threadLogFile) {
		//get the processed proteins from the outFile
		int proteinsRead = 0;
		ObjectInput ois = null;
		List<Protein> ProteinList = new ArrayList<Protein>();
		try {
			InputStream fin = new FileInputStream(proteinsOutFile);
			ois = new ObjectInputStream(fin);
			while(true){
				try{
					Object obj = ois.readObject();
					Protein currentProtein = (Protein) obj;
					ProteinList.add(currentProtein);
					proteinsRead++;
				} catch (EOFException eofe) {
					ois.close();
					fin.close();
					break;
				}//catch EOFException
			}//while
					
		} catch (ClassNotFoundException cnfe) {
			System.err.println("cannot find protein class on file open: " + cnfe.getMessage());
			cnfe.printStackTrace();
		} catch (FileNotFoundException fnfe) {
			System.err.println("error opening output file: " + fnfe.getMessage());
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("error opening output stream: " + ioe.getMessage());
			ioe.printStackTrace();
		}//last catch
		
		
		
		//iterate through protein list accumulating stats on error modes and if a genebank was found for that protein
		int numGenebankFound = 0;
		int numRecoredRemoved = 0;
		int numError = 0;
		int numAnchor = 0;
		int numReplaced = 0;
		int numNotClassified = 0;
		ListIterator<Protein> proteinListLiter = proteinList.listIterator();
		int proteinCounter = 0;
		Protein currentProtien = null;
		while(proteinListLiter.hasNext()){
			proteinCounter++;
			currentProtien = proteinListLiter.next();
			if(currentProtien.isGotGenebank()){
				numGenebankFound++;
			}
			else if(currentProtien.getErrorMode().compareToIgnoreCase("removed") == 0){
				numRecoredRemoved++;
			}
			else if(currentProtien.getErrorMode().compareToIgnoreCase("error") == 0){
				numError++;
			}
			else if(currentProtien.getErrorMode().compareToIgnoreCase("anchor") == 0){
				numAnchor++;
			}
			else if(currentProtien.getErrorMode().compareToIgnoreCase("replaced") == 0){
				numReplaced++;
			}
			else{
				System.err.println("protein " + proteinCounter + " is not classified");
				numNotClassified++;
			}
		}//while proteinListLiter hasNext
		
		//now make final output
		PrintWriter threadLogWriter = null;
		synchronized(threadLogFile){
			int numProteinsInList = proteinList.size();
			try {
				threadLogWriter = new PrintWriter(new FileWriter(threadLogFile,true));
				threadLogWriter.println("number of proteins read = " + proteinsRead);
				threadLogWriter.println("number of proteins for which at least 1 genbank element was found is: " + numGenebankFound + " in lsit of: " + numProteinsInList);
				threadLogWriter.println("number of proteins with error mode error: " + numError);
				threadLogWriter.println("number of proteins with error mode removed: " + numRecoredRemoved);
				threadLogWriter.println("number of proteins with error mode replaced: " + numReplaced);
				threadLogWriter.println("number of proteins with error mode anchor: " + numAnchor);
				threadLogWriter.println("number of proteins not classified: " + numNotClassified);
				threadLogWriter.println("number of anchor threads submitted: " + TheSinglePriorityThreadPool.getNumAnchorLinksSpwned());
				threadLogWriter.println("number of GO anchor threads submitted: " + TheSinglePriorityThreadPool.getNumGoAnchorLinksSpwned());
				threadLogWriter.close();
			} catch (IOException ioe) {
				System.err.println("error opening output stream: " + ioe.getMessage());
				ioe.printStackTrace();
			}//last catch
		}//synchronized
	}//windupStats
	
	private void writeProteinObjectsToFile(List<Protein> proteinList){

		FileOutputStream fout = null;
		ObjectOutputStream oos = null;
		try {
			fout = new FileOutputStream(proteinsOutFile);
			oos = new ObjectOutputStream(fout);
			//loop through proteins and write to file
			ListIterator<Protein> proteinListLiter = proteinList.listIterator();
			Protein currentProtien = null;
			int protienCount = 0;
			while(proteinListLiter.hasNext()){
				currentProtien = proteinListLiter.next();
				protienCount++;
				SingleLock lock = SingleLock.getInstance();
				synchronized(lock){
					oos.writeObject(currentProtien);
				}//synchronized
			}//while not finished writing to file
		System.out.println("number of proteins written to file: " + protienCount);	
		} catch (FileNotFoundException fnfe) {
			System.err.println("error opening output file: " + fnfe.getMessage());
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("error opening output stream: " + ioe.getMessage());
			ioe.printStackTrace();
		}
		
	}
	
}//class