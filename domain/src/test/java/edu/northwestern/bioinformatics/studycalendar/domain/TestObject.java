package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;

/**
 * @author Rhett Sutphin
 */
public class TestObject extends AbstractMutableDomainObject {
    public TestObject() { }

    public TestObject(int id) { setId(id); }

    public TestObject(int id, String gridId) { setId(id); setGridId(gridId); }

    public static class MockableDao implements DomainObjectDao<TestObject> {
        public Class<TestObject> domainClass() {
            return TestObject.class;
        }
        
        public TestObject getById(int id) {
            throw new UnsupportedOperationException("For mocking");
        }
    }
}
