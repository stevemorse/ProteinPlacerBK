package statistics;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import NLP.AllValidLocationsLoader;
import NLP.OneProteinTextReader;
import protein.Protein;
import utils.SourceSpliter;

/**
 * calculates the statistics for a protein file that allow us to see how well
 * the protein placement algorithms are working. 
 * @author Steve Morse
 * @version 1.0
 */
public class Statistics {
	public static final int CHLOROPLAST = 1;
	public static final int ENDOPLASMIC_RETICULUM = 2;
	public static final int MITOCHONDRION = 3;
	public static final int NUCLEUS = 4;
	public static final int PEROXISOME = 5;
	public static final int SECRETORY_PATHWAY = 6;
	public static final int OTHER = 7;
	//private static File inFile = new File("/home/steve/Desktop/ProteinPlacer/data/allResults.bin");
	private static String proteinsInFileBaseString = "/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_";
	private static File inFile =  new File ("/home/steve/Desktop/ProteinPlacer/data/allRuleBasedResults.bin");
	private static File outFile = new File("/home/steve/Desktop/ProteinPlacer/data/stats.txt");
	//private static File ontologyFile = new File("/home/steve/Desktop/ProteinPlacer/cellular_components.obo");
	//private static File sourceTextInFile = new File("/home/steve/Desktop/ProteinPlacer/data/allSourceText.txt");
	private static String sourceTextInFileBaseString = "/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_";
	private static String inFileBaseString = "/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_";
	
