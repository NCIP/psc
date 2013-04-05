/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.api;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;

import java.util.List;

/**
 * A provider which can determine what combinations of {@link Study} and {@link Site}
 * are allowed.  It may only provide association information for studies and sites
 * from the same provider (as designated by their respective provider tokens).
 * <p>
 * The <tt>getAssociated*</tt> methods return collections of {@link StudySite} instead of
 * merely the associated object to allow for metadata about the association to be included
 * in the future.  However, no such metadata is currently supported.
 *
 * @author Rhett Sutphin
 */
public interface StudySiteProvider extends DataProvider {
    /**
     * Retrieve and return new {@link StudySite} instances representing all known associated sites
     * for the given studies.
     * <p>
     * The returned <tt>StudySite</tt> instances should have only their {@link StudySite#setSite site}
     * fields set &mdash; not {@link StudySite#setStudy study}.  The resulting list-of-lists should 
     * have one entry for each input study.  (PSC will handle determining which sites are eventually
     * associated with the studies in the live system.)
     * <p>
     * Implementors may never return null from this method and must always return
     * an outer list of the same length as the input list.
     */
    List<List<StudySite>> getAssociatedSites(List<Study> studies);

    /**
     * Retrieve and return new {@link StudySite} instances representing all known associations
     * for the given studies.
     * <p>
     * The returned <tt>StudySite</tt> instances should have only their {@link StudySite#setStudy study}
     * fields set &mdash; not {@link StudySite#setSite site}.  The resulting list-of-lists should
     * have one entry for each input site.  (PSC will handle determining which studies are eventually
     * associated with the sites in the live system.)
     * <p>
     * Implementors may never return null from this method and must always return
     * an outer list of the same length as the input list.
     */
    List<List<StudySite>> getAssociatedStudies(List<Site> sites);
}
