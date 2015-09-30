package barqsoft.footballscores.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresProvider extends ContentProvider {

    public static final String LOG_TAG = ScoresProvider.class.getSimpleName();

    private static final int FIXTURES = 100;
    private static final int FIXTURES_WITH_ID = 101;
    private static final int FIXTURES_WITH_DATE = 102;
    private static final int LEAGUES = 200;
    private static final int LEAGUES_WITH_ID = 201;
    private static final int TEAMS = 300;
    private static final int TEAMS_WITH_ID = 301;

    private static final SQLiteQueryBuilder ScoreQuery =
            new SQLiteQueryBuilder();

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final String FIXTURES_BY_DATE =
            DatabaseContract.FixtureEntry.DATE_COL + " LIKE ?";
    private static final String FIXTURES_BY_ID =
            DatabaseContract.FixtureEntry.MATCH_ID + " = ?";

    private static final String TEAM_BY_ID =
            DatabaseContract.TeamEntry.TEAM_ID + " = ?";

    private static final String LEAGUE_BY_ID =
            DatabaseContract.LeagueEntry.LEAGUE_ID + " = ?";

    private static ScoresDBHelper mOpenHelper;
    private UriMatcher muriMatcher = buildUriMatcher();

    static UriMatcher buildUriMatcher() {
        final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
                DatabaseContract.FIXTURE_PATH, FIXTURES);

        /*
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
                DatabaseContract.FIXTURE_PATH + DatabaseContract.ID_PATH,
                FIXTURES_WITH_ID);
        */

        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
                DatabaseContract.FIXTURE_PATH + DatabaseContract.DATE_PATH,
                FIXTURES_WITH_DATE);

        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
                DatabaseContract.LEAGUE_PATH + "/#",
                LEAGUES_WITH_ID);

        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
                DatabaseContract.TEAM_PATH + "/#",
                TEAMS_WITH_ID);

        return sUriMatcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ScoresDBHelper(getContext());
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        final int match = muriMatcher.match(uri);

        switch (match) {
            case FIXTURES:
                return DatabaseContract.FixtureEntry.CONTENT_TYPE;
            case FIXTURES_WITH_ID:
                return DatabaseContract.FixtureEntry.CONTENT_ITEM_TYPE;
            case FIXTURES_WITH_DATE:
                return DatabaseContract.FixtureEntry.CONTENT_TYPE;
            case LEAGUES:
                return DatabaseContract.LeagueEntry.CONTENT_TYPE;
            case LEAGUES_WITH_ID:
                return DatabaseContract.LeagueEntry.CONTENT_TYPE;
            case TEAMS:
                return DatabaseContract.TeamEntry.CONTENT_TYPE;
            case TEAMS_WITH_ID:
                return DatabaseContract.TeamEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri :" + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {
            case FIXTURES:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DatabaseContract.FixtureEntry.TABLE_NAME,
                        projection, null, null, null, null, sortOrder);
                break;
            case FIXTURES_WITH_DATE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DatabaseContract.FixtureEntry.TABLE_NAME,
                        projection, FIXTURES_BY_DATE, selectionArgs, null, null, sortOrder);
                break;
            case FIXTURES_WITH_ID:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DatabaseContract.FixtureEntry.TABLE_NAME,
                        projection, FIXTURES_BY_ID, selectionArgs, null, null, sortOrder);
                break;
            case LEAGUES:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DatabaseContract.LeagueEntry.TABLE_NAME,
                        projection, null, null, null, null, sortOrder);
                break;
            case LEAGUES_WITH_ID:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DatabaseContract.LeagueEntry.TABLE_NAME,
                        projection, LEAGUE_BY_ID, selectionArgs, null, null, sortOrder);
                break;
            case TEAMS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DatabaseContract.TeamEntry.TABLE_NAME,
                        projection, null, null, null, null, sortOrder);
                break;
            case TEAMS_WITH_ID:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DatabaseContract.TeamEntry.TABLE_NAME,
                        projection, TEAM_BY_ID, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        Log.d(LOG_TAG, ";;;;;;;;;;;;;;;;;;;;;;;;;;; ScoresProvider:bulkInsert Uri: " + uri.toString() + " - sUriMatcher: " + sUriMatcher.match(uri));

        switch (sUriMatcher.match(uri)) {
            case FIXTURES:

                Log.d(LOG_TAG, ";;;;;;;;;;;;;;;;;;;;;;;;;;; ScoresProvider:bulkInsert FIXTURES : " + FIXTURES);

                db.beginTransaction();
                int returncount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(DatabaseContract.FixtureEntry.TABLE_NAME, null, value,
                                SQLiteDatabase.CONFLICT_REPLACE);

                        Log.d(LOG_TAG, ";;;;;;;;;;;;;;;;;;;;;;;;;;; ScoresProvider:bulkInsert id: " + _id + " - " + value.get(DatabaseContract.FixtureEntry.MATCH_ID));

                        if (_id != -1) {
                            returncount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returncount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }
}
