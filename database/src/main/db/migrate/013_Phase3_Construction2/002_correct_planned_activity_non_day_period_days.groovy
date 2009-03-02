/*
 * This fixes a long-standing bug in the way planned activities are created when they are
 * added to a period which does does not use express its duration in days.  Previously,
 * the "day" of the planned activity would be relative to the duration unit.  E.g., if
 * the duration was "week", a planned activity at the beginning of the second week would have
 * day 2.  It should have had day 8.
 *
 * This migration actually only fixes _most_ instances of this problem.  If a planned activity
 * is added in the current development amendment to an existing period, this script will not
 * find it.  Similarly, if a planned activity is removed from a period in an amendment other
 * than the current development amendment, this script will not find it.
 */
class CorrectPlannedActivityNonDayPeriodDays extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        correctDuration("week", 7)
        correctDuration("fortnight", 14)
        correctDuration("month", 28)
        correctDuration("quarter", 91)
    }

    void correctDuration(unitName, unitDays) {
        execute(
          "UPDATE planned_activities pa " +
          "SET day = ((day - 1) * " + unitDays + " + 1) " +
          "WHERE pa.period_id IN (SELECT id FROM periods WHERE duration_unit='" + unitName + "')"
        );
    }

    void down() {
        revertDuration("week", 7)
        revertDuration("fortnight", 14)
        revertDuration("month", 28)
        revertDuration("quarter", 91)
    }

    void revertDuration(unitName, unitDays) {
        execute(
          "UPDATE planned_activities pa " +
          "SET day = ((day - 1) / " + unitDays + " + 1) " +
          "WHERE pa.period_id IN (SELECT id FROM periods WHERE duration_unit='" + unitName + "')"
        );
    }
}