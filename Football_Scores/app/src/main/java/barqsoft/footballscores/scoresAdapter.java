package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.caverock.androidsvg.SVG;

import java.io.InputStream;

import barqsoft.footballscores.svg.SvgDecoder;
import barqsoft.footballscores.svg.SvgDrawableTranscoder;
import barqsoft.footballscores.svg.SvgSoftwareLayerSetter;

/**
 * Created by yehya khaled on 2/26/2015.
 */
public class scoresAdapter extends CursorAdapter {

    public static final String LOG_TAG = scoresAdapter.class.getSimpleName();

    public static final int COL_DATE = 1;
    public static final int COL_MATCHTIME = 2;
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_LEAGUE = 5;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_ID = 8;
    public static final int COL_MATCHDAY = 9;
    public static final int COL_HOME_TEAM_ID = 11;
    public static final int COL_HOME_TEAM_NAME = 12;
    public static final int COL_HOME_TEAM_ABBREVIATION = 13;
    public static final int COL_HOME_TEAM_CREST = 14;
    public static final int COL_AWAY_TEAM_ID = 16;
    public static final int COL_AWAY_TEAM_NAME = 17;
    public static final int COL_AWAY_TEAM_ABBREVIATION = 18;
    public static final int COL_AWAY_TEAM_CREST = 19;

    public double detail_match_id = 0;
    private String FOOTBALL_SCORES_HASHTAG = "#Football_Scores";

    public scoresAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View mItem = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder mHolder = new ViewHolder(mItem);
        mItem.setTag(mHolder);
        return mItem;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final ViewHolder mHolder = (ViewHolder) view.getTag();

        String columns = "";
        for (String colName : cursor.getColumnNames()) {
            columns = columns + colName + ", ";
        }

        String values = "";
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            values = values + cursor.getString(i) + ", ";
        }

        mHolder.home_name.setText(cursor.getString(COL_HOME_TEAM_NAME));
        mHolder.away_name.setText(cursor.getString(COL_AWAY_TEAM_NAME));
        mHolder.date.setText(cursor.getString(COL_MATCHTIME));
        mHolder.score.setText(Utility.getScores(cursor.getInt(COL_HOME_GOALS), cursor.getInt(COL_AWAY_GOALS)));
        mHolder.match_id = cursor.getDouble(COL_ID);

        showImage(mHolder.home_crest, cursor.getString(COL_HOME_TEAM_CREST));
        showImage(mHolder.away_crest, cursor.getString(COL_AWAY_TEAM_CREST));

        /*
        mHolder.home_crest.setImageResource(Utility.getTeamCrestByTeamName(
                cursor.getString(COL_HOME)));
        mHolder.away_crest.setImageResource(Utility.getTeamCrestByTeamName(
                cursor.getString(COL_AWAY)));
        */

        LayoutInflater vi = (LayoutInflater) context.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.detail_fragment, null);
        ViewGroup container = (ViewGroup) view.findViewById(R.id.details_fragment_container);

        if (mHolder.match_id == detail_match_id) {
            container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT));
            TextView match_day = (TextView) v.findViewById(R.id.matchday_textview);
            match_day.setText(Utility.getMatchDay(context, cursor.getInt(COL_MATCHDAY),
                    cursor.getInt(COL_LEAGUE)));
            TextView league = (TextView) v.findViewById(R.id.league_textview);
            league.setText(Utility.getLeague(context, cursor.getInt(COL_LEAGUE)));
            Button share_button = (Button) v.findViewById(R.id.share_button);
            share_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //add Share Action
                    context.startActivity(createShareForecastIntent(mHolder.home_name.getText() + " " +
                            mHolder.score.getText() + " " + mHolder.away_name.getText() + " "));
                }
            });
        } else {
            container.removeAllViews();
        }

    }

    public void showImage(ImageView view, String crestUrl) {

        if (crestUrl.endsWith(".svg")) {
            GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder;

            requestBuilder = Glide.with(mContext)
                    .using(Glide.buildStreamModelLoader(Uri.class, mContext), InputStream.class)
                    .from(Uri.class)
                    .as(SVG.class)
                    .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
                    .sourceEncoder(new StreamEncoder())
                    .cacheDecoder(new FileToStreamDecoder<SVG>(new SvgDecoder()))
                    .decoder(new SvgDecoder())
                    .placeholder(R.drawable.ic_launcher)
                    .error(R.drawable.no_icon)
                    .listener(new SvgSoftwareLayerSetter<Uri>());

            requestBuilder
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            // SVG cannot be serialized so it's not worth to cache it
                    .load(Uri.parse(crestUrl))
                    .into(view);

        } else {
            Glide.with(mContext)
                    .load(crestUrl)
                    .error(R.drawable.ic_launcher)
                    .crossFade()
                    .into(view);
        }

    }

    public Intent createShareForecastIntent(String ShareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText + FOOTBALL_SCORES_HASHTAG);
        return shareIntent;
    }

}
