package barqsoft.footballscores.data;

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
public class ScoresProvider extends ContentProvider {

    public static final String LOG_TAG = ScoresProvider.class.getSimpleName();

    private static final int FIXTURES = 100;
    private static final int FIXTURES_WITH_ID = 101;
    private static final int FIXTURES_WITH_DATE = 102;
    private static final int LEAGUES = 200;
    private static final int LEAGUES_WITH_ID = 201;
    private static final int TEAMS = 300;
    private static final int TEAMS_WITH_ID = 301;

    private static final SQLiteQueryBuilder sFixtureQueryBuilder;

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
                DatabaseContract.LEAGUE_PATH,
                LEAGUES);

        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
                DatabaseContract.LEAGUE_PATH + "/#",
                LEAGUES_WITH_ID);

        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
                DatabaseContract.TEAM_PATH,
                TEAMS);

        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
                DatabaseContract.TEAM_PATH + "/#",
                TEAMS_WITH_ID);

        return sUriMatcher;
    }

    static{
        sFixtureQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        // Songs
        // LEFT JOIN Artists ON Songs.artist_id = Artists._id
        // LEFT JOIN Albums ON Songs.album_id = Albums._id

        sFixtureQueryBuilder.setTables(
                DatabaseContract.FixtureEntry.TABLE_NAME + " LEFT JOIN " +
                        DatabaseContract.TeamEntry.TABLE_NAME +
                        " AS T1 ON " + DatabaseContract.FixtureEntry.TABLE_NAME +
                        "." + DatabaseContract.FixtureEntry.HOME_COL +
                        " = T1." + DatabaseContract.TeamEntry._ID + " LEFT JOIN " +
                        DatabaseContract.TeamEntry.TABLE_NAME +
                        " AS T2 ON " + DatabaseContract.FixtureEntry.TABLE_NAME +
                        "." + DatabaseContract.FixtureEntry.AWAY_COL +
                        " = T2." + DatabaseContract.TeamEntry._ID
        );

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
                retCursor = sFixtureQueryBuilder.query(mOpenHelper.getReadableDatabase(),
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
                retCursor = getTeamById(uri, projection, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case TEAMS: {
                long _id = db.insert(DatabaseContract.TeamEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = DatabaseContract.TeamEntry.buildTeamWithId(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LEAGUES: {
                long _id = db.insert(DatabaseContract.LeagueEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = DatabaseContract.LeagueEntry.buildLeagueWithId(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case FIXTURES:

                db.beginTransaction();
                int returncount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(DatabaseContract.FixtureEntry.TABLE_NAME, null, value,
                                SQLiteDatabase.CONFLICT_REPLACE);

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

    private Cursor getTeamById(
            Uri uri, String[] projection, String sortOrder) {
        long teamId = DatabaseContract.TeamEntry.getIdFromUri(uri);

        return mOpenHelper.getReadableDatabase().query(
                DatabaseContract.TeamEntry.TABLE_NAME,
                projection, TEAM_BY_ID, new String[]{Long.toString(teamId)}, null, null, sortOrder);

    }
}
