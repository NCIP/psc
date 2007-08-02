package edu.northwestern.bioinformatics.studycalendar.web;


/**
 * @author Jaron Sampson
 */

public class MarkCompleteCommand {

    private String completed;
    private Integer studyId;
    
    public MarkCompleteCommand(){
    }

    public String getCompleted() {
        return completed;
    }
    
    public void setCompleted(String completed) {
        this.completed = completed;
    }    

    public Integer getStudyId() {
        return studyId;
    }
    
    public void setStudyId(Integer studyId) {
        this.studyId = studyId;
    }   
}
