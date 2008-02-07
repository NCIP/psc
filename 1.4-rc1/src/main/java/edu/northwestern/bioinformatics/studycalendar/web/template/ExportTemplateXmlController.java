package edu.northwestern.bioinformatics.studycalendar.web.template;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Required;
import org.apache.commons.logging.Log;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * TODO: this is temporary -- will be replaced with the RESTful API's GET for the template
 *
 * @author Rhett Sutphin
 */
public class ExportTemplateXmlController extends AbstractController {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final Pattern ID_PATTERN = Pattern.compile("/([^/]+)\\.xml");
    private StudyDao studyDao;
    private DaoFinder daoFinder;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String identifier = extractIdentifier(request.getPathInfo());
        if (identifier == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not extract study identifier");
            return null;
        }
        Study study = findStudy(identifier);
        if (study == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        String xml = new StudyXMLWriter(daoFinder).createStudyXML(study);
        response.setContentType("text/xml");
        byte[] content = xml.getBytes();
        response.setContentLength(content.length);
        IOUtils.write(content, response.getOutputStream());
        return null;
    }

    // package level for testing
    String extractIdentifier(String pathInfo) {
        Matcher matcher = ID_PATTERN.matcher(pathInfo);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            log.info("Could not extract identifier from {}", pathInfo);
            return null;
        }
    }

    private Study findStudy(String identifier) {
        Study found;

        found = studyDao.getByGridId(identifier);
        if (found != null) return found;

        found = studyDao.getStudyByAssignedIdentifier(identifier);
        if (found != null) return found;

        return null;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }
}
