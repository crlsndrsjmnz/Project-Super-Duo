package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.R;
import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.widget.DetailWidgetProvider;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class myFetchService extends IntentService {
    public static final String LOG_TAG = "myFetchService";
    private static final String[] TEAM_COLUMNS = {
            DatabaseContract.TeamEntry._ID,
            DatabaseContract.TeamEntry.TEAM_ID,
            DatabaseContract.TeamEntry.NAME_COL,
            DatabaseContract.TeamEntry.ABBREVIATION_COL,
            DatabaseContract.TeamEntry.CREST_URL_COL
    };
    private Context mContext;


    public myFetchService() {
        super("myFetchService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mContext = getApplicationContext();

        getData("n2");
        getData("p2");

        return;
    }

    private void getData(String timeFrame) {
        //Creating fetch URL
        final String BASE_URL = "http://api.football-data.org/alpha/fixtures/149340"; //Base URL
        Uri fetch_build = Uri.parse(BASE_URL)
                .buildUpon()
                .build();

        Log.d(LOG_TAG, "::::::::::::::::: myFetchService:getData " + fetch_build.toString());
        /*
        final String BASE_URL = "http://api.football-data.org/alpha/fixtures"; //Base URL
        final String QUERY_TIME_FRAME = "timeFrame"; //Time Frame parameter to determine days
        //final String QUERY_MATCH_DAY = "matchday";

        Uri fetch_build = Uri.parse(BASE_URL).buildUpon().
                appendQueryParameter(QUERY_TIME_FRAME, timeFrame).build();
        */
        HttpURLConnection m_connection = null;
        BufferedReader reader = null;
        String JSON_data = null;
        //Opening Connection
        try {
            URL fetch = new URL(fetch_build.toString());
            m_connection = (HttpURLConnection) fetch.openConnection();
            m_connection.setRequestMethod("GET");
            m_connection.addRequestProperty("X-Auth-Token", getString(R.string.api_key));
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
                return;
            }
            JSON_data = buffer.toString();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception here" + e.getMessage());
        } finally {
            if (m_connection != null) {
                m_connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error Closing Stream");
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
                    processJSONdata(getString(R.string.dummy_data), false);
                    return;
                }


                processJSONdata(JSON_data, true);
            } else {
                //Could not Connect
                Log.d(LOG_TAG, "Could not connect to server.");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void processJSONdata(String JSONdata, boolean isReal) {

        Log.d(LOG_TAG, "::::::::::::::::: myFetchService:processJSONdata");

        //JSON data
        // This set of league codes is for the 2015/2016 season. In fall of 2016, they will need to
        // be updated. Feel free to use the codes
        final String BUNDESLIGA1 = "394";
        final String BUNDESLIGA2 = "395";
        final String LIGUE1 = "396";
        final String LIGUE2 = "397";
        final String PREMIER_LEAGUE = "398";
        final String PRIMERA_DIVISION = "399";
        final String SEGUNDA_DIVISION = "400";
        final String SERIE_A = "401";
        final String PRIMEIRA_LIGA = "402";
        final String Bundesliga3 = "403";
        final String EREDIVISIE = "404";
        final String CHAMPIONS_LEAGUE = "405";


        final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
        final String TEAM_LINK = "http://api.football-data.org/alpha/teams/";
        final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
        final String FIXTURES = "fixtures";
        final String LINKS = "_links";
        final String SOCCER_SEASON = "soccerseason";
        final String SELF = "self";
        final String MATCH_DATE = "date";
        final String HOME_TEAM = "homeTeamName";
        final String HOME_TEAM_LINK = "homeTeam";
        final String AWAY_TEAM = "awayTeamName";
        final String AWAY_TEAM_LINK = "awayTeam";
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
            Vector<ContentValues> values = new Vector<ContentValues>(matches.length());
            for (int i = 0; i < matches.length(); i++) {

                JSONObject match_data = matches.getJSONObject(i);
                League = match_data.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString("href");
                League = League.replace(SEASON_LINK, "");
                //This if statement controls which leagues we're interested in the data from.
                //add leagues here in order to have them be added to the DB.
                // If you are finding no data in the app, check that this contains all the leagues.
                // If it doesn't, that can cause an empty DB, bypassing the dummy data routine.
                if (League.equals(PREMIER_LEAGUE) ||
                        League.equals(SERIE_A) ||
                        League.equals(BUNDESLIGA1) ||
                        League.equals(BUNDESLIGA2) ||
                        League.equals(PRIMERA_DIVISION) ||
                        League.equals(CHAMPIONS_LEAGUE)) {
                    match_id = match_data.getJSONObject(LINKS).getJSONObject(SELF).
                            getString("href");
                    match_id = match_id.replace(MATCH_LINK, "");
                    if (!isReal) {
                        //This if statement changes the match ID of the dummy data so that it all goes into the database
                        match_id = match_id + Integer.toString(i);
                    }

                    mDate = match_data.getString(MATCH_DATE);
                    mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"));
                    mDate = mDate.substring(0, mDate.indexOf("T"));
                    SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                    match_date.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        Date parseddate = match_date.parse(mDate + mTime);
                        SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                        new_date.setTimeZone(TimeZone.getDefault());
                        mDate = new_date.format(parseddate);
                        mTime = mDate.substring(mDate.indexOf(":") + 1);
                        mDate = mDate.substring(0, mDate.indexOf(":"));

                        if (!isReal) {
                            //This if statement changes the dummy data's date to match our current date range.
                            Date fragmentdate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
                            SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                            mDate = mformat.format(fragmentdate);
                        }
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "error here!");
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    Home = match_data.getString(HOME_TEAM);
                    Away = match_data.getString(AWAY_TEAM);
                    Home_goals = match_data.getJSONObject(RESULT).getString(HOME_GOALS);
                    Away_goals = match_data.getJSONObject(RESULT).getString(AWAY_GOALS);
                    match_day = match_data.getString(MATCH_DAY);
                    ContentValues match_values = new ContentValues();
                    match_values.put(DatabaseContract.FixtureEntry.MATCH_ID, match_id);
                    match_values.put(DatabaseContract.FixtureEntry.DATE_COL, mDate);
                    match_values.put(DatabaseContract.FixtureEntry.TIME_COL, mTime);

                    match_values.put(DatabaseContract.FixtureEntry.HOME_COL, Home);
                    Home = match_data.getJSONObject(LINKS).getJSONObject(HOME_TEAM_LINK).
                            getString("href");
                    Home = Home.replace(TEAM_LINK, "");
                    checkTeamData(Home);

                    match_values.put(DatabaseContract.FixtureEntry.AWAY_COL, Away);
                    Away = match_data.getJSONObject(LINKS).getJSONObject(AWAY_TEAM_LINK).
                            getString("href");
                    Away = Away.replace(TEAM_LINK, "");
                    checkTeamData(Away);

                    match_values.put(DatabaseContract.FixtureEntry.HOME_GOALS_COL, Home_goals);
                    match_values.put(DatabaseContract.FixtureEntry.AWAY_GOALS_COL, Away_goals);

                    match_values.put(DatabaseContract.FixtureEntry.LEAGUE_COL, League);
                    checkLeagueData(League);

                    match_values.put(DatabaseContract.FixtureEntry.MATCH_DAY, match_day);

                    values.add(match_values);
                }
            }

            if (values.size() > 0) {
                int inserted_data = 0;
                ContentValues[] insert_data = new ContentValues[values.size()];
                values.toArray(insert_data);
                inserted_data = mContext.getContentResolver().bulkInsert(
                        DatabaseContract.FixtureEntry.CONTENT_URI, insert_data);

                if (inserted_data > 0) {
                    Intent intent = new Intent(DetailWidgetProvider.ACTION_DATA_UPDATED);
                    mContext.sendBroadcast(intent);
                }
            } else {
                Log.e(LOG_TAG, "No fixtures fetched");
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void checkTeamData(String teamId) {

        Log.d(LOG_TAG, ":::::::::::::::::::::::::::::::::: myFetchService:checkTeamData checking team: " + teamId);

        if (teamId == null || teamId.isEmpty())
            return;

        Cursor cursor = mContext.getContentResolver().query(
                DatabaseContract.TeamEntry.buildTeamWithId(Long.valueOf(teamId)),
                TEAM_COLUMNS,
                null,
                null,
                null);

        String test = cursor.getCount() == 0 ? "DOES NOT EXIST" : "EXIST";
        Log.d(LOG_TAG, ":::::::::::::::::::::::::::::::::: myFetchService:checkTeamData team: " + teamId + test);

        if (cursor.getCount() == 0)
            obtainTeamData(teamId);
    }

    private void obtainTeamData(String teamId) {
        //Creating fetch URL
        final String BASE_URL = "http://api.football-data.org/alpha/teams"; //Base URL

        Uri fetch_build = Uri.parse(BASE_URL)
                .buildUpon()
                .appendPath(teamId)
                .build();

        HttpURLConnection m_connection = null;
        BufferedReader reader = null;
        String JSON_data = null;
        //Opening Connection
        try {
            URL fetch = new URL(fetch_build.toString());
            m_connection = (HttpURLConnection) fetch.openConnection();
            m_connection.setRequestMethod("GET");
            m_connection.addRequestProperty("X-Auth-Token", getString(R.string.api_key));
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
                return;
            }
            JSON_data = buffer.toString();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception here" + e.getMessage());
        } finally {
            if (m_connection != null) {
                m_connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error Closing Stream");
                }
            }
        }
        try {

            Log.d(LOG_TAG, "++++++++++++++++++++++++++++ JSON returned: \n" + JSON_data);

            if (JSON_data != null) {
                //This bit is to check if the data contains any matches. If not, we call processJson on the dummy data
                JSONArray matches = new JSONObject(JSON_data).getJSONArray("fixtures");

                if (matches.length() != 0)
                    processTeamJSONdata(JSON_data, true);

            } else {
                //Could not Connect
                Log.d(LOG_TAG, "Could not connect to server.");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void processTeamJSONdata(String JSONdata, boolean isReal) {
        //JSON data
        // This set of league codes is for the 2015/2016 season. In fall of 2016, they will need to
        // be updated. Feel free to use the codes

        final String TEAM_URL = "http://api.football-data.org/alpha/teams/"; //Base URL
        final String NAME = "name";
        final String ABBREVIATION = "code";
        final String CREST = "crestUrl";
        final String LINKS = "_links";
        final String SELF = "self";

        String teamId = null;
        String name = null;
        String abbreviation = null;
        String crest = null;

        try {
            JSONObject team = new JSONObject(JSONdata);

            //ContentValues to be inserted
            teamId = team.getJSONObject(LINKS).getJSONObject(SELF).
                    getString("href");
            teamId = teamId.replace(TEAM_URL, "");
            name = team.getString(NAME);
            abbreviation = team.getString(ABBREVIATION);
            crest = team.getString(CREST);

            Log.d(LOG_TAG, "::::::::::::::::::::::::TEAM VALUES: "
                    + "\nteamId:" + teamId
                    + "\nname:" + name
                    + "\nabbreviation:" + abbreviation
                    + "\ncrest:" + crest);

            ContentValues teamData = new ContentValues();
            teamData.put(DatabaseContract.TeamEntry.TEAM_ID, teamId);
            teamData.put(DatabaseContract.TeamEntry.NAME_COL, name);
            teamData.put(DatabaseContract.TeamEntry.ABBREVIATION_COL, abbreviation);
            teamData.put(DatabaseContract.TeamEntry.CREST_URL_COL, crest);

            Uri uri = mContext.getContentResolver().insert(
                    DatabaseContract.FixtureEntry.CONTENT_URI, teamData);

            Log.d(LOG_TAG, "::::::::::::::::::::::::::: Team inserted " + uri.toString());

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

    }

    private void checkLeagueData(String leagueId) {
        return;
    }
}

