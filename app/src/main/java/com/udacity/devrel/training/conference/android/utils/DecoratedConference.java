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

import com.appspot.udacity_extras.conference.model.Conference;

/**
 * A wrapper around the {@link com.appspot.udacity_extras.conference.model.Conference}
 * to enable adding additional fields and operations.
 */
public class DecoratedConference {

    private Conference mConference;
    private boolean mRegistered;

    public DecoratedConference(Conference conference, boolean registered) {
        mConference = conference;

        /* keeps the status of user's attendance to this conference */
        mRegistered = registered;
    }

    public Conference getConference() {
        return mConference;
    }

    public void setConference(Conference conference) {
        mConference = conference;
    }

    public boolean isRegistered() {
        return mRegistered;
    }

    public void setRegistered(boolean registered) {
        mRegistered = registered;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DecoratedConference that = (DecoratedConference) o;

        if (mConference != null ? !mConference.equals(that.mConference)
                : that.mConference != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return mConference != null ? mConference.hashCode() : 0;
    }
}
