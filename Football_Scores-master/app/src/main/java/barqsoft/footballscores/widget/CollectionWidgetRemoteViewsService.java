package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;

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
            DatabaseContract.scores_table.MATCH_ID
    };

    static final int INDEX_SOCRE_ID = 0;
    static final int INDEX_DATE = 1;
    static final int INDEX_HOME = 2;
    static final int INDEX_AWAY = 3;
    static final int INDEX_MATCH_DAY = 4;
    static final int INDEX_TIME = 5;
    static final int INDEX_MATCH_ID = 6;

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
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_collection_list_item);
                int matchId = data.getInt(INDEX_MATCH_ID);
                String home = data.getString(INDEX_HOME);
                String away = data.getString(INDEX_AWAY);


                return null;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 0;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }
        };
    }
}
