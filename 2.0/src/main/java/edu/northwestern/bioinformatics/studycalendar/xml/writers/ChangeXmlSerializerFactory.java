package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChangeAction;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.BeansException;
import org.dom4j.Element;

public class ChangeXmlSerializerFactory implements BeanFactoryAware {
    private Study study;
    private BeanFactory beanFactory;
    private String ADD_SERIALIZER = "addXmlSerializer";
    private String REMOVE_SERIALIZER = "removeXmlSerializer";
    private String REORDER_SERIALIZER = "reorderXmlSerializer";
    private String PROPERTY_CHANGE_SERIALIZER = "propertyChangeXmlSerializer";

    public AbstractChangeXmlSerializer createXmlSerializer(final Change change, final PlanTreeNode<?> deltaNode) {
        if ((ChangeAction.ADD).equals(change.getAction())) {
            return getXmlSerialzier(ADD_SERIALIZER, ((PlanTreeInnerNode)deltaNode).childClass());
        } else if ((ChangeAction.REMOVE).equals(change.getAction())) {
            return getXmlSerialzier(REMOVE_SERIALIZER, ((PlanTreeInnerNode)deltaNode).childClass());
        } else if (ChangeAction.REORDER.equals(change.getAction())) {
            return getXmlSerialzier(REORDER_SERIALIZER, ((PlanTreeInnerNode)deltaNode).childClass());
        } else if (ChangeAction.CHANGE_PROPERTY.equals(change.getAction())) {
            return getXmlSerialzier(PROPERTY_CHANGE_SERIALIZER);
        } else {
            throw new StudyCalendarError("Problem processing template. Change is not recognized: %s", change.getAction());
        }
    }

    public AbstractChangeXmlSerializer createXmlSerializer(final Element eChange, final PlanTreeNode<?> deltaNode) {
        if ((AddXmlSerializer.ADD).equals(eChange.getName())) {
            return getXmlSerialzier(ADD_SERIALIZER, ((PlanTreeInnerNode)deltaNode).childClass());
        } else if ((RemoveXmlSerializer.REMOVE).equals(eChange.getName())) {
            return getXmlSerialzier(REMOVE_SERIALIZER, ((PlanTreeInnerNode)deltaNode).childClass());
        } else if ((ReorderXmlSerializer.REORDER).equals(eChange.getName())) {
            return getXmlSerialzier(REORDER_SERIALIZER, ((PlanTreeInnerNode)deltaNode).childClass());
       } else if ((PropertyChangeXmlSerializer.PROPERTY_CHANGE).equals(eChange.getName())) {
            return getXmlSerialzier(PROPERTY_CHANGE_SERIALIZER);
        } else {
            throw new StudyCalendarError("Problem processing template. Change is not recognized: %s", eChange.getName());
        }
    }

    //// Helper Methods
    private AbstractChangeXmlSerializer getXmlSerialzier(String beanName) {
        AbstractChangeXmlSerializer serializer = (AbstractChangeXmlSerializer) beanFactory.getBean(beanName);
        serializer.setStudy(study);
        return serializer;
    }

    private AbstractChildrenChangeXmlSerializer getXmlSerialzier(String beanName, Class<?> childClass) {
        AbstractChildrenChangeXmlSerializer serializer = (AbstractChildrenChangeXmlSerializer) getXmlSerialzier(beanName);
        serializer.setStudy(study);
        serializer.setChildClass(childClass);
        return serializer;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
