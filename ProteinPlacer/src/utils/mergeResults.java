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
import java.util.List;
import java.util.ListIterator;

import protein.Protein;

/**
 * Merges binary serialized protein files for chunked fasta files
 * into one large binary serialized protein file.
 * @author Steve Morse
 * @version 1.0
 */
public class mergeResults {
	private static int numFiles = 75;
	private static String proteinsInFileBaseString = "/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_";
	private static File outFile = new File("/home/steve/Desktop/ProteinPlacer/data/allResults.bin");
	private static File goldOutFile = new File("/home/steve/Desktop/ProteinPlacer/data/goldResults.bin");

	/**
	 * main method does the merging, outputs the data in two files,
	 * all the results and gold standard results (placed proteins).
	 * @param args
	 */
	public static void main (String args[]){
		
		List<Protein> proteinList = new ArrayList<Protein>();
		Protein currentProtein = null;
		
		for(int fileCount = 0; fileCount < numFiles; fileCount++){
			String currentInFile = proteinsInFileBaseString + fileCount + "\\proteinsOut_" + fileCount +".bin";
			
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
		try {
			fout = new FileOutputStream(outFile);
			goldFOut = new FileOutputStream(goldOutFile);
			oos = new ObjectOutputStream(fout);
			goos = new ObjectOutputStream(goldFOut);
		} catch (FileNotFoundException fnfe) {
			System.err.println("error opening output file: " + fnfe.getMessage());
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("error opening output stream: " + ioe.getMessage());
			ioe.printStackTrace();
		}
		
		ListIterator<Protein> proteinWriteLiter = proteinList.listIterator();
		while(proteinWriteLiter.hasNext()){
			Protein currentWriteProtein = proteinWriteLiter.next();
			try {
				oos.writeObject(currentWriteProtein);
				if(currentWriteProtein.isPlacedByText() || currentWriteProtein.isPlacedByGOTerms()){
					goos.writeObject(currentWriteProtein);
				}//if placed (and therefore part of gold standard)
			} catch (IOException e) {
				System.out.println("error writein to file: " + e.getMessage());
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
