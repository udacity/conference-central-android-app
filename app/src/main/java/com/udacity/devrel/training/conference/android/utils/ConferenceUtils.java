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

package com.udacity.devrel.training.conference.android.utils;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.udacity.devrel.training.conference.android.AppConstants;

import com.appspot.your_app_id.conference.model.Conference;
import com.appspot.your_app_id.conference.model.ConferenceCollection;
import com.appspot.your_app_id.conference.model.Profile;
import com.appspot.your_app_id.conference.model.WrappedBoolean;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for communication with the Cloud Endpoint.
 */
public class ConferenceUtils {

    private final static String TAG = "ConferenceUtils";
    private static com.appspot.your_app_id.conference.Conference sApiServiceHandler;

    public static void build(Context context, String email) {
        sApiServiceHandler = buildServiceHandler(context, email);
    }

    /**
     * Returns a list of {@link com.udacity.devrel.training.conference.android.utils.DecoratedConference}s.
     * This list includes information about what {@link com.appspot.your_app_id.conference.model.Conference}s
     * user has registered for.
     *
     * @return
     * @throws ConferenceException
     * @see <code>getProfile</code>
     */
    public static List<DecoratedConference> getConferences()
            throws ConferenceException, IOException {
        if (null == sApiServiceHandler) {
            Log.e(TAG, "getConferences(): no service handler was built");
            throw new ConferenceException();
        }

        com.appspot.your_app_id.conference.Conference.QueryConferences
                queryConferences = sApiServiceHandler.queryConferences(null);
        ConferenceCollection conferenceCollection = queryConferences.execute();

        if (conferenceCollection != null && conferenceCollection.getItems() != null) {
            List<Conference> conferences = conferenceCollection.getItems();
            List<DecoratedConference> decoratedList = null;
            if (null == conferences || conferences.isEmpty()) {
                return decoratedList;
            }
            decoratedList = new ArrayList<DecoratedConference>();
            Profile profile = getProfile();
            List<String> registeredConfKeys = null;
            if (null != profile) {
                registeredConfKeys = profile.getConferenceKeysToAttend();
            }
            if (null == registeredConfKeys) {
                registeredConfKeys = new ArrayList<String>();
            }
            for (Conference conference : conferences) {
                DecoratedConference decorated = new DecoratedConference(conference,
                        registeredConfKeys.contains(conference.getWebsafeKey()));
                decoratedList.add(decorated);
            }
            return decoratedList;
        }
        return null;
    }

    /**
     * Registers user for a {@link com.appspot.your_app_id.conference.model.Conference}
     *
     * @param conference
     * @return
     * @throws ConferenceException
     */
    public static boolean registerForConference(Conference conference)
            throws ConferenceException, IOException {
        if (null == sApiServiceHandler) {
            Log.e(TAG, "registerForConference(): no service handler was built");
            throw new ConferenceException();
        }

        com.appspot.your_app_id.conference.Conference.RegisterForConference
                registerForConference = sApiServiceHandler.registerForConference(
                conference.getWebsafeKey());
        WrappedBoolean result = registerForConference.execute();
        return result.getResult();
    }

    /**
     * Unregisters user from a {@link com.appspot.your_app_id.conference.model.Conference}.
     *
     * @param conference
     * @return
     * @throws ConferenceException
     */
    public static boolean unregisterFromConference(Conference conference)
            throws ConferenceException, IOException {
        if (null == sApiServiceHandler) {
            Log.e(TAG, "unregisterFromConference(): no service handler was built");
            throw new ConferenceException();
        }

        com.appspot.your_app_id.conference.Conference.UnregisterFromConference
                unregisterFromConference = sApiServiceHandler.unregisterFromConference(
                conference.getWebsafeKey());
        WrappedBoolean result = unregisterFromConference.execute();
        return result.getResult();
    }

    /**
     * Returns the user {@link com.appspot.your_app_id.conference.model.Profile}. Can
     * be used to find out what conferences user is registered for.
     *
     * @return
     * @throws ConferenceException
     */
    public static Profile getProfile() throws ConferenceException, IOException {
        if (null == sApiServiceHandler) {
            Log.e(TAG, "getProfile(): no service handler was built");
            throw new ConferenceException();
        }

        com.appspot.your_app_id.conference.Conference.GetProfile getProfile =
                sApiServiceHandler.getProfile();
        return getProfile.execute();
    }

    /**
     * Build and returns an instance of {@link com.appspot.your_app_id.conference.Conference}
     *
     * @param context
     * @param email
     * @return
     */
    public static com.appspot.your_app_id.conference.Conference buildServiceHandler(
            Context context, String email) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(
                context, AppConstants.AUDIENCE);
        credential.setSelectedAccountName(email);

        com.appspot.your_app_id.conference.Conference.Builder builder
                = new com.appspot.your_app_id.conference.Conference.Builder(
                AppConstants.HTTP_TRANSPORT,
                AppConstants.JSON_FACTORY, credential);
        builder.setApplicationName("conference-central-server");
        return builder.build();
    }
}
