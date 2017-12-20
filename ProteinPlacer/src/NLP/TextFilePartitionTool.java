package NLP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;

/**
 * Partitions the very large debug text out of proteins file into much smaller text files each
 * containing a set of text of proteins sorted by the first letters of their sequence.
 * These smaller files are under maxint bytes in size and can be loaded into memory directly.
 * @author Steve Morse
 * @version 1.0
 */
public class TextFilePartitionTool {
	private static String textInFileBaseStr1 = "/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_";
	private static String textInFileBaseStr2 = "/textOutOfProtiens_";
	private static String textInFileBaseStr3 = ".txt";
	private static Map<String,String> allPartitionFilenamesByCodeMap = new HashMap<String,String>();
	Map<String,PrintWriter> allPartitionFilesByCodeMap = new HashMap<String,PrintWriter>();
	private static boolean debug = false;
	
	/**
	 * Driver method for the class
	 */
	public static void main(String[] args){
		long startTime = System.currentTimeMillis();
		TextFilePartitionTool tool = new TextFilePartitionTool();
		int fileNumber = tool.getFileNumberFromConsole();
		tool.partitionFile(fileNumber);
		long elapsedTime = System.currentTimeMillis() - startTime;
		int hours = (int) ((elapsedTime/1000)/3600);
		int minutes  = (int) (elapsedTime - (hours * (1000 * 3600)))/(1000 * 60);
		int seconds = (int)  (elapsedTime - ((hours * (1000 * 3600)) + (minutes * (1000 * 60))))/1000;
		System.out.println("System Terminates Normally in: " + hours + " Hours " + minutes + " minutes and " + seconds + " seconds ");
	}//main
	
	/**
	 * Default constructor
	 */
	public TextFilePartitionTool(){}
	
	/**
	 * Gets the number of the FASTA results file to partition
	 */
	public int getFileNumberFromConsole(){
		Scanner scanner = new Scanner(System.in);
		int fileNumber = 0;
		System.out.println("Please enter the file number of the file to process:");
		fileNumber = scanner.nextInt();
		return fileNumber;
	}//method getFileNumberFromConsole   
	
	/**
	 * Reads each protein text from the debug output and writes it to the appropriate partition
	 * @return void
	 * @param fileNumber the results file of 1000 processed FASTA sequences to look for the debug file in
	 */
	public void partitionFile(int fileNumber){
		String textInFileStr = textInFileBaseStr1 + fileNumber + textInFileBaseStr2 + fileNumber + textInFileBaseStr3;
		int numTimesCalled = 0;
		String currentProteinText = "";
		FileReader sequenceFileReader = null;
		try {
			sequenceFileReader = new FileReader(textInFileStr);
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}//catch
		
		allPartitionFilesByCodeMap = openOutputFiles(fileNumber);
		MyMutableBoolean lastString = new MyMutableBoolean(false);
		MyMutableLong fileCharCount = new MyMutableLong(0L);
		while(!lastString.isB()){
			if(debug){
				System.out.println("make call to getOneProteinText");
			}//if debug
			currentProteinText = getOneProteinText(sequenceFileReader, textInFileStr, numTimesCalled, lastString, fileCharCount);
			if(debug){
				System.out.println("back from  call to getOneProteinText with lastString value: " + lastString.toString());
			}//if debug
			if(!currentProteinText.contentEquals("first call\n")){
				if(debug){
					System.out.println("make call to method writeToPartitionFile");
				}//if debug
				writeToPartitionFile(currentProteinText, allPartitionFilesByCodeMap);
				if(debug){
					System.out.println("back from call to method writeToPartitionFile for write of entry: " + numTimesCalled);
				}//if debug
			}//if not first call
			numTimesCalled++;
		}//while not EOF
		if(debug){
			System.out.println("about to call cleanup");
		}//if debug
		cleanup(allPartitionFilesByCodeMap);
		if(debug){
			System.out.println("back frmo call to cleanup");
		}//if debug
	}//method partitionFile
	
