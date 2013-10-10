package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * used to take in and clean blast 2 go annotation files.  These files contain
 * anomylous whitespace "characters" that must be replaced with spaces before
 * the entries can be operated on by string functions.
 * @author Steve Morse
 * @version 1.0
 */
public class FileCharReader {
	
	/**
	 * reads and cleans a file of non-valid whitespace "characters" replaceing
	 * them with standard space characters.
	 * @param inFile the file to read and clean
	 * @return the entire file's cleaned data as a String
	 */
	public String read(File inFile){
		
		String cleanedStr = "";
		char[] allChars = null;
		FileReader sequenceFileReader = null;
		
		//read sequence data from file and clean "unreal" whitespace characters
		try {
			sequenceFileReader = new FileReader(inFile);
			allChars = new char[(int) inFile.length()];
			//System.out.println("file length: " + inFile.length());
			char c = (char) sequenceFileReader.read();
			int count = 0;
			while(c != -1 && count < inFile.length()){
				if(Character.isWhitespace(c) && c != '\n'){
					c=' ';
				}
				allChars[count] = c;
				//System.out.println("char: " + c + " is whitespace: " + Character.isWhitespace(c));
				c = (char) sequenceFileReader.read();
				count++;
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
		cleanedStr = new String(allChars);	
		return cleanedStr;
	}
	
}
