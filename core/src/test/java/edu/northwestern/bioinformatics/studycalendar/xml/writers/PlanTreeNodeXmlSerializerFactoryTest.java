package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;
import org.dom4j.tree.DefaultElement;

/**
 * @author Rhett Sutphin
 */
public class PlanTreeNodeXmlSerializerFactoryTest extends StudyCalendarTestCase {
    private PlanTreeNodeXmlSerializerFactory factory = new PlanTreeNodeXmlSerializerFactory() {
        @Override
        StudyCalendarXmlSerializer<?> getXmlSerializer(String beanName) {
            return new BeanNameRecordingSerializer(beanName);
        }
    };

    public void testUnknownChangeableResultsInError() throws Exception {
        try {
            factory.createXmlSerializer(new EmptyChangeable(42));
            fail("Exception not thrown");
        } catch (StudyCalendarError sce) {
            assertEquals(
                "Could not build XML serializer for changeable EmptyChangeable[id=42].",
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


    private static class EmptyChangeable implements Changeable {
        private Integer id;

        private EmptyChangeable(int id) {
            this.id = id;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public boolean isDetached() {
            throw new UnsupportedOperationException("isDetached not implemented");
        }

        public Integer getVersion() {
            throw new UnsupportedOperationException("getVersion not implemented");
        }

        public void setVersion(Integer integer) {
            throw new UnsupportedOperationException("setVersion not implemented");
        }

        public String getGridId() {
            throw new UnsupportedOperationException("getGridId not implemented");
        }

        public void setGridId(String s) {
            throw new UnsupportedOperationException("setGridId not implemented");
        }

        public boolean hasGridId() {
            throw new UnsupportedOperationException("hasGridId not implemented");
        }

        public boolean isMemoryOnly() {
            throw new UnsupportedOperationException("isMemoryOnly not implemented");
        }

        public void setMemoryOnly(boolean memoryOnly) {
            throw new UnsupportedOperationException("setMemoryOnly not implemented");
        }

        public Changeable transientClone() {
            throw new UnsupportedOperationException("transientClone not implemented");
        }

        @Override
        public String toString() {
            return new StringBuilder(getClass().getSimpleName()).
                append("[id=").append(getId()).append(']').toString();
        }
    }
}
