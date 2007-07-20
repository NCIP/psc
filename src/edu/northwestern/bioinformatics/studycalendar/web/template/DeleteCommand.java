package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;

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

    private abstract class DeleteMode<T extends DomainObject> implements Mode {
        public final void performEdit() {
            if (getCollection().size() < 2) return;
            getCollection().remove(getObject());
        }

        public Map<String, Object> getModel() {
            return null;
        }

        protected abstract List<T> getCollection();

        protected abstract T getObject();
    }

    private class DeleteEpoch extends DeleteMode<Epoch> {
        public String getRelativeViewName() {
            return "deleteEpoch";
        }

        protected Epoch getObject() {
            return getEpoch();
        }

        protected List<Epoch> getCollection() {
            return getEpoch().getPlannedCalendar().getEpochs();
        }
    }

    private class DeleteArm extends DeleteMode<Arm> {
        public String getRelativeViewName() {
            return "deleteArm";
        }

        protected Arm getObject() {
            return getArm();
        }

        protected List<Arm> getCollection() {
            return getArm().getEpoch().getArms();
        }
    }
}
