package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChangeAction;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class ChangeXmlSerializerFactoryTest extends StudyCalendarTestCase {
    private ChangeXmlSerializerFactory factory = new ChangeXmlSerializerFactory() {
        @Override
        @SuppressWarnings({ "RawUseOfParameterizedType" })
        ChangeXmlSerializer getXmlSerializer(String beanName) {
            return new BeanNameRecordingChangeSerializer(beanName);
        }
    };

    public void testUnknownChangeResultsInError() throws Exception {
        try {
            factory.createXmlSerializer(new EmptyChange(7));
            fail("Exception not thrown");
        } catch (StudyCalendarError sce) {
            assertEquals(
                "Could not build XML serializer for change EmptyChange[id=7] (null).",
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

    private static class EmptyChange extends Change {
        private EmptyChange(int id) {
            setId(id);
        }

        @Override
        public ChangeAction getAction() {
            return null;
        }

        @Override
        public boolean isNoop() {
            throw new UnsupportedOperationException("isNoop not implemented");
        }

        @Override
        public Differences deepEquals(Change o) {
            throw new UnsupportedOperationException("deepEquals not implemented");
        }

        @Override
        protected MergeLogic createMergeLogic(Delta<?> delta, Date updateTime) {
            throw new UnsupportedOperationException("createMergeLogic not implemented");
        }

        public String getNaturalKey() {
            throw new UnsupportedOperationException("getNaturalKey not implemented");
        }

        @Override
        public String toString() {
            return new StringBuilder(getClass().getSimpleName()).
                append("[id=").append(getId()).append(']').toString();
        }
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    private static class BeanNameRecordingChangeSerializer
        extends BeanNameRecordingSerializer<Change>
        implements ChangeXmlSerializer
    {
        private BeanNameRecordingChangeSerializer(String beanName) {
            super(beanName);
        }

        public StringBuffer validateElement(Change change, Element eChange) {
            throw new UnsupportedOperationException("validateElement not implemented");
        }
    }
}
