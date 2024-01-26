package fr.insee.queen.batch.exception;

/**
 * Class to throw a BatchException during the execution of the batch
 * @author scorcaud
 *
 */
public class BatchException extends Exception {

	/**
	 * Defaut constructor of a BatchException
	 */
	public BatchException() {
	    super();
	}

	/**
	 * Constructor for a BatchExcpetion
	 * @param message message exception
	 */
	public BatchException(String message) {
		super(message);
	}
}
