package utils;

import java.io.BufferedInputStream;
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
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import NLP.FunctionalGoLoader;
import protein.Protein;


/**
 * Merges binary serialized protein files for chunked fasta files
 * into one large binary serialized protein file.
 * @author Steve Morse
 * @version 1.0
 */
public class mergeResults {
	private static boolean debug = true;
	private static int numFiles = 15;
	private static String proteinsInFileBaseString = "/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_";
	private static File molecularFunction = new File("/home/steve/Desktop/ProteinPlacer/molecular_function.obo");
	private static File outFile = new File("/home/steve/Desktop/ProteinPlacer/data/allResults.bin");
	private static File goldOutFile = new File("/home/steve/Desktop/ProteinPlacer/data/goldResults.bin");
	private static File goldFunctionalOutFile = new File("/home/steve/Desktop/ProteinPlacer/data/goldFunctionalResults.bin");
	private static String inFileBaseString = "/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_";
	private static File outSourceTextFile = new File("/home/steve/Desktop/ProteinPlacer/data/allSourceText.txt");
	
	/**
	 * main method does the merging, outputs the data in two files,
	 * all the results and gold standard results (placed proteins).
	 * @param args
	 */
	public static void main (String args[]){
		
		List<Protein> proteinList = new ArrayList<Protein>();
		List<Protein> goldProteinList = new ArrayList<Protein>();
		List<Protein> goldFunctionalProteinList = new ArrayList<Protein>();
		Map <Integer, List<Protein>> proteinListMap = new HashMap <Integer, List<Protein>>();
		Map <Integer, List<Protein>> goldProteinListMap = new HashMap <Integer, List<Protein>>();
		Map <Integer, List<Protein>> goldFunctionalProteinListMap = new HashMap <Integer, List<Protein>>();
		
		Protein currentProteinToLoad = null;
		String allSource = "";
		
		//load functional annotations
		FunctionalGoLoader ffgl = new FunctionalGoLoader();
		Map<String, String> GoAnnotationFunctions = ffgl.loadMolecularFunctions(molecularFunction, debug);
		List<String> GoAnnotationFunctionsCodes = new ArrayList<String>(GoAnnotationFunctions.values());
		
		for(int fileCount = 0; fileCount < numFiles; fileCount++){
			String currentInFile = proteinsInFileBaseString + fileCount + "/proteinsOut_" + fileCount +".bin";
			proteinList.clear();
			//load proteins from current file
			InputStream file = null;
			InputStream buffer = null;
			ObjectInput input = null;		
			try{
				file = new FileInputStream(currentInFile);
			    buffer = new BufferedInputStream(file);
			    input = new ObjectInputStream (buffer);
				while(true){
					currentProteinToLoad = (Protein) input.readObject();
					proteinList.add(currentProteinToLoad);
				}//while
			} catch(ClassNotFoundException cnfe){
				System.out.println("class not found: " + cnfe.getMessage());
			} catch(IOException ioe){
				System.out.println("class not found: " + ioe.getMessage());
			} 
			
			try {
				input.close();
				buffer.close();
				file.close();
			} catch (IOException ioe) {
				System.out.println("fail to close input: " + ioe.getMessage());
				ioe.printStackTrace();
			}
			
			System.out.println("number of proteins loaded from file number " + fileCount + " is: " + proteinList.size());		
			
			
			ListIterator<Protein> proteinLiter = proteinList.listIterator();
			while(proteinLiter.hasNext()){
				Protein currentProtein = proteinLiter.next();
					if(currentProtein.isPlacedByText() || currentProtein.isPlacedByGOTerms()){
						goldProteinList.add(currentProtein);
						//go through current proteins annotations to see if any are functional
						if(currentProtein.getAnnotations() != null && currentProtein.getAnnotations().size() > 0){
							List<String> currentGoCodesList = new ArrayList<String>(currentProtein.getAnnotations().values());
							ListIterator<String> currentGoCodesLiter = currentGoCodesList.listIterator();
							boolean done = false;
							while(currentGoCodesLiter.hasNext() && !done){
								String goCode = currentGoCodesLiter.next();
								if(GoAnnotationFunctionsCodes.contains(goCode)){
									goldFunctionalProteinList.add(currentProtein);
									done = true;
								}//if placed and has functional annotation(and therefore part of gold functional standard)
							}//while currentGoCodesLiter.hasNext() && !done
						}//if currentProtein.getAnnotations() != null && 
					}//if placed (and therefore part of gold standard)
				
			}//while proteinLiter.hasNext()
			//add all lists to their appropriate maps
			proteinListMap.put(fileCount, proteinList);
			System.out.println("proteinListMap key " + fileCount + " has " + proteinList.size() + " proteins");
			goldProteinListMap.put(fileCount, goldProteinList);
			System.out.println("goldProteinListMap key " + fileCount + " has " + goldProteinListMap.size() + " proteins");
			goldFunctionalProteinListMap.put(fileCount, goldFunctionalProteinList);
			System.out.println("goldFunctionalProteinListMap key " + fileCount + " has " + goldFunctionalProteinListMap.size() + " proteins");
			
			/*
			//now merge page source location from text files
			
			File curentInFile = new File(inFileBaseString + fileCount + "/textOutOfProtiens_" + fileCount + ".txt");
			
			char[] sourceInFileBuffer = new char[(int) curentInFile.length()];
			if(debug){
				System.out.println("file/buffer size is: " + (int) curentInFile.length());
			}//if debug
			//read source data from current file
			try {
				FileReader sourceFileReader = new FileReader(curentInFile);
				sourceFileReader.read(sourceInFileBuffer);
				sourceFileReader.close();
			} catch (FileNotFoundException e) {
				System.out.println("File Not Found: " + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("IOException: " + e.getMessage());
				e.printStackTrace();
			}
			
			//String sourcesStr = new String(sourceInFileBuffer);
			
			//output source text files
			FileWriter	writer = null;
			try {
				synchronized(outSourceTextFile){
					writer = new FileWriter(outSourceTextFile.getAbsoluteFile(),true);
				}//synchronized
			} catch (IOException e) {
				System.out.println("IOException: " + e.getMessage());
				e.printStackTrace();
			}
			//writer.println(allSource);
			//writer.println(sourcesStr);
			synchronized(outSourceTextFile){
				try {
					writer.write(sourceInFileBuffer);
					writer.flush();
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(debug){
					System.out.println("file/buffer write size is: " + sourceInFileBuffer.length);
					System.out.println("outfile size is now: " + (int) outSourceTextFile.length());
				}//if debug	
			}//synchronized(outSourceTextFile)
			
			//add to cumulative total
			//allSource = allSource + sourcesStr;	
			*/
			
		}//for fileCount
		
		
		
		//output maps of proteins and gold standards for rule based and functional processing
		FileOutputStream fout = null;
		ObjectOutputStream oos = null;
		FileOutputStream goldFOut = null;
		ObjectOutputStream goos = null;
		FileOutputStream goldFFOut = null;
		ObjectOutputStream gfoos = null;
		try {
			fout = new FileOutputStream(outFile);
			goldFOut = new FileOutputStream(goldOutFile);
			goldFFOut = new FileOutputStream(goldFunctionalOutFile);
			
			oos = new ObjectOutputStream(fout);
			goos = new ObjectOutputStream(goldFOut);
			gfoos = new ObjectOutputStream(goldFFOut);
			
		} catch (FileNotFoundException fnfe) {
			System.err.println("error opening output file: " + fnfe.getMessage());
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("error opening output stream: " + ioe.getMessage());
			ioe.printStackTrace();
		}
		
		
		try {
			oos.writeObject(proteinListMap);
			goos.writeObject(goldProteinListMap);
			gfoos.writeObject(goldFunctionalProteinListMap);
			
			oos.close();
			goos.close();
			gfoos.close();
			fout.close();
			goldFOut.close();
			goldFFOut.close();
		} catch (IOException ioe) {
			System.err.println("error writing to or closing map files: " + ioe.getMessage());
			ioe.printStackTrace();
		}
	
		/*
		//output source text files
		
		PrintWriter	writer = null;
		try {
			writer = new PrintWriter(new FileWriter(outSourceTextFile));
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}
		writer.println(allSource);
		writer.flush();
		writer.close();
		*/
	}//main
}