	/**
	 * Reads each protein text from the debug output and writes it to the appropriate partition
	 * @return allPartionCodes a Map<String,PrintWriter> keyed by partition codes of the first letters
	 * of a protein texts sequence with values of PrintWriter for each partition file 
	 * @param fileNumber the results file of 1000 processed FASTA sequences to look for the debug file in
	 */
	public Map<String,PrintWriter> openOutputFiles(int fileNumber){
		String directoryStr = "/partitionedTexts";
		String outputFileBaseStr = textInFileBaseStr1 + fileNumber + directoryStr;
		String endBaseStr = ".txt";
		List<String> allPartionCodes = new ArrayList<String>();
		Map<String,PrintWriter> allPartitionFilesByCodeMap = new HashMap<String,PrintWriter>();
		
		//make directory if not already made
		File dir = new File(outputFileBaseStr);
		if(!dir.exists()){
			dir.mkdir();
		}//if
		outputFileBaseStr += "/";
		
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
		
		//now make all files writers and stuff them all into a hHshMap as values keyed by partition codes
		ListIterator<String> allPartionCodesIter = allPartionCodes.listIterator();
		while(allPartionCodesIter.hasNext()){
			String currentPartitionCode = allPartionCodesIter.next();
			String filename = outputFileBaseStr + currentPartitionCode + endBaseStr;
			System.out.println("filename: " + filename);	
			File currentFile = new File(filename);
			PrintWriter currentWriter = null;
			try {
				currentWriter = new PrintWriter(new FileWriter(currentFile.getAbsoluteFile(), true));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}//catch
			allPartitionFilenamesByCodeMap.put(currentPartitionCode, filename);
			allPartitionFilesByCodeMap.put(currentPartitionCode, currentWriter);	
		}//while has next
		return allPartitionFilesByCodeMap;
	}//method openOutputFiles
	
	/**
	 * Writes each protein text from the debug output and writes it to the appropriate partition
	 * @return void
	 * @param currentProteinText a String of the protein text to be written to its proper partition file 
	 * @param allPartitionFilesByCodeMap a Map<String,PrintWriter> keyed by partition codes of the first letters
	 * of a protein texts sequence with values of PrintWriter for each partition file
	 * @see allPartitionFilenamesByCodeMap also uses this Map<String,String> keyed by partiton codes to extract the correct filename
	 * of the partition file for debug output.
	 */
	public void writeToPartitionFile(String currentProteinText, Map<String,PrintWriter> allPartitionFilesByCodeMap){
			int index = currentProteinText.trim().indexOf("protein: Protien [sequence= ") + "protein: Protien [sequence= ".length();
			String partitionCode = currentProteinText.trim().substring(index, index+3);
			if(debug){
				System.out.println("writing protein with first 3 : " + partitionCode + "to file: " 
						+ allPartitionFilenamesByCodeMap.get(partitionCode));
				System.out.println("of size b/f write of: " 
						+ (new File(allPartitionFilenamesByCodeMap.get(partitionCode)).length()));	
				System.out.println("writting string:\n" + currentProteinText);
			}//if debug
			allPartitionFilesByCodeMap.get(partitionCode).write(currentProteinText);
			if(debug){
				System.out.println("write done");
			}//if debug
	}//method writeToPartitionFile
	
	/**
	 * Flushes and closes all PrintWriters for the partition files
	 * @return void
	 * @param allPartitionFilesByCodeMap a Map<String,PrintWriter> keyed by partition codes of the first letters
	 * of a protein texts sequence with values of PrintWriter for each partition file
	 */
	public void cleanup(Map<String,PrintWriter> allPartitionFilesByCodeMap){
		List<PrintWriter> printWriterList = new ArrayList<PrintWriter>(allPartitionFilesByCodeMap.values());
		ListIterator<PrintWriter> printWriterIter = printWriterList.listIterator();
		while(printWriterIter.hasNext()){
			PrintWriter currentWriter = printWriterIter.next();
			currentWriter.flush();
			currentWriter.close();
		}//while printWriterIter has next
	}//method cleanup
	
