package NLP;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;

import org.concordia.cs.common.ncbi.qblast.GetCommand;


public class ProteinGetCommand extends GetCommand{
	int charBufferSize = 10000; 
	@Override
	public void parseResults(Reader in) throws Exception {
		File outFile = new File("C:\\Users\\Steve\\Desktop\\ProteinPlacer\\test.txt");
		File linksFile = new File("C:\\Users\\Steve\\Desktop\\ProteinPlacer\\links.txt");
		
		FileWriter writer = new FileWriter(outFile);
		char[] charBuffer = new char[charBufferSize];
		char ch = 0;
		String outStr = "";
		
		boolean done = false;
		do{
			int charCountEnd = 0;
			int questionMarks = 0;
			boolean countQuestionMarks = false;
			for(int charCount = 0; charCount < charBufferSize && ch != -1 && !done; charCount++){
				ch = (char) in.read();
				//writer.append(ch);
				charBuffer[charCount] = ch;
				if(ch == '?'){
					if(countQuestionMarks){
						if(questionMarks > 10){
							done = true;
							System.out.println("found it*******************************************************************************************************");
						}
						questionMarks++;
						charCount--;
						charCountEnd--;
					}//if counting already
					countQuestionMarks = true;
				}
				else{
					countQuestionMarks = false;
					questionMarks = 0;
				}
				charCountEnd++;
			}//for
			String tempStr = new String(charBuffer,0,charCountEnd);
			outStr = outStr + tempStr;
			writer.write(outStr);
			writer.write("n\n\n\n\n\n");
System.out.println("length of written string: " + outStr.length());
			
		}while (ch != -1 && !done && outStr.length() < 100000);
		writer.write(outStr);
		writer.flush();
		writer.close();

System.out.println("done writing html to file");
		
		//read back into a string
		FileReader reader = new FileReader(outFile);
		char[] buff = new char[(int) outFile.length()];
		reader.read(buff);
		String pageData = new String(buff);
		
System.out.println("done reading html from file");
		
		FileWriter linksWriter = new FileWriter(linksFile);
		//linksWriter.write("eValue\t\tLink\n\n");
		
System.out.println("processing for links");
		
		//now look for checkboxes in the html and split on them
		String[] checkboxSegments = pageData.split("checkbox");
System.out.println("number of checkboxes is: " + (checkboxSegments.length -1));
		//parse them for e values and anchors
		boolean writeTheLine = true;
		String eValue = "";
		String link = "";
		for(int linkCount = 1; linkCount < checkboxSegments.length; linkCount++ ){
			int linkNum = 0;
			int linkStart = checkboxSegments[linkCount].indexOf("http://");
System.out.println("linkStart is: " + linkStart);
			if(linkStart != -1){
				link = checkboxSegments[linkCount].substring(linkStart);			
				//int linkEnd = checkboxSegments[linkCount].indexOf("\">gb");
				//int linkEnd = checkboxSegments[linkCount].indexOf("\" name=\"");
				int linkEnd = link.indexOf(" ");
System.out.println("linkEnd is: " + linkEnd + linkStart);
				if(linkEnd != -1){
					link = checkboxSegments[linkCount].substring(linkStart, linkStart + linkEnd -1);
					link.trim();
System.out.println("found link: " + link);
				}
				else{writeTheLine = false;}
			}
			else{writeTheLine = false;}
			
			int eValueStart = checkboxSegments[linkCount].indexOf("Expect = ") +9;
System.out.println("eValueStart is: " + eValueStart);
			if(eValueStart != -1){
				eValue = checkboxSegments[linkCount].substring(eValueStart);
				//int eValueEnd = checkboxSegments[linkCount].indexOf(", Method");
				//int eValueEnd = checkboxSegments[linkCount].indexOf("Identities");
				int eValueEnd = checkboxSegments[linkCount].indexOf("\n Identities");
				//int eValueEnd = checkboxSegments[linkCount].indexOf(" ");
System.out.println("eValueEnd is: " + eValueEnd);
				if(eValueEnd != -1){
					eValue = checkboxSegments[linkCount].substring(eValueStart, eValueEnd);
					//eValue = eValue.substring(0, 7);
					eValue.trim();
System.out.println("found eValue: " + eValue);
				}
				else{writeTheLine = false;}
			}

			else{writeTheLine = false;}
System.out.println("link number: " + linkNum++);
System.out.println("writeTheLine is: " + writeTheLine);
			if(writeTheLine){
				synchronized(this){
				String writeStr = "eValue: " + eValue + "\nlink: " + link + "\n\n";
				linksWriter.write(writeStr);
				}//end sync block
			}//if write
		}//for
		linksWriter.flush();
		linksWriter.close();
	}//method

}//class
