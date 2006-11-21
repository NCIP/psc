package edu.northwestern.bioinformatics.studycalendar.domain;

/**
 * @author Rhett Sutphin
*/
public enum NextArmMode {
    /**
     * Transition to the next arm immediately; i.e., cancel all outstanding events for any currently
     * scheduled arms.
     */
    IMMEDIATE,

    /**
     * Transition to the next arm naturally; i.e., at the end of the last scheduled arm
     */
    PER_PROTOCOL
}
