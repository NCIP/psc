package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Named;
import edu.northwestern.bioinformatics.studycalendar.utils.NamedComparator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class AbstractSiteCoordinatorDashboardCommand<R extends Named, C extends Named> {
    //private Map<R, Map<C, GridCell>> studyAssignmentGrid;
    private List<Study> assignableStudies;
    private List<Site> assignableSites;
    private List<User> assignableUsers;

    protected AbstractSiteCoordinatorDashboardCommand(List<Study> assignableStudies, List<Site> assignableSites, List<User> assignableUsers) {
        this.assignableStudies = assignableStudies;
        this.assignableSites = assignableSites;
        this.assignableUsers = assignableUsers;
    }

    protected abstract void performCheckAction(R row, C column) throws Exception ;
    protected abstract void performUncheckAction(R row, C column) throws Exception ;

    protected abstract boolean isSiteSelected(R rowElement, C columnElement);
    protected abstract boolean isSiteAccessAllowed(R rowElement, C columnElement);
    public abstract Map<R,Map<C, GridCell>> getStudyAssignmentGrid();


    protected void buildStudyAssignmentGrid(List<R> rowList, List<C> columnList) {
        Map<R,Map<C,GridCell>> studyAssignmentGrid = getStudyAssignmentGrid();

        for (R rowElement : rowList) {
            if (!studyAssignmentGrid.containsKey(rowElement)) studyAssignmentGrid.put(rowElement, new TreeMap<C, GridCell>(new NamedComparator()));
            for (C columnElement : columnList) {
                studyAssignmentGrid.get(rowElement)
                        .put(columnElement,
                                createStudyAssignmentCell(isSiteSelected(rowElement, columnElement),
                                        isSiteAccessAllowed(rowElement, columnElement)));
            }
        }
    }

    public void apply() throws Exception {
        Map<R,Map<C,GridCell>> studyAssignmentGrid = getStudyAssignmentGrid();
        for (R row : studyAssignmentGrid.keySet()) {
            for (C column : studyAssignmentGrid.get(row).keySet()) {
                if (studyAssignmentGrid.get(row).get(column).isSelected()) {
                    performCheckAction(row, column);
                } else {
                    performUncheckAction(row, column);
                }
            }
        }
    }

    public static class GridCell {
        private boolean selected;
        private boolean siteAccessAllowed;

        public GridCell(boolean selected, boolean siteAccessAllowed) {
            this.selected = selected;
            this.siteAccessAllowed = siteAccessAllowed;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isSiteAccessAllowed() {
            return siteAccessAllowed;
        }

        public void setSiteAccessAllowed(boolean siteAccessAllowed) {
            this.siteAccessAllowed = siteAccessAllowed;
        }
    }

    protected static GridCell createStudyAssignmentCell(boolean selected, boolean siteAccessAllowed) {
        return new GridCell(selected, siteAccessAllowed);
    }

//    public Map<R, Map<C, GridCell>> getStudyAssignmentGrid() {
//        return studyAssignmentGrid;
//    }


    public List<Study> getAssignableStudies() {
        return assignableStudies;
    }

    public List<Site> getAssignableSites() {
        return assignableSites;
    }

    public List<User> getAssignableUsers() {
        return assignableUsers;
    }
}
