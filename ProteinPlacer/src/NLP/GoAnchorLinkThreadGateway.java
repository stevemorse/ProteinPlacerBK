package NLP;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import protein.Protein;

public class GoAnchorLinkThreadGateway {
	static ExecutorService goAnchorPool = null;
	private static volatile GoAnchorLinkThreadGateway instance = null;
	
	private static Protein currentProtien;
	private GoAnchorLinkThreadGateway(){
		//make thread pool
		goAnchorPool = Executors.newFixedThreadPool(5);
	}
	
	
	public static GoAnchorLinkThreadGateway getInstance(){
		if(instance == null){
			synchronized(GoAnchorLinkThreadGateway.class){
				if(instance == null){
					instance = new GoAnchorLinkThreadGateway();
				}
			}
		}
		return instance;
	}//getInstance
	
	//thread pool manager
	public static void getAnchorLinkThread(Protein currentProtein, String url, String currentGoAnchorString, 
			Map<String, String> GoAnnotationLocations, String TypeOfGoLookup, boolean debug){
		
		goAnchorPool.execute(new SingleGoAnchorThread(currentProtein, url, currentGoAnchorString, GoAnnotationLocations, TypeOfGoLookup, debug));
	}
	
	public static void AnchorPoolCleanup(){
		goAnchorPool.shutdown();
		while(!goAnchorPool.isTerminated()){}//wait on thread termination
	}
}//class
