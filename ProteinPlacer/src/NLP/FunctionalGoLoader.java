package NLP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads all desired GO ontology go code, go name pairs from text or OBO file.
 * @author Steve Morse
 * @version 1.0
 *
 */
public class FunctionalGoLoader {
	/**
	 * Performs the loading of all GO ontology go code, go name pairs from a text or OBO file.
	 * @param molecularFunction the file to read
	 * @param debug Verbose flag
	 * @return	A Map of all GO ontology go code, go name pairs.
	 */
	public Map<String, String> loadMolecularFunctions(File molecularFunction, boolean debug){
		
		char[] fucntionssOBODataBuffer = new char[(int) molecularFunction.length()];
		Map<String, String> GoAnnotationFunctions = new HashMap<String, String>();
		
		//read sequence data from ontology file
		try {
			FileReader sequenceFileReader = new FileReader(molecularFunction);
			sequenceFileReader.read(fucntionssOBODataBuffer);
			sequenceFileReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}
		
		String locationsStr = new String(fucntionssOBODataBuffer);
		String[] terms = locationsStr.split("\\[Term\\]");
		
		for(int termCount = 1; termCount < terms.length; termCount++){
			if(terms[termCount].contains("namespace: molecular_function")){
				int gObeginPoint = terms[termCount].indexOf("id: GO:") + "id: GO:".length();	
				int nameBeginPoint = terms[termCount].indexOf("name: ") + "name: ".length();
				int nameEndPoint = terms[termCount].indexOf("namespace:");
				String goAnnotationNumber = terms[termCount].substring(gObeginPoint, gObeginPoint + 7);
				String goAnnotationName = terms[termCount].substring(nameBeginPoint,nameEndPoint);
				goAnnotationName = goAnnotationName.trim();
				System.out.println("Loading olecular fnctions...goAnnotationNumber: " + goAnnotationNumber + " is: " + goAnnotationName);
				GoAnnotationFunctions.put(goAnnotationNumber, goAnnotationName);
			}//if a cellular component
		}//for entryCount
		System.out.println("loaded " + GoAnnotationFunctions.size() +" functions");
		return GoAnnotationFunctions;
	}//loadLocations
}
