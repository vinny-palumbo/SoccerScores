/*
 * Copyright (C) 2015 The Android Open Source Project
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
package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;


/**
 * IntentService which handles updating all Today widgets with the latest data
 */
public class TodayWidgetIntentService extends IntentService {
    private static final String[] SCORES_COLUMNS = {
            DatabaseContract.ScoresEntry._ID,
            DatabaseContract.ScoresEntry.TIME_COL,
            DatabaseContract.ScoresEntry.HOME_COL,
            DatabaseContract.ScoresEntry.AWAY_COL,
            DatabaseContract.ScoresEntry.HOME_GOALS_COL,
            DatabaseContract.ScoresEntry.AWAY_GOALS_COL
    };
    // these indices must match the projection
    private static final int INDEX_SCORES_ID  = 0;
    private static final int INDEX_SCORES_TIME  = 1;
    private static final int INDEX_SCORES_HOME_TEAM  = 2;
    private static final int INDEX_SCORES_AWAY_TEAM  = 3;
    private static final int INDEX_SCORES_HOME_GOALS  = 4;
    private static final int INDEX_SCORES_AWAY_GOALS  = 5;

    public TodayWidgetIntentService() {
        super("TodayWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                TodayWidgetProvider.class));

        // Get today's data from the ContentProvider
        Uri scoresUri = DatabaseContract.ScoresEntry.CONTENT_URI;
        Cursor data = getContentResolver().query(
                scoresUri,
                SCORES_COLUMNS,
                null,
                null,
                null);
        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        // Extract the weather data from the Cursor
        String homeTeam = data.getString(INDEX_SCORES_HOME_TEAM);
        int homeIconResourceId = Utilies.getTeamCrestByTeamName(homeTeam);
        String awayTeam = data.getString(INDEX_SCORES_AWAY_TEAM);
        int awayIconResourceId = Utilies.getTeamCrestByTeamName(awayTeam);
        String matchTime = data.getString(INDEX_SCORES_TIME);
        int scoreHome = data.getInt(INDEX_SCORES_HOME_GOALS);
        int scoreAway = data.getInt(INDEX_SCORES_AWAY_GOALS);
        String formattedScore = Utilies.getScores(scoreHome, scoreAway);

        data.close();

        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget_today;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            // Add the data to the RemoteViews
            views.setImageViewResource(R.id.widget_home_crest, homeIconResourceId);
            views.setImageViewResource(R.id.widget_away_crest, awayIconResourceId);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, homeTeam, awayTeam);
            }
            views.setTextViewText(R.id.widget_home_name, homeTeam);
            views.setTextViewText(R.id.widget_away_name, awayTeam);
            views.setTextViewText(R.id.widget_date_textview, matchTime);
            views.setTextViewText(R.id.widget_score_textview, formattedScore);

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String homeTeam, String awayTeam) {
        views.setContentDescription(R.id.widget_home_crest, homeTeam);
        views.setContentDescription(R.id.widget_away_crest, awayTeam);
    }
}