package edu.northwestern.bioinformatics.studycalendar.web.template;

import sun.jvm.hotspot.debugger.LongHashMap;

import java.util.Map;
import java.util.List;

import edu.northwestern.bioinformatics.studycalendar.domain.DomainObject;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;

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

        protected abstract List<T> getCollection();

        protected abstract T getObject();
    }

    private class DeleteEpoch extends DeleteMode<Epoch> {
        public String getRelativeViewName() {
            return "deleteEpoch";
        }

        public Map<String, Object> getModel() {
            return null;
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

        public Map<String, Object> getModel() {
             return null;
        }

        protected Arm getObject() {
            return getArm();
        }

        protected List<Arm> getCollection() {
            return getArm().getEpoch().getArms();
        }
    }
}
