/*
 * Copyright (C) 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.udacity.devrel.training.conference.android;

import com.udacity.devrel.training.conference.android.utils.ConferenceException;
import com.udacity.devrel.training.conference.android.utils.ConferenceUtils;
import com.udacity.devrel.training.conference.android.utils.DecoratedConference;
import com.udacity.devrel.training.conference.android.utils.Utils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

public class ConferenceListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<List<DecoratedConference>> {

    private static final String TAG = "ConferenceListFragment";

    private ConferenceDataAdapter mAdapter;

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setFastScrollEnabled(true);
        LayoutAnimationController controller = AnimationUtils
                .loadLayoutAnimation(getActivity(), R.anim.list_layout_controller);
        getListView().setLayoutAnimation(controller);
        mAdapter = new ConferenceDataAdapter(getActivity());
        setEmptyText(getString(R.string.no_conferences));
        setListAdapter(mAdapter);
        setListShown(false);
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android
     * .support.v4.content.Loader, java.lang.Object)
     */
    @Override
    public void onLoadFinished(Loader<List<DecoratedConference>> loader,
            List<DecoratedConference> data) {
        ConferenceLoader conferenceLoader = (ConferenceLoader) loader;
        if (conferenceLoader.getException() != null) {
            Utils.displayNetworkErrorMessage(getActivity());
            return;
        }
        mAdapter.setData(data);
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android
     * .support.v4.content.Loader)
     */
    @Override
    public void onLoaderReset(Loader<List<DecoratedConference>> arg0) {
        mAdapter.setData(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final DecoratedConference decoratedConference = mAdapter.getItem(position);

        final String message = Utils
                .getConferenceCard(getActivity(), decoratedConference.getConference());

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater li = LayoutInflater.from(getActivity());
        View conferenceDetailsView = li.inflate(R.layout.conference_details, null);
        alertDialogBuilder.setView(conferenceDetailsView);

        final TextView textViewTitle = (TextView) conferenceDetailsView
                .findViewById(R.id.textViewTitle);
        final TextView textViewMessage = (TextView) conferenceDetailsView
                .findViewById(R.id.textViewMessage);
        textViewTitle.setText(decoratedConference.getConference().getName());
        textViewMessage.setText(message);

        alertDialogBuilder
                .setPositiveButton(decoratedConference.isRegistered() ? R.string.unregister
                        : R.string.register, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        registerToConference(decoratedConference);
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
    }

    private void registerToConference(DecoratedConference decoratedConference) {
        new RegistrationAsyncTask(decoratedConference).execute();
    }

    class RegistrationAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private final DecoratedConference mDecoratedConference;
        private List<DecoratedConference> mDecoratedConferences;
        private Exception mException;

        public RegistrationAsyncTask(DecoratedConference conference) {
            this.mDecoratedConference = conference;
        }

        @Override
        protected void onPreExecute() {
            setListShown(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                try {
                    if (mDecoratedConference.isRegistered()) {
                        boolean success = ConferenceUtils
                                .unregisterFromConference(mDecoratedConference.getConference());
                        if (success) {
                            mDecoratedConferences = ConferenceUtils.getConferences();
                        }
                        return success;
                    } else {
                        boolean success = ConferenceUtils.registerForConference(
                                mDecoratedConference.getConference());
                        if (success) {
                            mDecoratedConferences = ConferenceUtils.getConferences();
                        }
                        return success;
                    }
                } catch (IOException e) {
                    mException = e;
                }
            } catch (ConferenceException e) {
                //logged
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (null != result && result.booleanValue()) {
                // success
                reload(mDecoratedConferences);
            } else {
                // failure
                Log.e(TAG, "Failed to perform registration update", mException);
                if (mException != null) {
                    Utils.displayNetworkErrorMessage(getActivity());
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int,
     * android.os.Bundle)
     */
    @Override
    public Loader<List<DecoratedConference>> onCreateLoader(int arg0, Bundle arg1) {
        return new ConferenceLoader(getActivity());
    }

    public static ConferenceListFragment newInstance() {
        ConferenceListFragment f = new ConferenceListFragment();
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    public void loadConferences() {
        getLoaderManager().initLoader(0, null, this);
    }

    public void reload() {
        setListShown(false);
        getLoaderManager().restartLoader(0, null, this).startLoading();
    }

    public void reload(List<DecoratedConference> conferences) {
        mAdapter.setData(conferences);
        mAdapter.notifyDataSetChanged();
        setListShown(true);
    }

}