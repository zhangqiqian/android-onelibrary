package org.onelibrary.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.onelibrary.util.Dict;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by niko on 5/7/16.
 */
public class ProfileOptions {

    private List<Dict> grades;
    private List<Dict> majors;
    private Map<Integer, List<Dict>> curriculas;

    public ProfileOptions() {}

    public ProfileOptions(JSONObject options){
        try{
            JSONArray gradesJson = options.getJSONArray("grades");
            List<Dict> gradesList = new ArrayList<Dict>();
            int gradeLength = gradesJson.length();
            for (int i = 0; i < gradeLength; i++) {
                JSONObject grade = gradesJson.getJSONObject(i);
                int id = grade.getInt("id");
                String name = grade.getString("name");
                gradesList.add(new Dict(id, name));
            }
            this.grades = gradesList;

            JSONArray majorsJson = options.getJSONArray("majors");
            List<Dict> majorsList = new ArrayList<Dict>();
            int majorLength = majorsJson.length();
            for (int i = 0; i < majorLength; i++) {
                JSONObject major = majorsJson.getJSONObject(i);
                int id = major.getInt("id");
                String name = major.getString("name");
                majorsList.add(new Dict(id, name));
            }
            this.majors = majorsList;

            JSONArray curriculasJson = options.getJSONArray("curriculas");
            Map<Integer, List<Dict>> curriculasMap = new HashMap<Integer, List<Dict>>();
            int majorCurriculaLength = curriculasJson.length();
            for (int i = 0; i < majorCurriculaLength; i++) {
                JSONObject majorCurriculaJson = curriculasJson.getJSONObject(i);
                int major_id = majorCurriculaJson.getInt("major_id");
                List<Dict> curriculasList = new ArrayList<Dict>();
                JSONArray majorCurriculas = majorCurriculaJson.getJSONArray("curriculas");
                int curriculasLength = majorCurriculas.length();
                for (int j = 0; j < curriculasLength; j++) {
                    JSONObject curricula = majorCurriculas.getJSONObject(j);
                    int id = curricula.getInt("id");
                    String name = curricula.getString("name");
                    curriculasList.add(new Dict(id, name));
                }
                curriculasMap.put(major_id, curriculasList);
            }
            this.curriculas = curriculasMap;

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public List<Dict> getGrades() {
        return grades;
    }

    public void setGrades(List<Dict> grades) {
        this.grades = grades;
    }

    public List<Dict> getMajors() {
        return majors;
    }

    public void setMajors(List<Dict> majors) {
        this.majors = majors;
    }

    public Map<Integer, List<Dict>> getCurriculas() {
        return curriculas;
    }

    public void setCurriculas(Map<Integer, List<Dict>> curriculas) {
        this.curriculas = curriculas;
    }
}
