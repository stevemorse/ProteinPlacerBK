package NLP;

import java.io.File;
import java.util.Random;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.io.TemporaryFilesystem;


public class FirefoxDriverWrapper extends FirefoxDriver{
	static int filenum = 0;
	int currentInstance = 0;
	File tempCFS = null;
	TemporaryFilesystem tempFS = null;
	
	public FirefoxDriverWrapper(){
		filenum++;
		currentInstance = filenum;
	}
	public FirefoxDriver open(){
		try {
        	FirefoxProfile profile = new FirefoxProfile();
        	Random genPort = new Random();
        	int port = genPort.nextInt(500) + 7000;
        	profile.setPreference("webdriver.firefox.port", port);
           
            
            tempFS = TemporaryFilesystem.getDefaultTmpFS();
            //System.out.println(tempFS.toString());
            String dirString = tempFS.toString() + filenum;
            tempCFS = new File(dirString);
            
            TemporaryFilesystem.setTemporaryDirectory(tempCFS);
            
            
            return new FirefoxDriver(profile);
            
        } catch (WebDriverException exc) {
        	System.out.println("recursive forceinit call: " + exc.getMessage());
        	this.open();			
        }
		return new FirefoxDriver();
	}
	public void close(){
		tempFS.deleteTempDir(tempCFS);
	}
}

