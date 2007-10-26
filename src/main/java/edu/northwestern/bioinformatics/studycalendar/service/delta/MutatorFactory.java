package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChangeAction;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Reorder;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedEventDao;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Rhett Sutphin
 */
public class MutatorFactory {
    private DaoFinder daoFinder;
    private ParticipantService participantService;
    private TemplateService templateService;
    private ScheduleService scheduleService;

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
        if (target instanceof Arm) {
            return new AddPeriodMutator(add, (PeriodDao) dao, participantService);
        } else if (target instanceof Period) {
            return new AddPlannedEventMutator(add, (PlannedEventDao) dao,
                participantService, templateService);
        } else if (add.getIndex() == null) {
            return new CollectionAddMutator(add, dao);
        } else {
            return new ListAddMutator(add, dao);
        }
    }

    private <T extends PlanTreeNode<?>> Mutator createRemoveMutator(T target, Remove remove) {
        DomainObjectDao<? extends PlanTreeNode<?>> dao = findChildDao(target);
        if (target instanceof Arm) {
            return new RemovePeriodMutator(remove, (PeriodDao) dao, templateService);
        } else if (target instanceof Period) {
            return new RemovePlannedEventMutator(remove, (PlannedEventDao) dao);
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
                return new ChangePeriodStartDayMutator(change, templateService, scheduleService);
            } else if ("repetitions".equals(change.getPropertyName())) {
                return new ChangePeriodRepetitionsMutator(change, templateService, participantService);
            } else if ("duration.quantity".equals(change.getPropertyName())) {
                return new ChangePeriodDurationQuantityMutator(change, templateService, scheduleService);
            } else if ("duration.unit".equals(change.getPropertyName())) {
                return new ChangePeriodDurationUnitMutator(change, templateService, scheduleService);
            }
            // fall through
        }
        // TODO
        // if (target instanceof PlannedEvent) {
        //
        // } 

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

    @Required
    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }

    @Required
    public void setParticipantService(ParticipantService participantService) {
        this.participantService = participantService;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setScheduleService(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }
}
