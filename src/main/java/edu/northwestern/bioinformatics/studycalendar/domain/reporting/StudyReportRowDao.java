package edu.northwestern.bioinformatics.studycalendar.domain.reporting;

/**
 * @author John Dzak
 */
public class StudyReportRowDao extends ReportDao<StudyReportRow>{

    public Class<StudyReportRow> domainClass() {
        return StudyReportRow.class;
    }
}
