package utils;

/**
 * a singleton lock to allow for multiple threads to coordinate their work
 * @author Steve Morse
 * @version 1.0
 */
public class SingleLock {

	private static volatile SingleLock instance = null;
	
	/**
	 * the private constructor
	 */
	private SingleLock() {       }
	
	/**
	 * gets the sole instance
	 * @return the sole instance
	 */
	public static synchronized SingleLock getInstance() {
		if(instance == null){
			instance = new SingleLock();
		}
		return instance;
	}//getInstance
	
}//SingleLock
