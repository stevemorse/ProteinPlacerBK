package ruleBasedPlacer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import protein.Protein;

public class ReteDriver {
	
	private static File proteinsInFile = new File ("/home/steve/Desktop/ProteinPlacer/data/allResults.bin");
	//private static File proteinsInFile = new File ("/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_0/proteinsRePassOut_0.bin");
	//private static File proteinsInFile = new File ("/home/steve/Desktop/ProteinPlacer/data/goldResults.bin");
	
	public static void main(String[] args){
		List<Protein> proteinList = new ArrayList<Protein>();
		ReteProcessor rp = new ReteProcessor();
		proteinList = rp.loadProteins(proteinsInFile);
		rp.processProteins(proteinList);	
	}//main
}
