
package NLP;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionNotFoundException;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import com.google.common.base.Function;

import ThreadPools.PriorityRunnable;
import protein.Protein;
import utils.ThreadLogSingleLock;

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
public class SingleGoAnchorRunnable extends PriorityRunnable{

	private Protein currentProtein = null;
	private String url = "";
	private String currentGoAnchorString = null;
	private Map<String, String> GoAnnotationLocations = null;
	private String TypeOfGoLookup = "";
	private File threadLogFile = null;
	boolean debug = true;
	
	//public void processGoAnchor(Protein currentProtein, String url, String currentGoAnchorString, 
	//		Map<String, String> GoAnnotationLocations, String TypeOfGoLookup, boolean debug) throws org.openqa.selenium.WebDriverException{
	public SingleGoAnchorRunnable(Protein currentProtein, String url, String currentGoAnchorString, 
			Map<String, String> GoAnnotationLocations, /*String TypeOfGoLookup,*/ File threadLogFile, int priority, boolean debug) {
		this.setPriority(priority);
		this.currentProtein = currentProtein;
		this.url = url;
		this.currentGoAnchorString = currentGoAnchorString;
		this.GoAnnotationLocations = GoAnnotationLocations;
		this.TypeOfGoLookup = "";
		this.threadLogFile = threadLogFile;
		this.debug = debug;
	}
	
