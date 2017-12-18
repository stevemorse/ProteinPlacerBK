package ruleBasedPlacer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import protein.Protein;

public class ReteDriver {
	
	private static File proteinsInFile = new File ("/home/steve/Desktop/ProteinPlacer/data/allResults.bin");
	//private static File proteinsInFile = new File ("/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_0/proteinsRePassOut_0.bin");
	//private static File proteinsInFile = new File ("/home/steve/Desktop/ProteinPlacer/data/goldResults.bin");
	
	public static void main(String[] args){
		Map<Integer, List<Protein>> allProteins = new HashMap<Integer, List<Protein>>();
		//List<Protein> proteinList = new ArrayList<Protein>();
		ReteProcessor rp = new ReteProcessor();
		allProteins = rp.loadProteins(proteinsInFile);
		System.out.println("allProteins size is:" + allProteins.size());
		rp.processProteins(allProteins);	
	}//main
}