	/**
	 * Creates a one protein text at a time stream of the very big (much larger than maxint bytes) 
	 * debug data file of protein texts
	 * @return a String containing one sequentially read protein text form the big debug file
	 * @param sequenceFileReader a FileReader to the big debug file of protein texts
	 * @param filename a String containing the name of the big debug data file so its size can be determined
	 * @param numTimesCalled a pass by value primitive int indicating the number of times this method 
	 * has been called and thus the number of protein texts read
	 * @param lastString a boolean flag warpped in a MyMutableBoolean object indicating if we have reached EOF and 
	 * thus have the last protein text
	 * @param fileCharCount a int wrapped in a MyMutableLong object indicating the total cumulative number of 
	 * characters read by the method used as a double check for EOF 
	 */
	public String getOneProteinText(FileReader sequenceFileReader, String filename, int numTimesCalled, MyMutableBoolean lastString, MyMutableLong fileCharCount){		
		String returnString = "";
		char c = ' ';	
		boolean gotText = false;
		StringBuilder currentStringBuilder = new StringBuilder();
			while(!gotText && !lastString.isB()){						
				try {
					if((c != -1 && fileCharCount.getL() < (new File(filename)).length())){
						c = (char) sequenceFileReader.read();
					}//if not EOF
					else{
						System.out.println("detects last string/EOF");
						lastString.setB(true);
						System.out.println("lastString is: " + lastString + " called count is: " + numTimesCalled + " contents: " + currentStringBuilder.toString());
					}
				} catch (FileNotFoundException e) {
					System.out.println("File Not Found: " + e.getMessage());
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("IOException: " + e.getMessage());
					e.printStackTrace();
				}//catch
				fileCharCount.setL(fileCharCount.getL() + 1);
				if (!lastString.isB()){
					currentStringBuilder.append(c);
					String currentString = currentStringBuilder.toString();
					//System.out.println("current string is: " + currentString);
					if(currentString.contains("protein: Protien [sequence=")){
						gotText = true;	
						//System.out.println("found: protein: Protien [sequence=");
						//System.out.println(currentString);
						if (numTimesCalled == 0){
							returnString = "first call\n";
						}//if
						else{
							int sourceTextSequenceEnd = currentString.indexOf("protein: Protien [sequence=");
							returnString = "protein: Protien [sequence=" + currentString.substring(0, sourceTextSequenceEnd);
						}//if not first call
					}//if currentString.contains("protein: Protien [sequence=")
					else if(currentString.indexOf("protein: Protien [sequence=") == -1){
						returnString = "protein: Protien [sequence=" + currentString;
					}//current string does not contain "protein: Protien [sequence=" (therefore last protein text)
					else{
						returnString = null;
					}//else
				}//if not last string/EOF			
			}//while(!gotText)
			System.out.println("Times called: " + numTimesCalled  + " From Text: \n" + returnString + "\n");
			numTimesCalled++;
			return returnString;
		//}//while	
	}//method getOneProteinText
	class MyMutableBoolean{
		boolean b;
		@Override
		public String toString() {
			return "" + b;
		}
		public MyMutableBoolean(){}
		public MyMutableBoolean(boolean b) {
			super();
			this.b = b;
		}
		public boolean isB() {
			return b;
		}
		public void setB(boolean b) {
			this.b = b;
		}
	}//class myMutableBoolean
	class MyMutableLong{
		@Override
		public String toString() {
			return "" + l;
		}
		public MyMutableLong(){}
		public MyMutableLong(long l) {
			super();
			this.l = l;
		}
		long l;
		public long getL() {
			return l;
		}
		public void setL(long l) {
			this.l = l;
		}
	}//class myMutableLong
}//outer class
