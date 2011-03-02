package edu.northwestern.bioinformatics.studycalendar.core.editors;
import edu.northwestern.bioinformatics.studycalendar.service.auditing.AuditEventFactory;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;

import java.util.*;

/**
 * @author Jalpa Patel
 */
public class CollectionEditor extends CustomCollectionEditor {
    private DomainObjectDao<?> dao;
    private final Class collectionType;

    public CollectionEditor(Class collectionType, DomainObjectDao<?> dao) {
        super(collectionType);
        this.dao = dao;
        this.collectionType = collectionType;
    }

    public CollectionEditor(Class collectionType) {
        super(collectionType);
        this.collectionType = collectionType;
    }

    @SuppressWarnings("unchecked")
	public void setValue(Object value) {
        if (value == null ) {
            super.setValue(null);
        } else if (value instanceof String ) {
            String valueString = (String) value;
            if (StringUtils.isBlank(valueString)) {
                super.setValue(null);
            } else {
                String[] texts = EditorUtils.splitValue(valueString);
                Collection target = createCollection(this.collectionType, texts.length);
                for (String text : texts) {
                    String decodedText = EditorUtils.getDecodedString(text);
                    if (decodedText == null || StringUtils.isBlank(decodedText)) {
                        target.add(null);
                    } else {
                        target.add(convertElement(decodedText));
                    }
                }
                super.setValue(target);
            }
        }
    }

    protected Object convertElement(String element) {
        if (dao!= null) {
            Integer id = new Integer(element);
            DomainObject newValue = dao.getById(id);
            if (newValue == null) {
                throw new IllegalArgumentException("There is no " + dao.domainClass().getSimpleName() + " with id = " + id);
            } else if (!(dao.domainClass().isAssignableFrom(newValue.getClass()))) {
                throw new IllegalArgumentException("This editor only handles instances of " + dao.domainClass().getName());
            }
            return newValue;
        } else {
            return element;
        }
    }

    public void setAsText(String text) throws IllegalArgumentException {
        setValue(text);
    }
}
