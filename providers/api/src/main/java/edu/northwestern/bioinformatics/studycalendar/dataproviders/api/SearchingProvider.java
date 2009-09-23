package edu.northwestern.bioinformatics.studycalendar.dataproviders.api;

import edu.northwestern.bioinformatics.studycalendar.domain.Providable;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public interface SearchingProvider<D extends Providable> {
    List<D> search(String partialName);
}
