package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import java.util.List;
import java.util.Date;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;

/*
 * @author Yufang Wang
 * @author Jaron Sampson 
 */

public class ReportBuilderCommand {
	private String startDate;
	private String endDate;
	private List<Study> studies;
	private List<Study> studiesFilter;
	private List<Site> sites;
	private List<Site> sitesFilter;
	private List<Participant> participants;
	private List<Participant> participantsFilter;

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
	
    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    
	public List<Study> getStudies() {
		return studies;
	}

	public void setStudies(List<Study> studies) {
		this.studies = studies;
	}
	
	public List<Site> getSites() {
		return sites;
	}

	public void setSites(List<Site> sites) {
		this.sites = sites;
	}
	
	public List<Participant> getParticipants() {
		return participants;
	}

	public void setParticipants(List<Participant> p) {
		this.participants = p;
	}

	public List<Site> getSitesFilter() {
		return sitesFilter;
	}

	public void setSitesFilter(List<Site> siteFilter) {
		this.sitesFilter = siteFilter;
	}

	public List<Participant> getParticipantsFilter() {
		return participantsFilter;
	}

	public void setParticipantsFilter(List<Participant> participantsFilter) {
		this.participantsFilter = participantsFilter;
	}

	public List<Study> getStudiesFilter() {
		return studiesFilter;
	}

	public void setStudiesFilter(List<Study> studiesFilter) {
		this.studiesFilter = studiesFilter;
	}
	

}
