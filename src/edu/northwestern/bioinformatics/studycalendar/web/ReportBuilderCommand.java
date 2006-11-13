package edu.northwestern.bioinformatics.studycalendar.web;

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
	private Date startDate;
	private Date endDate;
	private List<Study> studies;
	private List<Site> sites;
	private List<Participant> participants;
	/*
	private List<ScheduledEvent> scheduledEvents;
	private List<ScheduledEvent> occurredEvents;
	private List<ScheduledEvent> canceledEvents;
	private List<Epoch> epoches;
	private List<Arm> arms;
	*/
	
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
	
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
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
	
	/*
	public List<ScheduledEvent> getScheduledEvents() {
		return scheduledEvents;
	}

	public void setScheduledEvents(List<ScheduledEvent> se) {
		this.scheduledEvents = se;
	}
	
	public List<ScheduledEvent> getOccurredEvents() {
		return occurredEvents;
	}

	public void setOccurredEvents(List<ScheduledEvent> oe) {
		this.occurredEvents = oe;
	}
	
	public List<ScheduledEvent> getCanceledEventsEvents() {
		return canceledEvents;
	}

	public void setCanceledEvents(List<ScheduledEvent> ce) {
		this.canceledEvents = ce;
	}

	public List<Epoch> getEpoches() {
		return epoches;
	}

	public void setEpoches(List<Epoch> es) {
		this.epoches = es;
	}
	
	public List<Arm> getArms() {
		return arms;
	}

	public void setArms(List<Arm> as) {
		this.arms = as;
	}
	*/
}
