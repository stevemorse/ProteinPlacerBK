package learningBasedPlacer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import NLP.AllValidLocationsLoader;

import protein.Protein;
/**
 * Converts binary output serialized processed protein files into libsvm input files.
 * @author Steve Morse
 * @version 1.0
 */
public class FeatureBuilder {
	
	private static File inFile = new File("/home/steve/Desktop/ProteinPlacer/data/allResults.bin");
	private static File outFile = new File("/home/steve/Desktop/ProteinPlacer/data/svmFeatures.txt");
	private List<Float> featureValuesForOneEntry = new ArrayList<Float>();
	private Map<String, Integer> valueIndicesMap = new HashMap<String, Integer>();
	
	public static final int CHLOROPLAST = 1;
	public static final int ENDOPLASMIC_RETICULUM = 2;
	public static final int MITOCHONDRION = 3;
	public static final int NUCLEUS = 4;
	public static final int PEROXISOME = 5;
	public static final int SECRETORY_PATHWAY = 6;
	public static final int OTHER = 7;
	
	private int lengthOfSequence = 0;
	
	/**
	 * Method that does the controls the conversion of protein output files into libsvm
	 * input text files, a index integer for a location and a series of label:value pairs for
	 * features.
	 */
	public void build(){
		
		List<Protein> proteinList = new ArrayList<Protein>();
		Protein currentProtein = null;
		
		//load proteins from current file
		InputStream file = null;
		InputStream buffer = null;
		ObjectInput input = null;		
		try{
			file = new FileInputStream(inFile);
		    buffer = new BufferedInputStream(file);
		    input = new ObjectInputStream (buffer);
			while(true){
				currentProtein = (Protein) input.readObject();
				proteinList.add(currentProtein);
			}//while
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
		
		//make output file stream
		PrintWriter	writer = null;
		try {
			writer = new PrintWriter(new FileWriter(outFile));
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}

		//process proteins into a svm instances
		ListIterator<Protein> proteinListIter = proteinList.listIterator();
		while(proteinListIter.hasNext()){
			featureValuesForOneEntry = makeNewValuesForOneFeatureList(valueIndicesMap.size());
			currentProtein = proteinListIter.next();
			String sequence = currentProtein.getProteinSequence();
			sequence = sequence.toLowerCase();
			lengthOfSequence = sequence.length();
			//fill in all single amino and di-amino features
			for(int count = 0; count < lengthOfSequence -1; count++){
				incrementSingleAminoValue(sequence.charAt(count), lengthOfSequence, valueIndicesMap, featureValuesForOneEntry);
				String diAmino = sequence.substring(count, count+1);
				incrementDiAminoValue(diAmino, lengthOfSequence, valueIndicesMap, featureValuesForOneEntry);
			}//for count
			incrementSingleAminoValue(sequence.charAt(lengthOfSequence), lengthOfSequence, valueIndicesMap, featureValuesForOneEntry);
			String outLineString = "";
			outLineString = outLineString + getLocationValue(currentProtein) + " ";
			for(int arrayCount = 0; arrayCount < featureValuesForOneEntry.size(); arrayCount++){
				outLineString = outLineString + arrayCount + ":" + featureValuesForOneEntry.get(arrayCount);
			}//for arrayCount
			writer.println(outLineString);
			writer.flush();
		}//while proteinListIter
		writer.close();
	}//build
	
	/**
	 * Assigns a location value integer code to a protein based on text or GO code
	 * placement data from the protein matched against the subset GO entries for each
	 * supported cell location.
	 * @param currentProtein the protein to have a location value assessed
	 * @return the location value integer code for that protein
	 */
	public int getLocationValue(Protein currentProtein){
		int location = 7;
		AllValidLocationsLoader avll = new AllValidLocationsLoader();
		List<Map<String, String>> allLocations = new ArrayList<Map<String, String>>();
		String textLocation = "NO SUCH LOCATION";
		String goLocation = "NO SUCH LOCATION";
		
		allLocations = avll.loadAll();
		
		if(currentProtein.isPlacedByText()){
			textLocation = currentProtein.getExpressionPointText();
		}//if isPlacedByText 
		if(currentProtein.isPlacedByGOTerms()){
			goLocation = currentProtein.getExpressionPointGoText();
		}//if isPlacedByGOTerms
		
		//check if either location is in any of the subtrees of our valid locations
		if(allLocations.get(CHLOROPLAST-1).containsValue(textLocation) || allLocations.get(CHLOROPLAST-1).containsValue(goLocation)){
			location = CHLOROPLAST;
		}
		if(allLocations.get(ENDOPLASMIC_RETICULUM-1).containsValue(textLocation) || allLocations.get(ENDOPLASMIC_RETICULUM-1).containsValue(goLocation)){
			location = ENDOPLASMIC_RETICULUM;
		}
		if(allLocations.get(MITOCHONDRION-1).containsValue(textLocation) || allLocations.get(MITOCHONDRION-1).containsValue(goLocation)){
			location = MITOCHONDRION;
		}
		if(allLocations.get(NUCLEUS-1).containsValue(textLocation) || allLocations.get(NUCLEUS-1).containsValue(goLocation)){
			location = NUCLEUS;
		}
		if(allLocations.get(PEROXISOME-1).containsValue(textLocation) || allLocations.get(PEROXISOME-1).containsValue(goLocation)){
			location = PEROXISOME;
		}
		if(allLocations.get(SECRETORY_PATHWAY-1).containsValue(textLocation) || allLocations.get(SECRETORY_PATHWAY-1).containsValue(goLocation)){
			location = SECRETORY_PATHWAY;
		}
		
		return location;
		
	}//getLocationValue
	
	/**
	 * increments a single amino acid's ratio of total protein amino acid's value
	 * @param c a single amino acid fasta code
	 * @param lengthOfSequence the total length of the protein sequence
	 * @param valueIndicesMap A Map associating feature names with their indices in 
	 * the featureValuesForOneEntry List
	 * @param featureValuesForOneEntry a list of all the feature values for one input entry
	 */
	public void incrementSingleAminoValue(Character c, int lengthOfSequence, Map<String, Integer> valueIndicesMap, List<Float> featureValuesForOneEntry){
		char[] oneChar = new char[1];
		oneChar[0] = c;
		int valueListIndex = valueIndicesMap.get(new String(oneChar));
		float currentValue = featureValuesForOneEntry.get(valueListIndex);
		float newValue = currentValue + (1/lengthOfSequence);
		featureValuesForOneEntry.set(valueListIndex, newValue);	
	}//incrementSingleAminoValue
	/**
	 * increments a di-amino (a amino acid bigram) ratio of total protein amino acid's value
	 * @param diAmino the bigram to increment the value of
	 * @param lengthOfSequence the total length of the protein sequence
	 * @param valueIndicesMap A Map associating feature names with their indices in 
	 * the featureValuesForOneEntry List
	 * @param featureValuesForOneEntry a list of all the feature values for one input entry
	 */
	public void incrementDiAminoValue(String diAmino, int lengthOfSequence, Map<String, Integer> valueIndicesMap, List<Float> featureValuesForOneEntry){
		int valueListIndex = valueIndicesMap.get(diAmino);
		float currentValue = featureValuesForOneEntry.get(valueListIndex);
		float newValue = currentValue + (1/lengthOfSequence-1);
		featureValuesForOneEntry.set(valueListIndex, newValue);
	}
	
	/**
	 * loads the valueIndicesMap with feature names and indices
	 * @param valueIndicesMap the Map to be loaded with name and index values
	 */
	public void loadValueIndicesMap(Map<String, Integer> valueIndicesMap){
		char[] chars = {'a','p','q','c','r','d','s','e','t','f','g','v','h','w','i','y','k','l','m','n'};
		
		for(int count = 0; count < 20; count++){
			char[] oneChar = new char[1];
			oneChar[0] = chars[count];
			valueIndicesMap.put(new String(oneChar), new Integer(count));
		}//for single amino
		
		for(int count = 20; count < 420; count++){
			char first = chars[count];
			for(int count2 = 0; count2 < 20; count2++){
				char second = chars[count2];
				char[] both = new char[2];
				both[0] = first;
				both[1] = second;
				valueIndicesMap.put(new String(both), new Integer(count));
			}//for count2
		}//for count
	}//loadValueIndicesMap
	
	/**
	 * Makes a new empty (zero filled) feature value list for an data entry
	 * @param sizeOfList the size of the list to make and zero fill
	 * @return the newly minted list
	 */
	public List<Float> makeNewValuesForOneFeatureList(int sizeOfList){
		List<Float> valuesForOneFeature = new ArrayList<Float>();
		for(int count = 0; count < sizeOfList; count++){
			valuesForOneFeature.add(new Float(0.0));
		}//for		
		return valuesForOneFeature;
	}//makeNewvaluesForOneFeatureList
	
	/*
	public void makeBiGrams(){
		File outFile = new File("C:\\Users\\Steve\\Desktop\\ProteinPlacer\\CharsVars.txt");
		
		PrintWriter	writer = null;
		try {
			writer = new PrintWriter(new FileWriter(outFile));
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}

		char[] chars = {'A','P','Q','C','R','D','S','E','T','F','G','V','H','W','I','Y','K','L','M','N'};
		for(int count = 0; count < 20; count++){
			char first = chars[count];
			for(int count2 = 0; count2 < 20; count2++){
				char second = chars[count2];
				char[] both = new char[2];
				both[0] = first;
				both[1] = second;
				String out = new String(both);
				out = "private int " + out + " = " + (count + 21) +";";
				writer.println(out);
				writer.flush();
			}
		}
		writer.close();
	}//makeBiGrams
	*/
}//class
