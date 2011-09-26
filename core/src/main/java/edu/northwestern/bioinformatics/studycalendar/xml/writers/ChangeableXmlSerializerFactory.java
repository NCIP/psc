package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import java.util.Map;

public class ChangeableXmlSerializerFactory implements BeanFactoryAware {
    private static final Map<XsdElement, String> NON_DEFAULT_BEAN_NAMES =
        new MapBuilder<XsdElement, String>().
            put(XsdElement.PLANNED_CALENDAR, "plannedCalendarWithEpochsXmlSerializer").
            toMap();

    private BeanFactory beanFactory;

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public StudyCalendarXmlSerializer createXmlSerializer(final Element node) {
        XsdElement elementType;
        try {
            elementType = XsdElement.forElement(node);
        } catch (IllegalArgumentException iae) {
            throw new StudyCalendarError("There is no XsdElement for %s.", node.getName(), iae);
        }

        return getXmlSerializer(elementType);
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public StudyCalendarXmlSerializer createXmlSerializer(final Changeable node) {
        XsdElement elementType;
        try {
            elementType = XsdElement.forCorrespondingClass(node.getClass());
        } catch (IllegalArgumentException iae) {
            throw new StudyCalendarError("There is no XsdElement for %s.", node, iae);
        }

        return getXmlSerializer(elementType);
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    private StudyCalendarXmlSerializer getXmlSerializer(XsdElement elementType) {
        String beanName = NON_DEFAULT_BEAN_NAMES.get(elementType);
        if (beanName == null) {
            StringBuilder buildBeanName =
                new StringBuilder(elementType.correspondingClass().getSimpleName()).
                    append("XmlSerializer");
            buildBeanName.setCharAt(0, Character.toLowerCase(buildBeanName.charAt(0)));
            beanName = buildBeanName.toString();
        }
        return getXmlSerializer(beanName);
    }

    // package level for testing
    @SuppressWarnings({ "RawUseOfParameterizedType" })
    StudyCalendarXmlSerializer getXmlSerializer(String beanName) {
        return (StudyCalendarXmlSerializer) beanFactory.getBean(beanName);
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
