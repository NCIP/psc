/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Named;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.NamedComparator;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author John Dzak
 */
public abstract class AbstractGridCommand<R extends Named, C extends Named> {

    protected abstract void performCheckAction(R row, C column) throws Exception ;
    protected abstract void performUncheckAction(R row, C column) throws Exception ;

    protected abstract boolean isSiteSelected(R rowElement, C columnElement);
    protected abstract boolean isSiteAccessAllowed(R rowElement, C columnElement);

    public abstract Map<R, Map<C, GridCell>> getGrid();


    protected void buildGrid(List<R> rowList, List<C> columnList) {
        Map<R,Map<C,GridCell>> grid = getGrid();

        for (R rowElement : rowList) {
            if (!grid.containsKey(rowElement)) grid.put(rowElement, new TreeMap<C, GridCell>(new NamedComparator()));
            for (C columnElement : columnList) {
                grid.get(rowElement)
                        .put(columnElement,
                                createGridCell(isSiteSelected(rowElement, columnElement),
                                        isSiteAccessAllowed(rowElement, columnElement)));
            }
        }
    }

    public void apply() throws Exception {
        Map<R,Map<C,GridCell>> grid = getGrid();
        for (R row : grid.keySet()) {
            for (C column : grid.get(row).keySet()) {
                if (grid.get(row).get(column).isSelected()) {
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

    protected static GridCell createGridCell(boolean selected, boolean accessAllowed) {
        return new GridCell(selected, accessAllowed);
    }
}
