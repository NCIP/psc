/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataAccessException;

/**
 * Covariant PSC-specific variation on {@link UserDetailsService}.
 *
 * @author Rhett Sutphin
 */
public interface PscUserDetailsService extends UserDetailsService {
    PscUser loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException;
}
