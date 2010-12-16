package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.apache.commons.lang.StringUtils;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Rhett Sutphin
 */
public class ValidatingForm extends Form {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern INTEGER_NULL_PATTERN = Pattern.compile("^-?\\d*$");

    private List<String> errors = new ArrayList<String>();

    public ValidatingForm(Representation webForm) {
        super(webForm);
    }

    public ValidatingForm(String queryString) {
        super(queryString);
    }

    public ValidatingForm validatePresenceOf(FormParameters param) {
        String value = getFirstValue(param.attributeName());
        if (StringUtils.isBlank(value)) {
            addError("Missing required parameter %s", param.attributeName());
        }
        return this;
    }

    public ValidatingForm validateIntegralityOf(FormParameters param) {
        String name = param.attributeName();
        String value = getFirstValue(name);
        if (!StringUtils.isBlank(value)) {
            if (!INTEGER_PATTERN.matcher(value.trim()).matches()) {
                addError("Parameter %s must be an integer ('%s' isn't)", name, value);
            }
        }
        return this;
    }

    public ValidatingForm validateIntegralityOfPosNegNullInteger(FormParameters param) {
        String name = param.attributeName();
        String value = getFirstValue(name);
        if (!StringUtils.isBlank(value)) {
            if (!INTEGER_NULL_PATTERN.matcher(value.trim()).matches()) {
                addError("Parameter %s must be an integer ('%s' isn't)", name, value);
            }
        }
        return this;
    }

    public List<String> getErrors() {
        return errors;
    }

    public ValidatingForm addError(String format, Object... params) {
        getErrors().add(String.format(format, (Object[]) params));
        return this;
    }

    public void throwForValidationFailureIfNecessary() throws ResourceException {
        if (getErrors().size() == 0) return;
        throw new ResourceException(
            Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,
            StringUtils.join(getErrors().iterator(), "\n"));
    }
}
