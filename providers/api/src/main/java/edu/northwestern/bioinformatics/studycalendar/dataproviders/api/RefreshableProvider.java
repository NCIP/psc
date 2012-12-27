/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.api;

/**
 * Providers may implement this interface to control PSC's refreshing behavior
 * with respect to their data.
 * <p>
 * If a provider does not implement this interface, PSC will never automatically
 * refresh data from that provider.
 *
 * @author Rhett Sutphin
 */
public interface RefreshableProvider {
    /**
     * Defines how often PSC should refresh instances which are retrieved from the
     * implementing provider.
     * <ul>
     *   <li>If positive, the value is the minimum number of seconds PSC will wait before
     *       refreshing the data for a particular instance.</li>
     *   <li>If zero, PSC will refresh the data every time it is accessed.  (Not recommended.)</li>
     *   <li>If <tt>null</tt> or negative, PSC will never automatically refresh data from
     *       this provider.</li>
     * </ul>
     * @return
     */
    Integer getRefreshInterval();
}
