package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializerPostProcessor;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializerPreProcessor;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;

@Transactional
public class ImportTemplateService {
    private StudyXmlSerializer studyXmlSerializer;
    private StudyXmlSerializerPreProcessor studyPreProcessor;
    private StudyXmlSerializerPostProcessor studyPostProcessor;
    private StudyDao studyDao;

    /**
     * This method will be called when importing from within the Import Template Page
     * @param stream
     */
    public void readAndSaveTemplate(InputStream stream) {
        Document document;
        try {
            SAXReader saxReader = new SAXReader();
            document = saxReader.read(stream);
        } catch(DocumentException de) {
           de.printStackTrace();
            throw new StudyCalendarSystemException("Could not read the XML for deserialization", de);
        }
        String id = XsdAttribute.STUDY_ASSIGNED_IDENTIFIER.from(document.getRootElement());
        Study study = studyDao.getByAssignedIdentifier(id);
        resetStream(stream);

        readAndSaveTemplate(study, stream);
    }

    public Study readAndSaveTemplate(Study existingStudy, InputStream stream) {
        if (existingStudy != null && existingStudy.getId() != null) {
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

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}
