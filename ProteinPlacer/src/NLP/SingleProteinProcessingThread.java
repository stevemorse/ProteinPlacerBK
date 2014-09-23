package NLP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;


import com.google.common.base.Function;

import protein.Protein;
import utils.AppendingObjectOutputStream;
import utils.SingleLock;

/**
 * A thread class to handle the processing of one Protein.  Attempts to locate
 * the expression point of a protein by calling upon the NIH BlastP program to 
 * match the sequence of the protein.  It then mines the text for keywords to
 * the location of expression points.  It also them loads all GO html anchors 
 * into the protein and checks if any are GO cellular_components.
 * @author Steve Morse
 * @version 1.0
 *
 */
public class SingleProteinProcessingThread extends Thread{
	
	private Protein currentProtein = null;
	private Map<String, String> GoAnnotationLocations = null;
	private File outFile = null;
	private double thresholdEValue = 0;
	private boolean debug = true;
	private static File proteinsOutFile = new File("C:\\Users\\Steve\\Desktop\\ProteinPlacer\\proteinsOut.bin");
	private  ObjectOutputStream oos = null;
	
	//String PBlastURL = "http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?PROGRAM=blastp&BLAST_PROGRAMS=blastp&PAGE_TYPE=BlastSearch&SHOW_DEFAULTS=on&LINK_LOC=blasthome";
	String XBlastURL = "http://blast.ncbi.nlm.nih.gov/Blast.cgi?PROGRAM=blastx&BLAST_PROGRAMS=blastx&PAGE_TYPE=BlastSearch&SHOW_DEFAULTS=on&LINK_LOC=blasthome";
	
	/**
	 * Sole Constructor
	 * @param currentProtein	The protein the thread is to process.
	 * @param GoAnnotationLocations	A Map of all GO cellular components GO values and names.
	 * @param outFile	Debug text output file.
	 * @param thresholdEValue	The cut off eValue for processing BlastP matches for the protein's sequence.
	 * @param debug
	 */
	public SingleProteinProcessingThread(Protein currentProtein, Map<String, String> GoAnnotationLocations, File outFile, ObjectOutputStream oos, double thresholdEValue, boolean debug){
		this.currentProtein = currentProtein;
		this.GoAnnotationLocations = GoAnnotationLocations;
		this.outFile = outFile;
		this.thresholdEValue = thresholdEValue;
		this.debug = debug;
		this.oos = oos;
	}
	
	/**
	 * Calls upon the NIH BlastP search web page to collect all possible 
	 * matches for a protein sequence.  Processes all match http links 
	 * with an eValue less than the threshold eValue.
	 */
	public void run() throws UnreachableBrowserException {
		
		WebElement inputTextFeildElement = null;
		//get new web driver for each protein, each will be passed to a processing thread
		final WebDriver driver = new FirefoxDriver();
		
		Wait<WebDriver> waitingDriver = new FluentWait<WebDriver>(driver)
	       .withTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
	       .pollingEvery(5, java.util.concurrent.TimeUnit.SECONDS)
	       /*.ignoring(NoSuchElementException.class)*/;
	
		//get blast page
		boolean done = true;
		do{
			//driver.get(PBlastURL);
			driver.get(XBlastURL);
			try{
				inputTextFeildElement = waitingDriver.until(new Function<WebDriver,WebElement>(){
					public WebElement apply(WebDriver diver){
						return driver.findElement(By.name("QUERY"));
						}});
			}
			catch(NoSuchElementException nsee){
				done = false;
			}
		}while(!done);
		
		//enter FASTA search string into BlastP search box and submit query
		String sequenceDataStr  = (currentProtein).getSequence();
		inputTextFeildElement.sendKeys(sequenceDataStr);
		inputTextFeildElement.submit();
		
		WebElement element = null;
		WebElement searchingElement = null;	
		final Wait<WebDriver> waitingForTextDriver = new FluentWait<WebDriver>(driver)
			       .withTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
			       .pollingEvery(5, java.util.concurrent.TimeUnit.SECONDS)
			       .ignoring(NoSuchElementException.class);
		
		//skip over still searching pages
		String searchingText = "";
		int count = 0;
		do{	
			searchingText = "";
				try{
				searchingElement = waitingForTextDriver.until(new Function<WebDriver,WebElement>(){
					public WebElement apply(WebDriver diver){
						return driver.findElement(By.id("type-a"));
						}});
				if(searchingElement != null){
					searchingText = searchingElement.getText();
				}
			}
			catch(org.openqa.selenium.TimeoutException toe){
				searchingElement = null;
			}//catch
			catch(org.openqa.selenium.StaleElementReferenceException see){
				searchingText = " stale element";
			}//catch
			
			if(debug){
				System.out.println("iteration: " + count++ + " searchingText shows still on a search page: " + searchingText.contains(" This page will be automatically updated"));
			}//if debug
		}while(searchingText.contains("This page will be automatically updated") && searchingElement != null);
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException ie) {
			System.out.println("InterruptedException: " + ie.getMessage());
			ie.printStackTrace();
		}
		
