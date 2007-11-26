/**
 *
 */
package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.LoadStatus;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreatorImpl;
import gov.nih.nci.ccts.grid.ArmType;
import gov.nih.nci.ccts.grid.EpochType;
import gov.nih.nci.ccts.grid.IdentifierType;
import gov.nih.nci.ccts.grid.NonTreatmentEpochType;
import gov.nih.nci.ccts.grid.OrganizationAssignedIdentifierType;
import gov.nih.nci.ccts.grid.StudyOrganizationType;
import gov.nih.nci.ccts.grid.StudySiteType;
import gov.nih.nci.ccts.grid.TreatmentEpochType;
import gov.nih.nci.ccts.grid.common.StudyConsumerI;
import gov.nih.nci.ccts.grid.stubs.types.InvalidStudyException;
import gov.nih.nci.ccts.grid.stubs.types.StudyCreationException;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Assumptions: 1. Site should already be existing in DB..
 * 
 * @author <a href="mailto:saurabh.agrawal@semanticbits.com>Saurabh Agrawal</a>
 */
public class PSCStudyConsumer implements StudyConsumerI {

	private static final Log logger = LogFactory.getLog(PSCStudyConsumer.class);

	public static final String SERVICE_BEAN_NAME = "scheduledCalendarService";

	private static final String COORDINATING_CENTER_IDENTIFIER_TYPE = "Coordinating Center Identifier";

	private ApplicationContext ctx;

	private SiteDao siteDao;

	private StudyService studyService;

	private StudyDao studyDao;

	public PSCStudyConsumer() {
		ctx = new ClassPathXmlApplicationContext(new String[] {
				// "classpath:applicationContext.xml",
				"classpath:applicationContext-api.xml", "classpath:applicationContext-command.xml",
				"classpath:applicationContext-dao.xml", "classpath:applicationContext-db.xml",
				"classpath:applicationContext-security.xml", "classpath:applicationContext-service.xml",
				"classpath:applicationContext-spring.xml" });
		siteDao = getSiteDao();
		studyService = getStudyService();
		studyDao = getStudyDao();
	}

	public void createStudy(final gov.nih.nci.ccts.grid.Study studyDto) throws RemoteException, InvalidStudyException,
			StudyCreationException {
		if (studyDto == null) {
			throw new InvalidStudyException();
		}

		String ccIdentifier = findCoordinatingCenterIdentifier(studyDto);

		if (fetchStudy(ccIdentifier) != null) {
			logger.info("Already a study with the same Coordinating Center Identifier (" + ccIdentifier
					+ ") exists.Returning without processing the request.");
			return;
		}

		Study study = TemplateSkeletonCreatorImpl.createBase(studyDto.getShortTitleText());
		study.setAssignedIdentifier(ccIdentifier);
		study.setGridId(studyDto.getGridId());
		study.setLoadStatus(LoadStatus.INPROGRESS);

		StudyOrganizationType[] studyOrganizationTypes = studyDto.getStudyOrganization();
		populateStudySite(study, studyOrganizationTypes);

		// now add epochs and arms to the planned calendar of study
		populateEpochsAndArms(studyDto, study);

		studyService.save(study);
		logger.info("Created the study :" + study.getId());

	}

	public void commit(final gov.nih.nci.ccts.grid.Study studyDto) throws RemoteException, InvalidStudyException {
		if (studyDto == null) {
			throw new InvalidStudyException();
		}
		logger.info("commit called for study:long titlte-" + studyDto.getLongTitleText());
		String ccIdentifier = findCoordinatingCenterIdentifier(studyDto);

		try {
			studyDao.commitInProgressStudy(ccIdentifier);
		}

		catch (Exception exp) {
			logger.error("Exception while trying to commit the study", exp);
			InvalidStudyException e = new InvalidStudyException();
			e.setFaultReason("Exception while comitting study," + exp.getMessage());
			e.setFaultString("Exception while comitting study," + exp.getMessage());
		}
	}

	public void rollback(final gov.nih.nci.ccts.grid.Study studyDto) throws RemoteException, InvalidStudyException {
		if (studyDto == null) {
			throw new InvalidStudyException();
		}
		logger.info("rollback called for study:long titlte-" + studyDto.getLongTitleText());
		String ccIdentifier = findCoordinatingCenterIdentifier(studyDto);

		try {
			studyDao.deleteInprogressStudy(ccIdentifier);
		}
		catch (Exception exp) {
			logger.error("Exception while trying to rollback the study", exp);
			InvalidStudyException e = new InvalidStudyException();
			e.setFaultReason("Exception while rollback study," + exp.getMessage());
			e.setFaultString("Exception while rollback study," + exp.getMessage());
		}
	}

