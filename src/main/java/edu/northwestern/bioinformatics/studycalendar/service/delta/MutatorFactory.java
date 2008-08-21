package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChangeAction;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Reorder;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContext;

/**
 * @author Rhett Sutphin
 */
public class MutatorFactory implements ApplicationContextAware {
    private DaoFinder daoFinder;
    private ScheduledActivityDao scheduledActivityDao;
    private ApplicationContext applicationContext;

    @SuppressWarnings({ "unchecked" })
    public <T extends PlanTreeNode<?>, D extends Change> Mutator createMutator(T target, D change) {
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

    private <T extends PlanTreeNode<?>> Mutator createAddMutator(T target, Add add) {
        DomainObjectDao<? extends PlanTreeNode<?>> dao = findChildDao(target);
        if (target instanceof StudySegment) {
            return new AddPeriodMutator(add, (PeriodDao) dao, getSubjectService());
        } else if (target instanceof Period) {
            return new AddPlannedActivityMutator(add, (PlannedActivityDao) dao,
                getSubjectService(), getTemplateService());
        } else if (add.getIndex() == null) {
            return new CollectionAddMutator(add, dao);
        } else {
            return new ListAddMutator(add, dao);
        }
    }

    private <T extends PlanTreeNode<?>> Mutator createRemoveMutator(T target, Remove remove) {
        DomainObjectDao<? extends PlanTreeNode<?>> dao = findChildDao(target);
        if (target instanceof StudySegment) {
            return new RemovePeriodMutator(remove, (PeriodDao) dao, getTemplateService());
        } else if (target instanceof Period) {
            return new RemovePlannedActivityMutator(remove, (PlannedActivityDao) dao);
        } else {
            return new RemoveMutator(remove, dao);
        }
    }

    private Mutator createReorderMutator(Reorder reorder) {
        return new ReorderMutator(reorder);
    }

    private <T extends PlanTreeNode<?>> Mutator createPropertyMutator(T target, PropertyChange change) {
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
                return new ChangePlannedActivityDayMutator(change, scheduledActivityDao, getScheduleService());
            } else if ("details".equals(change.getPropertyName())) {
                return new ChangePlannedActivitySimplePropertyMutator(change, scheduledActivityDao);
            }
            // fall through
        }

        // default
        return new SimplePropertyChangeMutator(change);
    }

    @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
    private <T extends PlanTreeNode<?>> DomainObjectDao<? extends PlanTreeNode<?>> findChildDao(T target) {
        PlanTreeInnerNode inner = (PlanTreeInnerNode) target;
        return (DomainObjectDao<? extends PlanTreeNode<?>>) findDao(inner.childClass());
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
}