		final Wait<WebDriver> waitDriver = new FluentWait<WebDriver>(driver)
			       .withTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
			       .pollingEvery(5, java.util.concurrent.TimeUnit.SECONDS);
		
		//check for no conserved domain match
		boolean noMatchFlag = true;
		String matchText = "";
		boolean noMatch = false;
		do{
			//check for each of two possible failure fields
			try{
				element = waitDriver.until(new Function<WebDriver,WebElement>(){
					public WebElement apply(WebDriver diver){
						return driver.findElement(By.id("cddDesc"));
						}});
				matchText = element.getText();
			}
			catch(NoSuchElementException nsee){
				try{
					element = waitDriver.until(new Function<WebDriver,WebElement>(){
						public WebElement apply(WebDriver diver){
							return driver.findElement(By.className("info"));
							}});
					matchText = element.getText();
				}
				catch(NoSuchElementException nse){
					noMatchFlag = false;
				}
			}

			if(matchText.contains("No putative conserved domains have been detected") ||
				matchText.contains("No significant similarity found")){
				noMatch = true;
				if(debug){
					System.out.println("NO MATCH FOUND FOR SEQUENCE\n");
				}//if debug
			}//if matchText		
		}while(!noMatchFlag);
		
		//if search revealed a conserved domain match
		if(!noMatch){
			do{
				try{
					element = waitDriver.until(new Function<WebDriver,WebElement>(){
						public WebElement apply(WebDriver diver){
							return driver.findElement(By.id("dscTable"));
							}});
				}
				catch(NoSuchElementException nsee){
					done = false;
				}
				
			}while(!done);
			
			if(debug){
				String bodyText = element.getText();
				System.out.println("putative body text..............................................................\n");
				System.out.println(bodyText);
			}//if debug
			
			count = 0;
			List<WebElement> elements = ((RemoteWebDriver) driver).findElementsByClassName("dflLnk");
			System.out.println("row elements found: " + elements.size());
			ListIterator<WebElement> elementLiter = elements.listIterator();
			done = false;
			boolean firstDFLink = true;
			while(elementLiter.hasNext() && !done){
				WebElement elem = elementLiter.next();
				String elemText = elem.getText();
				if(debug){
					System.out.println("item: " + count++ + " is: ");
					System.out.println(elemText);
				}//if debug
				WebElement anchor = elem.findElement(By.className("dflSeq"));
				String anchorText = anchor.getText();
				String anchorURLString = anchor.getAttribute("href");
				WebElement eValueElement = elem.findElement(By.className("c6"));
				String eValueText = eValueElement.getText();
				double eValue = Double.parseDouble(eValueText);
				if(debug){
					System.out.println(anchorText);
					System.out.println("anchor value: " + anchorURLString);
					System.out.println(eValueText);
					System.out.println("eValueText in decimal: " + eValue);
				}//if debug
					if(eValue < thresholdEValue){
						String region = processAnchorLink(currentProtein,anchorURLString,GoAnnotationLocations, outFile, firstDFLink, debug);
						firstDFLink = false;
						if(region.compareTo("not found") != 0){
							if(debug){
								System.out.println("found region is: " + region);
							}//if debug
							currentProtein.setPlacedByText(true);
							currentProtein.setExpressionPointText(region);
							done = true;
						}
						else{
							if(debug){
								System.out.println("NOT FOUND IN TEXT: " + region);
							}//if debug
						}
					}//if eValue
					else{
						done = true;
					}//else
			}//elementLiter has next
		} //if not no match
		
