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

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.io.IOException;
import java.util.List;

public class ConferenceLoader extends AsyncTaskLoader<List<DecoratedConference>> {

    private static final String TAG = "ConferenceLoader";
    private Exception mException;

    public ConferenceLoader(Context context) {
        super(context);
    }

    @Override
    public List<DecoratedConference> loadInBackground() {
        try {
            ConferenceUtils.getProfile();
            return ConferenceUtils.getConferences();
        } catch (IOException e) {
            Log.e(TAG, "Failed to get conferences", e);
            mException = e;
        } catch (ConferenceException e) {
            // logged
        }
        return null;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        cancelLoad();
        if (mException != null) {
            Utils.displayNetworkErrorMessage(getContext());
        }
    }

    public Exception getException() {
        return mException;
    }

}