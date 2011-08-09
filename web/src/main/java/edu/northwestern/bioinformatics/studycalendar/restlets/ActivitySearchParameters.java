package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.nwu.bioinformatics.commons.StringUtils;
import org.restlet.Request;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST;

public class ActivitySearchParameters implements Pagination, Sortable {
    Request request;
    private Long totalActivities;
    private ActivityTypeDao activityTypeDao;

    public ActivitySearchParameters(Request request, Long totalActivities, ActivityTypeDao activityTypeDao) {
        this.request = request;
        this.totalActivities = totalActivities;
        this.activityTypeDao = activityTypeDao;
    }

    public String getQ() {
        return QueryParameters.Q.extractFrom(request);
    }

    public String getTypeName() {
        return QueryParameters.TYPE.extractFrom(request);
    }

    public String getTypeId() {
        return QueryParameters.TYPE_ID.extractFrom(request);
    }

    public ActivityType getType() {
        ActivityType type = null;
        if (getTypeName() != null) {
            type = activityTypeDao.getByName(getTypeName());
            if (type == null) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown activity type: " + getTypeName());
            }
        } else if (getTypeId() != null) {
            try {
                type = activityTypeDao.getById(Integer.parseInt(getTypeId()));
            } catch (NumberFormatException nfe) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "type-id must be an integer");
            }
        }
        return type;
    }

    public Integer getLimit() {
        String raw = QueryParameters.LIMIT.extractFrom(request);
        if (raw == null) return null;
        try {
            Integer limit = new Integer(raw);
            if (limit < 1) {
                throw new ResourceException(
                    CLIENT_ERROR_BAD_REQUEST, "Limit must be a positive integer.");
            }
            return limit;
        } catch (NumberFormatException nfe) {
            throw new ResourceException(
                CLIENT_ERROR_BAD_REQUEST, "Limit must be a positive integer.");
        }
    }

    public Integer getOffset() {
        String offsetS = QueryParameters.OFFSET.extractFrom(request);
        if (offsetS == null) return 0;
        try {
            Integer offset = new Integer(offsetS);
            if (offset < 0) {
                throw new ResourceException(
                    CLIENT_ERROR_BAD_REQUEST, "Offset must be a nonnegative integer.");
            }
            if (offset >= getTotal() && offset > 0) {
                throw new ResourceException(CLIENT_ERROR_BAD_REQUEST, String.format(
                    "Offset %d is too large.  There are %d result(s), so the max offset is %d.",
                    offset, getTotal(), Math.max(0, getTotal() - 1)));
            }
            return offset;
        } catch (NumberFormatException nfe) {
            throw new ResourceException(
                CLIENT_ERROR_BAD_REQUEST, "Offset must be a nonnegative integer.");
        }
    }

    public String getSort() {
        String sort = QueryParameters.SORT.extractFrom(request);
        if (sort == null) return null;
        Collection valid = asList("activity_name", "activity_type");
        if (valid.contains(sort)) {
            return sort;
        } else {
            throw new ResourceException(
                CLIENT_ERROR_BAD_REQUEST, "Sort must be " + StringUtils.join(valid, " or ") + ".");
        }    }

    public String getOrder() {
        String order = QueryParameters.ORDER.extractFrom(request);
        if (order == null) return null;
        Collection valid = asList("asc", "desc");
        if (valid.contains(order)) {
            return order;
        } else {
            throw new ResourceException(
                    CLIENT_ERROR_BAD_REQUEST, "Order must be " + StringUtils.join(valid, " or ") + ".");
        }
    }

    public Long getTotal() {
        return totalActivities;
    }

    public boolean isAllBlank() {
        return getQ() == null && getTypeId() == null && getTypeName() == null && getLimit() == null && getOffset() == null && getSort() == null && getOrder() == null;
    }
}
