package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.DeltaNodeType;
import org.dom4j.Element;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class DeltaXmlSerializerFactory implements BeanFactoryAware {
    private BeanFactory beanFactory;

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public DeltaXmlSerializer createXmlSerializer(final Delta delta) {
        try {
            DeltaNodeType.valueForDeltaClass(delta.getClass());
        } catch (IllegalArgumentException iae) {
            throw new StudyCalendarError("Could not build XML serializer for delta %s.", delta, iae);
        }

        return getXmlSerializer(delta.getClass());
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public DeltaXmlSerializer createXmlSerializer(final Element delta) {
        String name = delta.getName().replaceAll("-delta", "").replaceAll("-", "_").toUpperCase();

        DeltaNodeType deltaNodeType;
        try {
            deltaNodeType = DeltaNodeType.valueOf(name);
        } catch (IllegalArgumentException iae) {
            throw new StudyCalendarError(
                "Could not build XML serializer for element %s.", delta.getName(), iae);
        }

        return getXmlSerializer(deltaNodeType.getDeltaClass());
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    private DeltaXmlSerializer getXmlSerializer(Class<? extends Delta> deltaClass) {
        StringBuilder beanName = new StringBuilder(deltaClass.getSimpleName()).append("XmlSerializer");
        beanName.setCharAt(0, Character.toLowerCase(beanName.charAt(0)));
        return getXmlSerializer(beanName.toString());
    }

    // package level for testing
    DeltaXmlSerializer getXmlSerializer(String beanName) {
        return (DeltaXmlSerializer) beanFactory.getBean(beanName);
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
