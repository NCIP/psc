package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChangeAction;
import org.dom4j.Element;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class ChangeXmlSerializerFactory implements BeanFactoryAware {
    private BeanFactory beanFactory;
    private String ADD_SERIALIZER = "addXmlSerializer";
    private String REMOVE_SERIALIZER = "removeXmlSerializer";
    private String REORDER_SERIALIZER = "reorderXmlSerializer";
    private String PROPERTY_CHANGE_SERIALIZER = "propertyChangeXmlSerializer";

    public ChangeXmlSerializer createXmlSerializer(final Change change) {
        if ((ChangeAction.ADD).equals(change.getAction())) {
            return getXmlSerializer(ADD_SERIALIZER);
        } else if ((ChangeAction.REMOVE).equals(change.getAction())) {
            return getXmlSerializer(REMOVE_SERIALIZER);
        } else if (ChangeAction.REORDER.equals(change.getAction())) {
            return getXmlSerializer(REORDER_SERIALIZER);
        } else if (ChangeAction.CHANGE_PROPERTY.equals(change.getAction())) {
            return getXmlSerializer(PROPERTY_CHANGE_SERIALIZER);
        } else {
            throw new StudyCalendarError(
                "Could not build XML serializer for change %s (%s).", change, change.getAction());
        }
    }

    public ChangeXmlSerializer createXmlSerializer(final Element eChange) {
        if ((AddXmlSerializer.ADD).equals(eChange.getName())) {
            return getXmlSerializer(ADD_SERIALIZER);
        } else if ((RemoveXmlSerializer.REMOVE).equals(eChange.getName())) {
            return getXmlSerializer(REMOVE_SERIALIZER);
        } else if ((ReorderXmlSerializer.REORDER).equals(eChange.getName())) {
            return getXmlSerializer(REORDER_SERIALIZER);
       } else if ((PropertyChangeXmlSerializer.PROPERTY_CHANGE).equals(eChange.getName())) {
            return getXmlSerializer(PROPERTY_CHANGE_SERIALIZER);
        } else {
            throw new StudyCalendarError(
                "Could not build XML serializer for element %s.", eChange.getName());
        }
    }

    //// Helper Methods

    // package level for testing
    ChangeXmlSerializer getXmlSerializer(String beanName) {
        return (ChangeXmlSerializer) beanFactory.getBean(beanName);
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
