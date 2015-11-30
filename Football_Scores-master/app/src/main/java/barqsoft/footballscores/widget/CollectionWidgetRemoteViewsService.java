package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by Boram on 2015-11-10.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CollectionWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = CollectionWidgetRemoteViewsService.class.getSimpleName();
    private static final String[] SOCCER_COLUMNS = {
            DatabaseContract.SCORES_TABLE + "." + DatabaseContract.scores_table._ID,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.MATCH_DAY,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL
    };

    static final int INDEX_SOCRE_ID = 0;
    static final int INDEX_DATE = 1;
    static final int INDEX_HOME = 2;
    static final int INDEX_AWAY = 3;
    static final int INDEX_MATCH_DAY = 4;
    static final int INDEX_TIME = 5;
    static final int INDEX_MATCH_ID = 6;
    static final int INDEX_HOME_GOALS = 7;
    static final int INDEX_AWAY_GOALS = 8;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if(data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                Uri scoreForDateUri = DatabaseContract.scores_table.buildScoreWithDate();
                data = getContentResolver().query(scoreForDateUri,
                        SOCCER_COLUMNS,
                        null,
                        null,
                        DatabaseContract.scores_table.DATE_COL + " ASC");

                while(data.moveToNext()) {
                    Log.i("Boram", "Date: " + data.getString(INDEX_DATE));
                    Log.i("Boram", "Home: " + data.getString(INDEX_HOME));
                    Log.i("Boram", "Away: " + data.getString(INDEX_AWAY));
                    Log.i("Boram", "Match Day: " + data.getString(INDEX_MATCH_DAY));
                }
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if(data != null) {
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
                if(position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                Log.i("Boram", "getViewAt: " + position);

                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.scores_list_item);
                double matchId = data.getDouble(INDEX_MATCH_ID);
                String home = data.getString(INDEX_HOME);
                String away = data.getString(INDEX_AWAY);
                String date = data.getString(INDEX_MATCH_DAY);
                String scores = Utilies.getScores(data.getInt(INDEX_HOME_GOALS),
                        data.getInt(INDEX_AWAY_GOALS));
                int home_crest = Utilies.getTeamCrestByTeamName(
                        data.getString(INDEX_HOME));
                int away_crest = Utilies.getTeamCrestByTeamName(
                        data.getString(INDEX_AWAY));

                views.setImageViewResource(R.id.home_crest, home_crest);
                views.setTextViewText(R.id.home_name, home);
                views.setTextViewText(R.id.score_textview, scores);
                views.setTextViewText(R.id.data_textview, date);
                views.setImageViewResource(R.id.away_crest, away_crest);
                views.setTextViewText(R.id.away_name, away);


                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.scores_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if(data.moveToPosition(position))
                    return data.getLong(INDEX_MATCH_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
