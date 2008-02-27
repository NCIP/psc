package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.service.TestingTemplateService;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class GeneratedUriTemplateVariableTest extends StudyCalendarTestCase {
    private BreadcrumbContext context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        context = new BreadcrumbContext(new TestingTemplateService());
    }

    public void testResolveWhenResolveable() throws Exception {
        String gridId = "Expected";
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        assignment.setGridId(gridId);
        context.setStudySubjectAssignment(assignment);

        assertEquals("identifier not resolved", gridId, GeneratedUriTemplateVariable.ASSIGNMENT_IDENTIFIER.resolve(context));
    }

    public void testResolveWhenNotResolveable() throws Exception {
        assertNull(GeneratedUriTemplateVariable.ASSIGNMENT_IDENTIFIER.resolve(context));
    }

    public void testResolveStudyIdentifier() throws Exception {
        Study study = new Study();
        study.setAssignedIdentifier("ABC 0532");
        context.setStudy(study);
        
        assertEquals("Identifier not resolved", "ABC 0532", GeneratedUriTemplateVariable.STUDY_IDENTIFIER.resolve(context));
    }
    
    public void testCreateAllVariablesMap() throws Exception {
        String gridId = "Expected";
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        assignment.setGridId(gridId);
        context.setStudySubjectAssignment(assignment);

        Map<String, Object> all = GeneratedUriTemplateVariable.getAllTemplateValues(context);
        assertEquals("Missing value for assignment ident", gridId, all.get("assignment-identifier"));
    }
}
