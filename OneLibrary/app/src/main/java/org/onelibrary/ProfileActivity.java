package org.onelibrary;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.onelibrary.data.Profile;
import org.onelibrary.data.ProfileOptions;
import org.onelibrary.ui.processbutton.ProgressGenerator;
import org.onelibrary.ui.processbutton.iml.ActionProcessButton;
import org.onelibrary.util.Dict;
import org.onelibrary.util.NetworkAdapter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends Activity implements ProgressGenerator.OnCompleteListener{

    private Spinner gradeSpinner;
    private Spinner majorSpinner;

    private TextView researchText;
    private EditText researchEditText;
    private EditText interestEditText;
    private TextView projectText;
    private EditText projectEditText;

    private TextView curriculaText;
    private Spinner curriculaSpinner;

    private ProgressGenerator progressGenerator;
    private ActionProcessButton saveButton;

    private String domain;

    public final static String APP_STATUS = "app_status";
    public final static String IS_PROFILE_UPDATED = "is_profile_updated";
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        domain = settings.getString("server_address", "http://115.28.223.203:8080");
        pref = getSharedPreferences(APP_STATUS, Context.MODE_MULTI_PROCESS);

        progressGenerator = new ProgressGenerator(this);

        gradeSpinner = (Spinner) findViewById(R.id.gradeSpinner);
        majorSpinner = (Spinner) findViewById(R.id.majorSpinner);
        researchEditText = (EditText) findViewById(R.id.research);
        interestEditText = (EditText) findViewById(R.id.interest);
        projectEditText = (EditText) findViewById(R.id.project);
        curriculaSpinner = (Spinner) findViewById(R.id.curriculaSpinner);

        researchText = (TextView) findViewById(R.id.researchTextView);
        projectText = (TextView) findViewById(R.id.projectTextView);
        curriculaText = (TextView) findViewById(R.id.curriculaTextView);

        Bundle params = new Bundle();
        new GetProfileTask().execute(params);

        gradeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int gradeId = ((Dict) gradeSpinner.getSelectedItem()).getId();
                if(gradeId == 1){
                    researchText.setVisibility(View.GONE);
                    researchEditText.setVisibility(View.GONE);
                    researchEditText.setAnimation(AnimationUtils.makeOutAnimation(getBaseContext(), true));

                    projectText.setVisibility(View.GONE);
                    projectEditText.setVisibility(View.GONE);
                    projectEditText.setAnimation(AnimationUtils.makeOutAnimation(getBaseContext(), true));

                    curriculaText.setVisibility(View.VISIBLE);
                    curriculaSpinner.setVisibility(View.VISIBLE);
                    curriculaSpinner.setAnimation(AnimationUtils.makeInAnimation(getBaseContext(), true));
                }
                if(gradeId == 2){
                    researchText.setVisibility(View.VISIBLE);
                    researchEditText.setVisibility(View.VISIBLE);
                    researchEditText.setAnimation(AnimationUtils.makeInAnimation(getBaseContext(), true));

                    projectText.setVisibility(View.VISIBLE);
                    projectEditText.setVisibility(View.VISIBLE);
                    projectEditText.setAnimation(AnimationUtils.makeInAnimation(getBaseContext(), true));

                    curriculaText.setVisibility(View.VISIBLE);
                    curriculaSpinner.setVisibility(View.VISIBLE);
                    curriculaSpinner.setAnimation(AnimationUtils.makeInAnimation(getBaseContext(), true));
                }
                if(gradeId == 6){
                    researchText.setVisibility(View.VISIBLE);
                    researchEditText.setVisibility(View.VISIBLE);
                    researchEditText.setAnimation(AnimationUtils.makeInAnimation(getBaseContext(), true));

                    projectText.setVisibility(View.VISIBLE);
                    projectEditText.setVisibility(View.VISIBLE);
                    projectEditText.setAnimation(AnimationUtils.makeInAnimation(getBaseContext(), true));

                    curriculaText.setVisibility(View.GONE);
                    curriculaSpinner.setVisibility(View.GONE);
                    curriculaSpinner.setAnimation(AnimationUtils.makeOutAnimation(getBaseContext(), true));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        majorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        curriculaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        saveButton = (ActionProcessButton) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButton.setMode(ActionProcessButton.Mode.ENDLESS);
                progressGenerator.start(saveButton);
                gradeSpinner.setEnabled(false);
                majorSpinner.setEnabled(false);
                researchEditText.setEnabled(false);
                interestEditText.setEnabled(false);
                projectEditText.setEnabled(false);
                curriculaSpinner.setEnabled(false);
                saveButton.setEnabled(false);
                saveButton.setText(R.string.being_save);

                Bundle params = new Bundle();
                Dict grade = (Dict) gradeSpinner.getSelectedItem();
                Dict major = (Dict) majorSpinner.getSelectedItem();
                Dict curricula = (Dict) curriculaSpinner.getSelectedItem();
                params.putString("grade", grade.getId().toString());
                params.putString("major", major.getId().toString());
                params.putString("research", researchEditText.getText().toString());
                params.putString("interest", interestEditText.getText().toString());
                params.putString("project", projectEditText.getText().toString());
                params.putString("curricula", curricula.getId().toString());
                new UpdateProfileTask().execute(params);
            }
        });
    }

    @Override
    public void onComplete() {
        gradeSpinner.setEnabled(true);
        majorSpinner.setEnabled(true);
        researchEditText.setEnabled(true);
        interestEditText.setEnabled(true);
        projectEditText.setEnabled(true);
        curriculaSpinner.setEnabled(true);
        saveButton.setEnabled(true);
        saveButton.setText(R.string.button_save);
    }

    /**
     * Implementation of AsyncTask, to fetch the data in the background away from
     * the UI thread.
     */
    private class GetProfileTask extends AsyncTask<Bundle, Void, Profile> {

        @Override
        protected Profile doInBackground(Bundle...params) {
            Profile userProfile = null;
            try {
                String profileUrl = domain + getString(R.string.get_profile_url);
                String OptionsUrl = domain + getString(R.string.get_profile_options_url);
                NetworkAdapter adapter = new NetworkAdapter(getBaseContext());

                JSONObject profileResult = adapter.request(profileUrl, params[0]);

                if(profileResult != null && profileResult.getInt("errno") == 0) {
                    Log.d("ProfileActivity", "profile result: " + profileResult.toString());
                    JSONObject profileJson = profileResult.getJSONObject("result");
                    userProfile = new Profile(profileJson);
                }

                if(userProfile != null){
                    JSONObject optionsResult = adapter.request(OptionsUrl, params[0]);
                    ProfileOptions profileOptions = null;
                    if(optionsResult != null && optionsResult.getInt("errno") == 0) {
                        Log.d("ProfileActivity", "profile options result: " + optionsResult.toString());
                        JSONObject options = optionsResult.getJSONObject("result");
                        profileOptions = new ProfileOptions(options);
                    }
                    userProfile.setProfileOptions(profileOptions);
                }
                return userProfile;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return userProfile;
        }

        @Override
        protected void onPostExecute(Profile userProfile) {
            List<Dict> grades = userProfile.getProfileOptions().getGrades();
            ArrayAdapter<Dict> gradeAdapter = new ArrayAdapter<Dict>(getBaseContext(),
                    android.R.layout.simple_spinner_item, grades);
            gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            gradeSpinner.setAdapter(gradeAdapter);

            int gradePosition = 0;
            for (int i = 0; i < grades.size(); i++) {
                if(grades.get(i).getId().equals(userProfile.getGradeId())){
                    gradePosition = i;
                }
            }
            gradeSpinner.setSelection(gradePosition, true);

            List<Dict> majors = userProfile.getProfileOptions().getMajors();
            ArrayAdapter<Dict> majorAdapter = new ArrayAdapter<Dict>(getBaseContext(),
                    android.R.layout.simple_spinner_item, majors);
            majorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            majorSpinner.setAdapter(majorAdapter);

            int majorPosition = 0;
            for (int i = 0; i < majors.size(); i++) {
                if(majors.get(i).getId().equals(userProfile.getMajorId())){
                    majorPosition = i;
                }
            }
            majorSpinner.setSelection(majorPosition, true);

            researchEditText.setText(userProfile.getResearch());
            interestEditText.setText(userProfile.getInterest());
            projectEditText.setText(userProfile.getProject());

            Map<Integer, List<Dict>> majorCurriculasMap = userProfile.getProfileOptions().getCurriculas();

            List<Dict> curriculas = majorCurriculasMap.get(userProfile.getMajorId());
            ArrayAdapter<Dict> curriculasAdapter = new ArrayAdapter<Dict>(getBaseContext(),
                    android.R.layout.simple_spinner_item, curriculas);
            curriculasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            curriculaSpinner.setAdapter(curriculasAdapter);

            int curriculaPosition = 0;
            for (int i = 0; i < curriculas.size(); i++) {
                if(curriculas.get(i).getId().equals(userProfile.getCurriculaId())){
                    curriculaPosition = i;
                }
            }
            curriculaSpinner.setSelection(curriculaPosition, true);

        }
    }

    private class UpdateProfileTask extends AsyncTask<Bundle, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Bundle...params) {
            JSONObject result = null;
            Log.d("ProfileActivity", "UpdateProfileTask params: " + params[0].toString());
            try {
                NetworkAdapter adapter = new NetworkAdapter(getBaseContext());
                result = adapter.request(domain + getString(R.string.update_profile_url), params[0]);
                try{
                    Thread.sleep(2000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            Log.d("ProfileActivity", "update profile result: " + result);
            try {
                if(result.getInt("errno") == 0){
                    pref.edit().putBoolean(IS_PROFILE_UPDATED, true).apply();
                    setResult(RESULT_OK);
                    finish();
                }else{
                    Toast.makeText(ProfileActivity.this, result.getString("errmsg"), Toast.LENGTH_LONG).show();
                }
                saveButton.setText(R.string.button_save);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

}
