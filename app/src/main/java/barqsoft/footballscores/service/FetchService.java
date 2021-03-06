package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class FetchService extends IntentService
{
    public static final String LOG_TAG = "FetchService";
    public static final String ACTION_DATA_UPDATED = "barqsoft.footballscores.ACTION_DATA_UPDATED";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SCORES_STATUS_OK, SCORES_STATUS_SERVER_DOWN, SCORES_STATUS_SERVER_INVALID, SCORES_STATUS_UNKNOWN})
    public @interface ScoresStatus {}

    public static final int SCORES_STATUS_OK = 0;
    public static final int SCORES_STATUS_SERVER_DOWN = 1;
    public static final int
            SCORES_STATUS_SERVER_INVALID = 2;
    public static final int
            SCORES_STATUS_UNKNOWN = 3;

    public FetchService()
    {
        super("FetchService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        getData("n2");
        getData("p2");

        return;
    }

    private void getData (String timeFrame)
    {
        //Creating fetch URL
        final String BASE_URL = "http://api.football-data.org/alpha/fixtures"; //Base URL
        final String QUERY_TIME_FRAME = "timeFrame"; //Time Frame parameter to determine days
        //final String QUERY_MATCH_DAY = "matchday";

        Uri fetch_build = Uri.parse(BASE_URL).buildUpon().
                appendQueryParameter(QUERY_TIME_FRAME, timeFrame).build();
        //Log.v(LOG_TAG, "The url we are looking at is: "+fetch_build.toString()); //log spam
        HttpURLConnection m_connection = null;
        BufferedReader reader = null;
        String JSON_data = null;
        //Opening Connection
        try {
            URL fetch = new URL(fetch_build.toString());
            m_connection = (HttpURLConnection) fetch.openConnection();
            m_connection.setRequestMethod("GET");
            m_connection.addRequestProperty("X-Auth-Token",getString(R.string.api_key));
            m_connection.connect();

            // Read the input stream into a String
            InputStream inputStream = m_connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                setScoresStatus(FetchService.this, SCORES_STATUS_SERVER_DOWN);
                return;
            }
            JSON_data = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the scores data, there's no point in attempting
            // to parse it.
            setScoresStatus(FetchService.this, SCORES_STATUS_SERVER_DOWN);
        }finally {
            if(m_connection != null)
            {
                m_connection.disconnect();
            }
            if (reader != null)
            {
                try {
                    reader.close();
                }
                catch (IOException e)
                {
                    Log.e(LOG_TAG,"Error Closing Stream");
                }
            }
        }
        try {
            if (JSON_data != null) {
                //This bit is to check if the data contains any matches. If not, we call processJson on the dummy data
                JSONArray matches = new JSONObject(JSON_data).getJSONArray("fixtures");
                if (matches.length() == 0) {
                    //if there is no data, call the function on dummy data
                    //this is expected behavior during the off season.
                    processJSONdata(getString(R.string.dummy_data), getApplicationContext(), false);
                    return;
                }


                processJSONdata(JSON_data, getApplicationContext(), true);
            } else {
                //Could not Connect
                Log.d(LOG_TAG, "Could not connect to server.");
            }
        }
        catch(Exception e)
        {
            Log.e(LOG_TAG,e.getMessage());
        }
    }
    private void processJSONdata (String JSONdata,Context mContext, boolean isReal) {

        final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
        final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
        final String FIXTURES = "fixtures";
        final String LINKS = "_links";
        final String SOCCER_SEASON = "soccerseason";
        final String SELF = "self";
        final String MATCH_DATE = "date";
        final String HOME_TEAM = "homeTeamName";
        final String AWAY_TEAM = "awayTeamName";
        final String RESULT = "result";
        final String HOME_GOALS = "goalsHomeTeam";
        final String AWAY_GOALS = "goalsAwayTeam";
        final String MATCH_DAY = "matchday";

        //Match data
        String League = null;
        String mDate = null;
        String mTime = null;
        String Home = null;
        String Away = null;
        String Home_goals = null;
        String Away_goals = null;
        String match_id = null;
        String match_day = null;


        try {
            JSONArray matches = new JSONObject(JSONdata).getJSONArray(FIXTURES);


            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector <ContentValues> (matches.length());
            for(int i = 0;i < matches.length();i++)
            {

                JSONObject match_data = matches.getJSONObject(i);
                League = match_data.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString("href");
                League = League.replace(SEASON_LINK,"");

                match_id = match_data.getJSONObject(LINKS).getJSONObject(SELF).
                        getString("href");
                match_id = match_id.replace(MATCH_LINK, "");
                if(!isReal){
                    //This if statement changes the match ID of the dummy data so that it all goes into the database
                    match_id=match_id+Integer.toString(i);
                }

                mDate = match_data.getString(MATCH_DATE);
                mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"));
                mDate = mDate.substring(0,mDate.indexOf("T"));
                SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                match_date.setTimeZone(TimeZone.getTimeZone("UTC"));
                try {
                    Date parseddate = match_date.parse(mDate+mTime);
                    SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                    new_date.setTimeZone(TimeZone.getDefault());
                    mDate = new_date.format(parseddate);
                    mTime = mDate.substring(mDate.indexOf(":") + 1);
                    mDate = mDate.substring(0,mDate.indexOf(":"));

                    if(!isReal){
                        //This if statement changes the dummy data's date to match our current date range.
                        Date fragmentdate = new Date(System.currentTimeMillis()+((i-2)*86400000));
                        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                        mDate=mformat.format(fragmentdate);
                    }
                }
                catch (Exception e)
                {
                    Log.d(LOG_TAG, "error here!");
                    Log.e(LOG_TAG,e.getMessage());
                }
                Home = match_data.getString(HOME_TEAM);
                Away = match_data.getString(AWAY_TEAM);
                Home_goals = match_data.getJSONObject(RESULT).getString(HOME_GOALS);
                Away_goals = match_data.getJSONObject(RESULT).getString(AWAY_GOALS);
                match_day = match_data.getString(MATCH_DAY);

                ContentValues match_values = new ContentValues();
                match_values.put(DatabaseContract.ScoresEntry.MATCH_ID,match_id);
                match_values.put(DatabaseContract.ScoresEntry.DATE_COL,mDate);
                match_values.put(DatabaseContract.ScoresEntry.TIME_COL,mTime);
                match_values.put(DatabaseContract.ScoresEntry.HOME_COL,Home);
                match_values.put(DatabaseContract.ScoresEntry.AWAY_COL,Away);
                match_values.put(DatabaseContract.ScoresEntry.HOME_GOALS_COL,Home_goals);
                match_values.put(DatabaseContract.ScoresEntry.AWAY_GOALS_COL,Away_goals);
                match_values.put(DatabaseContract.ScoresEntry.LEAGUE_COL,League);
                match_values.put(DatabaseContract.ScoresEntry.MATCH_DAY,match_day);
                //log spam

                //Log.v(LOG_TAG,match_id);
                //Log.v(LOG_TAG,mDate);
                //Log.v(LOG_TAG,mTime);
                //Log.v(LOG_TAG,Home);
                //Log.v(LOG_TAG,Away);
                //Log.v(LOG_TAG,Home_goals);
                //Log.v(LOG_TAG,Away_goals);

                values.add(match_values);
            }
            int inserted_data = 0;
            // add to database
            if ( values.size() > 0 ) {
                // delete old data so we don't build up an endless history
                mContext.getContentResolver().delete(DatabaseContract.ScoresEntry.CONTENT_URI, null, null);
                ContentValues[] insert_data = new ContentValues[values.size()];
                values.toArray(insert_data);
                inserted_data = mContext.getContentResolver().bulkInsert(
                        DatabaseContract.ScoresEntry.CONTENT_URI, insert_data);

                updateWidgets();
            }

            Log.d(LOG_TAG, "Sync Complete. " + values.size() + " Inserted");
            setScoresStatus(FetchService.this, SCORES_STATUS_OK);
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG,e.getMessage());
            e.printStackTrace();
            setScoresStatus(FetchService.this, SCORES_STATUS_SERVER_INVALID);
        }

    }

    private void updateWidgets() {
        Context context = FetchService.this;
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

    /**
     * Sets the scores status into shared preference.  This function should not be called from
     * the UI thread because it uses commit to write to the shared preferences.
     * @param c Context to get the PreferenceManager from.
     * @param scoresStatus The IntDef value to set
     */
    static private void setScoresStatus(Context c, @ScoresStatus int scoresStatus){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_scores_status_key), scoresStatus);
        spe.commit();
    }
}

