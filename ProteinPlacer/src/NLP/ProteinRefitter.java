package NLP;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;

import org.openqa.selenium.remote.UnreachableBrowserException;

import protein.Protein;

import protein.Protein;

/**
 * The ProteinRefitter reads in a set of Proteins and matches each protein with all text entries for that
 * protein in the partitioned debug output files made by the TextFilePartitionTool.  It then uses the 
 * selected fields of the NIH genbank text collected as debug data to re-fit the expression point and 
 * matched flags foe each protein before writting all the re-fitted proteins to a new file (and 
 * writing texts of the re-fitted proteins to another file)
 * @author steve morse
 * @version 1.0
 */
public class ProteinRefitter {
	private static File inLocationsOBOFile = new File ("/home/steve/Desktop/ProteinPlacer/cellular_components.obo");
	private static String proteinInBaseStr = "/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_";
	private static String proteinInMidStr = "/proteinsOut_";
	private static String proteinBinStr = ".bin";
	private static String proteinTextStr = ".txt";
	private static String refittedProteinsOutBaseStr = "/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_";
	private static String textInFileBaseStr = "/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_";
	private static String refittedProteinsDirStr =  "/refitted";
	private static String textInFileDirStr =  "/partitionedTexts";
	private static String textInFileEndStr = ".txt";
	private static List<Protein> proteinList = null;
	private static List<Protein> refittedProteinList = null;
	private static List<String> allPartionCodes = new ArrayList<String>();
	private static int fileNumber = 0;
	private static Map<String,String> allPartitionFilenamesByCodeMap = new HashMap<String,String>();
	private static boolean debug = true;
	
