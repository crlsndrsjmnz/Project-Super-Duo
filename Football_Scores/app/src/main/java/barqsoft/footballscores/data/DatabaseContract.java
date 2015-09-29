package barqsoft.footballscores.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class DatabaseContract
{
    /*
    public static final String FIXTURE_TABLE = "fixture_table";
    public static final String TEAM_TABLE = "team_table";
    public static final String LEAGUE_TABLE = "league_table";
    */
    //URI data
    public static final String CONTENT_AUTHORITY = "barqsoft.footballscores";
    public static final String FIXTURE_PATH = "fixture";
    //public static final String ID_PATH = "/id/*";
    public static final String DATE_PATH = "/date";
    public static final String TEAM_PATH = "team";
    public static final String LEAGUE_PATH = "league";

    public static Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final class FixtureEntry implements BaseColumns {

        public static final String TABLE_NAME = "fixture";
        //Table data
        public static final String MATCH_ID = "match_id";
        public static final String LEAGUE_COL = "league";
        public static final String DATE_COL = "date";
        public static final String TIME_COL = "time";
        public static final String HOME_COL = "home";
        public static final String AWAY_COL = "away";
        public static final String HOME_GOALS_COL = "home_goals";
        public static final String AWAY_GOALS_COL = "away_goals";
        public static final String MATCH_DAY = "match_day";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(FIXTURE_PATH).build();

        //Types
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + FIXTURE_PATH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + FIXTURE_PATH;

        /*
        public static Uri buildFixtureWithId() {
            return CONTENT_URI.buildUpon().appendPath("id").build();
        }
        */
        public static Uri buildFixtureWithDate() {
            //return ContentUris.withAppendedId(CONTENT_URI, id);
            return CONTENT_URI.buildUpon().appendPath("date").build();
        }
    }

    public static final class TeamEntry implements BaseColumns {

        public static final String TABLE_NAME = "team";

        //Table data
        public static final String TEAM_ID = "team_id";
        public static final String NAME_COL = "league";
        public static final String ABBREVIATION_COL = "abbreviation";
        public static final String CREST_URL_COL = "crest_url";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(TEAM_PATH).build();

        //Types
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TEAM_PATH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TEAM_PATH;

        public static Uri buildTeamWithId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class LeagueEntry implements BaseColumns {

        public static final String TABLE_NAME = "league";

        //Table data
        public static final String LEAGUE_ID = "team_id";
        public static final String NAME_COL = "league";
        public static final String ABBREVIATION_COL = "abbreviation";
        public static final String NUMBER_OF_TEAMS_COL = "number_of_teams";
        public static final String NUMBER_OF_GAMES_COL = "number_of_games";
        public static final String LOGO_URL_COL = "logo_url";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(LEAGUE_PATH).build();

        //Types
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + LEAGUE_PATH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + LEAGUE_PATH;

        public static Uri buildLeagueWithId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
