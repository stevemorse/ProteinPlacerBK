package utils;

/**
 * a singleton lock to allow for multiple threads to coordinate their work
 * @author Steve Morse
 * @version 1.0
 */
public class ThreadLogSingleLock {

	private static volatile ThreadLogSingleLock instance = null;
	
	/**
	 * the private constructor
	 */
	private ThreadLogSingleLock() {       }
	
	/**
	 * gets the sole instance
	 * @return the sole instance
	 */
	public static synchronized ThreadLogSingleLock getInstance() {
		if(instance == null){
			instance = new ThreadLogSingleLock();
		}
		return instance;
	}//getInstance
	
}//SingleLock