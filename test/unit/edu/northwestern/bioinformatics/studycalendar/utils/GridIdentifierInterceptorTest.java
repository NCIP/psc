package edu.northwestern.bioinformatics.studycalendar.utils;

import static org.easymock.EasyMock.expect;

import org.hibernate.Interceptor;
import org.hibernate.type.Type;

import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

/**
 * @author Rhett Sutphin
 */
public class GridIdentifierInterceptorTest extends StudyCalendarTestCase {
    public void testDisabled() { }
    /*
	private static final Integer ENTITY_ID = 42;

	private static final String BIG_ID = "GUIDo";

	private GridIdentifierInterceptor interceptor;

	private Interceptor delegate;

	private static final String[] STUDY_PROPERTIES = new String[] { "id", "name", "gridId" };

	private static final Type[] STUDY_TYPES = new Type[3]; // Don't care about the values

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		delegate = registerMockFor(Interceptor.class);

		interceptor = new GridIdentifierInterceptor();
		interceptor.setGridIdentifierCreator(new GridIdentifierCreator() {
			public String getGridIdentifier() {
				return BIG_ID;
			}
		});
	}

	public void testIdSetOnWithGridIdWithoutGridId() throws Exception {
		Study study = Fixtures.createSingleEpochStudy("A", "E");
		Object[] state = new Object[] { null, "A", null };

		expect(delegate.onSave(study, ENTITY_ID, state, STUDY_PROPERTIES, STUDY_TYPES)).andReturn(false);

		replayMocks();
		assertTrue("Modification not flagged", interceptor.onSave(study, ENTITY_ID, state, STUDY_PROPERTIES,
				STUDY_TYPES));
		verifyMocks();

		assertEquals("Grid ID not set", BIG_ID, state[2]);
	}

	public void testNotSetIfAlreadyPresent() throws Exception {
		Study study = Fixtures.createSingleEpochStudy("A", "E");
		study.setGridId("Preexisting");
		Object[] state = new Object[] { null, "A", "Preexisting" };

		expect(delegate.onSave(study, ENTITY_ID, state, STUDY_PROPERTIES, STUDY_TYPES)).andReturn(false);

		replayMocks();
		assertFalse("No modification expected", interceptor.onSave(study, ENTITY_ID, state, STUDY_PROPERTIES,
				STUDY_TYPES));
		verifyMocks();

		assertEquals("Grid ID changed", "Preexisting", state[2]);
	}

	public void testNoErrorOnNonWithGridId() throws Exception {
		NoGridIdTestObject entity = new NoGridIdTestObject();
		Object[] state = new Object[] { ENTITY_ID };
		String[] propertyNames = new String[] { "id" };
		Type[] types = new Type[] { null };
		expect(delegate.onSave(entity, ENTITY_ID, state, propertyNames, types)).andReturn(false);

		replayMocks();
		assertFalse(interceptor.onSave(entity, ENTITY_ID, state, propertyNames, types));
		verifyMocks();
	}

	private static class NoGridIdTestObject implements DomainObject {
		public Integer getId() {
			throw new UnsupportedOperationException("getId not implemented");
		}

		public void setId(final Integer integer) {
			throw new UnsupportedOperationException("setId not implemented");
		}
	}
	*/
}
