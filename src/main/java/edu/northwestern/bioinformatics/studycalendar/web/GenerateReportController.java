package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.text.DateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.beans.factory.annotation.Required;

import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ReportRowDao;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ReportRow;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;

/**
 * @author Yufang Wang
 */

@AccessControl(roles = Role.SUBJECT_COORDINATOR)
public class GenerateReportController extends SimpleFormController {
	private ReportRowDao reportRowDao;
	
	public GenerateReportController() {
		setCommandClass(GenerateReportCommand.class);
		//setFormView("generateReport");
	}
	
	public ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {	
		Collection reportRows = initializeBeanCollection();
		JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportRows);
			
		Map<String,Object> model = new HashMap<String,Object>();
		model.put("datasource", dataSource);
			
		return new ModelAndView("report", model);
	}
	
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
        //log.debug("formBackingObject");
		GenerateReportCommand command = new GenerateReportCommand();
        return command;
    }
	
	private Collection initializeBeanCollection() {
		Date date = new Date();
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
		String sd = df.format(date);
		
		List<ReportRow> reportRows = reportRowDao.getAll();
		
		return reportRows;
	}
		
	public class ReportBuilderRow {
		private String dateStr;
		private String studyId;
		private String siteId;
		private String subjectId;
		private String eventId;
		private String epochId;
		private String armId;
		
		private String currentState;
		
		private String studyName;
		private String siteName;
		private String subjectName;
		private String eventName;
		private String epochName;
		private String armName;
		
		public ReportBuilderRow(String datestr, String studyid, String siteid, String pid, String eventid, String epochid, 
				String armid, String currentState, String studyname, String sitename, String pname, 
				String eventname, String epochname, String armname){
			this.dateStr = datestr;
			this.studyId = studyid;
			this.studyName = studyname;
			this.siteId = siteid;
			this.siteName = sitename;
			this.subjectId = pid;
			this.subjectName = pname;
			this.eventId = eventid;
			this.eventName = eventname;
			this.epochId = epochid;
			this.epochName = epochname;
			this.armId = armid;
			this.armName = armname;
			this.currentState = currentState;
		}
		
		public void setDateStr(String date){
			this.dateStr = date;
		}
		
		public String getDateStr(){
			return this.dateStr;
		}
		
		public void setStudyId(String studyid){
			this.studyId = studyid;
		}
		
		public String getStudyId(){
			return this.studyId;
		}
		
		public void setStudyName(String studyname) {
			this.studyName = studyname;
		}
		
		public String getStudyName() {
			return this.studyName;
		}

		public void setSiteId(String siteid){
			this.siteId = siteid;
		}
		
		public String getSiteId(){
			return this.siteId;
		}
		
		public void setSiteName(String sitename) {
			this.siteName = sitename;
		}
		
		public String getSiteName() {
			return this.siteName;
		}

		public void setSubjectId(String pid){
			this.subjectId = pid;
		}
		
		public String getSubjectId(){
			return this.subjectId;
		}
		
		public void setSubjectName(String pname) {
			this.subjectName = pname;
		}
		
		public String getSubjectName() {
			return this.subjectName;
		}

		public void setEventId(String eventid){
			this.eventId = eventid;
		}
		
		public String getEventId(){
			return this.eventId;
		}
		
		public void setEventName(String eventname) {
			this.eventName = eventname;
		}
		
		public String getEventName() {
			return this.eventName;
		}

		public void setCurrentState(String state){
			this.currentState = state;
		}
		
		public String getCurrentState(){
			return this.currentState;
		}

		public void setEpochId(String epochid){
			this.epochId = epochid;
		}
		
		public String getEpochId(){
			return this.epochId;
		}
		
		public void setEpochName(String epochname) {
			this.epochName = epochname;
		}
		
		public String getEpochName() {
			return this.epochName;
		}

		public void setArmId(String armid){
			this.armId = armid;
		}
		
		public String getArmId(){
			return this.armId;
		}
		
		public void setArmName(String armname) {
			this.armName = armname;
		}
		
		public String getArmName() {
			return this.armName;
		}
	}

	///////////////////Config 
	@Required
	public void setReportRowDao(ReportRowDao reportRowDao) {
		this.reportRowDao = reportRowDao;
	}
}