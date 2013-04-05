/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.BlackoutDate;
import edu.northwestern.bioinformatics.studycalendar.domain.RelativeRecurringBlackout;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.SpecificDateBlackout;
import edu.northwestern.bioinformatics.studycalendar.domain.WeekdayBlackout;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

/**
 * @author Saurabh Agrawal
 */
public class BlackoutDateXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<BlackoutDate> {

    @Override
    protected XsdElement collectionRootElement() {
        return XsdElement.BLACKOUT_DATES;
    }

    @Override
    protected XsdElement rootElement() {
        return XsdElement.BLACKOUT_DATE;
    }

    @Override
    protected Element createElement(final BlackoutDate blackoutDate, final boolean inCollection) {
        if (blackoutDate == null) {
            throw new StudyCalendarValidationException("blackoutDate can not be null");

        }
        Element blackoutDateElement = rootElement().create();

        XsdAttribute.BLACKOUT_DATE_ID.addTo(blackoutDateElement, blackoutDate.getGridId());
        XsdAttribute.BLACKOUT_DATE_DESCRIPTION.addTo(blackoutDateElement, blackoutDate.getDescription());
        XsdAttribute.BLACKOUT_DATE_SITE_ID.addTo(blackoutDateElement, blackoutDate.getSite().getAssignedIdentifier());

        if (blackoutDate instanceof WeekdayBlackout) {
            WeekdayBlackout dayOfTheWeek = (WeekdayBlackout) blackoutDate;
            XsdAttribute.BLACKOUT_DATE_DAY_OF_WEEK.addTo(blackoutDateElement, dayOfTheWeek.getDayOfTheWeek());

        } else if (blackoutDate instanceof SpecificDateBlackout) {
            SpecificDateBlackout monthDayHoliday = (SpecificDateBlackout) blackoutDate;
            XsdAttribute.BLACKOUT_DATE_DAY.addTo(blackoutDateElement, monthDayHoliday.getDay());
            XsdAttribute.BLACKOUT_DATE_MONTH.addTo(blackoutDateElement, monthDayHoliday.getMonth());
            XsdAttribute.BLACKOUT_DATE_YEAR.addTo(blackoutDateElement, monthDayHoliday.getYear());

        } else if (blackoutDate instanceof RelativeRecurringBlackout) {
            RelativeRecurringBlackout relativeRecurringHoliday = (RelativeRecurringBlackout) blackoutDate;
            XsdAttribute.BLACKOUT_DATE_DAY_OF_WEEK.addTo(blackoutDateElement, relativeRecurringHoliday.getDayOfTheWeek());
            XsdAttribute.BLACKOUT_DATE_WEEK_NUMBER.addTo(blackoutDateElement, relativeRecurringHoliday.getWeekNumber());
            XsdAttribute.BLACKOUT_DATE_MONTH.addTo(blackoutDateElement, relativeRecurringHoliday.getMonth());

        }
        return blackoutDateElement;
    }

    @Override
    public BlackoutDate readElement(Element element) {
        if (element == null) {
            throw new StudyCalendarValidationException("element can not be null");

        }
        BlackoutDate blackoutDate;
        String siteIdentifier = XsdAttribute.BLACKOUT_DATE_SITE_ID.from(element);
        String dayOfWeek = XsdAttribute.BLACKOUT_DATE_DAY_OF_WEEK.from(element);
        String day = XsdAttribute.BLACKOUT_DATE_DAY.from(element);
        String month = XsdAttribute.BLACKOUT_DATE_MONTH.from(element);
        String year = XsdAttribute.BLACKOUT_DATE_YEAR.from(element);
        String weekNumber = XsdAttribute.BLACKOUT_DATE_WEEK_NUMBER.from(element);
        String dateDesc = XsdAttribute.BLACKOUT_DATE_DESCRIPTION.from(element);

        if (month != null && day != null) {
            SpecificDateBlackout specificDateBlackout = new SpecificDateBlackout();
            specificDateBlackout.setMonth(Integer.parseInt(month));
            specificDateBlackout.setDay(Integer.parseInt(day));
            if (year != null) specificDateBlackout.setYear(Integer.parseInt(year));
            blackoutDate = specificDateBlackout;
        } else if (dayOfWeek != null && weekNumber == null && month == null) {
            WeekdayBlackout dayOfTheWeek = new WeekdayBlackout();
            dayOfTheWeek.setDayOfTheWeek(dayOfWeek);
            blackoutDate = dayOfTheWeek;
        } else {
            RelativeRecurringBlackout relativeRecurringBlackout = new RelativeRecurringBlackout();
            relativeRecurringBlackout.setDayOfTheWeek(dayOfWeek);
            relativeRecurringBlackout.setMonth(Integer.parseInt(month));
            relativeRecurringBlackout.setWeekNumber(Integer.parseInt(weekNumber));
            blackoutDate = relativeRecurringBlackout;
        }

        if (blackoutDate != null) {
            blackoutDate.setDescription(dateDesc);
        }
        
        if (siteIdentifier != null) {
            Site site = new Site();
            site.setAssignedIdentifier(siteIdentifier);
            blackoutDate.setSite(site);
        }
        return blackoutDate;
   }
}
