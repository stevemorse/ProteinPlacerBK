package utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import protein.Protein;

/**
 * Just prints a binary serialized protein file as a text file so 
 * that the results of processing can be browsed at leisure. 
 * @author Steve Morse
 * @version 1.0
 */
public class PrintToFile {

	private static File inFile = new File("/home/steve/Desktop/ProteinPlacer/data/proteinsOut0Test.bin");
	private static File outFile = new File("/home/steve/Desktop/ProteinPlacer/data/proteinTextFile.txt");
	
	/**
	 * main method, does the reading and outputs the text file.
	 * @param args
	 */
	public static void main(String args[]){
		List<Protein> proteinList = new ArrayList<Protein>();
		ObjectInput input = null;
		InputStream fileStream = null;
		try{
		      fileStream = new FileInputStream(inFile);
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
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(outFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ListIterator<Protein> proteinListLiter = proteinList.listIterator();
		while(proteinListLiter.hasNext()){
			Protein currentProtein = proteinListLiter.next();
			try {
				fw.write("Protein:\n" + currentProtein.toString());
			} catch (IOException e) {
				System.out.println("error writein to file: " + e.getMessage());
				e.printStackTrace();
			}
			
		}//while proteinListLiter.hasNext
		
		try {
			fw.close();
		} catch (IOException e) {
			System.out.println("error writein to file: " + e.getMessage());
			e.printStackTrace();
		}
		
	}
}
