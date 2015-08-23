package NLP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import protein.Protein;

public class SingleGoAnchorThread extends Thread{
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
	
	private Protein currentProtein = null;
	private String url = "";
	private String currentGoAnchorString = null;
	private Map<String, String> GoAnnotationLocations = null;
	private String TypeOfGoLookup = "";
	boolean debug = true;
	
	//public void processGoAnchor(Protein currentProtein, String url, String currentGoAnchorString, 
	//		Map<String, String> GoAnnotationLocations, String TypeOfGoLookup, boolean debug) throws org.openqa.selenium.WebDriverException{
	public SingleGoAnchorThread(Protein currentProtein, String url, String currentGoAnchorString, 
			Map<String, String> GoAnnotationLocations, String TypeOfGoLookup, boolean debug) {
		currentProtein = currentProtein;
		url = url;
		currentGoAnchorString = currentGoAnchorString;
		GoAnnotationLocations = GoAnnotationLocations;
		TypeOfGoLookup = TypeOfGoLookup;
		debug = debug;
	}
		
	public void run () throws org.openqa.selenium.WebDriverException{
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
	
}
