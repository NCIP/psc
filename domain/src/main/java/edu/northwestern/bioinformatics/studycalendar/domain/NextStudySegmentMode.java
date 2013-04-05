/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

/**
 * @author Rhett Sutphin
*/
public enum NextStudySegmentMode {
    /**
     * Transition to the next studySegment immediately; i.e., cancel all outstanding events for any currently
     * scheduled studySegments.
     */
    IMMEDIATE,

    /**
     * Transition to the next studySegment naturally; i.e., at the end of the last scheduled studySegment
     */
    PER_PROTOCOL
}
