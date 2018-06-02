package onestopsolns.neurowheels.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class MedicineContract {
    private MedicineContract() {}

    public static final String CONTENT_AUTHORITY = "onestopsolns.neurowheels";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_VEHICLE = "medicine-path";

    public static final class MedicineEntry implements BaseColumns{
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VEHICLE);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VEHICLE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VEHICLE;

        public final static String TABLE_NAME = "medicines";
        public final static String DELETE_TRIGGER_NAME = "medicinesdelete";

        public final static String _ID = BaseColumns._ID;

        public static final String KEY_TITLE = "title";

        public static String getColumnString(Cursor cursor, String columnName) {
            return cursor.getString( cursor.getColumnIndex(columnName) );
        }
    }
}
