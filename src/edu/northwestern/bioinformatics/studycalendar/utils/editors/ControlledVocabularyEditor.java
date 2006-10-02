package edu.northwestern.bioinformatics.studycalendar.utils.editors;

import edu.northwestern.bioinformatics.studycalendar.domain.AbstractControlledVocabularyObject;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

import java.beans.PropertyEditorSupport;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Rhett Sutphin
 */
public class ControlledVocabularyEditor extends PropertyEditorSupport {
    private static final String LOOKUP_METHOD = "getById";

    private Class<? extends AbstractControlledVocabularyObject> enumClass;

    public <T extends AbstractControlledVocabularyObject> ControlledVocabularyEditor(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    public void setAsText(String text) throws IllegalArgumentException {
        AbstractControlledVocabularyObject newValue;
        if (text == null) {
            newValue = null;
        } else {
            Integer id = new Integer(text);
            try {
                Method lookupMethod = enumClass.getMethod(LOOKUP_METHOD, Integer.TYPE);
                newValue = (AbstractControlledVocabularyObject) lookupMethod.invoke(null, id);
            } catch (NoSuchMethodException e) {
                throw new StudyCalendarError(
                    enumClass.getName() + " is missing lookup method " + LOOKUP_METHOD, e);
            } catch (IllegalAccessException e) {
                throw new StudyCalendarSystemException(e);
            } catch (InvocationTargetException e) {
                throw new StudyCalendarSystemException(e);
            }
        }
        setValue(newValue);
    }

    public String getAsText() {
        if (getValue() == null) {
            return null;
        } else {
            AbstractControlledVocabularyObject value = (AbstractControlledVocabularyObject) getValue();
            return Integer.toString(value.getId());
        }
    }
}
