package barqsoft.footballscores.widget;



import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * RemoteViewsService controlling the data being shown in the scrollable weather detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();
    private static final String[] SCORES_COLUMNS = {
            DatabaseContract.ScoresEntry.TABLE_NAME + "." + DatabaseContract.ScoresEntry._ID,
            DatabaseContract.ScoresEntry.TIME_COL,
            DatabaseContract.ScoresEntry.HOME_COL,
            DatabaseContract.ScoresEntry.AWAY_COL,
            DatabaseContract.ScoresEntry.HOME_GOALS_COL,
            DatabaseContract.ScoresEntry.AWAY_GOALS_COL,
    };
    // these indices must match the projection
    static final int INDEX_SCORES_ID = 0;
    static final int INDEX_SCORES_TIME = 1;
    static final int INDEX_SCORES_HOME_TEAM = 2;
    static final int INDEX_SCORES_AWAY_TEAM = 3;
    static final int INDEX_SCORES_HOME_GOALS = 4;
    static final int INDEX_SCORES_AWAY_GOALS = 5;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
//                String location = Utility.getPreferredLocation(DetailWidgetRemoteViewsService.this);
                Uri scoresUri = DatabaseContract.ScoresEntry.CONTENT_URI;
                data = getContentResolver().query(scoresUri,
                        SCORES_COLUMNS,
                        null,
                        null,
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                String homeTeam = data.getString(INDEX_SCORES_HOME_TEAM);
                int homeIconResourceId = Utilies.getTeamCrestByTeamName(homeTeam);
                String awayTeam = data.getString(INDEX_SCORES_AWAY_TEAM);
                int awayIconResourceId = Utilies.getTeamCrestByTeamName(awayTeam);
                String matchTime = data.getString(INDEX_SCORES_TIME);
                int scoreHome = data.getInt(INDEX_SCORES_HOME_GOALS);
                int scoreAway = data.getInt(INDEX_SCORES_AWAY_GOALS);
                String formattedScore = Utilies.getScores(scoreHome, scoreAway);

                views.setImageViewResource(R.id.widget_home_crest, homeIconResourceId);
                views.setImageViewResource(R.id.widget_away_crest, awayIconResourceId);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, homeTeam, awayTeam);
                }
                views.setTextViewText(R.id.widget_home_name, homeTeam);
                views.setTextViewText(R.id.widget_away_name, awayTeam);
                views.setTextViewText(R.id.widget_date_textview, matchTime);
                views.setTextViewText(R.id.widget_score_textview, formattedScore);

//                final Intent fillInIntent = new Intent();
//                Uri scoresUri = DatabaseContract.ScoresEntry.buildScoreWithDate();
//                fillInIntent.setData(scoresUri);
//                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String homeTeam, String awayTeam) {
                views.setContentDescription(R.id.widget_home_crest, homeTeam);
                views.setContentDescription(R.id.widget_away_crest, awayTeam);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_SCORES_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}