package edu.northwestern.bioinformatics.studycalendar.dao.reporting;

import org.hibernate.Session;
import org.hibernate.SQLQuery;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ReportRow;
import org.hibernate.cfg.Configuration;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

//import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import java.sql.SQLException;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

import org.apache.log4j.Logger;

/**
 * @author Yufang Wang
 */
public class ReportRowDao extends HibernateDaoSupport { 
	private static final Logger log = Logger.getLogger(ReportRowDao.class.getName());
	
	public List<ReportRow> getReportRow(){
		if(getSessionFactory() == null){
    		log.debug("!!!!!!!!!!!!!!!!!!!!!getSessionFactory is null!!!!!!!!!!!!!!!!!");
    	} else {
    		log.debug("!!!!!!!!!!!!!!!!!!!!!getSessionFactory generated successfully;");
    	}
		if(getHibernateTemplate() == null){
    		log.debug("!!!!!!!!!!!!!!!!!!!!!getHibernateTemplate is null!!!!!!!!!!!!!!!!!");
    	} else {
    		log.debug("!!!!!!!!!!!!!!!!!!!!!getHibernateTemplate generated successfully;");
    	}
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
	        	if(session == null){
	        		log.debug("!!!!!!!!!!!!!!!!!!!!!session is null!!!!!!!!!!!!!!!!!");
	        	} else {
	        		log.debug("!!!!!!!!!!!!!!!!!!!!!session generated successfully;");
	        	}
				SQLQuery myQuery = session.createSQLQuery(
                	"SELECT " +
                		"se.id as event_id, act.name as event_name, ses.name as state_name, se.actual_date as date, " +
                		"a.name as arm_name, e.name as epoch_name, p.first_name as p_first_name, p.last_name as p_last_name, " +
                		"st.name as study_name, si.name as site_name " + 
                	"FROM " + 
                		"scheduled_arms sa, planned_events pe, scheduled_calendars sc, " + 
        				"participant_assignments pa, study_sites ss, activities act, " +
        				"scheduled_event_states ses, scheduled_events se, arms a, epochs e, " +  
        				"participants p, studies st, sites si " +
                	"WHERE " +
                		"si.id in (150,155) and st.id in (94,96,97,98) and p.id in (2,4) " + 
                		"and ss.site_id=si.id and ss.study_id=st.id " +
                		"and pa.participant_id=p.id and pa.study_site_id=ss.id " +
                		"and sc.assignment_id=pa.id " +
                		"and sa.scheduled_calendar_id=sc.id " +
                		"and se.scheduled_arm_id=sa.id " +
                		"and se.actual_date between '01-01-2001' and '12-31-2006' " + 
                		"and ses.id=se.scheduled_event_state_id " +
                		"and pe.id=se.planned_event_id " +
                		"and act.id=pe.activity_id " +
                		"and a.id=sa.arm_id and e.id=a.epoch_id;"
	            );
	        	log.debug("!!!!!!!!!!!!!!!!!!!!!Query generated successfully;");
	        	if(myQuery == null){
	        		log.debug("!!!!!!!!!!!!!!!!!!!!!query is null, but!!!!!!!!!!!!!!!!!");
	        	}
	        	
	            myQuery = myQuery.setResultSetMapping("reportSqlQueryMapping");
	            log.debug("!!!!!!!!!!!!!!!!!!!!!resultSetMapping set");
	            if(myQuery == null){
	            	log.debug("!!!!!!!!!!!!!!!!!!!!!failed to set mapping!!!!!!");
	            }
	            //.addEntity(edu.northwestern.bioinformatics.studycalendar.domain.ReportRow.class)
	            List<ReportRow> reportRowList = myQuery.list();
	            if(reportRowList == null){
	            	log.debug("!!!!!!!!!!!!!!!!!!!!!the report list is null");
	            }
	            return reportRowList;
			}
		});
		
	} 
}
