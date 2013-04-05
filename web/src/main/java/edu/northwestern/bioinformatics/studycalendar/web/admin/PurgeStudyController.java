/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.web.PscCancellableFormController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.apache.commons.lang.StringUtils.*;

public class PurgeStudyController extends PscCancellableFormController implements PscAuthorizedHandler {
    private StudySiteDao studySiteDao;
    private StudyDao studyDao;
    private StudyService studyService;

    public PurgeStudyController() {
        setFormView("admin/purgeStudy");
        setSuccessView("purgeStudy");
        setCommandClass(PurgeStudyCommand.class);
    }

//    Study QA Manager (Lead site only)
    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(STUDY_QA_MANAGER);
    }

    @Override
    protected Map referenceData(HttpServletRequest request) throws Exception {
        Map<String, Object> ref = new HashMap<String, Object>();

        List<StudySite> studySites = studySiteDao.getAll();

        ref.put("studySiteJSON", new StudySiteCollectionJSONRepresentation(studySites).jsonRepresentation());

        return ref;
    }


    @Override
    protected ModelAndView onSubmit(Object o) throws Exception {
        PurgeStudyCommand c = (PurgeStudyCommand) o;

        Map<String, Object> model = new HashMap<String, Object>();

        String msg;
        if (isNotBlank(c.getStudyAssignedIdentifier())) {
            String identifier = c.getStudyAssignedIdentifier();
            Study toPurge = studyDao.getByAssignedIdentifier(identifier);
            if (toPurge != null) {
                studyService.purge(toPurge);
                msg = "Study " + identifier + " has been successfully purged";
            } else {
                msg = "Study " + identifier + " failed to be purged because it could not be found";
            }
        } else {
            msg = "Please select a study"; 
        }

        model.put("status", msg);
        logger.debug(msg);

        return new ModelAndView(new RedirectView(getSuccessView()), model);
    }

    public void setStudySiteDao(StudySiteDao studySiteDao) {
        this.studySiteDao = studySiteDao;
    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    private class StudySiteCollectionJSONRepresentation {
        private List<StudySite> studySites;

        private StudySiteCollectionJSONRepresentation(List<StudySite> studySites) {
            this.studySites = studySites;
        }

        public String jsonRepresentation() throws Exception {
            JSONArray j = new JSONArray();

            for (StudySite studySite : studySites) {
                JSONObject o = new JSONObject();
                o.put("study-assigned-identifier", studySite.getStudy().getAssignedIdentifier());
                o.put("site-assigned-identifier", studySite.getSite().getAssignedIdentifier());
                o.put("subject-assignment-count", studySite.getStudySubjectAssignments().size());
                j.put(o);
            }

            return j.toString();
        }
    }
}