		currentProtein.setProcessed(true);  //fully processed by this point
		
		if (debug){
			System.out.println("Protein:\n" + currentProtein.toString());
		}//if debug
		
		SingleLock lock = SingleLock.getInstance();
		synchronized(lock){
			/*
			FileOutputStream fout;
			ObjectOutputStream oos = null;
			AppendingObjectOutputStream aoos = null;
			try {
				fout = new FileOutputStream(proteinsOutFile,true); //open for append
				if(proteinsOutFile.exists()){
					aoos = new AppendingObjectOutputStream(fout);
					aoos.writeObject(currentProtein);
					aoos.close();
				}
				else{
					oos = new ObjectOutputStream(fout);
					oos.writeObject(currentProtein);
					oos.close();
				}
			} catch (FileNotFoundException fnfe) {
				System.err.println("error opening output file: " + fnfe.getMessage());
				fnfe.printStackTrace();
			} catch (IOException ioe) {
				System.err.println("error opening output stream: " + ioe.getMessage());
				ioe.printStackTrace();
			}*/
			try{
				oos.writeObject(currentProtein);
			} catch (FileNotFoundException fnfe) {
				System.err.println("error opening output file: " + fnfe.getMessage());
				fnfe.printStackTrace();
			} catch (IOException ioe) {
				System.err.println("error opening output stream: " + ioe.getMessage());
				ioe.printStackTrace();
			}
		}//synchronized
		driver.quit();
	}//run method
	

	/**
	 * Processes a single http link corresponding to a putative match returned 
	 * from the Blast search performed on the protein's sequence.
	 * @param currentProtien	The protein being processed.
	 * @param url	The current url link to process for this protein.
	 * @param GoAnnotationLocations	A Map of all GO cellular components GO values and names.
	 * @param outFile	Debug text out file.
	 * @param debug	Verbose flag.
	 * @param firstDFLink extract protein sequence from origin field for first dflink (lowest eValue)
	 * @return A String naming the expression point if found or "not found" otherwise.
	 */
	public String processAnchorLink(Protein currentProtien, String url, Map<String, String>GoAnnotationLocations, File outFile, boolean firstDFLink, boolean debug){
		
		String foundRegion = "not found";
		List<WebElement> featureElements = null;
		final WebDriver driver = new FirefoxDriver();
		driver.get(url);
		
		
		
		PrintWriter	writer = null;
		try {
			writer = new PrintWriter(new FileWriter(outFile));
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}
		
		String source = driver.getPageSource();
		writer.println("putative source text..............................................................\n");
		writer.println(source);
		writer.flush();
		
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException ie) {
			System.out.println("InterruptedException: " + ie.getMessage());
			ie.printStackTrace();
		}
		
		boolean gotFeatures = false;
		while(!gotFeatures){
			try{
				featureElements = ((RemoteWebDriver) driver).findElementsByClassName("feature");
				gotFeatures = true;
			}
			catch(NoSuchElementException nsee){
				gotFeatures = false;
			}
		}//while not gotFeatures
		
		boolean matched = false;
		System.out.println("number of features = " + featureElements.size());
		ListIterator<WebElement> featureElementsLiter = featureElements.listIterator();
		while(featureElementsLiter.hasNext() && !matched){
			WebElement feature = featureElementsLiter.next();
			String featureText = feature.getText();
			List<String> cellLocationNames = new ArrayList<String>(GoAnnotationLocations.values());
			ListIterator<String> cellLocationNamesLiter = cellLocationNames.listIterator();
			while(cellLocationNamesLiter.hasNext()  && !matched){
				boolean falseMatch = false;
				String cellLocationName = cellLocationNamesLiter.next();
				if((featureText.toLowerCase()).contains((cellLocationName.toLowerCase()))){
					//check for exact match on String "axon"
					if(cellLocationName.toLowerCase().compareTo("axon") == 0){
						int numAxon = (featureText.toLowerCase()).split("axon").length;
						int numTaxon = (featureText.toLowerCase()).split("taxon").length;
						if(numAxon == numTaxon){
							falseMatch = true;
						}//if axons == taxons
					}//if location is "axon"
					if(!falseMatch){
						foundRegion = cellLocationName;
						matched = true;
						System.out.println("MATCH FOUND IN FEATURES!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
					}
				}//if term in feature
			}//while termsLiter
		}//while featureElementsLiter
		
		final Wait<WebDriver> waitingDriver = new FluentWait<WebDriver>(driver)
			       .withTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
			       .pollingEvery(5, java.util.concurrent.TimeUnit.SECONDS);
		
		WebElement genbank = null;
		while(genbank == null){
			try{
				genbank = waitingDriver.until(new Function<WebDriver,WebElement>(){
					public WebElement apply(WebDriver diver){
						return driver.findElement(By.className("genbank"));
						}});
			
			}//try
			catch(NoSuchElementException nsee){
				genbank = null;
				System.out.println("no genebank elemant returned for url: " + url);
			}//catch
		}//while not got genebank element
		
		String genbankText = genbank.getText();
		if(debug){
			writer.println("\n\n\n\n\n\n\n\n\nputative genbank text..............................................................\n");
			writer.println(genbankText);
			writer.flush();
			writer.close();
		}//if debug
			
		//check KEYWORDS area of genbank text for location keywords if not found in features
		if (matched = false){
			int keywordPosition = genbankText.indexOf("KEYWORDS") + "KEYWORDS".length();
			int sourcePosition = genbankText.indexOf("SOURCE");
			String keywordText = genbankText.substring(keywordPosition, sourcePosition);
			List<String> cellLocationNames = new ArrayList<String>(GoAnnotationLocations.values());
			ListIterator<String> termsLiter = cellLocationNames.listIterator();
			while(termsLiter.hasNext()  && !matched){
				String term = termsLiter.next();
				if((keywordText.toLowerCase()).contains((term.toLowerCase()))){
					foundRegion = term;
					matched = true;
					System.out.println("MATCH FOUND IN KEYWORDS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
				}//if term in KEYWORDS area
			}//while termsLiter
		}//if (matched = false)
		
		//extract and load protein sequence
		if(firstDFLink){
			currentProtein.getProteinSequences().remove(0); //remove "NOT MATCHED" constructor string
		}
		int originPosition = genbankText.indexOf("ORIGIN") + 6;
		String originText = genbankText.substring(originPosition);
		originText = originText.replaceAll("\\d", "");
		originText = originText.replaceAll("\\s", "");
		originText = originText.replaceAll("/", "");
		originText = originText.trim();
		currentProtein.getProteinSequences().add(originText);
		if(debug){
			System.out.println("\n\n\n\n\n\n\n\n\nputative genbank text..............................................................\n");
			System.out.println(genbankText);
			System.out.println("proteinSequence is: " + originText);
		}//if debug
	
			
		//extract all GO and GOA text spans in genbank text
		List<String> goAssensionTextList = new ArrayList<String>();
		String[] goTexts = genbankText.split("GO:");
		for(int goTextsCount = 1; goTextsCount < goTexts.length; goTextsCount++){
			String goAssensionText = goTexts[goTextsCount].substring(0,7);
			goAssensionTextList.add(goAssensionText);
		}
		System.out.println("number of GO terms: " + goAssensionTextList.size());
		
		List<String> goAssensionQuickTextList = new ArrayList<String>();
		String[] goATexts = genbankText.split("GOA:");
		for(int goATextsCount = 1; goATextsCount < goATexts.length; goATextsCount++){
			String goAssensionText = goATexts[goATextsCount].substring(0,7);
			goAssensionQuickTextList.add(goAssensionText);
		}
		System.out.println("number of GOA terms: " + goAssensionQuickTextList.size());
		
		//get all anchors in genbank text
		//List<String> goAnchorLinks = new ArrayList<String>();
		List<WebElement> genebankAnchors = genbank.findElements(By.tagName("a"));
		System.out.println("number of genebank anchors found: " + genebankAnchors.size());
		ListIterator<WebElement> goAnchorsLiter = genebankAnchors.listIterator();
		while(goAnchorsLiter.hasNext()){
			WebElement currentGoAnchor = goAnchorsLiter.next();
			String currentGoAnchorString = currentGoAnchor.getText();
			String currentGoAnchorURLString = currentGoAnchor.getAttribute("href");
			System.out.println("GO anchor text: " + currentGoAnchorString + " href is: " + currentGoAnchorURLString);
			if(goAssensionTextList.contains(currentGoAnchorString)){				
				processGoAnchor(currentProtien, currentGoAnchorURLString, currentGoAnchorString, GoAnnotationLocations, "GO", debug);		
			}
			if(goAssensionQuickTextList.contains(currentGoAnchorString)){				
				processGoAnchor(currentProtien, currentGoAnchorURLString, currentGoAnchorString, GoAnnotationLocations, "GOA", debug);		
			}
		}//while(goAnchorsLiter.hasNext())

		driver.quit();
		return foundRegion;
	}//processAnchorLinks
	
	/**
	 * Processes a link to either an AmiGO or classic GO web page to extract GO 
	 * annotations, if there are any more, and see if any are GO 
	 * cellular_components.  Loads results into the current protein.
	 * @param currentProtein The protein being processed.
	 * @param url The current GO link to process for this protein.
	 * @param currentGoAnchorString  the text value of that anchor link.
	 * @param GoAnnotationLocations	A Map of all GO cellular components GO values and names.
	 * @param TypeOfGoLookup identifies the link as a classic GO anchor or an AmiGO anchor. 
	 * @param debug	Verbose flag.
	 */
	public void processGoAnchor(Protein currentProtein, String url, String currentGoAnchorString, Map<String, String> GoAnnotationLocations, String TypeOfGoLookup, boolean debug){
		
		String amiGoTextClassName = "name";
		String quickGoAnnotationRowOddName = "annotation-row-odd";
		String quickGoAnnotationRowEvenName = "annotation-row-even";
		String quickGoTextClassName = "info-definition";
		//String quickGoIDClassName = "goID";
		String quickGoIDTagName = "a";
		
		WebDriver driver = new FirefoxDriver();
		driver.get(url);
		
		if(TypeOfGoLookup.compareTo("GO") == 0){	//tradition Go web interface
			
			WebElement nameElement = ((RemoteWebDriver) driver).findElementByClassName(amiGoTextClassName);
			String nameTextString = nameElement.getText();
			(currentProtein.getAnnotations()).put(currentGoAnchorString, nameTextString);
			List<String> cellLocationGOCodes = new ArrayList<String>(GoAnnotationLocations.keySet());
			if(cellLocationGOCodes.contains(currentGoAnchorString)){
				currentProtein.setPlacedByGOTerms(true);
				currentProtein.setExpressionPointGOText(nameTextString);
			}//if(cellLocationGOCodes.contains(currentGoAnchorString)
		}
		else if (TypeOfGoLookup.compareTo("GOA") == 0){	//quick GO web interface
			
			List<WebElement> evenRowElements = ((RemoteWebDriver) driver).findElementsByClassName(quickGoAnnotationRowEvenName);
			List<WebElement> oddRowElements = ((RemoteWebDriver) driver).findElementsByClassName(quickGoAnnotationRowOddName);
			List<WebElement> allRowElements = new ArrayList<WebElement>();
			List<String> cellLocationGOCodes = new ArrayList<String>(GoAnnotationLocations.keySet());
			allRowElements.addAll(evenRowElements);
			allRowElements.addAll(oddRowElements);
			ListIterator<WebElement> allRowElementsLiter = allRowElements.listIterator();
			while(allRowElementsLiter.hasNext()){
				WebElement currentRowElement = allRowElementsLiter.next();
				WebElement goElement = currentRowElement.findElement(By.tagName(quickGoIDTagName));
				WebElement goDefinitionElement = currentRowElement.findElement(By.className(quickGoTextClassName));
				String goElementTextString = goElement.getText();
				String goDefinitionElementTextString = goDefinitionElement.getText();
				System.out.println("go code: " + goElementTextString + " is defined as: " + goDefinitionElementTextString);
				(currentProtein.getAnnotations()).put(goElementTextString, goDefinitionElementTextString);
				if(cellLocationGOCodes.contains(goElementTextString)){
					currentProtein.setPlacedByGOTerms(true);
					currentProtein.setExpressionPointGOText(goElementTextString);
				}//if(cellLocationGOCodes.contains(goElementTextString)
			}//while(allRowElementsLiter.hasNext())
			
		}
		else{
			System.err.println("invalid GO code: " + TypeOfGoLookup);
		}
		driver.quit();
	}//processGoAnchor
	
}//class
