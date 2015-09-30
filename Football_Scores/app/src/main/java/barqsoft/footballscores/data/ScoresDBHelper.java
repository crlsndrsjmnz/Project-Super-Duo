package barqsoft.footballscores.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import barqsoft.footballscores.data.DatabaseContract.FixtureEntry;
import barqsoft.footballscores.data.DatabaseContract.LeagueEntry;
import barqsoft.footballscores.data.DatabaseContract.TeamEntry;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "scores.db";
    private static final int DATABASE_VERSION = 2;

    public ScoresDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_LEAGUE_TABLE = "CREATE TABLE " + DatabaseContract.LeagueEntry.TABLE_NAME + " ("
                + LeagueEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + LeagueEntry.LEAGUE_ID + " TEXT UNIQUE NOT NULL, "
                + LeagueEntry.NAME_COL + " TEXT NOT NULL, "
                + LeagueEntry.ABBREVIATION_COL + " TEXT NOT NULL, "
                + LeagueEntry.NUMBER_OF_TEAMS_COL + " INTEGER NOT NULL, "
                + LeagueEntry.NUMBER_OF_GAMES_COL + " INTEGER NOT NULL, "
                + LeagueEntry.LOGO_URL_COL + " TEXT, "
                + " UNIQUE (" + LeagueEntry.LEAGUE_ID + ") ON CONFLICT REPLACE"
                + " );";
        sqLiteDatabase.execSQL(SQL_CREATE_LEAGUE_TABLE);

        final String SQL_CREATE_TEAM_TABLE = "CREATE TABLE " + DatabaseContract.TeamEntry.TABLE_NAME + " ("
                + TeamEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TeamEntry.TEAM_ID + " TEXT UNIQUE NOT NULL, "
                + TeamEntry.NAME_COL + " TEXT NOT NULL, "
                + TeamEntry.ABBREVIATION_COL + " TEXT NOT NULL, "
                + TeamEntry.CREST_URL_COL + " TEXT NOT NULL, "
                + " UNIQUE (" + TeamEntry.TEAM_ID + ") ON CONFLICT REPLACE"
                + " );";
        sqLiteDatabase.execSQL(SQL_CREATE_TEAM_TABLE);

        final String SQL_CREATE_FIXTURES_TABLE = "CREATE TABLE " + FixtureEntry.TABLE_NAME + " ("
                + FixtureEntry._ID + " INTEGER PRIMARY KEY,"
                + FixtureEntry.DATE_COL + " TEXT NOT NULL,"
                + FixtureEntry.TIME_COL + " INTEGER NOT NULL,"
                + FixtureEntry.HOME_COL + " TEXT NOT NULL,"
                + FixtureEntry.AWAY_COL + " TEXT NOT NULL,"
                + FixtureEntry.LEAGUE_COL + " INTEGER NOT NULL,"
                + FixtureEntry.HOME_GOALS_COL + " TEXT NOT NULL,"
                + FixtureEntry.AWAY_GOALS_COL + " TEXT NOT NULL,"
                + FixtureEntry.MATCH_ID + " INTEGER NOT NULL,"
                + FixtureEntry.MATCH_DAY + " INTEGER NOT NULL,"

                + " FOREIGN KEY (" + FixtureEntry.HOME_COL + ") REFERENCES "
                + TeamEntry.TABLE_NAME + " (" + TeamEntry.TEAM_ID + "), "
                + " FOREIGN KEY (" + FixtureEntry.AWAY_COL + ") REFERENCES "
                + TeamEntry.TABLE_NAME + " (" + TeamEntry.TEAM_ID + "), "

                + " FOREIGN KEY (" + FixtureEntry.LEAGUE_COL + ") REFERENCES "
                + LeagueEntry.TABLE_NAME + " (" + LeagueEntry.LEAGUE_ID + "), "

                + " UNIQUE (" + FixtureEntry.MATCH_ID + ") ON CONFLICT REPLACE"
                + " );";
        sqLiteDatabase.execSQL(SQL_CREATE_FIXTURES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Remove old values when upgrading.
        db.execSQL("DROP TABLE IF EXISTS " + FixtureEntry.TABLE_NAME);
        //db.execSQL("DROP TABLE IF EXISTS " + TeamEntry.TABLE_NAME);
        //db.execSQL("DROP TABLE IF EXISTS " + LeagueEntry.TABLE_NAME);
    }
}
