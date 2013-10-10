package NLP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import protein.Protein;

public class SequenceLoader {
	
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
		for(int seqCount = 0; seqCount < numSeq; seqCount++ ){
			//System.out.println("line: " + seqCount + " is: " + seqs[seqCount]);
		}//for seqCount
		
		for(int seqCounter = 1; seqCounter < numSeq; seqCounter++ ){
			String sequence = seqs[seqCounter].replaceAll("Contig", "");
			sequence = sequence.replaceAll("consensus_sequence", "");
			sequence = sequence.replaceAll("Locus.*Length","");
			sequence = sequence.replaceAll("_\\d++_?", "");
			sequence = sequence.trim();
			//System.out.println(sequence);
			SequenceList.add(sequence);
			Protein currentProtein = new Protein(sequence,"GO");
			proteinList.add(currentProtein);						
		}//for seqCounter	
		
	}//getSequences
	
}