	@Override	
	public void run () throws org.openqa.selenium.WebDriverException{
		final String amiGoTextClassName = "name";
		final String quickGoAnnotationRowOddName = "annotation-row-odd";
		final String quickGoAnnotationRowEvenName = "annotation-row-even";
		final String quickGoTextClassName = "info-definition";
		final String quickGoIDTagName = "a";
		
		long threadId = Thread.currentThread().getId();
		String threadName = Thread.currentThread().getName();
		PrintWriter threadLogWriter = null;
		if(!threadLogFile.exists()){
			System.err.println("threadLogFile not yet created");
			System.exit(1);
		}//if not exists
		
		//decide type of lookup from url passed to runner
		if(url.contains("amigo")){
			TypeOfGoLookup = "amigo";
		}//if
		else{
			TypeOfGoLookup = "go";
		}//else
		
		synchronized(threadLogFile){
			try {
				threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
				Date date = new Date();
				date.setTime(threadLogFile.lastModified());
				System.out.println("threadLogFile last modified: " + date);
				date.getTime();
				System.out.println("time is now: " + date);
				System.out.println("existing file is: " + threadLogFile.length() + " bytes long");
			} catch (IOException e1) {
				System.err.println("error opening thread log file in GO thread " + e1.getMessage());
				e1.printStackTrace();
				System.exit(1);
			}//catch
			
			//check if File is writable after putative open for append
			if(!threadLogFile.canWrite()){
				System.err.println("threadLogFile not open for write");
				System.exit(1);
			}//if not open for write
			threadLogWriter.println("Go Thread Id: " + threadId + " with thread name: " + threadName +
					" begins run method on anchor: " + currentGoAnchorString + " with url: " + url +" type is: " + TypeOfGoLookup);
			threadLogWriter.close();
		}//synchronized
		System.out.println("Go Thread Id: " + threadId + " with thread name: " + threadName + " begins run method");
		
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
		//driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		//driver.get(url);
		
		if(TypeOfGoLookup.compareTo("go") == 0){	//tradition Go web interface
			
			//get protein page
			WebElement nameElement = null;
			boolean done = true;
			do{
				driver.get(url);
				
				final Wait<WebDriver> waitDriver = new FluentWait<WebDriver>(driver)
					       .withTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
					       .pollingEvery(5, java.util.concurrent.TimeUnit.SECONDS);
				try{
					nameElement = waitDriver.until(new Function<WebDriver,WebElement>(){
						public WebElement apply(WebDriver diver){
							return driver.findElement(By.className(amiGoTextClassName));
							}});
				}
				catch(NoSuchElementException nsee){
					done = false;
				}
				catch(ElementNotVisibleException enve){
					done = false;
				}
			}while(!done);
			
			
			//WebElement nameElement = ((RemoteWebDriver) driver).findElementByClassName(amiGoTextClassName);
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
		
		
		else if (TypeOfGoLookup.compareTo("amigo") == 0){	//quick GO web interface
			
			//get the block element that contains all ami go links from the ami go page
			int errorCount = 0;
			WebElement blockElement = null;
			boolean done = true;
			do{
				driver.get(url);
				
				//invoke slight delay to allow page to render
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ie) {
					System.out.println("InterruptedException: " + ie.getMessage());
					ie.printStackTrace();
				}
				
				final Wait<WebDriver> waitDriver = new FluentWait<WebDriver>(driver)
					       .withTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
					       .pollingEvery(5, java.util.concurrent.TimeUnit.SECONDS);
				try{
					blockElement = waitDriver.until(new Function<WebDriver,WebElement>(){
						public WebElement apply(WebDriver diver){
							return driver.findElement(By.className("block"));
							}});
				}
				catch(NoSuchElementException nsee){
					System.out.println("no GOA block found");
					synchronized(threadLogFile){
						try {
							threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
						} catch (IOException ioe) {
							System.out.println("error opening file for append: " + ioe.getMessage());
							ioe.printStackTrace();
						}//catch
						threadLogWriter.println("no GOA block found");
						threadLogWriter.close();
					}//synchronized
					done = false;
				}
				catch(ElementNotVisibleException enve){
					System.out.println("no GOA block visible");
					synchronized(threadLogFile){
						try {
							threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
						} catch (IOException ioe) {
							System.out.println("error opening file for append: " + ioe.getMessage());
							ioe.printStackTrace();
						}//catch
						threadLogWriter.println("no GOA block visible");
						threadLogWriter.close();
					}//synchronized
					done = false;
				}
				errorCount++;
			}while(!done && (errorCount < 5));
			
			//List<WebElement> evenRowElements = ((RemoteWebDriver) driver).findElementsByClassName(quickGoAnnotationRowEvenName);
			//List<WebElement> oddRowElements = ((RemoteWebDriver) driver).findElementsByClassName(quickGoAnnotationRowOddName);
			List<WebElement> evenRowElements = blockElement.findElements(By.className(quickGoAnnotationRowEvenName));
			if(evenRowElements.isEmpty() || (evenRowElements == null)){
				System.out.println("no evenRowElements found");
				synchronized(threadLogFile){
					try {
						threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
					} catch (IOException ioe) {
						System.out.println("error opening file for append: " + ioe.getMessage());
						ioe.printStackTrace();
					}//catch
					threadLogWriter.println("no evenRowElements found");
					threadLogWriter.close();
				}//synchronized
			}//if evenRowElements.isEmpty()
			List<WebElement> oddRowElements = blockElement.findElements(By.className(quickGoAnnotationRowOddName));
			if(oddRowElements.isEmpty() || (oddRowElements == null)){
				System.out.println("no oddRowElements found");
				synchronized(threadLogFile){
					try {
						threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
					} catch (IOException ioe) {
						System.out.println("error opening file for append: " + ioe.getMessage());
						ioe.printStackTrace();
					}//catch
					threadLogWriter.println("no oddRowElements found");
					threadLogWriter.close();
				}//synchronized
			}//if oddRowElements.isEmpty()
			
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
		String currentUrl = driver.getCurrentUrl();
		synchronized(threadLogFile){
			try {
				threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
			} catch (IOException ioe) {
				System.out.println("error opening file for append: " + ioe.getMessage());
				ioe.printStackTrace();
			}//catch
			threadLogWriter.println("Go Thread Id: " + threadId + " with thread name: " + threadName + " tries to close browser for: " + currentGoAnchorString);
			threadLogWriter.println("Go Thread Id: " + threadId + " with thread name: " + threadName + " driver on url: " + currentUrl + " before close attempt");
			threadLogWriter.close();
		}//synchronized(writeToThreadLogLock)
		driver.close();
		driver.quit();
		//forceClose(driver);
		System.out.println("Go Thread Id: " + threadId + " with thread name: " + threadName + " ends run method");
		synchronized(threadLogFile){
			try {
				threadLogWriter = new PrintWriter(new FileWriter(threadLogFile.getAbsoluteFile(), true));
			} catch (IOException ioe) {
				System.out.println("error opening file for append: " + ioe.getMessage());
				ioe.printStackTrace();
			}//catch
			threadLogWriter.println("Go Thread Id: " + threadId + " with thread name: " + "driver on url: " + currentUrl + " after close attempt is closed: " + driverHasQuit(driver));
			threadLogWriter.println("Go Thread Id: " + threadId + " with thread name: " + threadName + " ends run method");
			threadLogWriter.close();
		}//synchronized(writeToThreadLogLock)
	}//processGoAnchor
	
	//check if driver closed properly
	private static boolean driverHasQuit(WebDriver driver){
		try{
			driver.getTitle();
			return false;
		} catch (SessionNotFoundException snfe ) {
			return true;
		}//catch
	}//driverHasQuit
}//class	

