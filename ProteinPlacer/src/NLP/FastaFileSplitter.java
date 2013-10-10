package NLP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Splits a fasta file into bite-sized (1000 sequence) chunks
 * @author Steve Morse
 * @version 1.0
 */
public class FastaFileSplitter {
	private static File inSequencesFile = new File ("/home/steve/Desktop/ProteinPlacer/VelvetAssembly.fasta");
	char[] sequencesDataBuffer = new char[(int) inSequencesFile.length()];
	
	/**
	 * does the fasta file splitting, puts out text files labeled in order
	 */
	public void split(){
		//read sequence data
		try {
			FileReader sequenceFileReader = new FileReader(inSequencesFile);
			sequenceFileReader.read(sequencesDataBuffer);
			sequenceFileReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException ioe) {
			System.out.println("IOException: " + ioe.getMessage());
			ioe.printStackTrace();
		}
		
		String DataStr = new String(sequencesDataBuffer);
		
		//parse sequence data string
		String[] seqs = DataStr.split(">");
		int numSeq = seqs.length;
		for(int seqCount = 0; seqCount < numSeq; seqCount++ ){
			//System.out.println("line: " + seqCount + " is: " + seqs[seqCount]);
		}//for seqCount
	
		
		
		int numFiles = 0;
		FileWriter fout = null;
		int perFileCount = 0;
		for(int seqCount = 1; seqCount < numSeq; seqCount++ ){
			if(perFileCount == 0){
				String sequencesOutFile = "C:/Users/Steve/Desktop/ProteinPlacer/data/Fasta" + numFiles + ".txt";
				try {
					fout = new FileWriter(sequencesOutFile); 
				} catch (FileNotFoundException fnfe) {
					System.err.println("error opening output file: " + fnfe.getMessage());
					fnfe.printStackTrace();
				} catch (IOException ioe) {
					System.err.println("error opening output stream: " + ioe.getMessage());
					ioe.printStackTrace();
				}
			}
				
				try {
					fout.write(">" + seqs[seqCount] + "\n");
				} catch (IOException ioe) {
					System.err.println("error writing object to file: " + ioe.getMessage());
					ioe.printStackTrace();
				}
				
				perFileCount++;
				if(perFileCount == 1000){
					perFileCount = 0;
					numFiles++;
					try {
						fout.close();
					} catch (IOException ioe) {
						System.err.println("error closing file: " + ioe.getMessage());
						ioe.printStackTrace();
					}
				}
		}//for seq count
		
	}//method split

	
}//class
