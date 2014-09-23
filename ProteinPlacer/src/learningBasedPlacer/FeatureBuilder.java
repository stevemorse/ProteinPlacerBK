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

import NLP.AllFunctionalGoAnnotationsLoader;
import NLP.AllValidLocationsLoader;
import protein.Protein;
/**
 * Converts binary output serialized processed protein files into libsvm input files.
 * @author Steve Morse
 * @version 1.0
 */
public class FeatureBuilder {
	
	private boolean functionalIncluded = false;
	private boolean physicalIncluded = false;
	private boolean debug = true;
	
	private static File inFile = new File("/home/steve/Desktop/ProteinPlacer/data/goldResults.bin");
	private static File outFile = new File("/home/steve/Desktop/ProteinPlacer/data/svmFeatures.txt");
	private List<Float> featureValuesForOneEntry = new ArrayList<Float>();
	private Map<String, Integer> valueIndicesMap = new HashMap<String, Integer>();
	private List<Map<String,String>> allFunctionalGOAnnotations = new ArrayList<Map<String,String>>();
	
	public static final int CHLOROPLAST = 1;
	public static final int ENDOPLASMIC_RETICULUM = 2;
	public static final int MITOCHONDRION = 3;
	public static final int NUCLEUS = 4;
	public static final int PEROXISOME = 5;
	public static final int SECRETORY_PATHWAY = 6;
	public static final int OTHER = 7;
	
	public static final int carbohydrateBinding = 0;
	public static final int catalyticActivity = 1;
	public static final int chromatinBinding = 2;
	public static final int dnaBinding = 3;
	public static final int enzymeRegulatorActivity = 4;
	public static final int lipidBinding = 5;
	public static final int nucleotideBinding = 6;
	public static final int oxygenBinding = 7;
	public static final int proteinBinding = 8;
	public static final int receptorActivity = 9;
	public static final int rnaBinding = 10;
	public static final int signalTransducerActivity = 11;
	public static final int ssDnaBindingTFActivity = 12;
	public static final int structuralMoleculeActivity = 13;
	public static final int translationRegulatorActivity = 14;
	public static final int transporterActivity = 15;
	
	private int lengthOfSequence = 0;
	private int numberOfFunctionaCatagories = 16;
	private int numberPhysicalCatagories = 26;
	
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

		//load values map
		loadValueIndicesMap(valueIndicesMap);
		System.out.println(valueIndicesMap.size());
		
		if(functionalIncluded){
			allFunctionalGOAnnotations = loadAllFunctionalGOAnnotations();
		}
		
