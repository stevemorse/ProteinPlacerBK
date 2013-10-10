package NLP;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.concordia.cs.common.ncbi.qblast.GetCommand;
import org.concordia.cs.common.ncbi.qblast.PutCommand;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;



public class Tester {

	public static void main(String args[]){
		
		File inSequencesFile = new File ("C:\\Users\\Steve\\Desktop\\ProteinPlacer\\seq.txt");
		File outFile = new File("C:\\Users\\Steve\\Desktop\\ProteinPlacer\\out.txt");
		
		char[] sequencesDataBuffer = new char[(int) inSequencesFile.length()];
		RunnerBarrierSyncObject runnerBarrier = new RunnerBarrierSyncObject();
		
		try {
			FileReader sequenceFileReader = new FileReader(inSequencesFile);
			sequenceFileReader.read(sequencesDataBuffer);
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}
				
		String DataStr = new String(sequencesDataBuffer);		
		GetCommand getCommand = new ProteinGetCommand();
		PutCommand putCommand = new PutCommand();
		
		//String proteinStr = "";
		
		String dbase = "nr";
		String searchProgram = "blastp";
		
		//parse data string
		String[] seqs = DataStr.split(">");
		int numSeq = seqs.length;
		for(int seqCount = 0; seqCount < numSeq; seqCount++ ){
			System.out.println("line: " + seqCount + " is: " + seqs[seqCount]);
		}
		for(int seqCounter = 1; seqCounter < numSeq; seqCounter++ ){
			String sequence = seqs[seqCounter].replaceAll("[a-z]", "");
			sequence = sequence.replaceAll(".*-", "");
			sequence = sequence.trim();
			System.out.println(seqs[seqCounter]);
			System.out.println("trims to: " + sequence);
						
			putCommand.setDatabase(dbase);
			putCommand.setProgram(searchProgram);
			putCommand.setQuery(sequence);
			
			SynchronizedRunner runner = new SynchronizedRunner(putCommand,getCommand,runnerBarrier);
			runner.start();
		}
		
		//wait on termination of runner threads
		while(runnerBarrier.getNumberOfRunners() != 0){
			System.out.println("wait on barrier:" + runnerBarrier.getNumberOfRunners());
		}
		
		File inURLsFile = new File ("C:\\Users\\Steve\\Desktop\\ProteinPlacer\\links.txt");
		char[] URLsDataBuffer = new char[(int) inURLsFile.length()];
		
		try {
			FileReader URLsFileReader = new FileReader(inURLsFile);
			URLsFileReader.read(URLsDataBuffer);
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}
		
		String URLsDataStr = new String(URLsDataBuffer);
		//parse data string
		String[] urls = URLsDataStr.split("eValue:");
		int numURLs = urls.length;
		PrintWriter	writer = null;
		try {
			writer = new PrintWriter(new FileWriter(outFile));
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}
		
		
		for(int urlCount = 1; urlCount < numURLs; urlCount++ ){
			String currentURL = urls[urlCount];		
			currentURL = currentURL.replaceAll(".*e-", "");
			currentURL = currentURL.replaceFirst("0.0","");
			currentURL = currentURL.replaceFirst("[\\d]*","");
			currentURL = currentURL.trim();
			currentURL = currentURL.replaceAll(".*http", "http");
			currentURL = currentURL.trim();
			System.out.println("line: " + urlCount + " is: " + currentURL);
			System.out.println("from: " + urls[urlCount]);
			
			try {
				URL dbMineURL = new URL(currentURL);
				URLConnection dbMineURLConn = dbMineURL.openConnection();
				//String content = processContent(dbMineURLConn,writer);
				String content = processContent(currentURL,writer);
//System.out.println("content is:" + content);				
			} catch (IOException e) {
				System.out.println("IOException: " + e.getMessage());
				e.printStackTrace();
			}
		}//for
		writer.flush();
		writer.close();
	}
	/*
	public static String processContent(URLConnection url,PrintWriter writer) throws IOException {
		String content = "";
        String line = "";
        // Read and print out the URL's contents.
		url.setDoInput(true);
        BufferedReader lineReader = new BufferedReader(new InputStreamReader(url.getInputStream()));
     
        while((line = lineReader.readLine()) != null) {
//System.out.println("\nline is: " + line); 
			writer.println(line);
        	content += line;
        }
        return content;
    }
	*/
	/*
	public static String processContent(String url,PrintWriter writer) throws IOException {
		String content = "";
		String line = "";
		//Document doc = Jsoup.connect(url).get();
		//URL dbMineURL = new URL(url);
		//Document doc = Jsoup.parse(dbMineURL, 1000);
		
		Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:7.0.1) Gecko/20100101 Firefox/10.0.2").get();

		Element body = doc.body();
		
		Elements genbank = doc.getElementsByClass("genbank");
		
System.out.println("\ngenbank number of elements is: " + genbank.size());
 
		for (Element e : genbank){
			line = e.text();
			content += line;
System.out.println("\nline is: " + line);
		}
		
		content = body.text();
		
	     
        
		writer.print(content);
		writer.println();
		writer.println();
		writer.println();
      
		
		return content;
	}
	*/
	
	public static String processContent(String url,PrintWriter writer) throws IOException {
		String contentStr = "";
		String elementContentStr = "";
		int iterations = 0;
		
		WebDriver driver = new FirefoxDriver();
		List<WebElement> genbankElements = null;
		
		while((genbankElements == null || genbankElements.size() == 0) && iterations < 6){
			driver.get(url);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ie) {
				System.out.println("InterruptedException: " + ie.getMessage());
				ie.printStackTrace();
			}
			genbankElements = ((RemoteWebDriver) driver).findElementsByClassName("genbank");		
			
			
	System.out.println("\ngenbank number of elements is: " + genbankElements.size());
			
			for(WebElement e : genbankElements){
				elementContentStr = e.getText();
	System.out.println("\nline is: " + elementContentStr);
				contentStr += elementContentStr;
			}
			iterations++;
		}//while
		
		writer.print(contentStr);
		writer.println();
		writer.println();
		writer.println();
		driver.quit();
		
		return contentStr;
	}
	
}
