package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;
import edu.northwestern.bioinformatics.studycalendar.web.ReturnSingleObjectController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class SelectScheduledArmController extends ReturnSingleObjectController {

    protected Map referenceData(HttpServletRequest request) {
        Map<String, Object> refdata = new HashMap<String, Object>();
        refdata.put("modes", ScheduledEventMode.values());

        return refdata;
    }
}
