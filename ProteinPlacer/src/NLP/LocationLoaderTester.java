package NLP;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Map;

public class LocationLoaderTester {
	private static boolean debug = true;
	private static File inLocationsOBOFile = new File ("/home/steve/Desktop/ProteinPlacer/cellular_components.obo");
	public static void main(String[] args){
		Map<String, String> GoAnnotationLocations = new HashMap<String, String>();
		LocationLoader loader = new LocationLoader();
		GoAnnotationLocations = loader.loadLocations(inLocationsOBOFile, debug);
		System.out.println("Total Loaded: " + GoAnnotationLocations.size());
		
		
		
		//out to file 
		List<String> locationsList = new ArrayList<String>(GoAnnotationLocations.values());
		
		File outFile = new File("/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/outLocations.txt");
		PrintWriter writer;
		try {
			writer = new PrintWriter(new FileWriter(outFile.getAbsoluteFile(), true));
		
		ListIterator<String> locationsListIter = locationsList.listIterator();
		while(locationsListIter.hasNext()){
			String location = locationsListIter.next();
			writer.println("location is:" + location);
		}
		//while
		writer.flush();
		writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void ArrayList(Collection<String> values) {
		// TODO Auto-generated method stub
		
	}//main

}	

