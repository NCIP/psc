/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

/**
 * Utility Class which invoked hibernate operation like session flush. New method can be added as required.
 * @author Jalpa Patel
 */
public class DaoTools extends org.springframework.orm.hibernate3.support.HibernateDaoSupport {

    /**
     * To flush the hibernate session whenever hibernate is not flushing session after commit operation.
     */
    public void forceFlush() {
        getSession().flush();
    }
}
