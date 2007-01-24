package edu.northwestern.bioinformatics.studycalendar.utils.editors;

import edu.northwestern.bioinformatics.studycalendar.dao.WithBigIdDao;
import edu.northwestern.bioinformatics.studycalendar.domain.WithBigId;

/**
 * A {@link java.beans.PropertyEditor} that supports binding domain objects by their IDs or their
 * 
 *
 * @author Rhett Sutphin
 */
public class WithBigIdDaoBasedEditor extends DaoBasedEditor {
    private WithBigIdDao<?> withBigIdDao;

    public WithBigIdDaoBasedEditor(WithBigIdDao<?> dao) {
        super(dao);
        withBigIdDao = dao;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        try {
            super.setAsText(text);
            return;
        } catch (IllegalArgumentException iae) {
            // Fall through
        }

        WithBigId value = withBigIdDao.getByBigId(text);
        if (value == null) {
            throw new IllegalArgumentException("There is no "
                + withBigIdDao.domainClass().getSimpleName() + " with id or bigId " + text);
        } else {
            setValue(value);
        }
    }
}
