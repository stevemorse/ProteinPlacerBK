package NLP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import mx4j.tools.config.DefaultConfigurationBuilder.Object;














//import org.apache.xpath.operations.String;
//import org.openqa.jetty.html.List;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import protein.Protein;
import utils.SingleLock;

import com.google.common.base.Function;

/**
 * A thread class to handle the processing of one Protein.  
 *  It mines the text of accessions from a balstx balst 2 go search for keywords to
 * the location of expression points.  It also them loads all GO html anchors 
 * into the protein and checks if any are GO cellular_components.
 * @author Steve Morse
 * @version 1.0
 *
 */
public class SingleGoProteinProcessingThread extends Thread{
	
	static int threadCount = 0;
	private int currentThread = 0;
	public SingleGoProteinProcessingThread(){
		threadCount++;
		currentThread = threadCount;
		System.out.println("currentThread: " + currentThread + " created");
	}
	
	private Protein currentProtein = null;
	private Map<String, String> GoAnnotationLocations = null;
	private Map<String, String> currentAccessionIds = null;
	private File outFile = null;
	private double thresholdEValue = 0;
	private static boolean debug = true;
	//private static File proteinsOutFile = new File("/home/steve/Desktop/ProteinPlacer/proteinsOut.bin");
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
	public SingleGoProteinProcessingThread(Protein currentProtein, Map<String, String> currentAccessionIds, 
			Map<String, String> GoAnnotationLocations, File outFile, ObjectOutputStream oos, double thresholdEValue, boolean debug){
		this.currentProtein = currentProtein;
		this.GoAnnotationLocations = GoAnnotationLocations;
		this.currentAccessionIds = currentAccessionIds;
		this.outFile = outFile;
		this.thresholdEValue = thresholdEValue;
		SingleGoProteinProcessingThread.debug = debug;
		this.oos = oos;
	}
	
