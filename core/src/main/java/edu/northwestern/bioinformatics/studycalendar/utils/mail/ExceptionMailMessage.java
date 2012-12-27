/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utils.mail;

import edu.nwu.bioinformatics.commons.ThrowableUtils;
import edu.nwu.bioinformatics.commons.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Rhett Sutphin
 */
public class ExceptionMailMessage extends StudyCalendarMailMessage {
    private static final Logger log = LoggerFactory.getLogger(ExceptionMailMessage.class);

    private static final String DISPLAY_NULL = "[null]";

    private HttpServletRequest request;
    private ServletContext servletContext;
    private Throwable uncaughtException;
    private Integer errorNumber;

    protected void onInitialization() {
        setSubject(getSubjectPrefix() + " Uncaught exception");
    }

    public String getTemplateName() {
        return "exception.ftl";
    }

    protected Map<String, Object> createTemplateContext() {
        validate();
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("stackTrace", ThrowableUtils.createStackTraceText(uncaughtException, "    "));
        context.put("errorNumber", errorNumber);
        context.put("requestParameters", convertMap(new TreeMap<String, String[]>(request.getParameterMap())));
        context.put("requestHeaders", convertMap(WebUtils.headersToMap(request)));
        context.put("requestProperties", convertMap(WebUtils.requestPropertiesToMap(request)));
        context.put("requestAttributes", convertMap(WebUtils.requestAttributesToMap(request)));
        context.put("sessionAttributes", convertMap(WebUtils.sessionAttributesToMap(request.getSession(false))));
        context.put("applicationAttributes", convertMap(WebUtils.servletContextAttributesToMap(servletContext)));
        context.put("cookies", convertCookies(request.getCookies()));
        context.put("initParameters", convertMap(WebUtils.contextInitializationParametersToMap(servletContext)));
        return context;
    }

    private List<StringElement> convertCookies(Cookie[] cookies) {
        List<StringElement> converted;
        if (cookies == null) {
            converted = Collections.emptyList();
        } else {
            converted = new ArrayList<StringElement>(cookies.length);
            for (Cookie cookie : cookies) {
                converted.add(new StringElement(cookie.getName(), cookie.getValue(), null));
            }
        }
        return converted;
    }

    private <K, E> List<DebugElement> convertMap(Map<K, E> map) {
        List<DebugElement> elts = new ArrayList<DebugElement>(map.size());
        // replace null values with display string
        for (Map.Entry<K, E> entry : map.entrySet()) {
            E value = entry.getValue();
            String name = entry.getKey().toString();
            if (value instanceof Object[]) {
                elts.add(new CollectionElement(name, convertCollection(Arrays.asList((Object[]) value)),
                    value.getClass().getName()));
            } else if (value instanceof Collection) {
                elts.add(new CollectionElement(name, convertCollection((Collection) value),
                    value.getClass().getName()));
            } else {
                elts.add(StringElement.create(name, value));
            }
        }
        return elts;
    }

    private Collection<String> convertCollection(Collection collection) {
        Collection<String> converted = new ArrayList<String>(collection.size());
        for (Object o : collection) {
            converted.add(safeToString(o));
        }
        return converted;
    }

    private void validate() {
        if (request == null) {
            throw new NullPointerException("request must be set");
        }
        if (uncaughtException == null) {
            throw new NullPointerException("uncaughtException must be set");
        }
    }

    ////// PROPERTIES

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public void setUncaughtException(Throwable uncaughtException) {
        this.uncaughtException = uncaughtException;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setErrorNumber(Integer errorNumber) {
        this.errorNumber = errorNumber;
    }

    public abstract static class DebugElement<V> {
        private String name;
        private String className;
        private V value;

        protected DebugElement(String name, V value, String className) {
            this.name = name;
            this.value = value;
            this.className = className;
        }

        protected abstract V getFilteredPasswordValue();

        public String getName() {
            return name;
        }

        public V getValue() {
            if (getName().toLowerCase().indexOf("password") >= 0) {
                return getFilteredPasswordValue();
            } else {
                return value;
            }
        }

        public String getClassName() {
            return className;
        }
    }

    private final static String FILTERED_PASSWORD_VALUE = "[PASSWORD]";

    public static final class StringElement extends DebugElement<String> {
        public StringElement(String name, String value, String className) {
            super(name,value,className);
        }

        protected String getFilteredPasswordValue() {
            return FILTERED_PASSWORD_VALUE;
        }

        public static StringElement create(String name, Object value) {
            return new StringElement(
                name,
                safeToString(value),
                value == null ? null : value.getClass().getName()
            );
        }
    }

    public static final class CollectionElement extends DebugElement<Collection<String>> {
        public CollectionElement(String name, Collection<String> value, String className) {
            super(name, value, className);
        }

        protected Collection<String> getFilteredPasswordValue() {
            return Arrays.asList(FILTERED_PASSWORD_VALUE);
        }
    }

    private static String safeToString(Object value) {
        try {
            return value == null ? DISPLAY_NULL : value.toString();
        } catch (Throwable th) {
            log.warn("Error while generating exception mail message", th);
            return String.format("[error extracting value: %s: %s]", th.getClass().getName(), th.getMessage());
        }
    }
}
