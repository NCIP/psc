/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import gov.nih.nci.cabig.ctms.domain.DomainObject;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

import java.util.*;

import static org.easymock.classextension.EasyMock.*;
import junit.framework.AssertionFailedError;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class DynamicMockDaoFinder implements DaoFinder {
    private Map<Class, DomainObjectDao> mocks;

    public DynamicMockDaoFinder() {
        mocks = new HashMap<Class, DomainObjectDao>();
    }

    public <T extends DomainObject> DomainObjectDao<?> expectDaoFor(Class<T> klass) {
        return expectDaoFor(klass, DomainObjectDao.class);
    }

    public <T extends DomainObject, C extends DomainObjectDao> C expectDaoFor(Class<T> klass, Class<C> mockClass) {
        C mock = createMock(mockClass);
        expect(mock.domainClass()).andReturn(klass).anyTimes();
        mocks.put(klass, mock);
        return mock;
    }

    public <T extends DomainObject> DomainObjectDao<?> findDao(Class<T> klass) {
        if (!mocks.containsKey(klass)) {
            throw new AssertionFailedError("Did not expect to need a DAO for " + klass.getName());
        }
        return mocks.get(klass);
    }

    public <T extends ChangeableDao> List<ChangeableDao<?>> findStudyCalendarMutableDomainObjectDaos() {
        Collection<DomainObjectDao> collectionOfDaos = mocks.values();
        List<ChangeableDao<?>> listOfDaos = new ArrayList<ChangeableDao<?>>();
        for(DomainObjectDao dao : collectionOfDaos) {
            listOfDaos.add((ChangeableDao)dao);
        }
        return listOfDaos;
    }

    public void replayAll() {
        for (DomainObjectDao mock : mocks.values()) replay(mock);
    }

    public void verifyAll() {
        for (DomainObjectDao mock : mocks.values()) verify(mock);
    }

    public void resetAll() {
        for (Map.Entry<Class, DomainObjectDao> e : mocks.entrySet()) {
            DomainObjectDao mock = e.getValue(); Class klass = e.getKey();
            reset(mock);
            expect(mock.domainClass()).andReturn(klass).anyTimes();
        }
    }
}
