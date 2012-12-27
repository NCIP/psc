/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.DateFormat;
import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;

/**
 * @author Jalpa Patel
 */

public class UndoableActionListJsonRepresentation extends StreamingJsonRepresentation {
    private List<UserAction> userActionList;
    private String context;
    private String rootURI;
    private final SimpleDateFormat sdf = DateFormat.getUTCFormat();

    public UndoableActionListJsonRepresentation(List<UserAction> userActionList, String context, String rootURI) {
        this.userActionList = userActionList;
        this.context = context;
        this.rootURI = rootURI;
    }

    @Override
    public void generate(JsonGenerator generator) throws IOException, JsonGenerationException {
        generator.writeStartObject();
        writeUndoableActionContextField(generator);
        generator.writeStartArray();

        for (UserAction ua : getUserActionList()) {
            writeUndoableActionObject(generator, ua);
        }

        generator.writeEndArray();
        generator.writeEndObject();
    }

    private void writeUndoableActionContextField(JsonGenerator generator) throws IOException {
        JacksonTools.nullSafeWriteStringField(generator, "context", getContext());
        generator.writeFieldName("undoable_actions");
    }


    private void writeUndoableActionObject(JsonGenerator generator, UserAction ua) throws IOException {
        generator.writeStartObject();
        JacksonTools.nullSafeWriteStringField(generator, "description", ua.getDescription());
        JacksonTools.nullSafeWriteStringField(generator, "context", ua.getContext());
        String URI = getRootURI().concat("/user-actions/").concat(ua.getGridId());
        JacksonTools.nullSafeWriteStringField(generator, "URI", URI);
        if (ua.getTime() != null) {
            JacksonTools.nullSafeWriteStringField(generator, "time", sdf.format(ua.getTime()));
        }
        JacksonTools.nullSafeWriteStringField(generator, "action_type", ua.getActionType());
        generator.writeEndObject();
    }

    public List<UserAction> getUserActionList() {
        return userActionList;
    }

    public String getContext() {
        return context;
    }

    public String getRootURI() {
        return rootURI;
    }
}
