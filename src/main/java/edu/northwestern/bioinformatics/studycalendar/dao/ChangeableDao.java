package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nataliya Shurupova
 */
@Transactional(readOnly = true)

public abstract class ChangeableDao<T extends Changeable> extends StudyCalendarMutableDomainObjectDao<T>
        implements DeletableDomainObjectDao<T> {
    


    @SuppressWarnings("unchecked")
    protected void deleteOrphans(String className, String parentName, String classNameDelta, String parentDeltaTypeCode) {
        List<T> listOfOrphans = getHibernateTemplate().find("from "+ className +
                                                            " orphan where orphan." + parentName + " is null " +
                                                             "AND not exists (select 'x' from Add a join a.delta d where d.class=" + classNameDelta+ " and a.childIdText=orphan.id)");
//                                                +
//                                                                         " AND not exists(select 'y' from Remove r join r.delta d where d.class=" + classNameDelta+ " and r.childIdText=orphan.id)");

        if (listOfOrphans != null) {
            for (T orphan: listOfOrphans) {
                delete(orphan);
            }
        }
    }

    public abstract void deleteOrphans();
}
