/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import edu.northwestern.bioinformatics.studycalendar.domain.Parent;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChangeAction;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Reorder;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Rhett Sutphin
 */
public class MutatorFactory implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    private DaoFinder daoFinder;
    private TemplateService templateService;
    private ScheduledActivityDao scheduledActivityDao;
    private ActivityDao activityDao;
    private PopulationDao populationDao;

    @SuppressWarnings({ "unchecked" })
    public <T extends Changeable, D extends Change> Mutator createMutator(T target, D change) {
        if (change.getAction() == ChangeAction.ADD) {
            return createAddMutator(target, (Add) change);
        } else if (change.getAction() == ChangeAction.REMOVE) {
            return createRemoveMutator(target, (Remove) change);
        } else if (change.getAction() == ChangeAction.REORDER) {
            return createReorderMutator((Reorder) change);
        } else if (change.getAction() == ChangeAction.CHANGE_PROPERTY) {
            return createPropertyMutator(target, (PropertyChange) change);
        }
        throw new UnsupportedOperationException("Could not construct mutator for " + change);
    }

    private <T extends Changeable> Mutator createAddMutator(T target, Add add) {
        DomainObjectDao<? extends Child<?>> dao = findChildDao(target);
        if (target instanceof StudySegment) {
            return new AddPeriodMutator(add, (PeriodDao) dao, getSubjectService());
        } else if (target instanceof Period) {
            return new AddPlannedActivityMutator(add, (PlannedActivityDao) dao,
                getSubjectService(), getTemplateService());
        } else if (target instanceof PlannedActivity)  {
            return new AddPlannedActivityLabelMutator(add, dao);
        } else if (add.getIndex() == null) {
            return new CollectionAddMutator(add, dao);
        } else {
            return new ListAddMutator(add, dao);
        }
    }

    private <T extends Changeable> Mutator createRemoveMutator(T target, Remove remove) {
        DomainObjectDao<? extends Child<?>> dao = findChildDao(target);
        if (target instanceof StudySegment) {
            return new RemovePeriodMutator(remove, (PeriodDao) dao, getTemplateService());
        } else if (target instanceof Period) {
            return new RemovePlannedActivityMutator(remove, (PlannedActivityDao) dao);
        } else if (target instanceof PlannedActivity)  {
            return new RemovePlannedActivityLabelMutator(remove, dao);
        } else {
            return new RemoveMutator(remove,  dao);
        }
    }

    private Mutator createReorderMutator(Reorder reorder) {
        return new ReorderMutator(reorder);
    }

    private <T extends Changeable> Mutator createPropertyMutator(T target, PropertyChange change) {
        if (target instanceof Period) {
            if ("startDay".equals(change.getPropertyName())) {
                return new ChangePeriodStartDayMutator(change, getTemplateService(), getScheduleService());
            } else if ("repetitions".equals(change.getPropertyName())) {
                return new ChangePeriodRepetitionsMutator(change, getTemplateService(), getSubjectService());
            } else if ("duration.quantity".equals(change.getPropertyName())) {
                return new ChangePeriodDurationQuantityMutator(change, getTemplateService(), getScheduleService());
            } else if ("duration.unit".equals(change.getPropertyName())) {
                return new ChangePeriodDurationUnitMutator(change, getTemplateService(), getScheduleService());
            }
            // fall through
        }
        if (target instanceof PlannedActivity) {
            if ("day".equals(change.getPropertyName())) {
                return new ChangePlannedActivityDayMutator(change, getScheduleService());
            } else if ("details".equals(change.getPropertyName())) {
                return new ChangePlannedActivitySimplePropertyMutator(change);
            } else if ("activity".equals(change.getPropertyName())) {
                return new ChangePlannedActivityActivityMutator(change, activityDao);
            } else if ("population".equals(change.getPropertyName())) {
                Study study = getTemplateService().findStudy((PlanTreeNode<?>) target);
                return new ChangePlannedActivityPopulationMutator(change, study, populationDao);
            }
            // fall through
        }

        // default
        return new SimplePropertyChangeMutator(change);
    }

    @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
    private <T extends Changeable> DomainObjectDao<? extends Child<?>> findChildDao(T target) {
        Parent inner = (Parent) target;
        return (DomainObjectDao<? extends Child<?>>) findDao(inner.childClass());
    }

    protected <T extends PlanTreeNode<?>> DomainObjectDao<?> findDao(Class<T> klass) {
        return daoFinder.findDao(klass);
    }

    ////// CONFIGURATION

    // These services are extracted from the application context instead of being injected
    // to resolve a circular reference.

    protected TemplateService getTemplateService() {
        return (TemplateService) applicationContext.getBean("templateService");
    }

    protected ScheduleService getScheduleService() {
        return (ScheduleService) applicationContext.getBean("scheduleService");
    }

    protected SubjectService getSubjectService() {
        return (SubjectService) applicationContext.getBean("subjectService");
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Required
    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }

    @Required
    public void setScheduledActivityDao(ScheduledActivityDao scheduledActivityDao) {
        this.scheduledActivityDao = scheduledActivityDao;
    }

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @Required
    public void setPopulationDao(PopulationDao populationDao) {
        this.populationDao = populationDao;
    }
}
