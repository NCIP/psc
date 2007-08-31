package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.web.template.ModalEditCommand;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;
import org.springframework.beans.factory.annotation.Required;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;


public class AmendAddToCommand extends AmendModalEditCommand {
    private static final Logger log = LoggerFactory.getLogger(AmendAddToCommand.class.getName());

    protected Mode studyMode() {
        return new AddEpoch();
    }

    protected Mode epochMode() {
        return new AddArm();
    }

    private class AddArm implements Mode {
        public String getRelativeViewName() {
            return "addArm";
        }

        public void performEdit() {
//            log.info("==== apparently I'm here");
//            Study study = getStudy();
//            Arm arm = new Arm();
//            arm.setName("[Unnamed arm]");
//            log.info(" ==== Get Epoch, get Id " + getEpoch().getId());
//            Change change = changeDao.getByNewValue(new Integer(getEpoch().getId()).toString());
//            log.info("====== Ever make it here? " + change);
//            getEpoch().addArm(arm);
//            log.info("==== ARM get Id " + arm.getId());

           //need to save the changes and edit delta
//            Add change = new Add();
//            change.setNewChildId(arm.getId());
//            //TODO - set index to change
//            change.setIndex(null);
//            change.setOldValue(null);
//            changeDao.save(change);
//
////            deltaDao.
//                Delta delta = new EpochDelta();
//                delta.setNode(arm);
//                delta.addChange(change);
////                deltaDao.save(delta);
//            getEpoch().addArm(arm);
        }

        public Map<String, Object> getModel() {
            List<Arm> arms = getEpoch().getArms();
            return new ModelMap("arm", arms.get(arms.size() - 1));
        }
    }

    private class AddEpoch implements Mode {
        public String getRelativeViewName() {
            return "addEpoch";
        }

        public Map<String, Object> getModel() {
            List<Epoch> epochs = getStudy().getPlannedCalendar().getEpochs();
            return new ModelMap("epoch", epochs.get(epochs.size() - 1));
        }

        public void performEdit() {
            Study study = getStudy();
            Amendment a = amendmentDao.getByStudyId(study.getId());
            setAmendment(a);
            Epoch epoch = Epoch.create("[Unnamed epoch]");
            epochDao.initialize(epoch);
            epochDao.save(epoch);
            //need to save the changes and edit delta
            Add change = new Add();
            //TODO - need to modify the index... 
            change.setIndex(null);
            change.setNewChildId(epoch.getId());
            change.setOldValue(null);

            Delta delta = new PlannedCalendarDelta();
            delta.setNode(study.getPlannedCalendar());
            a.addDelta(delta);
            delta.addChange(change);

            deltaDao.save(delta);
            changeDao.save(change);
            setChange(change);
            setDelta(delta);
        }
    }
}
