/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.DeletableDomainObjectDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;

import java.util.HashSet;
import java.util.Set;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class TemplateServiceTest extends StudyCalendarTestCase {
    private TemplateService service;

    private DaoFinder daoFinder;
    private DeltaDao deltaDao;

    private DeletableDomainObjectDao domainObjectDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        deltaDao = registerDaoMockFor(DeltaDao.class);
        daoFinder = registerMockFor(DaoFinder.class);
        domainObjectDao = registerMockFor(DeletableDomainObjectDao.class);

        service = new TemplateService();
        service.setDeltaDao(deltaDao);
        service.setDaoFinder(daoFinder);
    }

    public void testFindParentWhenImmediatelyAvailable() throws Exception {
        Study study = createBasicTemplate();
        assertSame(study.getPlannedCalendar(),
            service.findParent(study.getPlannedCalendar().getEpochs().get(1)));
        assertSame(study.getPlannedCalendar().getEpochs().get(1),
            service.findParent(study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0)));
    }

    public void testFindParentWhenNotImmediatelyAvailable() throws Exception {
        Study study = createBasicTemplate();
        Epoch e1 = study.getPlannedCalendar().getEpochs().get(1);
        StudySegment e1a0 = e1.getStudySegments().get(0);
        e1a0.setParent(null);
        e1.getStudySegments().remove(e1a0);

        expect(deltaDao.findDeltaWhereAdded(e1a0)).andReturn(Delta.createDeltaFor(e1));
        replayMocks();

        assertSame(e1, service.findParent(e1a0));
        verifyMocks();
    }

    public void testFindParentWhenAddedAsChildOfAnotherAddAndThenRemovedAlone() throws Exception {
        // simulates this process:
        //   Period P0 with child P0E0 added to E1A1 --> Results in Add(P0) in amendment R0
        //   P0E0 removed in amendment R1 --> Results in  Remove(P0E0) and P0E0.period=>null

        Study study = createBasicTemplate();
        Period p = createPeriod("P0", 3, 17, 1);
        PlannedActivity p0e0 = createPlannedActivity("P0E0", 4);
        study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(1).addPeriod(p);

        expect(deltaDao.findDeltaWhereAdded(p0e0)).andReturn(null);
        expect(deltaDao.findDeltaWhereRemoved(p0e0)).andReturn(Delta.createDeltaFor(p, Remove.create(p0e0)));
        replayMocks();

        assertSame(p, service.findParent(p0e0));
        verifyMocks();
    }

    @SuppressWarnings({ "unchecked" })
    public void testDelete() throws Exception {
        PlannedActivity pa = createPlannedActivity("P0E0", 4);
        PlannedActivityLabel pal = createPlannedActivityLabel("label");
        pa.addPlannedActivityLabel(pal);

        expect(daoFinder.findDao(PlannedActivity.class)).andReturn(domainObjectDao);
        expect(daoFinder.findDao(PlannedActivityLabel.class)).andReturn(domainObjectDao);
        domainObjectDao.delete(pal);
        domainObjectDao.delete(pa);

        replayMocks();
        service.delete(pa);
        verifyMocks();
    }

    public void testFindAncestorWhenPossible() throws Exception {
        Study study = createBasicTemplate();
        Epoch e1 = study.getPlannedCalendar().getEpochs().get(1);
        StudySegment e1a0 = e1.getStudySegments().get(0);

        assertEquals(e1, service.findAncestor(e1a0, Epoch.class));
        assertEquals(study.getPlannedCalendar(), service.findAncestor(e1a0, PlannedCalendar.class));
        assertEquals(study.getPlannedCalendar(), service.findAncestor(e1, PlannedCalendar.class));
    }

    public void testFindAncestorWhenDynamicSubclass() throws Exception {
        Study study = createBasicTemplate();
        Epoch dynamic = new Epoch() { };
        study.getPlannedCalendar().addEpoch(dynamic);

        assertSame(study.getPlannedCalendar(), service.findAncestor(dynamic, PlannedCalendar.class));
    }

    public void testFindAncestorWhenNotPossible() throws Exception {
        Study study = createBasicTemplate();
        Epoch e1 = study.getPlannedCalendar().getEpochs().get(1);

        try {
            service.findAncestor(e1, Period.class);
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException expected) {
            assertEquals("Epoch is not a descendant of Period", expected.getMessage());
        }
    }

    public void testFindStudyForPlannedCalendar() throws Exception {
        Study study = createBasicTemplate();
        assertSame(study, service.findStudy(study.getPlannedCalendar()));
    }

    public void testFindStudyForOtherNodes() throws Exception {
        Study study = createBasicTemplate();
        Epoch epoch = study.getPlannedCalendar().getEpochs().get(0);
        assertSame(study, service.findStudy(epoch));
        assertSame(study, service.findStudy(epoch.getStudySegments().get(0)));
    }

    public void testFindEquivalentChildForStudyNode() throws Exception {
        Study study = createBasicTemplate();
        assignIds(study);
        assertSame(study, service.findEquivalentChild(study, study));
    }

    public void testFindEquivalentChildForPopulationNode() throws Exception {
        Study study = createBasicTemplate();
        assignIds(study);
        Population population = new Population();
        population.setName("population name");
        population.setAbbreviation("population abbreviation");
        Set<Population> populations = new HashSet<Population>();
        populations.add(population);
        study.setPopulations(populations);
        assertSame(population, service.findEquivalentChild(study, population));
    }

    public void testFindEquivalentChildWhenActualChild() throws Exception {
        Study study = createBasicTemplate();
        assignIds(study);
        Epoch e = study.getPlannedCalendar().getEpochs().get(1);
        assertSame(e, service.findEquivalentChild(study, e));
    }

    public void testFindEquivalentChildByIdAndType() throws Exception {
        int sameId = 50;
        PlannedActivity expectedNode = setId(sameId, createPlannedActivity("PA0", 3));

        Study study = createBasicTemplate();
        assignIds(study);
        Period p0 = setId(sameId, createPeriod("P0", 1, 14, 4));
        p0.addPlannedActivity(setId(49, createPlannedActivity("PA1", 1)));
        p0.addPlannedActivity(expectedNode);
        study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0).addPeriod(p0);

        PlannedActivity parameter = setId(sameId, new PlannedActivity());
        // set one of each node to the same id to ensure that type checking is happening
        study.getPlannedCalendar().setId(sameId);
        study.getPlannedCalendar().getEpochs().get(0).setId(sameId);
        study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0).setId(sameId);

        assertSame(expectedNode, service.findEquivalentChild(study, parameter));
    }

    public void testFindEquivalentChildByGridIdAndType() throws Exception {
        String sameGridId = "gridId50";
        PlannedActivity expectedNode = setGridId(sameGridId, createPlannedActivity("PA0", 3));

        Study study = createBasicTemplate();
        assignIds(study);
        Period p0 = setGridId(sameGridId, createPeriod("P0", 1, 14, 4));
        p0.addPlannedActivity(setGridId("gridId49", createPlannedActivity("PA1", 1)));
        p0.addPlannedActivity(expectedNode);
        study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0).addPeriod(p0);

        PlannedActivity parameter = setGridId(sameGridId, new PlannedActivity());
        // set one of each node to the same id to ensure that type checking is happening
        study.getPlannedCalendar().setGridId(sameGridId);
        study.getPlannedCalendar().getEpochs().get(0).setGridId(sameGridId);
        study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0).setGridId(sameGridId);

        assertSame(expectedNode, service.findEquivalentChild(study, parameter));
    }
}
