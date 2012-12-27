/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.spring;

/**
 * Indicates that the bean would like to know the application path when it is
 * deployed inside a web container that has one.
 *
 * @author Jalpa Patel
 */
public interface ApplicationPathAware {
    void setApplicationPath(String applicationPath);
}