	private void populateEpochsAndArms(final gov.nih.nci.ccts.grid.Study studyDto, final Study study) {
		EpochType[] epochTypes = studyDto.getEpoch();

		for (int i = 0; i < epochTypes.length; i++) {
			EpochType epochType = epochTypes[i];
			if (epochType instanceof NonTreatmentEpochType) {
				TemplateSkeletonCreatorImpl.addEpoch(study, i, Epoch.create(epochType.getName()));
			}
			else if (epochType instanceof TreatmentEpochType) {
				TemplateSkeletonCreatorImpl.addEpoch(study, i,
						createEpochForTreatmentEpochType((TreatmentEpochType) epochType));

			}

		}

	}

	private Epoch createEpochForTreatmentEpochType(final TreatmentEpochType treatmentEpochType) {
		Epoch epoch = null;

		ArmType[] armTypes = treatmentEpochType.getArm();
		List<String> armNames = new ArrayList<String>();
		for (ArmType armType : armTypes) {
			armNames.add(armType.getName());
		}
		epoch = Epoch.create(treatmentEpochType.getName(), armNames.toArray(new String[0]));
		return epoch;
	}

	/**
	 * Populates study siste and returns it.
	 * 
	 * @param study
	 * @param studyOrganizationTypes
	 * @throws InvalidStudyException
	 */
	private void populateStudySite(final Study study, final StudyOrganizationType[] studyOrganizationTypes)
			throws StudyCreationException, InvalidStudyException {

		if (ArrayUtils.isEmpty(studyOrganizationTypes)) {
			logger.error("No site is associated to this study (gridId :" + study.getGridId() + ")");
			StudyCreationException exp = new StudyCreationException();
			exp.setFaultString("No site is associated to this study");
			exp.setFaultReason("No site is associated to this study (gridId :" + study.getGridId() + ")");
			throw exp;
		}

		List<StudySite> studySites = new ArrayList<StudySite>();
		for (StudyOrganizationType studyOrganizationType : studyOrganizationTypes) {
			StudySite studySite = null;
			if (studyOrganizationType instanceof StudySiteType) {
				studySite = new StudySite();
				studySite.setSite(fetchSite(studyOrganizationType.getHealthcareSite(0).getNciInstituteCode()));
				studySite.setStudy(study);
				studySite.setGridId(studyOrganizationType.getGridId());
			}
			studySites.add(studySite);
		}
		if (studySites.size() == 0) {
			logger.error("No sites are associated to this study (gridId :" + study.getGridId() + ")");
			StudyCreationException exp = new StudyCreationException();
			exp.setFaultString("No sites is associated to this study");
			exp.setFaultReason("No sites is associated to this study (gridId :" + study.getGridId() + ")");
			throw exp;
		}
		study.setStudySites(studySites);
	}

	/**
	 * Fetches the site from the DB
	 * 
	 * @param assignedIdentifier
	 * @return
	 */
	private Site fetchSite(final String assignedIdentifier) {

		Site site = siteDao.getByAssignedIdentifier(assignedIdentifier);
		if (site == null) {
			logger.error("No site exists  assignedIdentifier :" + assignedIdentifier);
			// throw new StudyCreationException("No organization exists with nciCode :" + assignedIdentifier);
		}

		return site;
	}

	/**
	 * This method will return the identifier specified by Coordinating center to this study.
	 * 
	 * @param studyDto
	 * @return
	 * @throws InvalidStudyException
	 */
	String findCoordinatingCenterIdentifier(final gov.nih.nci.ccts.grid.Study studyDto) throws InvalidStudyException {
		String ccIdentifier = null;
		for (IdentifierType idType : studyDto.getIdentifier()) {
			// FIXME:Saurabh check for type/value
			if (idType instanceof OrganizationAssignedIdentifierType
					&& StringUtils.equals(idType.getType(), COORDINATING_CENTER_IDENTIFIER_TYPE)) {
				ccIdentifier = idType.getValue();
				break;
			}
		}

		if (ccIdentifier == null) {
			logger.error("no cc identifier for study:long titlte-" + studyDto.getLongTitleText());
			InvalidStudyException exp = new InvalidStudyException();
			exp.setFaultReason("In Study/Identifiers, Coordinating Center Identifier is not available");
			exp.setFaultString("In Study/Identifiers, Coordinating Center Identifier is not available");
			throw exp;
		}
		return ccIdentifier;

	}

	public void rollback(final Study study) throws RemoteException, InvalidStudyException {
		// To change body of implemented methods use File | Settings | File Templates.
	}

	private Study fetchStudy(final String ccIdentifier) {

		Study study = studyService.getStudyNyAssignedIdentifier(ccIdentifier);

		return study;
	}

	public SiteDao getSiteDao() {
		return (SiteDao) ctx.getBean("siteDao");
	}

	public StudyService getStudyService() {
		return (StudyService) ctx.getBean("studyService");
	}

	public StudyDao getStudyDao() {
		return (StudyDao) ctx.getBean("studyDao");
	}

	@Required
	public void setStudyDao(final StudyDao studyDao) {
		this.studyDao = studyDao;
	}
}
