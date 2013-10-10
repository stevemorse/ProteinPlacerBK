package NLP;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import protein.Protein;

public class SequenceLoaderTester {

	private static File inSequencesFile = new File ("/home/steve/Desktop/ProteinPlacer/data/fasta0.txt");
	
	public static void main(String[] args){
		List<Protein> proteinList = new ArrayList<Protein>();
		SequenceBlast2GoLoader sl = new SequenceBlast2GoLoader();
		sl.getSequences(inSequencesFile, proteinList);
		ListIterator<Protein> Liter = proteinList.listIterator();
		/*
		while(Liter.hasNext()){
			Protein current = Liter.next();
			System.out.println(current.toString());
		}//while
		*/
	}//main
}//class
