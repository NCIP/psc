package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.StudySegmentDelta;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class DeltaXmlSerializerFactoryTest extends StudyCalendarTestCase {
    private DeltaXmlSerializerFactory factory = new DeltaXmlSerializerFactory() {
        @Override
        @SuppressWarnings({ "RawUseOfParameterizedType" })
        DeltaXmlSerializer getXmlSerializer(String beanName) {
            return new BeanNameRecordingDeltaSerializer(beanName);
        }
    };
    private Reflections domainReflections;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        domainReflections = new Reflections("edu.northwestern.bioinformatics.studycalendar.domain");
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public void testASerializerCanBeBuiltForEveryDelta() throws Exception {
        List<String> errors = new ArrayList<String>();
        for (Class<? extends Delta> aClass : domainReflections.getSubTypesOf(Delta.class)) {
            if (!Modifier.isAbstract(aClass.getModifiers()) && ! aClass.isAnonymousClass()) {
                Delta d = aClass.newInstance();
                try {
                    factory.createXmlSerializer(d);
                } catch (StudyCalendarError sce) {
                    errors.add(String.format("Failure with %s: %s", d, sce.getMessage()));
                }
            }
        }
        if (!errors.isEmpty()) {
            fail(StringUtils.join(errors.iterator(), '\n'));
        }
    }

    public void testUnknownDeltaResultsInError() throws Exception {
        try {
            factory.createXmlSerializer(new EmptyDelta());
            fail("Exception not thrown");
        } catch (StudyCalendarError sce) {
            assertEquals(
                "Could not build XML serializer for delta EmptyDelta[node=null].",
                sce.getMessage());
        }
    }

    public void testKnownDeltaIsMappedToTheRightBean() throws Exception {
        BeanNameRecordingDeltaSerializer xmlSerializer =
            (BeanNameRecordingDeltaSerializer) factory.createXmlSerializer(new StudySegmentDelta());
        assertEquals("studySegmentDeltaXmlSerializer", xmlSerializer.getBeanName());
    }

    public void testUnknownElementResultsInError() throws Exception {
        try {
            factory.createXmlSerializer(new DefaultElement("fooquux"));
            fail("Exception not thrown");
        } catch (StudyCalendarError sce) {
            assertEquals(
                "Could not build XML serializer for element fooquux.",
                sce.getMessage());
        }
    }

    public void testKnownElementNameIsMappedToTheRightBean() throws Exception {
        BeanNameRecordingDeltaSerializer xmlSerializer =
            (BeanNameRecordingDeltaSerializer) factory.createXmlSerializer(new DefaultElement("planned-calendar-delta"));
        assertEquals("plannedCalendarDeltaXmlSerializer", xmlSerializer.getBeanName());
    }

    private static class EmptyDelta<T extends Changeable> extends Delta<T> { }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    private static class BeanNameRecordingDeltaSerializer
        extends BeanNameRecordingSerializer<Delta>
        implements DeltaXmlSerializer
    {
        private BeanNameRecordingDeltaSerializer(String beanName) {
            super(beanName);
        }

        public String validate(Amendment releasedAmendment, Element eDelta) {
            throw new UnsupportedOperationException("validate not implemented");
        }
    }
}
