package edu.northwestern.bioinformatics.studycalendar.dataproviders.api;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public interface StudyProvider extends DataProvider, SearchingProvider<Study> {
    /**
     * Retrieve and return new {@link Study} instances which match the identities
     * associated with the input studies.
     * The instances must be returned in the same order as the input set.
     * If the provider doesn't know about one, it must return null in its position
     * in the list.
     * <p>
     * Particular care must be taken with the assignedIdentifier field in the
     * returned <tt>Study</tt>.  This field may be modified by users of PSC,
     * so an implementation should not store a crucial value there.  Use
     * {@link edu.northwestern.bioinformatics.studycalendar.domain.Study#getSecondaryIdentifiers()}
     * instead.
     * <p>
     * Implementors must not modify the input studies -- they should return new,
     * clean instances which contain whatever information this provider offers.
     * <p>
     * Implementors may never return null from this method and must always return
     * a list of the same length as the input list.
     */
    List<Study> getStudies(List<Study> parameters);

    /**
     * Perform a search of the {@link Study} instances available from this provider.
     * @param partialName A substring of the desired name
     */
    List<Study> search(String partialName);
}
