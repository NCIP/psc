/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.tools.UriTemplate;

import java.io.IOException;
import java.util.*;

public enum GeneratedUriTemplateVariable {
    STUDY_IDENTIFIER("study.assignedIdentifier"),
    ASSIGNMENT_IDENTIFIER("studySubjectAssignment.gridId"),
    SUBJECT_IDENTIFIER("subject.personId", "subject.gridId"),
    SCHEDULED_ACTIVITY_IDENTIFIER("scheduledActivity.gridId"),
    ACTIVITY_CODE("scheduledActivity.activity.code"),
    DAY_FROM_STUDY_PLAN("scheduledActivity.dayNumber"),
    STUDY_SUBJECT_IDENTIFIER("studySubjectAssignment.studySubjectId"),
    SITE_NAME("site.name"),
    SITE_IDENTIFIER("site.assignedIdentifier")
    ;

    private String[] resolutionPaths;
    private static Properties uriProperties;

    GeneratedUriTemplateVariable(String... resolutionPaths) {
        this.resolutionPaths = resolutionPaths;
    }

    public String attributeName() {
        return name().replaceAll("_", "-").toLowerCase();
    }

    public Object resolve(DomainContext context) {
        for (String resolutionPath : resolutionPaths) {
            Object result = context.getProperty(resolutionPath);
            if (result != null) return result;
        }
        return null;
    }

    public String getAttributeName() {
        return attributeName();
    }

    public String getDescription() {
        return getUriProperties().getProperty(attributeName() + ".description");
    }

    /**
     * Loads and returns the uri-template.properties resource.  This resource contains
     * descriptions about each variable.
    */
    private synchronized static Properties getUriProperties() {
        if (uriProperties == null) {
            uriProperties = new Properties();
            try {
                uriProperties.load(GeneratedUriTemplateVariable.class.getResourceAsStream("uri-template.properties"));
            } catch (IOException e) {
                throw new StudyCalendarError("Cannot load uri template variable descriptions from properties", e);
            }
        }
        return uriProperties;
    }

    /**
     * Returns a map containing all the statically-named URI template variables mapped to their
     * values in the given context.
     *
     * @param context
     * @return
     */
    private static Map<String, Object> getAllTemplateValues(DomainContext context) {
        Map<String, Object> all = new HashMap<String, Object>();
        for (GeneratedUriTemplateVariable variable : values()) {
            all.put(variable.attributeName(), variable.resolve(context));
        }
        return all;
    }

    /**
     * Fills the given template, using both the statically-named values from the enum and
     * any {subject-property:name} values.
     * @param template
     * @param context
     * @return the template, filled in
     */
    public static String fillTemplate(String template, DomainContext context) {
        return new UriTemplate(template).format(new Resolver(context));
    }

    private static class Resolver implements UriTemplate.Resolver {
        private DomainContext context;
        private Map<String, Object> staticValues;

        private Resolver(DomainContext context) {
            this.context = context;
            this.staticValues = getAllTemplateValues(context);
        }

        public String resolve(String name) {
            if (staticValues.containsKey(name)) {
                Object staticVal = staticValues.get(name);
                return staticVal != null ? staticVal.toString() : null;
            }
            {
                int colonAt = name.indexOf(":");
                if (colonAt > 0) {
                    String category = name.substring(0, colonAt);
                    String key = name.substring(colonAt + 1);
                    if ("subject-property".equals(category)) {
                        return context.getSubject().getProperty(key);
                    }
                }
            }

            return null;
        }
    }
}
