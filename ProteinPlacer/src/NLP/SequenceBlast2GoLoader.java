package NLP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import protein.Protein;

public class SequenceBlast2GoLoader {
public void getSequences(File inSequencesFile, List<Protein> proteinList){
		
		char[] sequencesDataBuffer = new char[(int) inSequencesFile.length()];
		List<String> SequenceList = new ArrayList<String>();
		
		//read sequence data
		try {
			FileReader sequenceFileReader = new FileReader(inSequencesFile);
			sequenceFileReader.read(sequencesDataBuffer);
			sequenceFileReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}
		
		String DataStr = new String(sequencesDataBuffer);
		
		//parse sequence data string
		String[] seqs = DataStr.split(">");
		int numSeq = seqs.length;
		/*
		for(int seqCount = 0; seqCount < numSeq; seqCount++ ){
			System.out.println("line: " + seqCount + " is: " + seqs[seqCount]); //if text output and not xml ouput
		}//for seqCount
		*/
		for(int seqCounter = 1; seqCounter < numSeq; seqCounter++ ){
			String sequence = seqs[seqCounter].replaceAll("Contig", "");
			sequence = sequence.replaceAll("consensus_sequence", "");
			sequence = sequence.replaceAll("Locus.*Length","");
			sequence = sequence.replaceAll("_\\d++_?", "");
			sequence = sequence.replaceAll("No_Hits_Assembly_", "");
			sequence = sequence.trim();
			System.out.println("sequence is: " + sequence);
			int beginSequence = seqs[seqCounter].indexOf(sequence);
			System.out.println(seqs[seqCounter] + " length = " + seqs[seqCounter].length() + " index of seq = " + beginSequence); 
			String blast2GoFileName = seqs[seqCounter].substring(0,beginSequence);
			blast2GoFileName = blast2GoFileName.trim();
			/*
			if(blast2GoFileName.contains("/")){
				blast2GoFileName = blast2GoFileName.replace("/", "--");
			}
			*/
			System.out.println("blast2GoFileName: " + blast2GoFileName + "\nsequence: " + sequence);
			SequenceList.add(sequence);
			Protein currentProtein = new Protein(sequence,"GO",blast2GoFileName);
			proteinList.add(currentProtein);
		}//for seqCounter	
		System.out.println("number of proteins loaded: " + proteinList.size());
	}//getSequences
	
}
