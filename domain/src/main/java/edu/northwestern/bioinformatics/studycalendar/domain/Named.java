package edu.northwestern.bioinformatics.studycalendar.domain;

/**
 * @author Rhett Sutphin
 * @see edu.northwestern.bioinformatics.studycalendar.utils.NamedComparator
 */
public interface Named {
    /**
     * Get the name for this instance, either set explicitly or computed.
     * @return
     */
    String getName();

    /**
     * Set the name.  This is an optional operation; if an implementor uses a computed
     * name, it should throw {@link UnsupportedOperationException}.
     * @param name
     */
    void setName(String name);
}
