/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core.editors;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.AbstractControlledVocabularyObject;
import org.apache.commons.lang.StringUtils;

import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Rhett Sutphin
 */
public class ControlledVocabularyEditor extends PropertyEditorSupport {
    private static final String LOOKUP_METHOD = "getById";

    private Class<? extends AbstractControlledVocabularyObject> enumClass;
    private boolean blankAsNull;

    public <T extends AbstractControlledVocabularyObject> ControlledVocabularyEditor(Class<T> enumClass) {
        this(enumClass, false);
    }

    public <T extends AbstractControlledVocabularyObject> ControlledVocabularyEditor(Class<T> enumClass, boolean blankAsNull) {
        this.enumClass = enumClass;
        this.blankAsNull = blankAsNull;
    }

    public void setAsText(String text) throws IllegalArgumentException {
        AbstractControlledVocabularyObject newValue;
        if (text == null) {
            newValue = null;
        } else if (blankAsNull && StringUtils.isBlank(text)) {
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

