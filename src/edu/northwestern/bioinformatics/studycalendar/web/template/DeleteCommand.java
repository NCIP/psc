package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;

import java.util.List;
import java.util.Map;

import gov.nih.nci.cabig.ctms.domain.DomainObject;

/**
 * @author Rhett Sutphin
 */
public class DeleteCommand extends ModalEditCommand {

    ////// MODES

    protected Mode epochMode() {
        return new DeleteEpoch();
    }

    protected Mode armMode() {
        return new DeleteArm();
    }

    private abstract class DeleteMode<T extends PlanTreeNode<? extends PlanTreeInnerNode>> implements Mode {
        public final void performEdit() {
            if (getObjectParent().getChildren().size() < 2) return;
            updateRevision(getObjectParent(), Remove.create(getObject()));
        }

        public Map<String, Object> getModel() {
            return null;
        }

        protected abstract T getObject();
        protected abstract PlanTreeInnerNode getObjectParent();
    }

    private class DeleteEpoch extends DeleteMode<Epoch> {
        public String getRelativeViewName() {
            return "deleteEpoch";
        }

        @Override
        protected Epoch getObject() {
            return getEpoch();
        }

        @Override
        protected PlanTreeInnerNode getObjectParent() {
            return getSafeEpochParent();
        }
    }

    private class DeleteArm extends DeleteMode<Arm> {
        public String getRelativeViewName() {
            return "deleteArm";
        }

        @Override
        protected Arm getObject() {
            return getArm();
        }

        @Override
        protected PlanTreeInnerNode getObjectParent() {
            return getSafeArmParent();
        }
    }
}
