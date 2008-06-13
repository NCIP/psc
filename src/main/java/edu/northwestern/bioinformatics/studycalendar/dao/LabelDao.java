package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Label;

import java.util.List;


public class LabelDao extends StudyCalendarMutableDomainObjectDao<Label> implements DeletableDomainObjectDao<Label> {
    @Override
    public Class<Label> domainClass() {
        return Label.class;
    }

   /**
    * Returns a list of all the label currently available.
    *
    * @return      list of all the Labels currently available
    */
    public List<Label> getAll() {
        List<Label> sortedList;
        sortedList = getHibernateTemplate().find("from Label");
        return sortedList;
    }

    /**
    * Finds the label by label name.
    *
    * @param  name the name of the label we want to find
    * @return      the label found that corresponds to the name parameter
    */
    public Label getByName(String name) {
        List<Label> labels = getHibernateTemplate().find("from Label where name = ?", name);
        if (labels.size() == 0) {
            return null;
        }
        return labels.get(0);
    }

     /**
    * Finds the label doing a LIKE search with some search text for label name.
    *
    * @param  searchText the text we are searching with
    * @return      a list of labels found based on the search text
    */
    public List<Label> getLablesBySearchText(String searchText) {
        String search = "%" + searchText.toLowerCase() +"%";
        List<Label> labels = getHibernateTemplate().find("from Label where lower(name) LIKE ?", search);
        return labels;
    }

    /**
    * Deletes a label
    *
    * @param  label to delete
    */
    public void delete(Label label) {
        getHibernateTemplate().delete(label);
    }
}