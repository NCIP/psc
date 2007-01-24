package edu.northwestern.bioinformatics.studycalendar.utils.editors;

import edu.northwestern.bioinformatics.studycalendar.domain.DomainObject;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;

import java.beans.PropertyEditorSupport;

/**
 * A {@link java.beans.PropertyEditor} that supports binding domain objects by their IDs
 *
 * @author Rhett Sutphin
 */
public class DaoBasedEditor extends PropertyEditorSupport {
    private StudyCalendarDao<?> dao;

    public DaoBasedEditor(StudyCalendarDao<?> dao) {
        this.dao = dao;
    }

    @Override
    public void setValue(Object value) {
        if (value != null && !(dao.domainClass().isAssignableFrom(value.getClass()))) {
            throw new IllegalArgumentException("This editor only handles instances of " + dao.domainClass().getName());
        }
        setValue((DomainObject) value);
    }

    private void setValue(DomainObject value) {
        if (value != null && value.getId() == null) {
            throw new IllegalArgumentException("This editor can't handle values without IDs");
        }
        super.setValue(value);
    }

    @Override
    public String getAsText() {
        DomainObject domainObj = (DomainObject) getValue();
        if (domainObj == null) {
            return null;
        } else {
            return domainObj.getId().toString();
        }
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        DomainObject newValue;
        if (text == null) {
            newValue = null;
        } else {
            Integer id = new Integer(text);
            newValue = dao.getById(id);
            if (newValue == null) {
                throw new IllegalArgumentException("There is no " + dao.domainClass().getSimpleName() + " with id=" + id);
            }
        }
        setValue(newValue);
    }
}