	/**
	 * The main driver method for the class
	 * @param args
	 */
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		ProteinRefitter refitter = new ProteinRefitter();
		refittedProteinList = new ArrayList<Protein>();		
		fileNumber = refitter.getFileNumberFromConsole();
		proteinList = refitter.getProteinList(fileNumber);
		allPartitionFilenamesByCodeMap = refitter.getAllPartitionFilenamesByCodeMap();
		refittedProteinList = refitter.refitProteins();
		refitter.writeRefittedProteins(refittedProteinList);	
		long elapsedTime = System.currentTimeMillis() - startTime;
		int hours = (int) ((elapsedTime/1000)/3600);
		int minutes  = (int) (elapsedTime - (hours * (1000 * 3600)))/(1000 * 60);
		int seconds = (int)  (elapsedTime - ((hours * (1000 * 3600)) + (minutes * (1000 * 60))))/1000;
		System.out.println("Systemb Terminates Normally in: " + hours + " Hours " + minutes + " minutes and " + seconds + " seconds ");
	}//main
	
	public ProteinRefitter(){}
	
	public List<Protein> refitProteins(){
		Map<String, String> GoAnnotationLocations = new HashMap<String, String>();
		LocationLoader loader = new LocationLoader();
		GoAnnotationLocations = loader.loadLocations(inLocationsOBOFile, debug);
		System.out.println("Total Loaded: " + GoAnnotationLocations.size());
		
		//process each protein
		List<String> texts = null;
		int proteinCount = 0;
		int numProtienMatched = 0;
		int fnumFeartureMatched = 0;
		int numMatched = 0;
		ListIterator<Protein> proteinListIterator = proteinList.listIterator();
		while(proteinListIterator.hasNext()){
			Protein currentProtein = proteinListIterator.next();
			proteinCount++;
			texts = this.getAllSequenceMatchesFromText(allPartitionFilenamesByCodeMap, currentProtein, proteinCount);
			System.out.println("term count is: " + texts.size() + " for protein number: " + proteinCount);
			
			for(int textsCount = 1; textsCount < texts.size(); textsCount++){
				boolean matched = false;
				boolean featureMatched = false;
				boolean proteinMatched = false;
				String featureText = "";
				String[]  splitTerms = texts.get(textsCount).split("source and protien feature texts");
				String protienText = splitTerms[0];
				if(splitTerms.length == 2){
					featureText = splitTerms[1];
				}
				else{
					featureText = "";
				}
				List<String> cellLocationNames = new ArrayList<String>(GoAnnotationLocations.values());
				ListIterator<String> cellLocationNamesLiter = cellLocationNames.listIterator();
				while(cellLocationNamesLiter.hasNext()  && !matched){					
					String cellLocationName = cellLocationNamesLiter.next();
					String seperateName = " " + cellLocationName.toLowerCase() + " ";
					if(!featureMatched){
						if((featureText.toLowerCase()).contains((seperateName.toLowerCase()))){
							//foundRegion = cellLocationName;
							featureMatched = true;
							numMatched++;
							fnumFeartureMatched++;
							System.out.println("MATCH FOUND IN FEATURES!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:" + seperateName);
							//set found status and location in the protein itself
							currentProtein.setPlacedByText(true);
							currentProtein.setExpressionPointText(seperateName.trim());
						}//if term in feature\
					}//if not featureMatched
					if(!proteinMatched){
						if((protienText.toLowerCase()).contains((seperateName.toLowerCase()))){
							//foundRegion = cellLocationName;
							proteinMatched = true;
							numMatched++;
							numProtienMatched++;
							System.out.println("MATCH FOUND IN PROTIENS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:" + seperateName);
							//set found status and location in the protein itself
							currentProtein.setPlacedByGOTerms(true);
							currentProtein.setExpressionPointGOText((seperateName.toLowerCase()));
						}//if term in GO codes
					}//if not proteinMatched
					if(featureMatched && proteinMatched){
						matched = true;
					}
				}//while termsLiter	
			}//for 
			//write re-fitted protein to object and text files
			refittedProteinList.add(currentProtein);
		}//while proteinListIterator
		System.out.println("done...matched count is: " + numMatched + " feature matched: " + fnumFeartureMatched
				+ " protien matched: " + numProtienMatched);
		return refittedProteinList;
	}//method refitProteins
		
	
	public List<Protein> getProteinList(int fileNumber){
		File proteinInFile = new File("roteinInBaseStr + fileNumber + proteinInMidStr + fileNumber + proteinTextStr");
		proteinList = new ArrayList<Protein>();
		ObjectInput input = null;
		InputStream fileStream = null;
		System.out.println("protein file size = " + proteinInFile.length());
		try{
		      fileStream = new FileInputStream(proteinInFile);
		      InputStream buffer = new BufferedInputStream(fileStream);
		      input = new ObjectInputStream (buffer);
		      while(true){
		    	  Protein currentProtein = (Protein)input.readObject();
		    	  proteinList.add(currentProtein);
		      }//try
		} catch(ClassNotFoundException cnfe){
		      System.out.println("class not found: " + cnfe.getMessage());
	    } catch(IOException ioe){
	    	
	    }//catch		
		System.out.println("number of proteins is: " + proteinList.size());
		
		return proteinList;
	}//method getProteinList
	
	public Map<String,String> getAllPartitionFilenamesByCodeMap(){
		Map<String,String> allPartitionFilenamesByCodeMap = new HashMap<String,String>();
		//make partition codes
		char[] gtac = {'G','T','A','C'};
		for(char c1 = 0; c1 < 4; c1++){
			for(char c2 = 0; c2 < 4; c2++){
				for(char c3 = 0; c3 < 4; c3++){
					String currentPartitionCode =  (Character.toString(gtac[c1])) + 
							(Character.toString(gtac[c2])) +
							(Character.toString(gtac[c3]));
					System.out.println(currentPartitionCode);
					allPartionCodes.add(currentPartitionCode);
				}//for c3
			}//for c2
		}//for c1

		//make and put all file names in Map
		ListIterator<String> allPartionCodesIter = allPartionCodes.listIterator();
		while(allPartionCodesIter.hasNext()){
			String currentPartitionCode = allPartionCodesIter.next();
			String filename = textInFileBaseStr + fileNumber + currentPartitionCode + textInFileEndStr;
			File file = new File(filename);
			System.out.println("partition text file size = " + file.length());
			allPartitionFilenamesByCodeMap.put(currentPartitionCode, filename);
		}//while has next
		return allPartitionFilenamesByCodeMap;
	}//method getAllPartitionFilenamesByCodeMa
	
	public int getFileNumberFromConsole(){
		Scanner scanner = new Scanner(System.in);
		int fileNumber = 0;
		System.out.println("Please enter the file number of the file to process:");
		fileNumber = scanner.nextInt();
		return fileNumber;
	}//method getFileNumberFromConsole   
	
	boolean isSameSequence(String textOfProtein, Protein currentProtien){
		int endOfSequence = textOfProtein.indexOf("proteinSequences = proteinSequence: 0");
		String sequence = textOfProtein.substring(0,endOfSequence);
		return (sequence.contentEquals(currentProtien.getSequence()));
	}//method isSameSequence
	
	List<String> getAllSequenceMatchesFromText(Map<String,String> allPartitionFilesByCodeMap, Protein currentProtein, int currentProteinNumber) {
		String currentSequence = currentProtein.getSequence();
		String partitionCode = currentSequence.trim().substring(0,3);
		String filename = allPartitionFilesByCodeMap.get(partitionCode);
		FileInputStream sequenceFileReader = null;
		//int numTimesCalled = 0;
		List<String> allMatchingTexts = new ArrayList<String>();
		byte[] partitionDataBuffer = new byte[(int) new File(filename).length()];
		try {
			sequenceFileReader = new FileInputStream(filename);
			sequenceFileReader.read(partitionDataBuffer);
			sequenceFileReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String patitionTextStr = new String(partitionDataBuffer);
		String[] texts = patitionTextStr.split("protein: Protien [sequence=");
		for(int textCount = 1; textCount < texts.length; textCount++){
			if(isSameSequence(texts[textCount], currentProtein)){
				allMatchingTexts.add(texts[textCount]);
			}//if
		}//for textCount
		System.out.println("Processed protein: " + currentProteinNumber  + " found: " + allMatchingTexts.size() + " sequence matches in partitioned text\n");	
		return allMatchingTexts;
	}//method getAllSequenceMatchesFromText
	
	public void writeRefittedProteins(List<Protein> refittedProteinList){
		String outDirStr = refittedProteinsOutBaseStr + fileNumber + refittedProteinsDirStr;
		String objectsOutStr = refittedProteinsOutBaseStr + fileNumber + refittedProteinsDirStr + "/" + "proteins" + proteinBinStr;
		String textOutStr = refittedProteinsOutBaseStr + fileNumber + refittedProteinsDirStr + "/" + "textsOfProteins" + proteinTextStr;
		System.out.println("place to write objects is\n" + objectsOutStr);
		System.out.println("place to write texts is\n" + textOutStr);
		
		//make directory if not already made
		File dir = new File(outDirStr);
		if(!dir.exists()){
			dir.mkdir();
		}//if
		
		FileOutputStream fout;
		ObjectOutputStream oos = null;
		try {
			fout = new FileOutputStream(objectsOutStr);
			oos = new ObjectOutputStream(fout);
		} catch (FileNotFoundException fnfe) {
			System.err.println("error opening output file: " + fnfe.getMessage());
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("error opening output stream: " + ioe.getMessage());
			ioe.printStackTrace();
		}
		
		File textFile = new File(textOutStr);
		PrintWriter textWriter = null;
		try {
			textWriter = new PrintWriter(new FileWriter(textFile.getAbsoluteFile(), true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//catch
		
		ListIterator<Protein> proteinListLiter = refittedProteinList.listIterator();
		Protein currentProtein = null;
		while(proteinListLiter.hasNext()){
			currentProtein = proteinListLiter.next();
			textWriter.write(currentProtein.toString());
			System.out.println("wrote to text file:/n" + currentProtein.toString());
			try {
				oos.writeObject(currentProtein);
				System.out.println("wrote refitted protein to object file");
			} catch (IOException ioe) {
				System.err.println("error writing to output stream: " + ioe.getMessage());
				ioe.printStackTrace();
			}//catch
		}//while proteinListLiter
		
		try {
			oos.flush();
			oos.close();
			textWriter.flush();
			textWriter.close();
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
			System.err.println("failure to flush and/or close oos and/or text file writer " + ioe.getMessage());
			ioe.printStackTrace();
		}//catch	
	}//method writeReffitedProteins
	
}//class