	/**
	 * main method, calculates the statistics and makes output
	 * @param args
	 */
	public static void main (String args[]){
		AllValidLocationsLoader avll = new AllValidLocationsLoader();
		List<Map<String, String>> allLocations = new ArrayList<Map<String, String>>();
		List<Protein> proteinList = new ArrayList<Protein>();
		Map <Integer, List<Protein>> proteinListMap = new HashMap <Integer, List<Protein>>();
		Protein currentProtein = null;
		int matched = 0;
		int notMatched = 0;
		int placedByText = 0;
		int placedByGOTerms = 0;
		int placedByRBS = 0;
		int placedByTextAndRBS = 0;
		int truePositive = 0;
		int falsePositive = 0;
		
		//load location subsets
		allLocations = avll.loadAll();
		//load proteins from current file
		InputStream file = null;
		InputStream buffer = null;
		ObjectInput input = null;		
		try{
			file = new FileInputStream(inFile);
		    buffer = new BufferedInputStream(file);
		    input = new ObjectInputStream (buffer);
		    proteinListMap = (Map <Integer, List<Protein>>) input.readObject();
		    /*
			while(true){
				currentProtein = (Protein) input.readObject();
				proteinList.add(currentProtein);
			}//while
			*/
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
		
		/*
		//load source text
		long fileSizeLong = (long) sourceTextInFile.length();
		System.out.println("fileSizeLong: " + fileSizeLong);
		int fileSizeInt = (int) fileSizeLong;
		System.out.println("fileSizeInt: " + fileSizeInt);
		char[] sourceInFileBuffer = new char[fileSizeInt];
		
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
		//split and process source data
		List<List<String>> proteinDataList = SourceSpliter.split(sourcesStr);
		*/
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(outFile);
		} catch (IOException e) {
			System.out.println("error writing to file: " + e.getMessage());
			e.printStackTrace();
		}
		
		
		for(int fileCount = 0; fileCount < proteinListMap.size(); fileCount++){
			proteinList = proteinListMap.get(fileCount);
			ListIterator<Protein> proteinListIter = proteinList.listIterator();
			System.out.println("number of proteins loaded is: " + proteinList.size());
			//read source data from current file
			String sourceTextInFileName = sourceTextInFileBaseString + fileCount + "/textOutOfProtiens_" + fileCount + ".txt";
			/*
			File sourceTextInFile = new File(sourceTextInFileBaseString + fileCount + "/textOutOfProtiens_" + fileCount + ".txt");
			long fileSizeLong = (long) sourceTextInFile.length();
			int fileSizeInt = (int) fileSizeLong;
			System.out.println("fileSizeInt: " + fileSizeInt);
			char[] sourceInFileBuffer = new char[fileSizeInt];
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
			//split and process source data
			List<List<String>> proteinDataList = SourceSpliter.split(sourcesStr);
			List<List<String>> proteinDataList = SourceSpliter.split(sourceInFileBuffer);
			 */
			while(proteinListIter.hasNext()){
				currentProtein = proteinListIter.next();
				//accumulate stats
				if(currentProtein.getProteinSequences().get(0).compareTo("NOT MATCHED") == 0){
					notMatched++;
				}//if not matched
				else{
					matched++;
				}
				
				if(currentProtein.isPlacedByText()){
					placedByText++;
				}
				
				if(currentProtein.isPlacedByGOTerms()){
					placedByGOTerms++;
				}
				
				if(currentProtein.isPlacedByRBS()){
					placedByRBS++;
				}
				
				if((currentProtein.isPlacedByText() || currentProtein.isPlacedByGOTerms()) && currentProtein.isPlacedByRBS()){
					placedByTextAndRBS++;
					boolean foundInGo = false;
					boolean foundInText = false;
					if(currentProtein.isPlacedByGOTerms()){
						if(currentProtein.getAnnotations().size() > 1){
							List<String> annotationNames = new ArrayList<String>(currentProtein.getAnnotations().values()); 
							ListIterator<String> annotationNamesLiter = annotationNames.listIterator();
							while(annotationNamesLiter.hasNext() && !foundInGo){
								String goName = annotationNamesLiter.next();
								if(isLocationsMatch(currentProtein.getExpressionPointRBS(), goName, allLocations)){	
									foundInGo = true;
								}//if match
							}//while annotationNamesLiter
						}//if annotations size > 1
						else if(currentProtein.getAnnotations().size() == 1){
							if(isLocationsMatch(currentProtein.getExpressionPointRBS(), currentProtein.getExpressionPointGoText(), allLocations)){
								foundInGo = true;
							}//if match
						}//else only one go annotation
						else if(currentProtein.getAnnotations().size() == 0){
							System.err.println("in statitics...found improper loading of protein anotiations");
						}
					}//if currentProtein.isPlacedByGOTerms()
					else if(currentProtein.isPlacedByText()){
						if(isLocationsMatch(currentProtein.getExpressionPointRBS(), currentProtein.getExpressionPointText(), allLocations)){
							foundInText = true;
						}//if match
					}//else if currentProtein.isPlacedByText() 
					else{
						System.err.println("in statitics...found improper loading of expression points");
					}
					if(foundInGo || foundInText){
						truePositive++;
					}//if match found in either
					else{
						falsePositive++;
						//make output for false positive to file
						System.out.println(currentProtein);
						//makeFalsePositiveOutput(fw,proteinDataList,currentProtein);
						makeFalsePositiveOutput(fw,sourceTextInFileName,currentProtein);
						/*
						try{
							fw.write(currentProtein.toString());
						} catch (IOException e) {
							System.out.println("error writing to file: " + e.getMessage());
							e.printStackTrace();
						}
						*/
					}//match not found in either
				}//if placed by the two systems
			}//for fileCount
		}//while
		
		//output stats
		System.out.println("number of proteins loaded is: " + proteinList.size());
		System.out.println("number matched: " + matched);
		System.out.println("number not matched: " + notMatched);
		System.out.println("number of proteins placed by text is: " + placedByText);
		System.out.println("number of proteins placed by Go Terms is: " + placedByGOTerms);
		System.out.println("number of proteins placed by rules is: " + placedByRBS);
		System.out.println("number of proteins placed by text or GO and also rules is: " + placedByTextAndRBS);
		System.out.println("number of true positives is: " + truePositive);
		System.out.println("number of false positives is: " + falsePositive);
		
		
		try {
			fw.write("number of proteins loaded is: " + proteinList.size() + "\n");
			fw.write("number matched: " + matched + "\n");
			fw.write("number not matched: " + notMatched + "\n");
			fw.write("number of proteins placed by text is: " + placedByText + "\n");
			fw.write("number of proteins placed by Go Terms is: " + placedByGOTerms + "\n");
			fw.write("number of proteins placed by rules is: " + placedByRBS + "\n");
			fw.write("number of proteins placed by text or GO and also rules is: " + placedByTextAndRBS + "\n");
			fw.write("number of true positives is: " + truePositive + "\n");
			fw.write("number of false positives is: " + falsePositive + "\n");
			fw.close();
		} catch (IOException e) {
			System.out.println("error writein to file: " + e.getMessage());
			e.printStackTrace();
		}
		try {
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}//main
	
	/**
	 * Discovers if two terms are in the same cell location GO annotation subset
	 * @param exp1 first term
	 * @param exp2 second term
	 * @return boolean - true if they match
	 */
	public static boolean isLocationsMatch(String exp1, String exp2, List<Map<String, String>> allLocations){
		//check if either location is in any of the subtrees of our valid locations
		if(allLocations.get(CHLOROPLAST-1).containsValue(exp1) && allLocations.get(CHLOROPLAST-1).containsValue(exp2)){
			return true;
		}
		if(allLocations.get(ENDOPLASMIC_RETICULUM-1).containsValue(exp1) && allLocations.get(ENDOPLASMIC_RETICULUM-1).containsValue(exp2)){
			return true;
		}
		if(allLocations.get(MITOCHONDRION-1).containsValue(exp1) && allLocations.get(MITOCHONDRION-1).containsValue(exp2)){
			return true;
		}
		if(allLocations.get(NUCLEUS-1).containsValue(exp1) && allLocations.get(NUCLEUS-1).containsValue(exp2)){
			return true;
		}
		if(allLocations.get(PEROXISOME-1).containsValue(exp1) && allLocations.get(PEROXISOME-1).containsValue(exp2)){
			return true;
		}
		if(allLocations.get(SECRETORY_PATHWAY-1).containsValue(exp1) && allLocations.get(SECRETORY_PATHWAY-1).containsValue(exp2)){
			return true;
		}

		return false;
	}//isSubSetOf
	
	/**
	 * 
	 * @param fw FileWriter to use to write to output file
	 * @param proteinDataList List<List<String>> of List of protein sequences and synchronous list of source texts
	 * @param currentProtein the current protein being processed
	 * @return none
	 */
	public static void makeFalsePositiveOutput (FileWriter fw, List<List<String>> proteinDataList, Protein currentProtein){
		
		int indexOfSource = proteinDataList.indexOf(currentProtein.getSequence());
		List<String> sourceText = proteinDataList.get(indexOfSource);
		ListIterator<String>
		sourceTextLiter = sourceText.listIterator();
		String outStr = "";
		while(sourceTextLiter.hasNext()){
			String text = sourceTextLiter.next();
			outStr = outStr + text;
		}
		try{
			fw.write(currentProtein.toString());
			fw.write(outStr);
		} catch (IOException e) {
			System.out.println("error writing to file: " + e.getMessage());
			e.printStackTrace();
		}
	}//makeFalsePositiveOutput
	
	/**
	 * 
	 * @param fw FileWriter to use to write to output file
	 * @param inFileNmae the name of the file to be "streamed" one protein text at a time
	 * @param currentProtein the current protein being processed
	 * @return none
	 */
	public static void makeFalsePositiveOutput (FileWriter fw, String inFileName, Protein currentProtein){
		OneProteinTextReader reader = new OneProteinTextReader(inFileName);
		boolean matched = false;
		String outStr = "";
		
		while(!matched){
			outStr = reader.getOneProteinText();
			if(outStr.contains(currentProtein.getSequence())){
				matched = true;
			}//if contains current sequence
		}//while not matched
		
		try{
			fw.write(currentProtein.toString());
			fw.write(outStr);
		} catch (IOException e) {
			System.out.println("error writing to file: " + e.getMessage());
			e.printStackTrace();
		}
		
	}//makeFalsePositiveOutput
	
}//class
