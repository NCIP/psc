package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

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
