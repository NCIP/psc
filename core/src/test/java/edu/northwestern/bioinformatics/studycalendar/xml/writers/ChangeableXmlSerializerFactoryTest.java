/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.apache.commons.lang.StringUtils;
import org.dom4j.tree.DefaultElement;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class ChangeableXmlSerializerFactoryTest extends StudyCalendarTestCase {
    private ChangeableXmlSerializerFactory factory = new ChangeableXmlSerializerFactory() {
        @Override
        StudyCalendarXmlSerializer<?> getXmlSerializer(String beanName) {
            return new BeanNameRecordingSerializer(beanName);
        }
    };
    private Reflections domainReflections;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        domainReflections = new Reflections("edu.northwestern.bioinformatics.studycalendar.domain");
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public void testASerializerCanBeBuiltForEveryChangeable() throws Exception {
        List<String> errors = new ArrayList<String>();
        for (Class<? extends Changeable> aClass : domainReflections.getSubTypesOf(Changeable.class)) {
            if (!Modifier.isAbstract(aClass.getModifiers()) && !aClass.isAnonymousClass()) {
                Changeable c = aClass.newInstance();
                try {
                    factory.createXmlSerializer(c);
                } catch (StudyCalendarError sce) {
                    errors.add(String.format("Failure with %s: %s", c, sce.getMessage()));
                }
            }
        }
        if (!errors.isEmpty()) {
            fail(StringUtils.join(errors.iterator(), '\n'));
        }
    }

    @SuppressWarnings({ "unchecked" })
    public void testTheRightSerializerIsBuiltForAKnownChangeable() throws Exception {
        BeanNameRecordingSerializer<Epoch> actual =
            (BeanNameRecordingSerializer<Epoch>) factory.createXmlSerializer(new StudySegment());
        assertEquals("studySegmentXmlSerializer", actual.getBeanName());
    }

    @SuppressWarnings({ "unchecked" })
    public void testASerializerCanBeBuiltForANodeSubclass() throws Exception {
        BeanNameRecordingSerializer<Epoch> actual =
            (BeanNameRecordingSerializer<Epoch>) factory.createXmlSerializer(new Epoch() { });
        assertEquals("epochXmlSerializer", actual.getBeanName());
    }

    @SuppressWarnings({ "unchecked" })
    public void testASpecialSerializerIsBuiltForPlannedCalendar() throws Exception {
        BeanNameRecordingSerializer<Epoch> actual =
            (BeanNameRecordingSerializer<Epoch>) factory.createXmlSerializer(new PlannedCalendar());
        assertEquals("plannedCalendarWithEpochsXmlSerializer", actual.getBeanName());
    }

    public void testUnknownChangeableResultsInError() throws Exception {
        try {
            factory.createXmlSerializer(new EmptyChangeable(42));
            fail("Exception not thrown");
        } catch (StudyCalendarError sce) {
            assertEquals(
                "There is no XsdElement for EmptyChangeable[id=42].",
                sce.getMessage());
        }
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public void testASerializerCanBeBuiltForEveryElementCorrespondingToAChangeable() throws Exception {
        List<String> errors = new ArrayList<String>();
        for (Class<? extends Changeable> aClass : domainReflections.getSubTypesOf(Changeable.class)) {
            if (!Modifier.isAbstract(aClass.getModifiers()) && !aClass.isAnonymousClass()) {
                XsdElement x = XsdElement.forCorrespondingClass(aClass);
                try {
                    factory.createXmlSerializer(x.create());
                } catch (StudyCalendarError sce) {
                    errors.add(String.format("Failure with %s: %s", x, sce.getMessage()));
                }
            }
        }
        if (!errors.isEmpty()) {
            fail(StringUtils.join(errors.iterator(), '\n'));
        }
    }

    public void testUnknownElementResultsInError() throws Exception {
        try {
            factory.createXmlSerializer(new DefaultElement("fooquux"));
            fail("Exception not thrown");
        } catch (StudyCalendarError sce) {
            assertEquals(
                "There is no XsdElement for fooquux.",
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
