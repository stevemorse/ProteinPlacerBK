package NLP;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LocationLoaderTester {
	private static boolean debug = true;
	private static File inLocationsOBOFile = new File ("/home/steve/Desktop/ProteinPlacer/cellular_components.obo");
	public static void main(String[] args){
		Map<String, String> GoAnnotationLocations = new HashMap<String, String>();
		LocationLoader loader = new LocationLoader();
		GoAnnotationLocations = loader.loadLocations(inLocationsOBOFile, debug);
		System.out.println("Total Loaded: " + GoAnnotationLocations.size());
	}//main
	
}
