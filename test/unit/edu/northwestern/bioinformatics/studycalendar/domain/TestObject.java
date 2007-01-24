package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.WithBigIdDao;

/**
 * @author Rhett Sutphin
 */
public class TestObject extends AbstractDomainObjectWithBigId {
    public TestObject() { }

    public TestObject(int id) { setId(id); }

    public TestObject(int id, String bigId) { setId(id); setBigId(bigId); }

    public static class MockableDao extends WithBigIdDao<TestObject> {
        @Override
        public Class<TestObject> domainClass() {
            return TestObject.class;
        }
    }
}
