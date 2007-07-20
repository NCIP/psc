package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarGridIdentifiableDao;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;

/**
 * @author Rhett Sutphin
 */
public class TestObject extends AbstractMutableDomainObject {
    public TestObject() { }

    public TestObject(int id) { setId(id); }

    public TestObject(int id, String gridId) { setId(id); setGridId(gridId); }

    public static class MockableDao extends StudyCalendarGridIdentifiableDao<TestObject> {
        @Override
        public Class<TestObject> domainClass() {
            return TestObject.class;
        }
    }
}