		//process proteins into a svm instances\
		boolean done = false;
		ListIterator<Protein> proteinListIter = proteinList.listIterator();
		while(proteinListIter.hasNext() && !done){
			featureValuesForOneEntry = makeNewValuesForOneFeatureList(valueIndicesMap.size());
			currentProtein = proteinListIter.next();
			String sequence = currentProtein.getProteinSequences().get(0);
			sequence = sequence.toLowerCase();
			lengthOfSequence = sequence.length();
			//fill in all single amino and di-amino features
			for(int count = 0; count < lengthOfSequence -1; count++){
				incrementSingleAminoValue(sequence.charAt(count), lengthOfSequence, valueIndicesMap, featureValuesForOneEntry);
				String diAmino = sequence.substring(count, count+2);
				incrementDiAminoValue(diAmino, lengthOfSequence, valueIndicesMap, featureValuesForOneEntry);
			}//for count
			incrementSingleAminoValue(sequence.charAt(lengthOfSequence-1), lengthOfSequence, valueIndicesMap, featureValuesForOneEntry);
			String outLineString = "";
			outLineString = outLineString + getLocationValue(currentProtein) + " ";
			for(int arrayCount = 0; arrayCount < featureValuesForOneEntry.size(); arrayCount++){
				outLineString = outLineString + arrayCount + ":" + featureValuesForOneEntry.get(arrayCount) + " ";
			}//for arrayCount
			if(functionalIncluded){
				List<Integer> functionalValues = getFunctionalAnnotations(currentProtein, allFunctionalGOAnnotations);
				for(int functionalCount = featureValuesForOneEntry.size(); 
						functionalCount < featureValuesForOneEntry.size() + numberOfFunctionaCatagories;
						functionalCount++){
					outLineString = outLineString + functionalCount + ":" 
						+ functionalValues.get(functionalCount - featureValuesForOneEntry.size()) + " ";
				}//for
			}//if functionalIncluded
			if(physicalIncluded){
				int functionalCatagories = 0;
				if(functionalIncluded){
					functionalCatagories = numberOfFunctionaCatagories;
				}
				List<Double> physicalValues = PhysicalFeatureSetter.setPhysicalFeatures(currentProtein, functionalCatagories);
				for(int physicalCount = featureValuesForOneEntry.size(); 
						physicalCount < featureValuesForOneEntry.size() + physicalValues.size() + functionalCatagories;
						physicalCount++){
					outLineString = outLineString + physicalCount + ":" 
						+ physicalValues.get(physicalCount - featureValuesForOneEntry.size()) + " ";
				}//for
			}
			writer.println(outLineString);
			System.out.println(outLineString);
			writer.flush();
			done = true;
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
		String charStr = new String(oneChar);
		int valueListIndex = valueIndicesMap.get(charStr);
		float currentValue = featureValuesForOneEntry.get(valueListIndex);
		float newValue = currentValue + (1/(float)lengthOfSequence);
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
		float newValue = currentValue + (1/(float)(lengthOfSequence-1));
		featureValuesForOneEntry.set(valueListIndex, newValue);
		System.out.println("diamino: " + diAmino + " valueListIndex: " + valueListIndex + " + currentValue: " + currentValue +  " newValue: " + newValue);
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
		
		for(int count = 0; count < 20; count++){
			char first = chars[count];
			for(int count2 = 0; count2 < 20; count2++){
				char second = chars[count2];
				char[] both = new char[2];
				both[0] = first;
				both[1] = second;
				valueIndicesMap.put(new String(both), new Integer(count * 20 + count2 + 20));
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
	
	public List<Map<String,String>> loadAllFunctionalGOAnnotations(){
		List<Map<String,String>> allFunctionalGpLocatains = new ArrayList<Map<String,String>>();
		AllFunctionalGoAnnotationsLoader afgal = new AllFunctionalGoAnnotationsLoader();
		allFunctionalGpLocatains = afgal.loadAll();
		return allFunctionalGpLocatains;	
	}//loadAllFunctionalGOAnnotations
	
	public List<Integer> getFunctionalAnnotations(Protein currentProtein, List<Map<String,String>> allFunctionalGOAnnotations){
		List<Integer> functionals = new ArrayList<Integer>();
		for(int functionalsCount = 0; functionalsCount < numberOfFunctionaCatagories; functionalsCount++){
			functionals.add(0);
		}
		List<String> currentGoAnnotations = new ArrayList<String>(currentProtein.getAnnotations().values());
		ListIterator<String> currentGoAnnotationsLiter = currentGoAnnotations.listIterator();
		while(currentGoAnnotationsLiter.hasNext()){
			String currentValue = currentGoAnnotationsLiter.next();
			//check if functional annotation in any of our valid sets of interest
			Integer idx  = null;
			if(allFunctionalGOAnnotations.get(carbohydrateBinding).containsValue(currentValue)){
				idx = functionals.get(carbohydrateBinding);
				idx++;
				functionals.set(carbohydrateBinding, idx);			
			}
			if(allFunctionalGOAnnotations.get(catalyticActivity).containsValue(currentValue)){
				idx = functionals.get(catalyticActivity);
				idx++;
				functionals.set(catalyticActivity, idx);			
			}
			if(allFunctionalGOAnnotations.get(chromatinBinding).containsValue(currentValue)){
				idx = functionals.get(chromatinBinding);
				idx++;
				functionals.set(chromatinBinding, idx);			
			}
			if(allFunctionalGOAnnotations.get(dnaBinding).containsValue(currentValue)){
				idx = functionals.get(dnaBinding);
				idx++;
				functionals.set(dnaBinding, idx);			
			}
			if(allFunctionalGOAnnotations.get(enzymeRegulatorActivity).containsValue(currentValue)){
				idx = functionals.get(enzymeRegulatorActivity);
				idx++;
				functionals.set(enzymeRegulatorActivity, idx);			
			}
			if(allFunctionalGOAnnotations.get(lipidBinding).containsValue(currentValue)){
				idx = functionals.get(lipidBinding);
				idx++;
				functionals.set(lipidBinding, idx);			
			}
			if(allFunctionalGOAnnotations.get(nucleotideBinding).containsValue(currentValue)){
				idx = functionals.get(nucleotideBinding);
				idx++;
				functionals.set(nucleotideBinding, idx);			
			}
			if(allFunctionalGOAnnotations.get(oxygenBinding).containsValue(currentValue)){
				idx = functionals.get(oxygenBinding);
				idx++;
				functionals.set(oxygenBinding, idx);			
			}
			if(allFunctionalGOAnnotations.get(proteinBinding).containsValue(currentValue)){
				idx = functionals.get(proteinBinding);
				idx++;
				functionals.set(proteinBinding, idx);			
			}
			if(allFunctionalGOAnnotations.get(receptorActivity).containsValue(currentValue)){
				idx = functionals.get(receptorActivity);
				idx++;
				functionals.set(receptorActivity, idx);			
			}
			if(allFunctionalGOAnnotations.get(rnaBinding).containsValue(currentValue)){
				idx = functionals.get(rnaBinding);
				idx++;
				functionals.set(rnaBinding, idx);			
			}
			if(allFunctionalGOAnnotations.get(signalTransducerActivity).containsValue(currentValue)){
				idx = functionals.get(signalTransducerActivity);
				idx++;
				functionals.set(signalTransducerActivity, idx);			
			}
			if(allFunctionalGOAnnotations.get(ssDnaBindingTFActivity).containsValue(currentValue)){
				idx = functionals.get(ssDnaBindingTFActivity);
				idx++;
				functionals.set(ssDnaBindingTFActivity, idx);			
			}
			if(allFunctionalGOAnnotations.get(structuralMoleculeActivity).containsValue(currentValue)){
				idx = functionals.get(structuralMoleculeActivity);
				idx++;
				functionals.set(structuralMoleculeActivity, idx);			
			}
			if(allFunctionalGOAnnotations.get(translationRegulatorActivity).containsValue(currentValue)){
				idx = functionals.get(translationRegulatorActivity);
				idx++;
				functionals.set(translationRegulatorActivity, idx);			
			}
			if(allFunctionalGOAnnotations.get(transporterActivity).containsValue(currentValue)){
				idx = functionals.get(transporterActivity);
				idx++;
				functionals.set(transporterActivity, idx);			
			}
		}//while
		return functionals;	
	}//getFunctionalAnnotations
	
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
