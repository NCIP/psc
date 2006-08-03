package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author Rhett Sutphin
 */
public abstract class ContextDaoTestCase<D extends HibernateDaoSupport> extends DaoTestCase {
    protected D getDao() {
        return (D) getApplicationContext().getBean(getDaoBeanName());
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
