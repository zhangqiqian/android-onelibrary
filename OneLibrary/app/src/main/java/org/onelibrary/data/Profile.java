package org.onelibrary.data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by niko on 5/7/16.
 */
public class Profile {

    private Integer gradeId;
    private Integer majorId;
    private Integer curriculaId;
    private String research;
    private String interest;
    private String project;
    private ProfileOptions profileOptions;

    public Profile() {}

    public Profile(Integer gradeId, Integer majorId, Integer curriculaId, String research, String interest, String project) {
        this.gradeId = gradeId;
        this.majorId = majorId;
        this.curriculaId = curriculaId;
        this.research = research;
        this.interest = interest;
        this.project = project;
    }

    public Profile(JSONObject info){
        try{
            this.gradeId = info.getInt("grade");
            this.majorId = info.getInt("major");
            this.curriculaId = info.getInt("curricula_id");
            this.research = info.getString("research");
            this.interest = info.getString("interest");
            this.project = info.getString("project");

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public Integer getGradeId() {
        return gradeId;
    }

    public void setGradeId(Integer gradeId) {
        this.gradeId = gradeId;
    }

    public Integer getMajorId() {
        return majorId;
    }

    public void setMajorId(Integer majorId) {
        this.majorId = majorId;
    }

    public Integer getCurriculaId() {
        return curriculaId;
    }

    public void setCurriculaId(Integer curriculaId) {
        this.curriculaId = curriculaId;
    }

    public String getResearch() {
        return research;
    }

    public void setResearch(String research) {
        this.research = research;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public ProfileOptions getProfileOptions() {
        return profileOptions;
    }

    public void setProfileOptions(ProfileOptions profileOptions) {
        this.profileOptions = profileOptions;
    }
}
