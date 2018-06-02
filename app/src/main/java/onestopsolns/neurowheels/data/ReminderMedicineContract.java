package onestopsolns.neurowheels.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class ReminderMedicineContract {

    ReminderMedicineContract(){}

    public static final String CONTENT_AUTHORITY = "onestopsolns.neurowheels";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_VEHICLE = "reminder-medicine-path";
    public static final String PATH_VEHICLE_NULL = "reminder-medicine-path/null";
    public static final String PATH_VEHICLE_JOIN = "reminder-medicine-path/join";
    public static final String PATH_VEHICLE_JOIN_NULL = "reminder-medicine-path/joinnull";
    public static final String PATH_VEHICLE_DELETE_NULL = "reminder-medicine-path/deletenull";
    public static final String PATH_VEHICLE_DELETE_FK_MED_ID = "reminder-medicine-path/deletefkmedid";
    public static final String PATH_VEHICLE_DELETE_FK_REM_ID = "reminder-medicine-path/deletefkremid";

    public static final class ReminderMedicineEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VEHICLE);
        public static final Uri CONTENT_URI_NULL_ID = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VEHICLE_NULL);
        public static final Uri CONTENT_URI_JOIN = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VEHICLE_JOIN);
        public static final Uri CONTENT_URI_JOIN_NULL = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VEHICLE_JOIN_NULL);
        public static final Uri CONTENT_URI_DELETE_NULL = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VEHICLE_DELETE_NULL);
        public static final Uri CONTENT_URI_DELETE_FK_MED_ID = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VEHICLE_DELETE_FK_MED_ID);
        public static final Uri CONTENT_URI_DELETE_FK_REMID = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VEHICLE_DELETE_FK_REM_ID);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VEHICLE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VEHICLE;

        public final static String TABLE_NAME = "remindermedicines";

        public final static String _ID = BaseColumns._ID;

        public static final String KEY_FK_REMINDER_ID = "fk_reminder_id";
        public static final String KEY_FK_MEDICINE_ID = "fk_medicine_id";
        public static final String KEY_QUANTITY = "quantity";

        public static String getColumnString(Cursor cursor, String columnName) {
            return cursor.getString( cursor.getColumnIndex(columnName) );
        }
    }

}
