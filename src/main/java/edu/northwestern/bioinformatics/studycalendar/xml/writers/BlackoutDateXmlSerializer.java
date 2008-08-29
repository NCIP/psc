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

import java.util.List;

/**
 * @author Saurabh Agrawal
 */
public class BlackoutDateXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<BlackoutDate> {

    private Site site;

    public BlackoutDateXmlSerializer(final Site site) {
        if (site == null) {
            throw new StudyCalendarValidationException("site can not be null");
        }
        this.site = site;
    }

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

        XsdAttribute.BLACKOUT_DATE_ID.addTo(blackoutDateElement, blackoutDate.getId());
        XsdAttribute.BLACKOUT_DATE_DESCRIPTION.addTo(blackoutDateElement, blackoutDate.getDescription());
        XsdAttribute.BLACKOUT_DATE_SITE_ID.addTo(blackoutDateElement, site.getId());

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
        String holidayId = XsdAttribute.BLACKOUT_DATE_ID.from(element);

        if (holidayId == null) {
            //create a new blackoutDate
            BlackoutDate blackoutDate = null;
            final String blackOutDateId = XsdAttribute.BLACKOUT_DATE_ID.from(element);

            final String dayOfWeek = XsdAttribute.BLACKOUT_DATE_DAY_OF_WEEK.from(element);
            final String day = XsdAttribute.BLACKOUT_DATE_DAY.from(element);

            final String month = XsdAttribute.BLACKOUT_DATE_MONTH.from(element);
            final String year = XsdAttribute.BLACKOUT_DATE_YEAR.from(element);
            final String weekNumber = XsdAttribute.BLACKOUT_DATE_WEEK_NUMBER.from(element);
            final String dateDesc = XsdAttribute.BLACKOUT_DATE_DESCRIPTION.from(element);

            if (dayOfWeek != null && !dayOfWeek.trim().equals("")) {
                blackoutDate = new WeekdayBlackout();
                ((WeekdayBlackout) blackoutDate).setDayOfTheWeek(dayOfWeek);

            } else if (month != null || day != null || year != null) {
                blackoutDate = new SpecificDateBlackout();
                if (day != null && !day.trim().equalsIgnoreCase("")) {

                    ((SpecificDateBlackout) blackoutDate).setDay(Integer.parseInt(day));
                }
                if (month != null && !month.trim().equalsIgnoreCase("")) {

                    ((SpecificDateBlackout) blackoutDate).setMonth(Integer.parseInt(month));
                }
                if (year != null && !year.trim().equalsIgnoreCase("")) {

                    ((SpecificDateBlackout) blackoutDate).setYear(Integer.parseInt(year));
                }
            } else if (weekNumber != null || dayOfWeek != null || month != null) {
                blackoutDate = new RelativeRecurringBlackout();
                ((RelativeRecurringBlackout) blackoutDate).setDayOfTheWeek(dayOfWeek);
                if (month != null && !month.trim().equalsIgnoreCase("")) {
                    ((RelativeRecurringBlackout) blackoutDate).setMonth(Integer.parseInt(month));
                }
                if (weekNumber != null && !weekNumber.trim().equalsIgnoreCase("")) {

                    ((RelativeRecurringBlackout) blackoutDate).setWeekNumber(Integer.parseInt(weekNumber));
                }

            }
            if (blackoutDate != null) {
                blackoutDate.setDescription(dateDesc);
            }
            return blackoutDate;
        }

        List<BlackoutDate> blackoutDates = site.getBlackoutDates();
        for (BlackoutDate blackoutDate : blackoutDates) {
            if (blackoutDate.getId() != null && holidayId.equals(blackoutDate.getId().intValue() + "")) {
                return blackoutDate;
            }
        }


        throw new StudyCalendarValidationException("No Holday existis with id:" + holidayId + " at the site:" + site.getAssignedIdentifier());

    }

}
