package ThreadPools;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriverException;

import NLP.SingleAnchorLinkRunnable;
import NLP.SingleGoAnchorRunnable;
import protein.Protein;

public class TheSinglePriorityThreadPool {
	static ExecutorService theOnlyPool = null;
	private static volatile TheSinglePriorityThreadPool instance = null;
	private static final int THREAD_POOL_SIZE = 5;
	private static final int ANCHOR_THREAD_PRIORITY = 2;
	private static final int GO_ANCHOR_THREAD_PRIORITY = 1;
	
	//private static Protein currentProtien;
	TheSinglePriorityThreadPool(){
		//make thread pool
		theOnlyPool = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 1000L, TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue<Runnable>(THREAD_POOL_SIZE, new PriorityComparator())) {
			
			@Override
            protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable){
                RunnableFuture<T> newTaskFor = super.newTaskFor(callable);
                return new PriorityFuture<T> (newTaskFor, ((PriorityFuture<?>) callable).getPriority());
            }
			
			@Override
            protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value){
				PriorityFuture<T> f = null;
				//FutureTask<T> t = null;
				if(runnable instanceof PriorityRunnable){
					f = new PriorityFuture<T> (new FutureTask<T>(runnable, value), ((PriorityRunnable)runnable).getPriority());
				}//if
				else{
					System.err.println("task is not PriorityRunnable");
					System.exit(1);
				}
				return f;
            }
        };
		
	}//constructor
	
	
	public static TheSinglePriorityThreadPool getInstance(){
		if(instance == null){
			synchronized(TheSinglePriorityThreadPool.class){
				if(instance == null){
					instance = new TheSinglePriorityThreadPool();
				}//if instance == null
			}//synchronized
		}//if instance == null
		return instance;
	}//getInstance
	
	//thread pool manager
	public static void getAnchorLinkThread(Protein currentProtien, String accession, StringBuilder region,
			Map<String, String> goAnnotationLocations, File outFile, File threadLogFile, boolean firstAccession, boolean debug){
		try{
			theOnlyPool.submit(new SingleAnchorLinkRunnable(currentProtien, accession, region, goAnnotationLocations, outFile, threadLogFile, firstAccession, ANCHOR_THREAD_PRIORITY, debug));
		} catch (WebDriverException wde){
			System.err.println("submitted runnable throws exception: " + wde.getMessage());
		}//catch wde
		catch (Exception ge) {
				System.err.println("submitted runnable throws exception: " + ge.getMessage());	
		}//catch general exception
	}//getAnchorLinkThread
		
	public static void getGoAnchorLinkThread(Protein currentProtein, String url, String currentGoAnchorString, 
			Map<String, String> GoAnnotationLocations, /*String TypeOfGoLookup,*/ File threadLogFile, boolean debug){
		try{
			theOnlyPool.submit(new SingleGoAnchorRunnable(currentProtein, url, currentGoAnchorString, GoAnnotationLocations, /*TypeOfGoLookup,*/ threadLogFile, GO_ANCHOR_THREAD_PRIORITY, debug));
		} catch (WebDriverException wde){
			System.err.println("submitted runnable throws exception: " + wde.getMessage());
		}//catch wde
		catch (Exception ge) {
			System.err.println("submitted runnable throws exception: " + ge.getMessage());	
		}//catch general exception
	}//getGoAnchorLinkThread
	/*
	public static void AnchorPoolCleanup(){
		goAnchorPool.shutdown();
		while(!goAnchorPool.isTerminated()){}//wait on thread termination
	}//AnchorPoolCleanup
	*/
		
	/**
	 * From oracle docs
	 * http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html
	 * @param pool
	 */
	void shutdownAndAwaitTermination(ExecutorService pool) {
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(60, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
				}//
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
	   }//catch
	}//shutdownAndAwaitTermination
		
}//class
