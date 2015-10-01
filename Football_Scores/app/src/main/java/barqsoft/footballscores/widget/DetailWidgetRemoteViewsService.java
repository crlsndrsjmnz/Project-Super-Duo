package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import barqsoft.footballscores.R;
import barqsoft.footballscores.Utility;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * Created by Carlos on 9/22/2015.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    private static final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new DetailWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class DetailWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    public static final double INDEX_DETAIL_MATCH_ID = 0;
    public static final int INDEX_DATE = 1;
    public static final int INDEX_MATCH_TIME = 2;
    public static final int INDEX_HOME = 3;
    public static final int INDEX_AWAY = 4;
    public static final int INDEX_LEAGUE = 5;
    public static final int INDEX_HOME_GOALS = 6;
    public static final int INDEX_AWAY_GOALS = 7;
    public static final int INDEX_ID = 8;
    public static final int INDEX_MATCHDAY = 9;
    public static final int INDEX_HOME_NAME = 10;
    public static final int INDEX_HOME_ABB = 11;
    public static final int INDEX_HOME_CREST = 12;
    public static final int INDEX_AWAY_NAME = 13;
    public static final int INDEX_AWAY_ABB = 14;
    public static final int INDEX_AWAY_CREST = 15;
    private static final String[] SCORES_COLUMNS = {
            DatabaseContract.FixtureEntry.TABLE_NAME + "." + DatabaseContract.FixtureEntry._ID,
            DatabaseContract.FixtureEntry.DATE_COL,
            DatabaseContract.FixtureEntry.TIME_COL,
            DatabaseContract.FixtureEntry.HOME_COL,
            DatabaseContract.FixtureEntry.AWAY_COL,
            DatabaseContract.FixtureEntry.LEAGUE_COL,
            DatabaseContract.FixtureEntry.HOME_GOALS_COL,
            DatabaseContract.FixtureEntry.AWAY_GOALS_COL,
            DatabaseContract.FixtureEntry.MATCH_ID,
            DatabaseContract.FixtureEntry.MATCH_DAY,
            "T1." + DatabaseContract.TeamEntry.NAME_COL,
            "T1." + DatabaseContract.TeamEntry.ABBREVIATION_COL,
            "T1." + DatabaseContract.TeamEntry.CREST_URL_COL,
            "T2." + DatabaseContract.TeamEntry.NAME_COL,
            "T2." + DatabaseContract.TeamEntry.ABBREVIATION_COL,
            "T2." + DatabaseContract.TeamEntry.CREST_URL_COL

    };
    private static final String LOG_TAG = DetailWidgetRemoteViewsFactory.class.getSimpleName();
    private Context mContext;
    private int mAppWidgetId;
    private Cursor mCursor;

    public DetailWidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {

        if (mCursor != null) {
            mCursor.close();
        }

        Uri scoreWithDateUri = DatabaseContract.FixtureEntry.buildFixtureWithDate();

        //Date currentDate = new Date(System.currentTimeMillis());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -2);

        SimpleDateFormat formattedDate = new SimpleDateFormat("yyyy-MM-dd");

        final long token = Binder.clearCallingIdentity();
        try {
            mCursor = mContext.getContentResolver().query(
                    scoreWithDateUri,
                    SCORES_COLUMNS,
                    null,
                    new String[]{formattedDate.format(cal.getTime())},
                    null);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }

    @Override
    public int getCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public RemoteViews getViewAt(int position) {

        if (position == AdapterView.INVALID_POSITION ||
                mCursor == null || !mCursor.moveToPosition(position)) {
            return null;
        }

        String homeTeam, awayTeam, time, homeGoals, awayGoals;
        double matchId;
        int homeIcon, awayIcon;

        time = mCursor.getString(INDEX_MATCH_TIME);
        homeGoals = mCursor.getInt(INDEX_HOME_GOALS) >= 0 ? String.valueOf(mCursor.getInt(INDEX_HOME_GOALS)) : "";
        awayGoals = mCursor.getInt(INDEX_AWAY_GOALS) >= 0 ? String.valueOf(mCursor.getInt(INDEX_AWAY_GOALS)) : "";
        matchId = mCursor.getDouble(INDEX_ID);

        // Get the layout for the App Widget and attach an on-click listener
        // to the button
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_score_list);

        views.setOnClickFillInIntent(R.id.widget_frame, new Intent());

        String homeTeamName = mCursor.getString(INDEX_HOME_NAME);
        String homeCrestUrl = mCursor.getString(INDEX_HOME_CREST);
        String awayTeamName = mCursor.getString(INDEX_AWAY_NAME);
        String awayCrestUrl = mCursor.getString(INDEX_AWAY_CREST);

        setTeamCrest(views, R.id.widget_home_icon, homeTeamName, homeCrestUrl);
        setTeamCrest(views, R.id.widget_away_icon, awayTeamName, awayCrestUrl);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            setRemoteContentDescription(views, R.id.widget_home_icon, homeTeamName);
            setRemoteContentDescription(views, R.id.widget_away_icon, awayTeamName);
        }

        views.setTextViewText(R.id.widget_home_name, mCursor.getString(INDEX_HOME_ABB));
        views.setTextViewText(R.id.widget_away_name, mCursor.getString(INDEX_AWAY_ABB));
        views.setTextViewText(R.id.widget_home_goals, homeGoals);
        views.setTextViewText(R.id.widget_away_goals, awayGoals);
        views.setTextViewText(R.id.widget_match_time, time);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (mCursor.moveToPosition(position))
            return mCursor.getLong(INDEX_ID);
        return position;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, int viewId, String description) {
        views.setContentDescription(viewId, description);
    }

    private void setTeamCrest(RemoteViews views, int viewId, String teamName, String crestUrl) {
        Bitmap crestBitmap = null;

        try {
            if (crestUrl.endsWith(".svg")) {

                // TODO: How to convert URL svg files to Bitmap?
                // http://stackoverflow.com/questions/32889269/getting-a-bitmap-from-a-svg-url-resource

                views.setImageViewResource(viewId, Utility.getTeamCrestByTeamName(teamName));

            } else {

                crestBitmap = Glide.with(mContext)
                        .load(crestUrl)
                        .asBitmap()
                        .error(R.drawable.ic_launcher)
                        .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get();

                views.setImageViewBitmap(viewId, crestBitmap);
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error retrieving large icon from " + crestUrl, e);
        }
    }
}

