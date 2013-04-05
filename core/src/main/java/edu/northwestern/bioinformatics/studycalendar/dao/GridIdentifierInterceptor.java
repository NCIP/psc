/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import gov.nih.nci.cabig.ctms.domain.GridIdentifiable;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Wrapper interceptor to add grid identifiers to objects which support them.
 * A cleaner implementation would be to have a separate compound interceptor implementation
 * that delegated to this class and to {@link edu.northwestern.bioinformatics.studycalendar.utils.auditing.AuditInterceptor}; unfortunately, Hibernate's
 * {@link org.hibernate.Interceptor} interface does not permit this.  (It passes single-use
 * objects as parameters -- specifically, {@link java.util.Iterator}s.  This is presumably why
 * hibernate itself only allows one interceptor at a time.)
 *
 * @author Rhett Sutphin
 */
public class GridIdentifierInterceptor extends EmptyInterceptor {
    private GridIdentifierCreator gridIdentifierCreator;


    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        boolean localMod = false;
        if (entity instanceof GridIdentifiable) {
            int gridIdIdx = findGridId(propertyNames);
            if (gridIdIdx < 0) {
                throw new StudyCalendarError(
                    "Object implements GridIdentifiable but doesn't have gridId property; class: " + entity.getClass().getName() + "; properties: " + Arrays.asList(propertyNames));
            }
            if (state[gridIdIdx] == null) {
                state[gridIdIdx] = gridIdentifierCreator.getGridIdentifier();
                localMod = true;
            }
        }

        return localMod ;
    }

    private int findGridId(String[] propertyNames) {
        for (int i = 0; i < propertyNames.length; i++) {
            if ("gridId".equals(propertyNames[i])) return i;
        }
        return -1; // defer throwing exception so we can report class
    }

    ////// CONFIGURATION

    public void setGridIdentifierCreator(GridIdentifierCreator gridIdentifierCreator) {
        this.gridIdentifierCreator = gridIdentifierCreator;
    }


}
