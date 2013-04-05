/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.NamedComparator;

import static java.util.Arrays.asList;
import java.util.*;

/**
 * @author John Dzak
 */
public class AbstractGridCommandTest extends StudyCalendarTestCase {
    private List<Site> sites;
    private List<Study> studies;
    private AbstractGridCommandStub command;
    private Study study0, study1;
    private Site site0, site1;
    Map<Study, Map<Site, AbstractGridCommand.GridCell>> testGrid;

    protected void setUp() throws Exception {
        super.setUp();

        command = new AbstractGridCommandStub();

        study0  = createNamedInstance("Study B", Study.class);
        study1  = createNamedInstance("Study A", Study.class);
        studies = asList(study0, study1);

        site0   = createNamedInstance("Site B", Site.class);
        site1   = createNamedInstance("Site A", Site.class);
        sites   = asList(site0, site1);

        /////////   Test Grid: (Study0,Site0=Checked) (Study0,Site1=Unchecked) (Study1,Site0=Unchecked) (Study1,Site1=Checked)
        testGrid = new TreeMap<Study, Map<Site, AbstractGridCommand.GridCell>>(new NamedComparator());

        Map<Site, AbstractGridCommand.GridCell> siteMap0 = new TreeMap<Site, AbstractGridCommand.GridCell>(new NamedComparator());
        siteMap0.put(site0, AbstractGridCommand.createGridCell(true, true));
        siteMap0.put(site1, AbstractGridCommand.createGridCell(false, true));

        Map<Site, AbstractGridCommand.GridCell> siteMap1 = new TreeMap<Site, AbstractGridCommand.GridCell>(new NamedComparator());
        siteMap1.put(site0, AbstractGridCommand.createGridCell(false, true));
        siteMap1.put(site1, AbstractGridCommand.createGridCell(true, true));

        testGrid.put(study0, siteMap0);
        testGrid.put(study1, siteMap1);
    }

    public void testBuildGridAlphabetically() {
        command.buildGrid(studies, sites);

        assertEquals("Wrong Study Size", studies.size(), command.getGrid().keySet().size());

        assertEquals("Wrong Site Size", sites.size(), command.getGrid().get(study0).keySet().size());
        assertEquals("Wrong Site Size", sites.size(), command.getGrid().get(study1).keySet().size());

        Iterator<Study> actualStudyIter = command.getGrid().keySet().iterator();
        assertEquals("Wrong Study", study1.getName(), actualStudyIter.next().getName());
        assertEquals("Wrong Study", study0.getName(), actualStudyIter.next().getName());


        Iterator<Site> actualStudy0SiteIter = command.getGrid().get(study0).keySet().iterator();
        assertEquals("Wrong Site", site1.getName(), actualStudy0SiteIter.next().getName());
        assertEquals("Wrong Site", site0.getName(), actualStudy0SiteIter.next().getName());


        Iterator<Site> actualStudy1SiteIter = command.getGrid().get(study1).keySet().iterator();
        assertEquals("Wrong Site", site1.getName(), actualStudy1SiteIter.next().getName());
        assertEquals("Wrong Site", site0.getName(), actualStudy1SiteIter.next().getName());
    }

    public void testApply() throws Exception {
        AbstractGridCommandStub command = registerMockFor(AbstractGridCommandStub.class,
                AbstractGridCommandStub.class.getMethod("performCheckAction", Study.class, Site.class),
                AbstractGridCommandStub.class.getMethod("performUncheckAction", Study.class, Site.class));

        command.setGrid(testGrid);

        command.performCheckAction(study0, site0);
        command.performCheckAction(study1, site1);
        command.performUncheckAction(study0, site1);
        command.performUncheckAction(study1, site0);
        replayMocks();

        command.apply();
        verifyMocks();
    }

    private class AbstractGridCommandStub extends AbstractGridCommand<Study, Site>    {
        Map<Study, Map<Site, GridCell>> grid = new TreeMap<Study, Map<Site, GridCell>>(new NamedComparator());
        
        public void performCheckAction(Study row, Site column) throws Exception { }
        public void performUncheckAction(Study row, Site column) throws Exception { }

        protected boolean isSiteSelected(Study rowElement, Site columnElement) {
            return true;
        }

        protected boolean isSiteAccessAllowed(Study rowElement, Site columnElement) {
            return true;
        }

        public Map<Study, Map<Site, GridCell>> getGrid() {
            return grid;
        }

        public void setGrid(Map<Study, Map<Site, GridCell>> grid) {
            this.grid = grid;
        }
    }
}
