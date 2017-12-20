package NLP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class OneProteinTextReader {

	//File inFile = new File("/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_0/textOutOfProtiens_0.txt");
	File inFile = null;
	FileReader sequenceFileReader = null;
	int numTimesCalled = 0;
	int count = 0;
	/*
	public static void main (String args[]){
		inFile = new File("/home/steve/Desktop/ProteinPlacer/data/Blast2GoXML/results_0/textOutOfProtiens_0.txt");
		File inFile = null;
		String returnString = "";
		char c = ' ';
		FileReader sequenceFileReader = null;
		
		//read sequence data from file 
		try {
			sequenceFileReader = new FileReader(inFile);
			System.out.println("file length: " + inFile.length());
			
			int count = 0;
			while(c != -1 && count < inFile.length()){		
				boolean gotText = false;
				StringBuilder currentStringBuilder = new StringBuilder();
					while(!gotText){
						
						c = (char) sequenceFileReader.read();
						count++;
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
								int sourceTextSequenceBegin = currentString.indexOf("source and protien feature texts");
								//sourceTextSequenceBegin += "source and protien feature texts".length();
								
								int sourceTextSequenceEnd = currentString.indexOf("protein: Protien [sequence=");
								//sourceTextSequenceEnd += "Protein [sequence= ".length();
								returnString = currentString.substring(0, sourceTextSequenceEnd);
							}//else
						}//if currentString.contains("protein: Protien [sequence=")
					}//while(!gotText)
					System.out.println("From Text: \n" + returnString + "\n\n");
					numTimesCalled++;
			}//while
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}
		
		try {
			sequenceFileReader.close();
		} catch (IOException e) {
			System.out.println("error closing file reader: " + e.getMessage());
			e.printStackTrace();
		}
		//cleanedStr = new String(allChars);	
		//return cleanedStr;
	}//main
	*/
	public OneProteinTextReader(String inFileName){
		inFile = new File(inFileName);
		try {
			sequenceFileReader = new FileReader(inFile);
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}//catch
		numTimesCalled = 0;
		count = 0;
	}
	
	public String getOneProteinText(){
		
		String returnString = "";
		char c = ' ';
		//FileReader sequenceFileReader = null;
		
		//read sequence data from file 	
		//while(c != -1 && count < inFile.length()){		
			boolean gotText = false;
			StringBuilder currentStringBuilder = new StringBuilder();
				while(!gotText){						
					try {
						if((c != -1 && count < inFile.length())){
							c = (char) sequenceFileReader.read();
						}//if not EOF
						else{
							return "EOF";
						}
					} catch (FileNotFoundException e) {
						System.out.println("File Not Found: " + e.getMessage());
						e.printStackTrace();
					} catch (IOException e) {
						System.out.println("IOException: " + e.getMessage());
						e.printStackTrace();
					}//catch
					count++;
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
								returnString = currentString.substring(0, sourceTextSequenceEnd);
						}//if not first call
					}//if currentString.contains("protein: Protien [sequence=")
					else if(currentString.indexOf("protein: Protien [sequence=") == -1){
						returnString = currentString;
					}//current string does not contain "protein: Protien [sequence=" (therefore last protein text)
					else{
						returnString = null;
					}
				}//while(!gotText)
				System.out.println("Times called: " + numTimesCalled  + "From Text: \n" + returnString + "\n\n");
				numTimesCalled++;
				return returnString;
		//}//while
		
		
		
	}//method
	
	public void windup(){
		try {
			sequenceFileReader.close();
		} catch (IOException e) {
			System.out.println("error closing file reader: " + e.getMessage());
			e.printStackTrace();
		}//catch
	}
}//class
