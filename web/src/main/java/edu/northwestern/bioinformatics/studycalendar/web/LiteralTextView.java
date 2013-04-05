/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class LiteralTextView implements View {
    private static final String DEFAULT_CONTENT_TYPE = "text/plain";

    private String contentType;
    private String literalValue;

    public LiteralTextView(String literalValue) {
        this(DEFAULT_CONTENT_TYPE, literalValue);
    }

    public LiteralTextView(String contentType, String literalValue) {
        this.contentType = contentType;
        this.literalValue = literalValue;
    }

    public String getContentType() {
        return contentType;
    }

    public void render(Map ignored, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(getContentType());
        response.getWriter().write(literalValue);
    }
}