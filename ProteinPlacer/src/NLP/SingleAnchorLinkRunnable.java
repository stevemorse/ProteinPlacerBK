
package NLP;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionNotFoundException;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import protein.Protein;
import utils.ThreadLogSingleLock;

import com.google.common.base.Function;

import ThreadPools.PriorityRunnable;
import ThreadPools.TheSinglePriorityThreadPool;

/**
 * Processes a single http link (accession) corresponding to a putative match returned 
 * from the Blast search performed on the protein's sequence (if it was of 
 * lower eValue than threshold).  The run method modifies a StringBuilder parameter as
 * an effective return value which holds the region the protein is thought to be
 * expressed according to this accession or "not found" if no region data is mined
 * for this accession.
 */
public class SingleAnchorLinkRunnable extends PriorityRunnable{
	
	//public String processAnchorLink(Protein currentProtien, String accession, Map<String, String>GoAnnotationLocations, 
	//File outFile, boolean firstDFLink, boolean debug) throws org.openqa.selenium.WebDriverException{
	
	private Protein currentProtien = null;
	private String accession = "";
	private String proteinName = "";
	private StringBuilder region = new StringBuilder("");
	private Map<String, String>GoAnnotationLocations = null;
	private List<String>sourceOrProteinFeaturesListText = null;
	private File outFile = null;
	private File threadLogFile = null;
	private boolean firstDFLink;
	private boolean debug;
	
	public SingleAnchorLinkRunnable(Protein currentProtien, String accession, StringBuilder region, Map<String, String>GoAnnotationLocations, 
			File outFile, File threadLogFile, boolean firstDFLink, int priority, boolean debug){
		this.setPriority(priority);
		this.currentProtien = currentProtien;
		this.accession = accession;
		this.region = region;
		this.GoAnnotationLocations = GoAnnotationLocations;
		this.outFile = outFile;
		this.threadLogFile = threadLogFile;
		this.firstDFLink = firstDFLink;
		this.debug = debug;
	}
	
