/* Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.udacity.devrel.training.conference.android;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.udacity.devrel.training.conference.android.utils.ConferenceUtils;
import com.udacity.devrel.training.conference.android.utils.Utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;

import static com.udacity.devrel.training.conference.android.BuildConfig.DEBUG;

/**
 * Sample Android application for the Conference Central class for Google Cloud Endpoints.
 */
public class MainActivity extends ActionBarActivity {

    private static final String LOG_TAG = "MainActivity";

    /**
     * Activity result indicating a return from the Google account selection intent.
     */
    private static final int ACTIVITY_RESULT_FROM_ACCOUNT_SELECTION = 2222;

    private AuthorizationCheckTask mAuthTask;
    private String mEmailAccount;

    private ConferenceListFragment mConferenceListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEmailAccount = Utils.getEmailAccount(this);

        if (savedInstanceState == null) {
            mConferenceListFragment = ConferenceListFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mConferenceListFragment)
                    .commit();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAuthTask != null) {
            mAuthTask.cancel(true);
            mAuthTask = null;
        }
    }

    protected void onResume() {
        super.onResume();

        if (null != mEmailAccount) {
            performAuthCheck(mEmailAccount);
        } else {
            selectAccount();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear_account:
                new AlertDialog.Builder(MainActivity.this).setTitle(null)
                        .setMessage(getString(R.string.clear_account_message))
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Utils.saveEmailAccount(MainActivity.this, null);
                                dialog.cancel();
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .create()
                        .show();

                break;
            case R.id.action_reload:
                mConferenceListFragment.reload();
                break;
        }
        return true;
    }

    /*
     * Selects an account for talking to Google Play services. If there is more than one account on
     * the device, it allows user to choose one.
     */
    private void selectAccount() {
        Account[] accounts = Utils.getGoogleAccounts(this);
        int numOfAccount = accounts.length;
        switch (numOfAccount) {
            case 0:
                // No accounts registered, nothing to do.
                Toast.makeText(this, R.string.toast_no_google_accounts_registered,
                        Toast.LENGTH_LONG).show();
                break;
            case 1:
                mEmailAccount = accounts[0].name;
                performAuthCheck(mEmailAccount);
                break;
            default:
                // More than one Google Account is present, a chooser is necessary.
                // Invoke an {@code Intent} to allow the user to select a Google account.
                Intent accountSelector = AccountPicker.newChooseAccountIntent(null, null,
                        new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false,
                        getString(R.string.select_account_for_access), null, null, null);
                startActivityForResult(accountSelector, ACTIVITY_RESULT_FROM_ACCOUNT_SELECTION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTIVITY_RESULT_FROM_ACCOUNT_SELECTION && resultCode == RESULT_OK) {
            // This path indicates the account selection activity resulted in the user selecting a
            // Google account and clicking OK.
            mEmailAccount = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        } else {
            finish();
        }
    }

    /*
     * Schedule the authorization check.
     */
    private void performAuthCheck(String email) {
        // Cancel previously running tasks.
        if (mAuthTask != null) {
            mAuthTask.cancel(true);
        }

        // Start task to check authorization.
        mAuthTask = new AuthorizationCheckTask();
        mAuthTask.execute(email);
    }

    /**
     * Verifies OAuth2 token access for the application and Google account combination with
     * the {@code AccountManager} and the Play Services installed application. If the appropriate
     * OAuth2 access hasn't been granted (to this application) then the task may fire an
     * {@code Intent} to request that the user approve such access. If the appropriate access does
     * exist then the button that will let the user proceed to the next activity is enabled.
     */
    private class AuthorizationCheckTask extends AsyncTask<String, Integer, Boolean> {

        private final static boolean SUCCESS = true;
        private final static boolean FAILURE = false;
        private Exception mException;

        @Override
        protected Boolean doInBackground(String... emailAccounts) {
            Log.i(LOG_TAG, "Background task started.");

            if (!Utils.checkGooglePlayServicesAvailable(MainActivity.this)) {
                publishProgress(R.string.gms_not_available);
                return FAILURE;
            }

            String emailAccount = emailAccounts[0];
            // Ensure only one task is running at a time.
            mAuthTask = this;

            // Ensure an email was selected.
            if (TextUtils.isEmpty(emailAccount)) {
                publishProgress(R.string.toast_no_google_account_selected);
                return FAILURE;
            }

            mEmailAccount = emailAccount;
            Utils.saveEmailAccount(MainActivity.this, emailAccount);

            return SUCCESS;
        }

        @Override
        protected void onProgressUpdate(Integer... stringIds) {
            // Toast only the most recent.
            Integer stringId = stringIds[0];
            Toast.makeText(MainActivity.this, getString(stringId), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPreExecute() {
            mAuthTask = this;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                // Authorization check successful, get conferences.
                ConferenceUtils.build(MainActivity.this, mEmailAccount);
                getConferencesForList();
            } else {
                // Authorization check unsuccessful.
                mEmailAccount = null;
                if (mException != null) {
                    Utils.displayNetworkErrorMessage(MainActivity.this);
                }
            }
            mAuthTask = null;
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    private void getConferencesForList() {
        if (TextUtils.isEmpty(mEmailAccount)) {
            return;
        }
        mConferenceListFragment.loadConferences();
    }
}
