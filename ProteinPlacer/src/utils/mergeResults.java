package utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
	private static int numFiles = 3;
	private static String proteinsInFileBaseString = "/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_";
	private static File molecularFunction = new File("/home/steve/Desktop/ProteinPlacer/molecular_function.obo");
	private static File outFile = new File("/home/steve/Desktop/ProteinPlacer/data/allResults.bin");
	private static File goldOutFile = new File("/home/steve/Desktop/ProteinPlacer/data/goldResults.bin");
	private static File goldFunctionalOutFile = new File("/home/steve/Desktop/ProteinPlacer/data/goldFunctionalResults.bin");

	/**
	 * main method does the merging, outputs the data in two files,
	 * all the results and gold standard results (placed proteins).
	 * @param args
	 */
	public static void main (String args[]){
		
		List<Protein> proteinList = new ArrayList<Protein>();
		Protein currentProtein = null;
		
		for(int fileCount = 0; fileCount < numFiles; fileCount++){
			String currentInFile = proteinsInFileBaseString + fileCount + "/proteinsOut_" + fileCount +".bin";
			
			//load proteins from current file
			InputStream file = null;
			InputStream buffer = null;
			ObjectInput input = null;		
			try{
				file = new FileInputStream(currentInFile);
			    buffer = new BufferedInputStream(file);
			    input = new ObjectInputStream (buffer);
				while(true){
					currentProtein = (Protein) input.readObject();
					proteinList.add(currentProtein);
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
			
			System.out.println("number of proteins loaded is: " + proteinList.size());			
		}//for fileCount
		
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
		
		//load functional annotations
		FunctionalGoLoader ffgl = new FunctionalGoLoader();
		Map<String, String> GoAnnotationFunctions = ffgl.loadMolecularFunctions(molecularFunction, debug);
		List<String> GoAnnotationFunctionsCodes = (List<String> ) GoAnnotationFunctions.values();
		
		ListIterator<Protein> proteinWriteLiter = proteinList.listIterator();
		while(proteinWriteLiter.hasNext()){
			Protein currentWriteProtein = proteinWriteLiter.next();
			try {
				oos.writeObject(currentWriteProtein);
				if(currentWriteProtein.isPlacedByText() || currentWriteProtein.isPlacedByGOTerms()){
					goos.writeObject(currentWriteProtein);
					//go through current proteins annotations to see if any are functional
					List<String> currentGoCodesList = (List<String>) currentWriteProtein.getAnnotations().values();
					ListIterator<String> currentGoCodesLiter = currentGoCodesList.listIterator();
					boolean done = false;
					while(currentGoCodesLiter.hasNext() && !done){
						String goCode = currentGoCodesLiter.next();
						if(GoAnnotationFunctionsCodes.contains(goCode)){
							gfoos.writeObject(currentWriteProtein);
							done = true;
						}//if placed and has functional annotation(and therefore part of gold functional standard)
					}//while
				}//if placed (and therefore part of gold standard)
			} catch (IOException e) {
				System.out.println("error writing to file: " + e.getMessage());
				e.printStackTrace();
			}
		}//while
		
		try {
			oos.close();
			goos.close();
			fout.close();
			goldFOut.close();
		} catch (IOException ioe) {
			System.err.println("error closing file: " + ioe.getMessage());
			ioe.printStackTrace();
		}
	
	}//main
}