	/**
	 * Processes a single http link corresponding to a putative match returned 
	 * from the Blast search performed on the protein's sequence.
	 * @param currentProtien	The protein being processed.
	 * @param url	The current url link to process for this protein.
	 * @param GoAnnotationLocations	A Map of all GO cellular components GO values and names.
	 * @param outFile	Debug text out file.
	 * @param debug	Verbose flag.
	 * @param firstDFLink extract protein sequence from origin field for first dflink (lowest eValue)
	 * @param A StringBuilder "region" to be set by the thread naming the expression point if found or "not found" otherwise.  
	 * This is effectively a return value for the thread made a parameter so as to use the default run method pattern.
	 */
	@Override
	public void run() throws org.openqa.selenium.WebDriverException{
		String foundRegion = "not found";
		List<WebElement> featureElements = null;
		WebElement inputTextFeildElement = null;
		String proteinPageUrl = "http://www.ncbi.nlm.nih.gov/protein/";
		String genbankText = "";
		boolean gotGenebank = false;
		
		long threadId = Thread.currentThread().getId();
		String threadName = Thread.currentThread().getName();
		PrintWriter threadLogWriter = null;
		
		synchronized(threadLogFile){
			try {
				threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
			} catch (IOException ioe) {
				System.out.println("error opening file for append: " + ioe.getMessage());
				ioe.printStackTrace();
			}//catch
			threadLogWriter.println("Thread Id: " + threadId + " with thread name: " + threadName + " begins run method on accession: " + accession);
			threadLogWriter.flush();
			threadLogWriter.close();
		}//synchronized
		System.out.println("Thread Id: " + threadId + " with thread name: " + threadName + " begins run method on accession: " + accession);
		//starting more than firefox driver instance at a time can result in socket lock
		//also manually force re-init of driver to prevent hanging attachment of old driver
		//to port until garbage collection.
		//see http://code.google.com/p/selenium/issues/detail?id=5061
		//see http://stackoverflow.com/questions/16140865/unable-to-bind-to-locking-port-7054-within-45000-ms
		//fixed by new selenium release
		final WebDriver driver;
		//final FirefoxDriverWrapper driver = new FirefoxDriverWrapper();
		
		//fix for new firefox problem...startpage private browsing
		//ProfilesIni profile = new ProfilesIni();
		FirefoxProfile prof = new FirefoxProfile();
		//FirefoxProfile prof = profile.getProfile("default");
		//prof.setPreference("browser.startup.homepage", proteinPageUrl);
		//prof.setPreference("startup.homepage_welcome_url", proteinPageUrl);
		//prof.setPreference("startup.homepage_welcome_url.additional", proteinPageUrl);
		prof.setPreference("xpinstall.signatures.required", false);
		prof.setPreference("toolkit.telemetry.reportingpolicy.firstRun", false);
		//Object socketLock = new Object();
		//synchronized(socketLock){
System.out.println("before driver instantion");
		//driver = new FirefoxDriver();
		driver = new FirefoxDriver(prof);
System.out.println("after driver instantion");
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
				//if not find by name try find by id
				if(driver.findElements(By.id("term")).size() != 0){
					try{
						inputTextFeildElement = driver.findElement(By.id("term"));
						done = true;
					} catch(NoSuchElementException nsee2){
						synchronized(threadLogFile){
							try {
								threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
							} catch (IOException ioe) {
								System.out.println("error opening file for append: " + ioe.getMessage());
								ioe.printStackTrace();
							}//catch
							threadLogWriter.println("Thread Id: " + threadId + " with thread name: " + threadName + " fails to find input element by name or id to put accession: " + accession);
							threadLogWriter.flush();
							threadLogWriter.close();
						}//synchronized
						done = false;
					}//catch nsee2
				}//catch nsee
			}
			catch(ElementNotVisibleException enve){
				done = false;
			}
		}while(!done);
		
		//enter accession string into protein page search box and submit query
		int inputTextFeildCounter = 0;
		while(driver.getCurrentUrl().compareToIgnoreCase(proteinPageUrl) == 0){
			inputTextFeildCounter++;
			if(inputTextFeildElement.getAttribute("value").compareToIgnoreCase(accession) != 0){
				inputTextFeildElement.sendKeys(accession);
			}//fill input text field if not yet filled with accession value
			//invoke slight delay to allow sending keys to complete
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
				System.out.println("InterruptedException: " + ie.getMessage());
				ie.printStackTrace();
			}
			inputTextFeildElement.submit();
			//didn't work first time...make note of this in the threadlog
			if(inputTextFeildCounter > 1){ 
				synchronized(threadLogFile){
					try {
						threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
					} catch (IOException ioe) {
						System.out.println("error opening file for append: " + ioe.getMessage());
						ioe.printStackTrace();
					}//catch
					threadLogWriter.println("Thread Id: " + threadId + " with thread name: " + threadName + " fails to submit accession: " + accession + " to input element, loop count is: " + inputTextFeildCounter + " input feild is: " + inputTextFeildElement.getAttribute("value"));
					threadLogWriter.flush();
					threadLogWriter.close();
				}//synchronized
			}//if(inputTextFeildCounter > 1)
		}//while driver still on splash page
		
		//get and process genebank text for the protein accession string corresponds to
		//String source = driver.getPageSource();	
		try {
			Thread.sleep(5000);
		} catch (InterruptedException ie) {
			System.out.println("InterruptedException: " + ie.getMessage());
			ie.printStackTrace();
		}
		
		
		final Wait<WebDriver> waitingDriver = new FluentWait<WebDriver>(driver)
			       .withTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
			       .pollingEvery(5, java.util.concurrent.TimeUnit.SECONDS);
		
		WebElement genbank = null;
		int errorCount = 0;
		boolean doneGenebank = false;
		while((errorCount < 5) && (!doneGenebank)){
			try{
				genbank = waitingDriver.until(new Function<WebDriver,WebElement>(){
					public WebElement apply(WebDriver diver){
						return driver.findElement(By.className("genbank"));
						}});
				doneGenebank = true;
				if(!currentProtien.isGotGenebank()){
					currentProtien.setGotGenebank(true);
					currentProtien.setErrorMode("");
				}//if(!currentProtien.isGotGenebank()
				synchronized(threadLogFile){
					try {
						threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
					} catch (IOException ioe) {
						System.out.println("error opening file for append: " + ioe.getMessage());
						ioe.printStackTrace();
					}//catch
					threadLogWriter.println("Thread Id: " + threadId + " with thread name: " + threadName + " finds genbank for: " + accession);
					threadLogWriter.flush();
					threadLogWriter.close();
				}//synchronized
			}//try
			catch(NoSuchElementException nsee){//if trying to find genebank throws no such element
				synchronized(threadLogFile){
					try {
						threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
					} catch (IOException ioe) {
						System.out.println("error opening file for append: " + ioe.getMessage());
						ioe.printStackTrace();
					}//catch
					threadLogWriter.println("Thread Id: " + threadId + " with thread name: " + threadName + " cannot retrieve genbank for: " 
					+ accession + " " + nsee.getMessage());
					threadLogWriter.flush();
					threadLogWriter.close();
				}//synchronized
				errorCount++;
				WebElement itemid = null;
				System.out.println("no genebank elemant returned for accession: " + accession + " " + nsee.getMessage());
				
				//check for obsolete link and if found go to it to get obsolete page (with genebank)
				try{
					itemid = waitingDriver.until(new Function<WebDriver,WebElement>(){
						public WebElement apply(WebDriver diver){
							return driver.findElement(By.className("itemid"));
							}});
					
					WebElement icon  = itemid.findElement(By.className("hi_warn.icon"));
					String iconText = icon.getText();
					if (iconText.contains("has been replaced by")){
						int beginNewAccession = iconText.indexOf("has been replaced by");
						beginNewAccession += "has been replaced by".length();
						String newAccession = iconText.substring(beginNewAccession);
						newAccession = newAccession.trim();
						System.out.println("submitting new accession code: " + newAccession);
						inputTextFeildElement.sendKeys(accession);
						inputTextFeildElement.submit();
						synchronized(threadLogFile){
							try {
								threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
							} catch (IOException ioe) {
								System.out.println("error opening file for append: " + ioe.getMessage());
								ioe.printStackTrace();
							}//catch
							threadLogWriter.println("Thread Id: " + threadId + " with thread name: " + threadName + " finds record redirect for: " + accession);
							threadLogWriter.flush();
							threadLogWriter.close();
						}//synchronized
						doneGenebank = false;
						if(currentProtien.getErrorMode().compareToIgnoreCase("") == 0){
							currentProtien.setErrorMode("replaced");
						}//if error mode not yet set
					}//if icon redirects to new accession and old accession 
					
					else if (iconText.contains("error")){
						System.out.println("found error icon");
						System.out.println(iconText);
						System.exit(0);
						genbankText = "error icon";
						synchronized(threadLogFile){
							try {
								threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
							} catch (IOException ioe) {
								System.out.println("error opening file for append: " + ioe.getMessage());
								ioe.printStackTrace();
							}//catch
							threadLogWriter.println("Thread Id: " + threadId + " with thread name: " + threadName + " finds error icon for: " + accession);
							threadLogWriter.flush();
							threadLogWriter.close();
						}//synchronized
						doneGenebank = true;
						if(currentProtien.getErrorMode().compareToIgnoreCase("") == 0){
							currentProtien.setErrorMode("error");
						}//if error mode not yet set
					}//if icon contains an error message
					
					else if (iconText.contains("record removed")){
						System.out.println("found record removed icon");
						System.out.println(iconText);
						System.exit(0);
						genbankText = "record removed";
						synchronized(threadLogFile){
							try {
								threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
							} catch (IOException ioe) {
								System.out.println("error opening file for append: " + ioe.getMessage());
								ioe.printStackTrace();
							}//catch
							threadLogWriter.println("Thread Id: " + threadId + " with thread name: " + threadName + " finds record removed for: " + accession);
							threadLogWriter.flush();
							threadLogWriter.close();
						}//synchronized
						doneGenebank = true;
						if(currentProtien.getErrorMode().compareToIgnoreCase("") == 0){
							currentProtien.setErrorMode("removed");
						}//if error mode not yet set
					}//if icon contains a record removed message
					
					else{
						WebElement href = itemid.findElement(By.tagName("a"));
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ie) {
							System.out.println("InterruptedException: " + ie.getMessage());
							ie.printStackTrace();
						}
						synchronized(threadLogFile){
							try {
								threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
							} catch (IOException ioe) {
								System.out.println("error opening file for append: " + ioe.getMessage());
								ioe.printStackTrace();
							}//catch
							threadLogWriter.println("Thread Id: " + threadId + " with thread name: " + threadName + " finds anchor redirect for: " + accession);
							threadLogWriter.flush();
							threadLogWriter.close();
						}//synchronized
						
						href.click();
						
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ie) {
							System.out.println("InterruptedException: " + ie.getMessage());
							ie.printStackTrace();
							doneGenebank = false;
						}
						System.out.println(href.getText());
						synchronized(threadLogFile){
							try {
								threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
							} catch (IOException ioe) {
								System.out.println("error opening file for append: " + ioe.getMessage());
								ioe.printStackTrace();
							}//catch
							threadLogWriter.println("Thread Id: " + threadId + " with thread name: " + threadName + " redirects to: " + href.getText());
							threadLogWriter.flush();
							threadLogWriter.close();
						}//synchronized

						doneGenebank = false;
						if(currentProtien.getErrorMode().compareToIgnoreCase("") == 0){
							currentProtien.setErrorMode("anchor");
						}//if error mode not yet set
					}//last else
					genbank = icon;
				}//try 
				catch(NoSuchElementException nsee2){//if trying to find all of the above fails
					System.out.println(nsee2.getMessage());
					try {
						threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
					} catch (IOException ioe) {
						System.out.println("error opening file for append: " + ioe.getMessage());
						ioe.printStackTrace();
					}//catch
					threadLogWriter.println("Thread Id: " + threadId + " with thread name: " + threadName + " has failed " + nsee2.getMessage());
					threadLogWriter.flush();
					threadLogWriter.close();
				}//catch NoSuchElementException nsee2
			}//catch NoSuchElementException nsee
		}//while not got genebank element
		
		synchronized(threadLogFile){
			try {
				threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
			} catch (IOException ioe) {
				System.out.println("error opening file for append: " + ioe.getMessage());
				ioe.printStackTrace();
			}//catch
			threadLogWriter.println("Thread Id: " + threadId + " with thread name: " + threadName + "leaves genebank loop with values errorCount = "
			+ errorCount + " doneGenebank = " + doneGenebank);
			threadLogWriter.flush();
			threadLogWriter.close();
		}//synchronized
		
		
		if((genbankText.compareToIgnoreCase("error icon") != 0) && (genbankText.compareToIgnoreCase("record removed") != 0) 
				&& (genbank != null)){
			genbankText = genbank.getText();
			gotGenebank = true;
		}//if not error icon or record removed icon retrieve genebank text
		
		if (gotGenebank){
			//get name of protein from genebank for debug purposes
			int proteinNameBeginIndex = genbankText.indexOf("DEFINITION") + 11;
			int proteinNameEndIndex = genbankText.indexOf("ACCESSION");
			proteinName = genbankText.substring(proteinNameBeginIndex, proteinNameEndIndex);
			proteinName = proteinName.trim();
			
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
				
			ListIterator<WebElement> featureElementsLiterText = featureElements.listIterator();
			sourceOrProteinFeaturesListText = new ArrayList<String>();
			while(featureElementsLiterText.hasNext()){
				WebElement feature = featureElementsLiterText.next();
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
						//set found status and location in the protein itself
						currentProtien.setPlacedByText(true);
						currentProtien.setExpressionPointText(foundRegion); 
						try {
							threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
						} catch (IOException ioe) {
							System.out.println("error opening file for append: " + ioe.getMessage());
							ioe.printStackTrace();
						}//catch
						threadLogWriter.println("Thread Id: " + threadId + " with thread name: " + threadName + "MATCH FOUND IN FEATURES!!!!!!!!!!!!:");
						threadLogWriter.println("Thread Id: " + threadId + " with thread name: " + threadName + " placed by text in the region: " + foundRegion);
						threadLogWriter.flush();
						threadLogWriter.close();
					}//if term in feature
				}//while termsLiter
			}//while featureElementsLiter
			//check KEYWORDS area of genbank text for location keywords if not found in features
			if (matched == false){				
				int keywordPosition = genbankText.indexOf("KEYWORDS") + "KEYWORDS".length();
				int sourcePosition = genbankText.indexOf("SOURCE", keywordPosition);
				String keywordText = genbankText.substring(keywordPosition, sourcePosition);
				keywordText = keywordText.trim();
				if(keywordText.compareToIgnoreCase(".") != 0  && keywordText.compareToIgnoreCase("RefSeq.") != 0){
					List<String> cellLocationNames = new ArrayList<String>(GoAnnotationLocations.values());
					ListIterator<String> termsLiter = cellLocationNames.listIterator();
					while(termsLiter.hasNext()  && !matched){
						String term = termsLiter.next();
						if((keywordText.toLowerCase()).contains((term.toLowerCase()))){
							foundRegion = term;
							matched = true;
							System.out.println("MATCH FOUND IN KEYWORDS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
							//set found status and location in the protein itself
							currentProtien.setPlacedByText(true);
							currentProtien.setExpressionPointText(foundRegion);
						}//if term in KEYWORDS area
					}//while termsLiter
				}//if real keyword
			}//if (matched == false)	

			//extract and load protein sequence
			if(firstDFLink){
				synchronized(currentProtien){		
						currentProtien.getProteinSequences().remove(0);//remove "NOT MATCHED" constructor string
						currentProtien.getProteinSequences().removeAll(Collections.singleton(null)); //fixs for ArrayList remove() shift to left bug
						currentProtien.getProteinSequences().removeAll(Collections.singleton("NOT MATCHED"));
						currentProtien.getProteinSequences().removeAll(Collections.singleton(""));
				}//Synchronized	
				if(debug){
					System.out.println("REMOVED NOT MATCHED CONSTRUCTOR STRING");
				}//if debug
			}//if(firstDFLink)
			
			//load into list whether first accession or not
			int originPosition = genbankText.indexOf("ORIGIN") + 6;
			String originText = genbankText.substring(originPosition);
			originText = originText.replaceAll("\\d", "");
			originText = originText.replaceAll("\\s", "");
			originText = originText.replaceAll("/", "");
			originText = originText.trim();
			currentProtien.getProteinSequences().add(originText);
			if(debug){
				System.out.println("\n\n\n\n\n\n\n\n\nputative genbank text..............................................................\n");
				System.out.println(genbankText);
				System.out.println("proteinSequence is: " + originText);
			}//if debug
			
System.out.println("Thread " + threadId + " after sequence load");			
				
			//extract all GO and GOA text spans in genbank text
			List<String> goAssensionTextList = new ArrayList<String>();
			String[] goTexts = genbankText.split("GO:");
			for(int goTextsCount = 1; goTextsCount < goTexts.length; goTextsCount++){
				String goAssensionText = goTexts[goTextsCount].substring(0,7);
				goAssensionTextList.add(goAssensionText);
			}//for go text spans
			System.out.println("number of GO terms: " + goAssensionTextList.size());
			
			List<String> goAssensionQuickTextList = new ArrayList<String>();
			String[] goATexts = genbankText.split("GOA:");
			for(int goATextsCount = 1; goATextsCount < goATexts.length; goATextsCount++){
				String goAssensionText = goATexts[goATextsCount].substring(0,7);
				goAssensionQuickTextList.add(goAssensionText);
			}//for goATexts
			System.out.println("number of GOA terms: " + goAssensionQuickTextList.size());
			
			//now that they are counted stick them all together to be processed
			goAssensionTextList.addAll(goAssensionQuickTextList);
			System.out.println("total go terms: " + goAssensionTextList.size());
			
			//get all anchors in genbank text
			//List<String> goAnchorLinks = new ArrayList<String>();
			List<WebElement> genebankAnchors = genbank.findElements(By.tagName("a"));
			System.out.println("number of genebank anchors found: " + genebankAnchors.size());
			ListIterator<WebElement> goAnchorsLiter = genebankAnchors.listIterator();
			while(goAnchorsLiter.hasNext()){
				WebElement currentGoAnchor = goAnchorsLiter.next();
				String currentGoAnchorString = currentGoAnchor.getText();
				String currentGoAnchorURLString = currentGoAnchor.getAttribute("href");
				if(currentGoAnchorURLString != null){ 
					if(goAssensionTextList.contains(currentGoAnchorString)){
						System.out.println("GO anchor text: " + currentGoAnchorString + " href is: " + currentGoAnchorURLString);
						synchronized(threadLogFile){
							try {
								threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
							}	catch(WebDriverException wbe)	{
								System.out.println("error attemting to call GO anchor thread: " + wbe.getMessage());
								threadLogWriter.println("Thread Id: " + threadId + " with thread name: " + threadName + 
										" ERROR: fails to spawn GO anchor thread for url: " + currentGoAnchorURLString);
								wbe.printStackTrace();
							} catch (IOException ioe) {
								System.out.println("error opening file for append: " + ioe.getMessage());
								ioe.printStackTrace();
							}//catch
							threadLogWriter.println("Thread Id: " + threadId + " with thread name: " + threadName + 
									" spawns GO anchor thread for url: " + currentGoAnchorURLString);
							threadLogWriter.flush();
							threadLogWriter.close();
						}//synchronized
						processGoAnchor(currentProtien, currentGoAnchorURLString, currentGoAnchorString, GoAnnotationLocations, "GO", threadLogFile, debug);		
					}//if goAssensionTextList.contains(currentGoAnchorString
				}//currentGoAnchorURLString != null
			}//while(goAnchorsLiter.hasNext())
		}//if gotGenebank
		
		//make output to source text file
		PrintWriter	writer = null;
		try {
			writer = new PrintWriter(new FileWriter(outFile,true));
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}//catch
		synchronized(outFile){	
			if (sourceOrProteinFeaturesListText != null){
				ListIterator<String> sourceOrProteinFeatureTextLiterForWrite = sourceOrProteinFeaturesListText.listIterator();
				writer.println("protein: " + currentProtien.toString());
				writer.println("source and protien feature texts");
				while(sourceOrProteinFeatureTextLiterForWrite.hasNext()){
					String featureText = sourceOrProteinFeatureTextLiterForWrite.next();
					writer.println(featureText);
				}//while featureElementsLiter
			}//if (sourceOrProteinFeaturesListText != null)
			else{
				writer.println("protein: " + currentProtien.toString());
				writer.println("has no features");
			}//else
			writer.println();
			writer.flush();
			writer.close();
		}//synch
		
		System.out.println("Thread Id: " + threadId + " with thread name: " + threadName + " tries to close browser for: " + proteinName);
		String currentUrl = driver.getCurrentUrl();
		synchronized(threadLogFile){
			try {
				threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
			} catch (IOException ioe) {
				System.out.println("error opening file for append: " + ioe.getMessage());
				ioe.printStackTrace();
			}//catch
			threadLogWriter.println("Thread Id: " + threadId + " with thread name: " + threadName + " tries to close browser for: " + proteinName);
			threadLogWriter.println("Thread Id: " + threadId + " with thread name: " + "driver on url: " + currentUrl + " before close attempt");
			threadLogWriter.flush();
			threadLogWriter.close();
		}//synchronized(writeToThreadLogLock)
		driver.close();
		driver.quit();
		//forceClose(driver);
		region.append(foundRegion);

		System.out.println("Thread Id: " + threadId + " with thread name: " + threadName + " ends run method");
		synchronized(threadLogFile){
			try {
				threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
			} catch (IOException ioe) {
				System.out.println("error opening file for append: " + ioe.getMessage());
				ioe.printStackTrace();
			}//catch
			threadLogWriter.println("Thread Id: " + threadId + " with thread name: " + threadName + " driver on url: " + currentUrl + " after close attempt has quit: " + driverHasQuit(driver));
			threadLogWriter.println("Thread Id: " + threadId + " with thread name: " + threadName + " ends run method");		
			threadLogWriter.flush();
			threadLogWriter.close();
		}//synchronized(writeToThreadLogLock)
		//threadLogWriter.close();
	}//processAnchorLinks
	
	//check if driver closed properly
	private static boolean driverHasQuit(WebDriver driver){
		try{
			driver.getTitle();
			return false;
		} catch (SessionNotFoundException snfe ) {
			return true;
		}//catch
	}//driverHasQuit
	
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
			Map<String, String> GoAnnotationLocations, String TypeOfGoLookup, File threadLogFile, boolean debug) throws org.openqa.selenium.WebDriverException {
		//GoAnchorLinkThreadGateway.getInstance();
		//GoAnchorLinkThreadGateway.getAnchorLinkThread(currentProtein, url, currentGoAnchorString, GoAnnotationLocations, TypeOfGoLookup, debug);
		TheSinglePriorityThreadPool.getInstance();
		TheSinglePriorityThreadPool.getGoAnchorLinkThread(currentProtein, url, currentGoAnchorString, GoAnnotationLocations,/* TypeOfGoLookup,*/ threadLogFile, debug);
		
		long threadId = Thread.currentThread().getId();
		String threadName = Thread.currentThread().getName();
		PrintWriter threadLogWriter = null;
		synchronized(threadLogFile){
			try {
				threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
			} catch (IOException ioe) {
				System.out.println("error opening file for append: " + ioe.getMessage());
				ioe.printStackTrace();
			}//catch
			threadLogWriter.println("Thread Id: " + threadId + " with thread name: " + threadName + " is in processGoAnchor after spawn call");
			threadLogWriter.flush();
			threadLogWriter.close();
		}//synchronized(writeToThreadLogLock)
	}//processGoAnchor
	
}//class


