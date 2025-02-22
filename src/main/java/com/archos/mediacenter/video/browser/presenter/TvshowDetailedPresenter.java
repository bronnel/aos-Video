// Copyright 2017 Archos SA
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.archos.mediacenter.video.browser.presenter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.archos.mediacenter.utils.ThumbnailEngine;
import com.archos.mediacenter.video.R;
import com.archos.mediacenter.video.browser.adapters.AdapterDefaultValuesDetails;
import com.archos.mediacenter.video.browser.adapters.AdapterDefaultValuesDetailsShow;
import com.archos.mediacenter.video.browser.adapters.AdapterDefaultValuesList;
import com.archos.mediacenter.video.browser.adapters.object.Tvshow;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

/**
 * Created by alexandre on 27/10/15.
 */
public class TvshowDetailedPresenter extends TvshowListPresenter{
    private final NumberFormat mNumberFormat;
    private final DateFormat mDateFormat;

    public TvshowDetailedPresenter(Context context, ExtendedClickListener listener) {
        super(context, AdapterDefaultValuesDetailsShow.INSTANCE, listener);
        mNumberFormat = NumberFormat.getInstance();
        mNumberFormat.setMinimumFractionDigits(1);
        mNumberFormat.setMaximumFractionDigits(1);
        mDateFormat = DateFormat.getDateInstance(DateFormat.LONG);
    }

    static class ViewHolderDetails extends ViewHolder {
        TextView detailLineOne;
        TextView detailLineTwo;
        TextView detailLineThree;
        TextView rating;
        TextView release_date;
    }
    @Override
    public ViewHolderDetails getNewViewHolder() {
        return new ViewHolderDetails();
    }

    @Override
    public View getView(ViewGroup parent, Object object, View view) {
        view = super.getView(parent, object, view);

        ViewHolderDetails holder = (ViewHolderDetails) view.getTag();
        holder.detailLineOne = (TextView) view.findViewById(R.id.detail_line_one);
        holder.detailLineTwo = (TextView) view.findViewById(R.id.detail_line_two);
        holder.detailLineThree = (TextView) view.findViewById(R.id.detail_line_three);
        holder.rating = (TextView) view.findViewById(R.id.rating);
        holder.release_date = (TextView) view.findViewById(R.id.release_date);

        return view;
    }

    @Override
    public View bindView(View view, Object object, ThumbnailEngine.Result result, int positionInAdapter) {
        Tvshow tvShow = (Tvshow) object;
        super.bindView(view,object, result, positionInAdapter);
        ViewHolderDetails holder = (ViewHolderDetails) view.getTag();

            String name;
            name = tvShow.getName();

            if (name == null) name = "";
           if(holder.name!=null) {
                holder.name.setText(name);
            }
            if(holder.name!=null)
                holder.name.setEllipsize(TextUtils.TruncateAt.END);

            setViewHolderVisibility(holder, View.VISIBLE);
            holder.detailLineOne.setText(tvShow.getStudio());
            holder.detailLineTwo.setText(tvShow.getPlot());

            holder.detailLineThree.setText(tvShow.getActors());
            holder.detailLineThree.setVisibility(View.GONE);
            long date = tvShow.getYear();
            float rating = tvShow.getRating();
            String ratingFormated;
            if (rating >= 0.0f) {
                ratingFormated = mNumberFormat.format(rating);
            } else {
                ratingFormated = "";
            }
            holder.rating.setText(mContext.getResources().getString(R.string.scrap_rating_format, ratingFormated));

            if (date > 0) {
                holder.release_date.setText(mContext.getResources().getString(
                        R.string.scrap_aired_format, mDateFormat.format(new Date(date))));
            } else {
                holder.release_date.setText(R.string.scrap_aired);
            }
        holder.release_date.setVisibility(View.GONE);

        holder.resume.setVisibility(View.GONE);
            holder.detailLineTwo.setMaxLines(2);
            holder.detailLineThree.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
            holder.detailLineThree.setSingleLine(false);
            holder.detailLineThree.setMaxLines(3);
        return view;
    }
    private void setViewHolderVisibility(ViewHolderDetails holder, int visibility) {
        holder.detailLineOne.setVisibility(visibility);
        holder.detailLineTwo.setVisibility(visibility);
        holder.detailLineThree.setVisibility(visibility);
        holder.release_date.setVisibility(visibility);
        holder.rating.setVisibility(visibility);
    }
}
