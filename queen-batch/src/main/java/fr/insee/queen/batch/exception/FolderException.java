package fr.insee.queen.batch.exception;

import java.io.Serial;

/**
 * Class to throw a FolderException
 * @author scorcaud
 *
 */
public class FolderException extends Exception {

	/**
	 * Defaut constructor of a FolderException
	 */
	public FolderException() {
		super();
	}

	/**
	 * Constructor for a FolderException
	 * @param message message exception
	 */
	public FolderException(String message) {
		super(message);
	}
}
