/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.UndoableActionListJsonRepresentation;
import edu.northwestern.bioinformatics.studycalendar.service.UserActionService;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.DATA_READER;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_TEAM_ADMINISTRATOR;

/**
 * @author Jalpa Patel
 */
public class UndoableActionsResource extends AbstractPscResource {
    private Subject subject;
    private SubjectDao subjectDao;
    private UserActionService userActionService;
    private String context;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        addAuthorizationsFor(Method.GET,
            STUDY_SUBJECT_CALENDAR_MANAGER,
            STUDY_TEAM_ADMINISTRATOR,
            DATA_READER);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    private List<UserAction> getAllObjects() throws ResourceException {
        String subjectId = UriTemplateParameters.SUBJECT_IDENTIFIER.extractFrom(getRequest());
        if (subjectId == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,"No subject identifier in request");
        }
        subject = subjectDao.findSubjectByPersonId(subjectId);
        if (subject == null) {
            subject = subjectDao.getByGridId(subjectId);
            if (subject == null) {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,"Subject doesn't exist with id " + subjectId);
            }
        }
        String rootRef = getRootRef().toString();
        context = rootRef.concat("/subjects/").concat(subject.getGridId()).concat("/schedules");
        List<UserAction> undoableActions = userActionService.getUndoableActions(context);
        if (undoableActions.isEmpty()) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,"No undoable actions available for subject with id" + subjectId);
        }
        return undoableActions;
    }

    @Override
    public Representation get(Variant variant) throws ResourceException {
        if (variant.getMediaType().includes(MediaType.APPLICATION_JSON)) {
            return new UndoableActionListJsonRepresentation(getAllObjects(), context, getRootRef().toString());
        } else {
            return super.get(variant);
        }
    }

    @Required
    public void setSubjectDao(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    @Required
    public void setUserActionService(UserActionService userActionService) {
        this.userActionService = userActionService;
    }
}
