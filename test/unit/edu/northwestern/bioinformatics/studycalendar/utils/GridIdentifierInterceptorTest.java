package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.TestObject;
import edu.northwestern.bioinformatics.studycalendar.domain.AbstractDomainObject;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class GridIdentifierInterceptorTest extends StudyCalendarTestCase {
    private static final Integer ENTITY_ID = 42;
    private static final String BIG_ID = "GUIDo";
    private GridIdentifierInterceptor interceptor;
    private Interceptor delegate;

    private static final String[] STUDY_PROPERTIES = new String[] {
        "id", "name", "bigId"
    };
    private static final Type[] STUDY_TYPES = new Type[3]; // Don't care about the values

    protected void setUp() throws Exception {
        super.setUp();
        delegate = registerMockFor(Interceptor.class);

        interceptor = new GridIdentifierInterceptor();
        interceptor.setGridIdentifierCreator(new GridIdentifierCreator() {
            public String getGridIdentifier() {
                return BIG_ID;
            }
        });
        interceptor.setDelegate(delegate);
    }

    public void testIdSetOnWithBigIdWithoutGridId() throws Exception {
        Study study = Fixtures.createSingleEpochStudy("A", "E");
        Object[] state = new Object[] { null, "A", null };

        expect(delegate.onSave(study, ENTITY_ID, state, STUDY_PROPERTIES, STUDY_TYPES)).andReturn(false);

        replayMocks();
        assertTrue("Modification not flagged", interceptor.onSave(study, ENTITY_ID, state, STUDY_PROPERTIES, STUDY_TYPES));
        verifyMocks();

        assertEquals("Grid ID not set", BIG_ID, state[2]);
    }

    public void testNotSetIfAlreadyPresent() throws Exception {
        Study study = Fixtures.createSingleEpochStudy("A", "E");
        study.setBigId("Preexisting");
        Object[] state = new Object[] { null, "A", "Preexisting" };

        expect(delegate.onSave(study, ENTITY_ID, state, STUDY_PROPERTIES, STUDY_TYPES)).andReturn(false);

        replayMocks();
        assertFalse("No modification expected", interceptor.onSave(study, ENTITY_ID, state, STUDY_PROPERTIES, STUDY_TYPES));
        verifyMocks();

        assertEquals("Grid ID changed", "Preexisting", state[2]);
    }

    public void testNoErrorOnNonWithBigId() throws Exception {
        NoBigIdTestObject entity = new NoBigIdTestObject();
        Object[] state = new Object[] { };
        String[] propertyNames = new String[] { };
        Type[] types = new Type[] { };
        expect(delegate.onSave(entity, ENTITY_ID, state, propertyNames, types)).andReturn(false);

        replayMocks();
        assertFalse(interceptor.onSave(entity, ENTITY_ID, state, propertyNames, types));
        verifyMocks();
    }

    private static class NoBigIdTestObject extends AbstractDomainObject { }
}
