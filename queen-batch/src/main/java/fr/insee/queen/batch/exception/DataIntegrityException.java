package fr.insee.queen.batch.exception;

/**
 * Class to throw a ValidateException during the step of validation
 * @author scorcaud
 *
 */
public class DataIntegrityException extends Exception {

	/**
	 * Defaut constructor of a ValidateException
	 */
	public DataIntegrityException() {
		super();
	}

	/**
	 * Constructor for a ValidateException
	 * @param message message exception
	 */
	public DataIntegrityException(String message) {
		super(message);
	}
}