	/**
	 * Processes all accession links with an eValue less than the threshold eValue.
	 */
	public void run() throws org.openqa.selenium.WebDriverException{
		if(currentAccessionIds != null){
			List<String> accessions =  new ArrayList<String>(currentAccessionIds.keySet());
			ListIterator<String> accessionsLiter = accessions.listIterator();
			boolean done = false;
			boolean firstAccession = true;
			while(accessionsLiter.hasNext() && !done){
				String accessionName = accessionsLiter.next();
				String eValueText = currentAccessionIds.get(accessionName);
				double eValue = Double.parseDouble(eValueText);
				if(debug){
					System.out.println("accession value: " + accessionName);
					System.out.println("eValueText in decimal: " + eValue);
				}//if debug
					if(eValue < thresholdEValue){
						String region = processAnchorLink(currentProtein,accessionName,GoAnnotationLocations, outFile, firstAccession, debug);
						firstAccession = false;
						if(region != null && region.compareTo("not found") != 0){
							if(debug){
								System.out.println("found region is: " + region);
							}//if debug
							currentProtein.setPlacedByText(true);
							currentProtein.setExpressionPointText(region);
							//done = true;
						}
						else{
							if(debug){
								System.out.println("NOT FOUND IN TEXT: " + region);
							}//if debug
						}
					}//if eValue
			}//accessionsLiter has next
		}//accessions not null
		currentProtein.setProcessed(true);  //fully processed by this point
		
		if (debug){
			System.out.println("Protein:\n" + currentProtein.toString());
		}//if debug
		
		SingleLock lock = SingleLock.getInstance();
		synchronized(lock){
			try{
				oos.writeObject(currentProtein);
			} catch (FileNotFoundException fnfe) {
				System.err.println("error opening output file: " + fnfe.getMessage());
				fnfe.printStackTrace();
			} catch (IOException ioe) {
				System.err.println("error opening output stream: " + ioe.getMessage());
				ioe.printStackTrace();
			}//try block
		}//synchronized
		System.out.println("currentThread: " + currentThread + " ends run normally");
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
	public String processAnchorLink(Protein currentProtien, String accession, Map<String, String>GoAnnotationLocations, 
			File outFile, boolean firstDFLink, boolean debug) throws org.openqa.selenium.WebDriverException{
		
		String foundRegion = "not found";
		List<WebElement> featureElements = null;
		WebElement inputTextFeildElement = null;
		String proteinPageUrl = "http://www.ncbi.nlm.nih.gov/protein/";
		
		//starting more than firefox driver instance at a time can result in socket lock
		//also manually force re-init of driver to prevent hanging attachment of old driver
		//to port until garbage collection.
		//see http://code.google.com/p/selenium/issues/detail?id=5061
		//see http://stackoverflow.com/questions/16140865/unable-to-bind-to-locking-port-7054-within-45000-ms
		final WebDriver driver;
		//final FirefoxDriverWrapper driver = new FirefoxDriverWrapper();
		
		//Object socketLock = new Object();
		//synchronized(socketLock){
		driver = new FirefoxDriver();
			//driver = forceInit();
			//driver.open();
		//}//end synch block
		
		//get protein page
		boolean done = true;
		do{
			driver.get(proteinPageUrl);
			
			final Wait<WebDriver> waitDriver = new FluentWait<WebDriver>(driver)
				       .withTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
				       .pollingEvery(5, java.util.concurrent.TimeUnit.SECONDS);
			try{
				inputTextFeildElement = waitDriver.until(new Function<WebDriver,WebElement>(){
					public WebElement apply(WebDriver diver){
						return driver.findElement(By.name("term"));
						}});
			}
			catch(NoSuchElementException nsee){
				done = false;
			}
			catch(ElementNotVisibleException enve){
				done = false;
			}
		}while(!done);
		
		//enter accession string into protein page search box and submit query
		inputTextFeildElement.sendKeys(accession);
		inputTextFeildElement.submit();
		
		//get and process genebank text for the protein accession string corresponds to
		String source = driver.getPageSource();	
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
		
		
		ListIterator<WebElement> featureElementsLiterTest = featureElements.listIterator();
		List<String>sourceOrProteinFeaturesListText = new ArrayList<String>();
		while(featureElementsLiterTest.hasNext()){
			WebElement feature = featureElementsLiterTest.next();
			System.out.println("FEATURE IS NAMED: " + feature.getTagName());
			String featureIDString = feature.getAttribute("id").toString();
			System.out.println("FEATURE HAS ID: " + featureIDString);
			if(featureIDString.contains("Protein")||featureIDString.contains("protein")
					||featureIDString.contains("Source")||featureIDString.contains("source")){
				System.out.println("FEATURE TEXT IS: " + feature.getText());
				sourceOrProteinFeaturesListText.add(feature.getText());
			}//if contains right id
		}
		
		boolean matched = false;
		System.out.println("number of features = " + featureElements.size());
		System.out.println("number of source and protein features = " + sourceOrProteinFeaturesListText.size());
		ListIterator<String> sourceOrProteinFeatureTextLiter = sourceOrProteinFeaturesListText.listIterator();
		while(sourceOrProteinFeatureTextLiter.hasNext() && !matched){
			String featureText = sourceOrProteinFeatureTextLiter.next();
			//String featureText = feature.getText();
			List<String> cellLocationNames = new ArrayList<String>(GoAnnotationLocations.values());
			ListIterator<String> cellLocationNamesLiter = cellLocationNames.listIterator();
			while(cellLocationNamesLiter.hasNext()  && !matched){
				String cellLocationName = cellLocationNamesLiter.next();
				String seperateName = " " + cellLocationName.toLowerCase() + " ";
				if((featureText.toLowerCase()).contains((seperateName.toLowerCase()))){
					foundRegion = cellLocationName;
					matched = true;
					System.out.println("MATCH FOUND IN FEATURES!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
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
				WebElement itemid = null;
				itemid = null;
				System.out.println("no genebank elemant returned for accession: " + accession + " " + nsee.getMessage());
				//check for obsolete link and if found click on it to get obsolete page (with genebank)
				try{
					itemid = waitingDriver.until(new Function<WebDriver,WebElement>(){
						public WebElement apply(WebDriver diver){
							return driver.findElement(By.className("itemid"));
							}});
					//System.out.println("found obsolete link");
					//System.out.println(itemid.getText());
					WebElement href = itemid.findElement(By.tagName("a"));
					//System.out.println(href.getText());
					href.click();
					//System.exit(0);
				
				}//try 
				catch(NoSuchElementException nsee2){
					System.out.println(nsee2.getMessage());
					//<li class="error icon">
				    //<span class="icon"></span>
					//</li>
				}
			}//catch
		}//while not got genebank element
		
		String genbankText = genbank.getText();
		
		//make output to source text file
		PrintWriter	writer = null;
		try {
			writer = new PrintWriter(new FileWriter(outFile,true));
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}
		synchronized(outFile){
			ListIterator<String> sourceOrProteinFeatureTextLiterForWrite = sourceOrProteinFeaturesListText.listIterator();
			writer.println("protein: " + currentProtien.toString());
			writer.println("source and protien feature texts");
			while(sourceOrProteinFeatureTextLiterForWrite.hasNext()){
				String featureText = sourceOrProteinFeatureTextLiterForWrite.next();
				writer.println(featureText);
			}//while featureElementsLiter
			writer.println();
			writer.flush();
			writer.close();
		}//synch
			
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
			currentProtein.getProteinSequences().remove(0);//remove "NOT MATCHED" constructor string
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
		
		driver.close();
		driver.quit();
		//forceClose(driver);
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
	public void processGoAnchor(Protein currentProtein, String url, String currentGoAnchorString, 
			Map<String, String> GoAnnotationLocations, String TypeOfGoLookup, boolean debug) throws org.openqa.selenium.WebDriverException{
		
		String amiGoTextClassName = "name";
		String quickGoAnnotationRowOddName = "annotation-row-odd";
		String quickGoAnnotationRowEvenName = "annotation-row-even";
		String quickGoTextClassName = "info-definition";
		String quickGoIDTagName = "a";
		
		//WebDriver driver = new FirefoxDriver();
		
		//starting more than firefox driver instance at a time can result in socket lock
		//also manually force re-init of driver to prevent hanging attachment of old driver
		//to port until garbage collection.
		//see http://code.google.com/p/selenium/issues/detail?id=5061
		//see http://stackoverflow.com/questions/16140865/unable-to-bind-to-locking-port-7054-within-45000-ms
		final WebDriver driver;
		//Object socketLock = new Object();
		//synchronized(socketLock){
		driver = new FirefoxDriver();
			//driver = forceInit();
			//driver = new FirefoxDriverWrapper();
		//}//end synch block
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.get(url);
		
		if(TypeOfGoLookup.compareTo("GO") == 0){	//tradition Go web interface
			
			WebElement nameElement = ((RemoteWebDriver) driver).findElementByClassName(amiGoTextClassName);
			String nameTextString = nameElement.getText();
			if(currentProtein.getAnnotations() == null){
				currentProtein.setAnnotations(new HashMap<String, String>());
			}
			(currentProtein.getAnnotations()).put(currentGoAnchorString, nameTextString);
			List<String> cellLocationGOCodes = new ArrayList<String>(GoAnnotationLocations.keySet());
			if(cellLocationGOCodes.contains(currentGoAnchorString) && !currentProtein.isPlacedByGOTerms()){
				currentProtein.setPlacedByGOTerms(true);
				currentProtein.setExpressionPointGOText(nameTextString);
			}//if(cellLocationGOCodes.contains(currentGoAnchorString)
		}//if TypeOfGoLookup
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
				if(cellLocationGOCodes.contains(goElementTextString) && !currentProtein.isPlacedByGOTerms()){
					currentProtein.setPlacedByGOTerms(true);
					currentProtein.setExpressionPointGOText(goElementTextString);
				}//if(cellLocationGOCodes.contains(goElementTextString)
			}//while(allRowElementsLiter.hasNext())	
		}//else if TypeOfGoLookup
		else{
			System.err.println("invalid GO code: " + TypeOfGoLookup);
		}//else
		
		driver.close();
		driver.quit();
		//forceClose(driver);
	}//processGoAnchor
	
}//class




