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
public class LocationLoader {
	
	/**
	 * Performs the loading of all GO ontology go code, go name pairs from a text or OBO file.
	 * @param inLocationsOBOFile the file to read
	 * @param debug Verbose flag
	 * @return	A Map of all GO ontology go code, go name pairs.
	 */
	public Map<String, String> loadLocations(File inLocationsOBOFile, boolean debug){
		
		char[] locationsOBODataBuffer = new char[(int) inLocationsOBOFile.length()];
		Map<String, String> GoAnnotationLocations = new HashMap<String, String>();
		
		//read sequence data from ontology file
		try {
			FileReader sequenceFileReader = new FileReader(inLocationsOBOFile);
			sequenceFileReader.read(locationsOBODataBuffer);
			sequenceFileReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}
		
		String locationsStr = new String(locationsOBODataBuffer);
		String[] terms = locationsStr.split("\\[Term\\]");
		
		for(int termCount = 1; termCount < terms.length; termCount++){
			if(terms[termCount].contains("namespace: cellular_component")){
				int gObeginPoint = terms[termCount].indexOf("id: GO:") + "id: GO:".length();	
				int nameBeginPoint = terms[termCount].indexOf("name: ") + "name: ".length();
				int nameEndPoint = terms[termCount].indexOf("namespace:");
				String goAnnotationNumber = terms[termCount].substring(gObeginPoint, gObeginPoint + 7);
				String goAnnotationName = terms[termCount].substring(nameBeginPoint,nameEndPoint);
				goAnnotationName = goAnnotationName.trim();
				System.out.println("Loading Cell Locations...goAnnotationNumber: " + goAnnotationNumber + " is: " + goAnnotationName);
				GoAnnotationLocations.put(goAnnotationNumber, goAnnotationName);
			}//if a cellular component
		}//for entryCount
		System.out.println("loaded " + GoAnnotationLocations.size() +" locations");
		//trim out bad values (turn up in text in context where they do not indicate expression location)
		GoAnnotationLocations.values().remove("cell");
		GoAnnotationLocations.values().remove("host");
		GoAnnotationLocations.values().remove("membrane");
		GoAnnotationLocations.values().remove("axon");
		GoAnnotationLocations.values().remove("chromosome");
		GoAnnotationLocations.values().remove("organelle");
		System.out.println("after trim " + GoAnnotationLocations.size() +" locations");
		return GoAnnotationLocations;
	}//loadLocations
}
