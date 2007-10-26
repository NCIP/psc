package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.northwestern.bioinformatics.studycalendar.domain.Named;

import java.util.Comparator;

public class NamedComparator implements Comparator<Named> {
    public int compare(Named named, Named named1) {
        return named.getName().compareTo(named1.getName());
    }
}
