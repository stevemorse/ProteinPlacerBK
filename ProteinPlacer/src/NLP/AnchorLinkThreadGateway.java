package NLP;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import protein.Protein;

public class AnchorLinkThreadGateway {
	static ExecutorService anchorPool = null;
	
	private static volatile AnchorLinkThreadGateway instance = null;

	private static Protein currentProtien;
	private AnchorLinkThreadGateway(){
		//make thread pool
		anchorPool = Executors.newFixedThreadPool(5);
	}
	
	
	
	public static AnchorLinkThreadGateway getInstance(){
		if(instance == null){
			synchronized(AnchorLinkThreadGateway.class){
				if(instance == null){
					instance = new AnchorLinkThreadGateway();
				}
			}
		}
		return instance;
	}//getInstance
	
	//thread pool manager
	public static void getAnchorLinkThread(Protein currentProtien, String accession, StringBuilder region,
			Map<String, String> goAnnotationLocations, File outFile, boolean firstAccession, boolean debug){
		
		anchorPool.execute(new SingleAnchorLinkThread(currentProtien, accession, region, goAnnotationLocations, outFile, firstAccession, debug));
	}
	
	public static void AnchorPoolCleanup(){
		anchorPool.shutdown();
		while(!anchorPool.isTerminated()){}//wait on thread termination
	}
}
