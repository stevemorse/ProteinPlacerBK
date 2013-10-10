package NLP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OntologyMapParser {
	public static void main(String args[]){
		HashMap <String, Set<String>> parseMap = new HashMap <String, Set<String>>();
		File ontologyFile = new File("/home/steve/Desktop/ProteinPlacer/cellular_components.obo");
		parseOntology(parseMap,ontologyFile);
	}
	
	public static void parseOntology(Map<String, Set<String>> parseMap, File ontologyFile){
		//read in ontology file as text
		char[] sequencesDataBuffer = new char[(int) ontologyFile.length()];
		List<String> SequenceList = new ArrayList<String>();
		
		//read data
		try {
			FileReader sequenceFileReader = new FileReader(ontologyFile);
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
		
		//parse data string
		String[] seqs = DataStr.split("/[Term/]");
		int numSeq = seqs.length;
		System.out.println("length is: " + seqs.length);
		for(int seqCount = 1; seqCount < 100; seqCount++ ){
		//for(int seqCount = 1; seqCount < numSeq; seqCount++ ){
			System.out.println("term: " + seqCount + " is: " + seqs[seqCount]);
			int nameBeginPosition = seqs[seqCount].indexOf("name:");
			int NameEndPosition = seqs[seqCount].indexOf("namespace:");
			String name = seqs[seqCount].substring(nameBeginPosition, NameEndPosition);
			name = name.toLowerCase();
			name = name.trim();
		}//for seqCount
		
		
	}
}
