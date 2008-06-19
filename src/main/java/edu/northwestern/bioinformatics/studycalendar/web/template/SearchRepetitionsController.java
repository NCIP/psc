package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityLabelDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class SearchRepetitionsController extends PscAbstractController {
    private PlannedActivityLabelDao plannedActivityLabelDao;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    public SearchRepetitionsController() {
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if ("GET".equals(request.getMethod())) {
            Map<String, Object> model =  new HashMap<String, Object>();
            String arrayOfIndices = ServletRequestUtils.getRequiredStringParameter(request, "arrayOfPlannedActivityIndices");
            String labelName = ServletRequestUtils.getStringParameter(request, "labelName");
            Integer labelId = ServletRequestUtils.getIntParameter(request, "labelId");
            Integer[] arrayOfPAIds = getArrayFromPlannedActivityString(arrayOfIndices);
            List<Integer[]> listOfRepetitionsPerPA = new ArrayList<Integer[]>();

            for (Integer arrayOfPAId : arrayOfPAIds) {
                if (arrayOfPAId != null) {
                    List<Object> repetitionList = plannedActivityLabelDao.getRepetitionsByPlannedActivityIdAndLabelId(arrayOfPAId, labelId);
                    Integer[] array = repetitionList.toArray(new Integer[]{});
                    listOfRepetitionsPerPA.add(array);
                }
            }
            model.put("repetitions", listOfRepetitionsPerPA);
            
            return new ModelAndView("template/ajax/arrayOfRepetitions", model);
        } else {
            getControllerTools().sendGetOnlyError(response);
            return null;
        }
    }


    public PlannedActivityLabelDao getPlannedActivityLabelDao() {
        return plannedActivityLabelDao;
    }

    public void setPlannedActivityLabelDao(PlannedActivityLabelDao plannedActivityLabelDao) {
        this.plannedActivityLabelDao = plannedActivityLabelDao;
    }

    private Integer[] getArrayFromPlannedActivityString(String plannedActivity) {
        String[] parsedArray = plannedActivity.split(",");
        Integer[] result = new Integer[parsedArray.length];
        for (int i=0; i< parsedArray.length; i++) {
            if (parsedArray[i]!=null && parsedArray[i].length()>0) {
                Integer something = new Integer(parsedArray[i]);
                result[i] = something;
            } else {
                result[i] = null;
            }
        }
        return result;
    }

}