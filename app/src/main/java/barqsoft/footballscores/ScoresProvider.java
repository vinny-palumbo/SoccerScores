package barqsoft.footballscores;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresProvider extends ContentProvider
{
    private UriMatcher muriMatcher = buildUriMatcher();
    private static ScoresDBHelper mOpenHelper;

    private static final int MATCHES = 100;
    private static final int MATCHES_WITH_LEAGUE = 101;
    private static final int MATCHES_WITH_ID = 102;
    private static final int MATCHES_WITH_DATE = 103;

    private static final SQLiteQueryBuilder ScoreQuery =
            new SQLiteQueryBuilder();

    private static final String SCORES_BY_LEAGUE =
            DatabaseContract.ScoresEntry.TABLE_NAME +
                "." + DatabaseContract.ScoresEntry.LEAGUE_COL + " = ?";
    private static final String SCORES_BY_DATE =
            DatabaseContract.ScoresEntry.TABLE_NAME +
                "." + DatabaseContract.ScoresEntry.DATE_COL + " LIKE ?";
    private static final String SCORES_BY_ID =
            DatabaseContract.ScoresEntry.TABLE_NAME +
                    "." + DatabaseContract.ScoresEntry.MATCH_ID + " = ?";


    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DatabaseContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, DatabaseContract.PATH , MATCHES);
        matcher.addURI(authority, DatabaseContract.PATH + "league" , MATCHES_WITH_LEAGUE);
        matcher.addURI(authority, DatabaseContract.PATH + "id" , MATCHES_WITH_ID);
        matcher.addURI(authority, DatabaseContract.PATH + "date" , MATCHES_WITH_DATE);
        return matcher;
    }

    private int match_uri(Uri uri)
    {
        String link = uri.toString();
        {
           if(link.contentEquals(DatabaseContract.ScoresEntry.CONTENT_URI.toString()))
           {
               return MATCHES;
           }
           else if(link.contentEquals(DatabaseContract.ScoresEntry.buildScoreWithDate().toString()))
           {
               return MATCHES_WITH_DATE;
           }
           else if(link.contentEquals(DatabaseContract.ScoresEntry.buildScoreWithId().toString()))
           {
               return MATCHES_WITH_ID;
           }
           else if(link.contentEquals(DatabaseContract.ScoresEntry.buildScoreWithLeague().toString()))
           {
               return MATCHES_WITH_LEAGUE;
           }
        }
        return -1;
    }
    @Override
    public boolean onCreate()
    {
        mOpenHelper = new ScoresDBHelper(getContext());
        return true;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        return 0;
    }

    @Override
    public String getType(Uri uri)
    {
        final int match = muriMatcher.match(uri);
        switch (match) {
            case MATCHES:
                return DatabaseContract.ScoresEntry.CONTENT_TYPE;
            case MATCHES_WITH_LEAGUE:
                return DatabaseContract.ScoresEntry.CONTENT_TYPE;
            case MATCHES_WITH_ID:
                return DatabaseContract.ScoresEntry.CONTENT_ITEM_TYPE;
            case MATCHES_WITH_DATE:
                return DatabaseContract.ScoresEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri :" + uri );
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        Cursor retCursor;
        //Log.v(FetchScoreTask.LOG_TAG,uri.getPathSegments().toString());
        int match = match_uri(uri);
        //Log.v(FetchScoreTask.LOG_TAG,SCORES_BY_LEAGUE);
        //Log.v(FetchScoreTask.LOG_TAG,selectionArgs[0]);
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(match));
        switch (match)
        {
            case MATCHES: retCursor = mOpenHelper.getReadableDatabase().query(
                    DatabaseContract.ScoresEntry.TABLE_NAME,
                    projection,null,null,null,null,sortOrder); break;
            case MATCHES_WITH_DATE:
                    //Log.v(FetchScoreTask.LOG_TAG,selectionArgs[1]);
                    //Log.v(FetchScoreTask.LOG_TAG,selectionArgs[2]);
                    retCursor = mOpenHelper.getReadableDatabase().query(
                    DatabaseContract.ScoresEntry.TABLE_NAME,
                    projection,SCORES_BY_DATE,selectionArgs,null,null,sortOrder); break;
            case MATCHES_WITH_ID: retCursor = mOpenHelper.getReadableDatabase().query(
                    DatabaseContract.ScoresEntry.TABLE_NAME,
                    projection,SCORES_BY_ID,selectionArgs,null,null,sortOrder); break;
            case MATCHES_WITH_LEAGUE: retCursor = mOpenHelper.getReadableDatabase().query(
                    DatabaseContract.ScoresEntry.TABLE_NAME,
                    projection,SCORES_BY_LEAGUE,selectionArgs,null,null,sortOrder); break;
            default: throw new UnsupportedOperationException("Unknown Uri" + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(),uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        return null;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        //db.delete(DatabaseContract.TABLE_NAME,null,null);
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(muriMatcher.match(uri)));
        switch (match_uri(uri))
        {
            case MATCHES:
                db.beginTransaction();
                int returncount = 0;
                try
                {
                    for(ContentValues value : values)
                    {
                        long _id = db.insertWithOnConflict(DatabaseContract.ScoresEntry.TABLE_NAME, null, value,
                                SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1)
                        {
                            returncount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri,null);
                return returncount;
            default:
                return super.bulkInsert(uri,values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }
}
