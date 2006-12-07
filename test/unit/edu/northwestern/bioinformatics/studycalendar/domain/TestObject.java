package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;

/**
 * @author Rhett Sutphin
 */
public class TestObject extends AbstractDomainObject { 
    public TestObject() { }

    public TestObject(int id) { setId(id); }

    public static class MockableDao extends StudyCalendarDao<TestObject> {
        public Class<TestObject> domainClass() {
            return TestObject.class;
        }
    }
}
