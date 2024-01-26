package fr.insee.queen.batch.utils;

/**
 * Enum wich defines the differents error messages when a problem occurs
 * @author scorcaud
 *
 */
public enum BatchErrorCode {
	OK(0, "Exécution correcte(sans avertissement)"),
	OK_WITH_STOP(100, "Exécution correcte (mais arrêt)"),
	OK_FONCTIONAL_WARNING(200, "Exécution correcte avec des avertissements fonctionnels"),
	OK_TECHNICAL_WARNING(201, "Exécution correcte avec des avertissements techniques"),
	KO_TECHNICAL_ERROR(202, "Echec de l'exécution avec des avertissements techniques"),
	KO_FONCTIONAL_ERROR(203, "Echec de l'exécution avec des avertissements fonctionnels");

	/**
     * return code
     */
    private final int code;
	
	/**
     * label
     */
    private final String label;
	
    /**
     * Defaut constructor for a BatchErrorCode
     * @param code error code
     * @param label error label
     */
	BatchErrorCode(int code, String label) {
		this.code=code;
		this.label=label;
	}
	
	/**
	 * Get the code for a BatchErrocode
	 * @return code
	 */
	public int getCode() {
		return code;
	}
	
	/**
	 * Get the label for a BatchErrocode
	 * @return label
	 */
	public String getLabel() {
		return label;
	}
}
