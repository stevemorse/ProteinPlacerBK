package NLP;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * The parser for all location subset GO ontology files, one for each functional group
 * supported/searched for by the learning based placer.
 * Each file contains the GO ontology entries of everything that has a particular supported
 * functional groups (or a synonym) as a ancestor.
 * @author Steve Morse
 * @version 1.0
 */
public class AllFunctionalGoAnnotationsLoader {
	private static boolean debug = true;
	
	/**
	 * Gets all the subsets of the GO ontology for all supported functional groups at the Go slim plant level.
	 * @return allLocations a List<Map<String, String>> a alphabetically ordered List of Maps
	 * of GO codes and names for all entries in the GO ontology that are descendants of the
	 * supported functional groups
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
			String fileNamestring = currentFile.getName();
			int peroid = fileNamestring.indexOf(".obo");
			String selfFileNamestring = fileNamestring.substring(0, peroid);
			selfFileNamestring = selfFileNamestring + "_self.obo";
			Map<String, String> selfLocationsMap = loader.loadLocations(currentFile, debug);
			currentLocationsMap.putAll(selfLocationsMap);
			allLocations.add(currentLocationsMap);
		}//while 	
		
		return allLocations;		
	}//AllValidLocationsLoader
	
	/**
	 * Gets all the  GO ontology subset files for the supported functional groups
	 * @return allLocationsFiles a List<File> of all the GO ontology subset files, one for 
	 * each functional groups supported/searched for by the learning based placer
	 */
	public List<File> getFiles(){
		List<File> allLocationsFiles = new ArrayList<File>();
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/functionalGroups/carbohydrateBinding.obo"));
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/functionalGroups/catalyticActivity.obo"));
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/functionalGroups/chromatinBinding.obo"));
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/functionalGroups/dnaBinding.obo"));
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/functionalGroups/enzymeRegulatorActivity.obo"));
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/functionalGroups/lipidBinding.obo"));
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/functionalGroups/nucleotideBinding.obo"));
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/functionalGroups/oxygenBinding.obo"));
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/functionalGroups/proteinBinding.obo"));
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/functionalGroups/receptorActivity.obo"));
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/functionalGroups/rnaBinding.obo"));
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/functionalGroups/signalTransducerActivity.obo"));
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/functionalGroups/ssDnaBindingTFActivity.obo"));
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/functionalGroups/structuralMoleculeActivity.obo"));
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/functionalGroups/translationRegulatorActivity.obo"));
		allLocationsFiles.add(new File ("/home/steve/Desktop/ProteinPlacer/functionalGroups/transporterActivity.obo"));
		return allLocationsFiles;		
	}//getFiles
}
