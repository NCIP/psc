package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.web.schedule.ICalTools;
import org.restlet.data.MediaType;
import org.restlet.resource.StringRepresentation;

/**
 * @author Saurabh Agrawal
 */
public class ICSRepresentation extends StringRepresentation {


    public ICSRepresentation(final StudySubjectAssignment studySubjectAssignment) {
        super(ICalTools.create(studySubjectAssignment), MediaType.TEXT_CALENDAR);
        this.setDownloadable(true);
        this.setDownloadName(ICalTools.generateICSfileName(studySubjectAssignment));

    }


}