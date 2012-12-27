/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import org.codehaus.jackson.JsonGenerator;

import java.io.IOException;
import java.util.List;


/**
 * @author Nataliya Shurupova
 */
public class ActivitySourcesJsonRepresentation extends StreamingJsonRepresentation  {
    private List<Activity> activities;
    private Integer limit, offset;
    Long total;
    private List<ActivityType> activityTypes;

    public ActivitySourcesJsonRepresentation(
            List<Activity> activities, Long total, Integer offset, Integer limit, List<ActivityType> activityTypes) {
        this.activities = activities;
        this.total = total;
        this.offset = offset == null ? 0 : offset;
        this.limit = limit;
        this.activityTypes = activityTypes;
    }

    @Override
    public void generate(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
            generator.writeNumberField("total", total);
            generator.writeNumberField("offset", offset);
            if (limit != null) {
                generator.writeNumberField("limit", limit);
            }
            generator.writeFieldName("activities");
            generator.writeStartArray();
                if (activities != null) {
                    for (Activity activity: activities) {
                        writeActivity(generator, activity);
                    }
                }
            generator.writeEndArray();
            generator.writeFieldName("activity_types");
            generator.writeStartArray();
                for (ActivityType activityType: activityTypes) {
                    writeActivityType(generator, activityType);
                }
            generator.writeEndArray();
        generator.writeEndObject();
    }

    public static void writeActivityType(JsonGenerator generator, ActivityType activityType) throws IOException {
        generator.writeStartObject();
            JacksonTools.nullSafeWriteStringField(generator, "activity_type_name", activityType.getName());
        generator.writeEndObject();
    }

    public static void writeActivity(JsonGenerator generator, Activity activity) throws IOException {
        generator.writeStartObject();
            JacksonTools.nullSafeWriteStringField(generator, "activity_name", activity.getName());
            JacksonTools.nullSafeWriteStringField(generator, "activity_type", activity.getType().getName());
            JacksonTools.nullSafeWriteStringField(generator, "activity_code", activity.getCode());
            JacksonTools.nullSafeWriteStringField(generator, "activity_description", activity.getDescription());
            JacksonTools.nullSafeWriteStringField(generator, "deletable", Boolean.toString(activity.isDeletable()));
        generator.writeEndObject();
    }
}

