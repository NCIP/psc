package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializerPostProcessor;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializerPreProcessor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;

@Transactional
public class ImportTemplateService {
    private StudyXmlSerializer studyXmlSerializer;
    private StudyXmlSerializerPreProcessor studyPreProcessor;
    private StudyXmlSerializerPostProcessor studyPostProcessor;

    /**
     * This method will be called when importing from within the Import Template Page
     * @param stream
     */
    public void readAndSaveTemplate(InputStream stream) {
        Study study = studyXmlSerializer.readDocument(stream);
        resetStream(stream);

        readAndSaveTemplate(study, stream);
    }

    public Study readAndSaveTemplate(Study existingStudy, InputStream stream) {
        if (existingStudy != null) {
            studyPreProcessor.process(existingStudy);
        }

        Study study = studyXmlSerializer.readDocument(stream);

        studyPostProcessor.process(study);

        return study;
    }

    ////// Helper Methods
    private void resetStream(InputStream stream) {
        try {
            stream.reset();
        } catch (IOException ioe) {
            throw new StudyCalendarSystemException("Problem importing template");
        }
    }

    ////// Bean Setters
    @Required
    public void setStudyXmlSerializer(StudyXmlSerializer studyXmlSerializer) {
        this.studyXmlSerializer = studyXmlSerializer;
    }

    @Required
    public void setStudyXmlSerializerPreProcessor(StudyXmlSerializerPreProcessor studyPreProcessor) {
        this.studyPreProcessor = studyPreProcessor;
    }

    @Required
    public void setStudyXmlSerializerPostProcessor(StudyXmlSerializerPostProcessor studyPostProcessor) {
        this.studyPostProcessor = studyPostProcessor;
    }
}
