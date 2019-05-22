/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class MainActivity extends AppCompatActivity {

  private Switch selectionSwitch;

  public void getStarted(View view) {
      String riderOrDiver = "rider";

      if (selectionSwitch.isChecked()) {
          riderOrDiver = "driver";
      }

      ParseUser.getCurrentUser().put("riderOrDriver", riderOrDiver);
      ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
          @Override
          public void done(ParseException e) {
              if (e == null) {
                  if (!selectionSwitch.isChecked()) {
                      Intent intent = new Intent(getApplicationContext(), RiderActivity.class);
                      startActivity(intent);
                  } else {
                      Intent intent = new Intent(getApplicationContext(), RequestsList.class);
                      startActivity(intent);
                  }
              }
          }
      });
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    getSupportActionBar().hide();

    selectionSwitch = (Switch) findViewById(R.id.selectionSwitch);

    //ParseUser.getCurrentUser().logOut();

    if (ParseUser.getCurrentUser() == null) {
        ParseAnonymousUtils.logIn(new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e == null) {
                    Log.i("Anonymous Login", "successful");
                } else {
                    Log.i("Anonymous Login", "failed");
                }
            }
        });
    } else {
        if (ParseUser.getCurrentUser().get("riderOrDriver") != null) {
            if (ParseUser.getCurrentUser().get("riderOrDriver").equals("rider")) {
                Intent intent = new Intent(getApplicationContext(), RiderActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getApplicationContext(), RequestsList.class);
                startActivity(intent);
            }
        }
    }
    
    ParseAnalytics.trackAppOpenedInBackground(getIntent());
  }

}