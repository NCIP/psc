package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.Parent;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChangeAction;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
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

    public ChangeXmlSerializer createXmlSerializer(final Change change, final Changeable deltaNode) {
        if ((ChangeAction.ADD).equals(change.getAction())) {
            return getXmlSerializer(ADD_SERIALIZER, ((Parent) deltaNode).childClass());
        } else if ((ChangeAction.REMOVE).equals(change.getAction())) {
            return getXmlSerializer(REMOVE_SERIALIZER, ((Parent) deltaNode).childClass());
        } else if (ChangeAction.REORDER.equals(change.getAction())) {
            return getXmlSerializer(REORDER_SERIALIZER, ((Parent) deltaNode).childClass());
        } else if (ChangeAction.CHANGE_PROPERTY.equals(change.getAction())) {
            return getXmlSerializer(PROPERTY_CHANGE_SERIALIZER);
        } else {
            throw new StudyCalendarError("Problem processing template. Change is not recognized: %s", change.getAction());
        }
    }

    public ChangeXmlSerializer createXmlSerializer(final Element eChange, final Changeable deltaNode) {
        if ((AddXmlSerializer.ADD).equals(eChange.getName())) {
            return getXmlSerializer(ADD_SERIALIZER, ((Parent) deltaNode).childClass());
        } else if ((RemoveXmlSerializer.REMOVE).equals(eChange.getName())) {
            return getXmlSerializer(REMOVE_SERIALIZER, ((Parent) deltaNode).childClass());
        } else if ((ReorderXmlSerializer.REORDER).equals(eChange.getName())) {
            return getXmlSerializer(REORDER_SERIALIZER, ((Parent) deltaNode).childClass());
       } else if ((PropertyChangeXmlSerializer.PROPERTY_CHANGE).equals(eChange.getName())) {
            return getXmlSerializer(PROPERTY_CHANGE_SERIALIZER);
        } else {
            throw new StudyCalendarError("Problem processing template. Change is not recognized: %s", eChange.getName());
        }
    }

    //// Helper Methods

    // package level for testing
    ChangeXmlSerializer getXmlSerializer(String beanName) {
        return (ChangeXmlSerializer) beanFactory.getBean(beanName);
    }

    private AbstractChildrenChangeXmlSerializer getXmlSerialzier(String beanName, Class<?> childClass) {
        AbstractChildrenChangeXmlSerializer serializer = (AbstractChildrenChangeXmlSerializer) getXmlSerializer(beanName);
        serializer.setChildClass(childClass);
        return serializer;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
