/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
import gov.nih.nci.cabig.ctms.audit.dao.DataAuditRepository;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author Rhett Sutphin
 */
public abstract class ContextDaoTestCase<D extends HibernateDaoSupport> extends DaoTestCase {
	protected D getDao() {
		return (D) getApplicationContext().getBean(getDaoBeanName());
	}

	protected DataAuditRepository getAuditDao() {
		return (DataAuditRepository) getApplicationContext().getBean("dataAuditRepository");
	}

	/**
	 * Defaults to the name of the class, less "Test", first letter in lowercase.
	 */
	protected String getDaoBeanName() {
		StringBuilder name = new StringBuilder(getClass().getSimpleName());
		name.setLength(name.length() - 4); // trim off "Test"
		name.setCharAt(0, Character.toLowerCase(name.charAt(0)));
		return name.toString();
	}
}
