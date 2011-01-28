package edu.northwestern.bioinformatics.studycalendar.tools;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An alternative to Restlet's <code>Template</code>.  While it is generally much less flexible,
 * it works for PSC and it allows variables containing any characters, including URI-reserved
 * characters.
 *
 * @author Rhett Sutphin
 */
public class UriTemplate {
    public static final Pattern VARIABLE_RE = Pattern.compile("\\{([^\\}]+)\\}");

    private String pattern;

    public UriTemplate(String pattern) {
        this.pattern = pattern;
    }

    public String format(Resolver resolver) {
        Matcher matcher = VARIABLE_RE.matcher(pattern);
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        while(matcher.find()) {
            result.append(pattern.substring(lastEnd, matcher.start()));
            String value = resolver.resolve(matcher.group(1));
            if (value != null) result.append(value);
            lastEnd = matcher.end();
        }
        result.append(pattern.substring(lastEnd));
        return result.toString();
    }

    public interface Resolver {
        String resolve(String name);
    }

    public static class MapResolver implements Resolver {
        private Map<String, String> map;

        public MapResolver(Map<String, String> map) {
            this.map = map;
        }

        public String resolve(String name) {
            return map.get(name);
        }
    }
}
