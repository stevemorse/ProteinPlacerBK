package ThreadPools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
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
	private static int numAnchorLinksSpwned;
	private static int numGoAnchorLinksSpwned;
	private static List<Callable<Void>> tasks;
	

	//private static Protein currentProtien;
	TheSinglePriorityThreadPool(){
		numAnchorLinksSpwned = 0;
		numGoAnchorLinksSpwned = 0;
		tasks = new ArrayList<Callable<Void>>();
		//make thread pool
		theOnlyPool = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 1000L, TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue<Runnable>(THREAD_POOL_SIZE, new PriorityComparator())) {
			/*
			@Override
            protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable){
                RunnableFuture<T> newTaskFor = super.newTaskFor(callable);
                return new PriorityFuture<T> (newTaskFor, ((PriorityFuture<?>) callable).getPriority());
            }
			*/
			@Override
            protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable){
				PriorityFuture<T> f = null;
				if(callable instanceof PriorityCallable){ 
	                //RunnableFuture<T> newTask = super.newTaskFor(callable);
	                f = new PriorityFuture<T> ((new FutureTask<T>(callable)), ((PriorityCallable)callable).getPriority());
				}
				else{
					System.err.println("task is not PriorityRunnable");
					System.exit(1);
				}
				return f;
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
			numAnchorLinksSpwned++;
			Runnable task = new SingleAnchorLinkRunnable(currentProtien, accession, region,
					goAnnotationLocations, outFile, threadLogFile, firstAccession, ANCHOR_THREAD_PRIORITY, debug);
			
			tasks.add(makePriorityRunnableIntoPriorityCallable((PriorityRunnable) task));
			theOnlyPool.submit(task);
		} catch (WebDriverException wde){
			System.err.println("submitted runnable throws exception: " + wde.getMessage());
			wde.printStackTrace(System.err);
		}//catch wde
		catch (Exception ge) {
				System.err.println("submitted runnable throws exception: " + ge.getMessage());	
				ge.printStackTrace(System.err);
		}//catch general exception
	}//getAnchorLinkThread
		
	public static void getGoAnchorLinkThread(Protein currentProtein, String url, String currentGoAnchorString, 
			Map<String, String> GoAnnotationLocations, /*String TypeOfGoLookup,*/ File threadLogFile, boolean debug){
		try{
			numGoAnchorLinksSpwned++;
			Runnable task = new SingleGoAnchorRunnable(currentProtein, url, currentGoAnchorString, GoAnnotationLocations, 
					/*TypeOfGoLookup,*/ threadLogFile, GO_ANCHOR_THREAD_PRIORITY, debug);
			tasks.add(makePriorityRunnableIntoPriorityCallable((PriorityRunnable) task));
			theOnlyPool.submit(task);
		} catch (WebDriverException wde){
			System.err.println("submitted runnable throws wde exception: " + wde.getMessage());
			wde.printStackTrace(System.err);
		}//catch WebDriverException
		catch (Exception ge) {
			System.err.println("submitted runnable throws general exception of type: " + ge.getMessage());
			ge.printStackTrace(System.err);
		}//catch general exception
	}//getGoAnchorLinkThread
	/*
	public static void AnchorPoolCleanup(){
		goAnchorPool.shutdown();
		while(!goAnchorPool.isTerminated()){}//wait on thread termination
	}//AnchorPoolCleanup
	*/
	
	public <T> void invokeAll(){
		try {
			theOnlyPool.invokeAll(tasks);
		} catch (InterruptedException ie) {
			System.err.println("ioexception in invoke all: " + ie.getMessage());
			ie.printStackTrace();
		} catch(Exception ge){
			System.err.println(" general exception in invoke all: " + ge.getMessage());
			ge.printStackTrace();
		}
	}//invokeAll()
	
	public void shutdown(){
		theOnlyPool.shutdown();
	}//shutdown
	
	public boolean awaitTermination(long timeout, TimeUnit unit){
		boolean result = false;
		try {
			result = theOnlyPool.awaitTermination(timeout, unit);
		} catch (InterruptedException ie) {
			System.err.println("ioexception in await termination " + ie.getMessage());
			ie.printStackTrace();
		}
		return result;
	}
	
	public boolean isShutDown(){
		return theOnlyPool.isShutdown();
	}
	
	/**
	 * From oracle docs
	 * http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html
	 * @param pool
	 */
	public void shutdownAndAwaitTermination(ExecutorService pool) {
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
	
	/*
	public List<Future<PriorityRunnable>> invokeAll(List<Callable<PriorityRunnable>> list){
		try {
			return theOnlyPool.invokeAll(list);
		} catch (InterruptedException ie) {
			System.err.println("error invoking all " + ie.getMessage());
			ie.printStackTrace();
			return null;
		}
	}
	*/
	//Utility function to wrap Runnables as Callables
	private static Callable makePriorityRunnableIntoPriorityCallable(final PriorityRunnable currentRunnable){
		
		return new PriorityCallable(currentRunnable.getPriority()) {
			@Override
			public Void call(){
				currentRunnable.run();
				return null;
			}
		};
	}
	
	private static Runnable convertToRunnable(final Callable callable){
		return new Runnable(){
				@Override
				public void run(){
					try{
						callable.call();
					} catch(Exception ge){
						System.err.println("error converting callable to runnable: " + ge.getMessage());
					}
				}
		};
	}
	
	public static int getNumAnchorLinksSpwned() {
		return numAnchorLinksSpwned;
	}


	public static void setNumAnchorLinksSpwned(int numAnchorLinksSpwned) {
		TheSinglePriorityThreadPool.numAnchorLinksSpwned = numAnchorLinksSpwned;
	}


	public static int getNumGoAnchorLinksSpwned() {
		return numGoAnchorLinksSpwned;
	}


	public static void setNumGoAnchorLinksSpwned(int numGoAnchorLinksSpwned) {
		TheSinglePriorityThreadPool.numGoAnchorLinksSpwned = numGoAnchorLinksSpwned;
	}

}//class
