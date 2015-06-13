package me.skipr.skipr;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;

/**
 * Created by Navjot on 6/13/2015.
 */
public class SplashActivity extends Activity {

    @Override
    protected void onStart() {
        super.onStart();
        Branch branch = Branch.getInstance(getApplicationContext());
        branch.initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                Intent launchMainIntent = new Intent(SplashActivity.this, MainActivity.class);
                if (error == null) {
                    if(referringParams.has("group_id")){

                        try {
                            launchMainIntent.putExtra("group_id", referringParams.getString("group_id"));
                        } catch (JSONException e) {
                            //just dont add to intent
                        }
                    }
                    // params are the deep linked params associated with the link that the user clicked before showing up
                    Log.i("BranchConfigTest", "deep link data: " + referringParams.toString());
                }
                startActivity(launchMainIntent);
            }
        }, this.getIntent().getData(), this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Branch.getInstance(getApplicationContext()).closeSession();
    }
}
