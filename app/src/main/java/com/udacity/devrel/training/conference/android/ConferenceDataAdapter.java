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

import com.udacity.devrel.training.conference.android.utils.DecoratedConference;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.udacity.devrel.training.conference.android.utils.Utils;

import java.util.List;

/**
 *
 */
public class ConferenceDataAdapter extends ArrayAdapter<DecoratedConference> {

    private static final String TAG = "ConferenceDataAdapter";

    private final Context mContext;

    public ConferenceDataAdapter(Context context) {
        super(context, 0);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        DecoratedConference decoratedConference = getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.conference_row, null);
            holder = new ViewHolder();
            holder.titleView = (TextView) convertView.findViewById(R.id.textView1);
            holder.descriptionView = (TextView) convertView.findViewById(R.id.textView2);
            holder.cityAndDateView = (TextView) convertView.findViewById(R.id.textView3);
            holder.registerView = (ImageView) convertView.findViewById(R.id.imageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.titleView.setText(decoratedConference.getConference().getName());
        holder.descriptionView.setText(decoratedConference.getConference().getDescription());
        holder.cityAndDateView.setText(decoratedConference.getConference().getCity() + ", " + Utils
                .getConferenceDate(mContext, decoratedConference.getConference()));
        holder.registerView
                .setVisibility(decoratedConference.isRegistered() ? View.VISIBLE : View.GONE);

        return convertView;
    }

    private class ViewHolder {

        TextView titleView;
        TextView descriptionView;
        TextView cityAndDateView;
        ImageView registerView;
    }

    public void setData(List<DecoratedConference> data) {
        clear();
        if (data != null) {
            for (DecoratedConference item : data) {
                add(item);
            }
        }

    }
}