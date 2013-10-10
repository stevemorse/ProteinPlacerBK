package NLP;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * The parser for all location subset GO ontology files, one for each cell location
 * supported/searched for by the rule based placer and the learning based placer.
 * Each file contains the GO ontology entries of everything that has a particular supported
 * location (or a synonym) as a ancestor.
 * @author Steve Morse
 * @version 1.0
 */
public class AllValidLocationsLoader {
	private static boolean debug = true;
	
	/**
	 * Gets all the subsets of the GO ontology for all supported cell locations.
	 * @return allLocations a List<Map<String, String>> a alphabetically ordered List of Maps
	 * of GO codes and names for all entries in the GO ontology that are descendants of the
	 * supported cell locations
	 */
	public List<Map<String, String>>loadAll(){
		
		LocationLoader loader = new LocationLoader();
		List<Map<String, String>> allLocations = new ArrayList<Map<String, String>>();
		List<File> allLocationsFiles = new ArrayList<File>();
		
		allLocationsFiles = getFiles();
		ListIterator<File> allLocationsFilesLiter = allLocationsFiles.listIterator();
		while(allLocationsFilesLiter.hasNext()){
			File currentFile = allLocationsFilesLiter.next();
			Map<String, String> currentLocationsMap = loader.loadLocations(currentFile, debug);
			allLocations.add(currentLocationsMap);
		}//while 	
		
		return allLocations;		
	}//AllValidLocationsLoader
	
	/**
	 * Gets all the  GO ontology subset files for the supported cell locations
	 * @return allLocationsFiles a List<File> of all the GO ontology subset files, one for 
	 * each cell location supported/searched for by the rule based placer and the learning based placer
	 */
	public List<File> getFiles(){
		List<File> allLocationsFiles = new ArrayList<File>();
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/locationSets/chloroplast.obo"));
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/locationSets/endoplasmic_reticulum.obo"));
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/locationSets/mitochondria.obo"));
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/locationSets/nucleus.obo"));
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/locationSets/peroxisome.obo"));
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/locationSets/secretory_pathway.obo"));
		return allLocationsFiles;		
	}//getFiles
}
