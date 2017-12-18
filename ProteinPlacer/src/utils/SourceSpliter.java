package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Spits source text data extracted from protein accession pages and parses them into matched lists of 
 * protein sequences and the source and protein feature text data they generates
 * @author steve
 * @version 1.0
 */
public class SourceSpliter {
	private static String sourceBaseString = "/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_";
	static List<List<String>> split = null;
	
	/**
	 * stand alone tester
	 * @param args standard main runtime parameters
	 */
	public static void main (String[] args){
		int fileNumberStart = 0;
		File sourceTextInFile = new File(sourceBaseString + fileNumberStart + "/textOutOfProtiens_" +
				fileNumberStart + ".txt");
		
		//load source text
		char[] sourceInFileBuffer = new char[(int) sourceTextInFile.length()];
		
		//read source data from current file
		try {
			FileReader sourceFileReader = new FileReader(sourceTextInFile);
			sourceFileReader.read(sourceInFileBuffer);
			sourceFileReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}
		String sourcesStr = new String(sourceInFileBuffer);
		split = split(sourcesStr);
					
}//main	
	/**
	 * 
	 * @param sourcesStr the string of source data of sequences and matched source feature text
	 * @return  List<List<String>> of List of protein sequences and synchronous list of source texts
	 */
	public static List<List<String>> split (String sourcesStr){
		List<List<String>> splitSource = new ArrayList<List<String>>();
		List<String> proteinList = new ArrayList<String>();
		List<String> sourceProteinSequences = new ArrayList<String>();
		List<String> sourceTexts = new ArrayList<String>();
		
		String[] proteins = sourcesStr.split("protein:");
		for(int sourcesCount = 1; sourcesCount < proteins.length; sourcesCount++){
			proteinList.add(proteins[sourcesCount]);
		}
		
		ListIterator<String> proteinListIter = proteinList.listIterator();
		int proteinCount = 0;
		while(proteinListIter.hasNext()){
			String current = proteinListIter.next();
			System.out.println(current + "\n");
			int sequenceBegin = current.indexOf("Protien [sequence= ");
			sequenceBegin += "Protien [sequence= ".length();
			int sequenceEnd = current.indexOf(",",sequenceBegin);
			if(sequenceEnd == -1)
			{
				sequenceEnd = current.indexOf(" ",sequenceBegin);
				if(sequenceEnd == -1){
					sequenceEnd = current.indexOf("\\n",sequenceBegin);
				}//if
				
			}//if
			System.out.println("current:\n" + current);
			System.out.println("sequenceBegin " + sequenceBegin + " sequenceEnd " + sequenceEnd + " for protein: " + proteinCount);
			proteinCount++;
			String sequence = current.substring(sequenceBegin, sequenceEnd);
			sequence.trim();
			System.out.println("sequnce is: " + sequence + "\n");
			int sourceTextsBegin = current.indexOf("source and protien feature texts");
			sourceTextsBegin += "source and protien feature texts".length();
			String sourceText = current.substring(sourceTextsBegin);
			sourceText.trim();
			System.out.println("source text is: " + sourceText + "\n\n\n");
			sourceTexts.add(sourceText);
			sourceProteinSequences.add(sequence);	
		}//while
		
		//load both lists into return list and send it back
		splitSource.add(sourceProteinSequences);
		splitSource.add(sourceTexts);
		return splitSource;	
	}
	
	/**
	 * 
	 * @param sourcesStr the string of source data of sequences and matched source feature text
	 * @return  List<List<String>> of List of protein sequences and synchronous list of source texts
	 */
	public static List<List<String>> split (char[] sourceInFileBuffer){
		List<List<String>> splitSource = new ArrayList<List<String>>();
		List<String> proteinList = new ArrayList<String>();
		List<String> sourceProteinSequences = new ArrayList<String>();
		List<String> sourceTexts = new ArrayList<String>();
		
		String sourcesStr = new String(sourceInFileBuffer);
		String[] proteins = sourcesStr.split("protein:");
		for(int sourcesCount = 1; sourcesCount < proteins.length; sourcesCount++){
			proteinList.add(proteins[sourcesCount]);
		}
		
		ListIterator<String> proteinListIter = proteinList.listIterator();
		while(proteinListIter.hasNext()){
			String current = proteinListIter.next();
			System.out.println(current + "\n");
			int sequenceBegin = current.indexOf("Protien [sequence= ");
			sequenceBegin += "Protien [sequence= ".length();
			int sequenceEnd = current.indexOf(",",sequenceBegin);
			String sequence = current.substring(sequenceBegin, sequenceEnd);
			sequence.trim();
			System.out.println("sequnce is: " + sequence + "\n");
			int sourceTextsBegin = current.indexOf("source and protien feature texts");
			sourceTextsBegin += "source and protien feature texts".length();
			String sourceText = current.substring(sourceTextsBegin);
			sourceText.trim();
			System.out.println("source text is: " + sourceText + "\n\n\n");
			sourceTexts.add(sourceText);
			sourceProteinSequences.add(sequence);	
		}//while
		
		//load both lists into return list and send it back
		splitSource.add(sourceProteinSequences);
		splitSource.add(sourceTexts);
		return splitSource;	
	}
}
