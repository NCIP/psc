package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

/**
 * @author Rhett Sutphin
 */
public class MembraneException extends RuntimeException {
    public MembraneException(String message, Object... formatParams) {
        super(String.format(message, formatParams));
    }

    public MembraneException(Throwable cause, String message, Object... formatParams) {
        super(String.format(message, formatParams), cause);
    }
}
