package onestopsolns.neurowheels.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import onestopsolns.neurowheels.model.MedicineModel;

/**
 * Created by Adnan on 09-05-2018.
 */

public class AlarmReminderProvider extends ContentProvider {

    public static final String LOG_TAG = AlarmReminderProvider.class.getSimpleName();

    private static final int REMINDER = 100;

    private static final int REMINDER_ID = 101;

    private static final int MEDICINE = 102;
    private static final int MEDICINE_ID = 103;

    private static final int REMINDERMEDICINE = 104;
    private static final int REMINDERMEDICINE_ID = 105;
    private static final int REMINDERMEDICINEFKREM_ID = 106;
    private static final int REMINDERMEDICINEJOIN = 107;
    private static final int REMINDERMEDICINEJOINNULL = 108;
    private static final int REMINDERMEDICINEDELETENULL = 109;
    private static final int REMINDERMEDICINEDELETEFKMEDID = 110;
    private static final int REMINDERMEDICINEDELETEFKREMID = 111;


    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        sUriMatcher.addURI(AlarmReminderContract.CONTENT_AUTHORITY, AlarmReminderContract.PATH_VEHICLE, REMINDER);

        sUriMatcher.addURI(AlarmReminderContract.CONTENT_AUTHORITY, AlarmReminderContract.PATH_VEHICLE + "/#", REMINDER_ID);

        sUriMatcher.addURI(MedicineContract.CONTENT_AUTHORITY, MedicineContract.PATH_VEHICLE, MEDICINE);

        sUriMatcher.addURI(MedicineContract.CONTENT_AUTHORITY, MedicineContract.PATH_VEHICLE + "/#", MEDICINE_ID);

       sUriMatcher.addURI(ReminderMedicineContract.CONTENT_AUTHORITY, ReminderMedicineContract.PATH_VEHICLE, REMINDERMEDICINE);

        sUriMatcher.addURI(ReminderMedicineContract.CONTENT_AUTHORITY, ReminderMedicineContract.PATH_VEHICLE + "/#", REMINDERMEDICINE_ID);

        sUriMatcher.addURI(ReminderMedicineContract.CONTENT_AUTHORITY, ReminderMedicineContract.PATH_VEHICLE + "/null", REMINDERMEDICINEFKREM_ID);
        sUriMatcher.addURI(ReminderMedicineContract.CONTENT_AUTHORITY, ReminderMedicineContract.PATH_VEHICLE + "/join/#", REMINDERMEDICINEJOIN);
        sUriMatcher.addURI(ReminderMedicineContract.CONTENT_AUTHORITY, ReminderMedicineContract.PATH_VEHICLE + "/joinnull", REMINDERMEDICINEJOINNULL);
        sUriMatcher.addURI(ReminderMedicineContract.CONTENT_AUTHORITY, ReminderMedicineContract.PATH_VEHICLE + "/deletenull", REMINDERMEDICINEDELETENULL);
        sUriMatcher.addURI(ReminderMedicineContract.CONTENT_AUTHORITY, ReminderMedicineContract.PATH_VEHICLE + "/deletefkmedid/#", REMINDERMEDICINEDELETEFKMEDID);
        sUriMatcher.addURI(ReminderMedicineContract.CONTENT_AUTHORITY, ReminderMedicineContract.PATH_VEHICLE + "/deletefkremid/#", REMINDERMEDICINEDELETEFKREMID);

    }

    private AlarmReminderDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new AlarmReminderDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor = null;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case REMINDER:
                cursor = database.query(AlarmReminderContract.AlarmReminderEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case REMINDER_ID:
                selection = AlarmReminderContract.AlarmReminderEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(AlarmReminderContract.AlarmReminderEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case MEDICINE:
                cursor = database.query(MedicineContract.MedicineEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case MEDICINE_ID:
                selection = MedicineContract.MedicineEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(MedicineContract.MedicineEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case REMINDERMEDICINE:
                cursor = database.query(ReminderMedicineContract.ReminderMedicineEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case REMINDERMEDICINE_ID:
                selection = ReminderMedicineContract.ReminderMedicineEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(ReminderMedicineContract.ReminderMedicineEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case REMINDERMEDICINEJOIN:
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                String joinQuery = "SELECT "
                        + ReminderMedicineContract.ReminderMedicineEntry.TABLE_NAME + "." + ReminderMedicineContract.ReminderMedicineEntry._ID + ","
                        + ReminderMedicineContract.ReminderMedicineEntry.TABLE_NAME + "." + ReminderMedicineContract.ReminderMedicineEntry.KEY_QUANTITY + ","
                        + MedicineContract.MedicineEntry.TABLE_NAME + "." + MedicineContract.MedicineEntry.KEY_TITLE + ","
                        + ReminderMedicineContract.ReminderMedicineEntry.TABLE_NAME + "." + ReminderMedicineContract.ReminderMedicineEntry.KEY_FK_MEDICINE_ID
                        + " FROM "
                        + ReminderMedicineContract.ReminderMedicineEntry.TABLE_NAME
                        + " INNER JOIN "
                        + MedicineContract.MedicineEntry.TABLE_NAME
                        + " ON "
                        + ReminderMedicineContract.ReminderMedicineEntry.TABLE_NAME + "." + ReminderMedicineContract.ReminderMedicineEntry.KEY_FK_MEDICINE_ID
                        + " = "
                        + MedicineContract.MedicineEntry.TABLE_NAME + "." + MedicineContract.MedicineEntry._ID
                        + " WHERE "
                        + ReminderMedicineContract.ReminderMedicineEntry.TABLE_NAME + "." + ReminderMedicineContract.ReminderMedicineEntry.KEY_FK_REMINDER_ID
                        + " = ?";
                cursor = database.rawQuery(joinQuery,selectionArgs);
                break;

            case REMINDERMEDICINEJOINNULL:
                String joinQuery1 = "SELECT "
                        + ReminderMedicineContract.ReminderMedicineEntry.TABLE_NAME + "." + ReminderMedicineContract.ReminderMedicineEntry._ID + ","
                        + ReminderMedicineContract.ReminderMedicineEntry.TABLE_NAME + "." + ReminderMedicineContract.ReminderMedicineEntry.KEY_QUANTITY + ","
                        + MedicineContract.MedicineEntry.TABLE_NAME + "." + MedicineContract.MedicineEntry.KEY_TITLE + ","
                        + ReminderMedicineContract.ReminderMedicineEntry.TABLE_NAME + "." + ReminderMedicineContract.ReminderMedicineEntry.KEY_FK_MEDICINE_ID
                        + " FROM "
                        + ReminderMedicineContract.ReminderMedicineEntry.TABLE_NAME
                        + " INNER JOIN "
                        + MedicineContract.MedicineEntry.TABLE_NAME
                        + " ON "
                        + ReminderMedicineContract.ReminderMedicineEntry.TABLE_NAME + "." + ReminderMedicineContract.ReminderMedicineEntry.KEY_FK_MEDICINE_ID
                        + " = "
                        + MedicineContract.MedicineEntry.TABLE_NAME + "." + MedicineContract.MedicineEntry._ID
                        + " WHERE "
                        + ReminderMedicineContract.ReminderMedicineEntry.TABLE_NAME + "." + ReminderMedicineContract.ReminderMedicineEntry.KEY_FK_REMINDER_ID
                        + " IS NULL";
                cursor = database.rawQuery(joinQuery1,null);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }


        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case REMINDER:
                return AlarmReminderContract.AlarmReminderEntry.CONTENT_LIST_TYPE;
            case REMINDER_ID:
                return AlarmReminderContract.AlarmReminderEntry.CONTENT_ITEM_TYPE;
            case MEDICINE:
                return MedicineContract.MedicineEntry.CONTENT_LIST_TYPE;
            case MEDICINE_ID:
                return MedicineContract.MedicineEntry.CONTENT_ITEM_TYPE;
            case REMINDERMEDICINE:
                return ReminderMedicineContract.ReminderMedicineEntry.CONTENT_LIST_TYPE;
            case REMINDERMEDICINE_ID:
                return ReminderMedicineContract.ReminderMedicineEntry.CONTENT_ITEM_TYPE;
            case REMINDERMEDICINEFKREM_ID:
                return ReminderMedicineContract.ReminderMedicineEntry.CONTENT_LIST_TYPE;
            case REMINDERMEDICINEJOIN:
                return ReminderMedicineContract.ReminderMedicineEntry.CONTENT_LIST_TYPE;
            case REMINDERMEDICINEJOINNULL:
                return ReminderMedicineContract.ReminderMedicineEntry.CONTENT_LIST_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case REMINDER:
                return insertReminder(uri, contentValues);

            case MEDICINE:
                return insertMedicine(uri, contentValues);

            case REMINDERMEDICINE:
                return insertReminderMedicine(uri, contentValues);

            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertReminderMedicine(Uri uri, ContentValues values) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(ReminderMedicineContract.ReminderMedicineEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertReminder(Uri uri, ContentValues values) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(AlarmReminderContract.AlarmReminderEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertMedicine(Uri uri, ContentValues values) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(MedicineContract.MedicineEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case REMINDER:
                rowsDeleted = database.delete(AlarmReminderContract.AlarmReminderEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REMINDER_ID:
                selection = AlarmReminderContract.AlarmReminderEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(AlarmReminderContract.AlarmReminderEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case MEDICINE:
                rowsDeleted = database.delete(MedicineContract.MedicineEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case MEDICINE_ID:
                selection = MedicineContract.MedicineEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(MedicineContract.MedicineEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case REMINDERMEDICINE:
                rowsDeleted = database.delete(ReminderMedicineContract.ReminderMedicineEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case REMINDERMEDICINE_ID:
                selection = ReminderMedicineContract.ReminderMedicineEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(ReminderMedicineContract.ReminderMedicineEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case REMINDERMEDICINEDELETENULL:
                selection = ReminderMedicineContract.ReminderMedicineEntry.KEY_FK_REMINDER_ID + " IS NULL";
                rowsDeleted = database.delete(ReminderMedicineContract.ReminderMedicineEntry.TABLE_NAME, selection, null);
                break;

            case REMINDERMEDICINEDELETEFKMEDID:
                selection = ReminderMedicineContract.ReminderMedicineEntry.KEY_FK_MEDICINE_ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(ReminderMedicineContract.ReminderMedicineEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case REMINDERMEDICINEDELETEFKREMID:
                selection = ReminderMedicineContract.ReminderMedicineEntry.KEY_FK_REMINDER_ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(ReminderMedicineContract.ReminderMedicineEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case REMINDER:
                return updateReminder(uri, contentValues, selection, selectionArgs);
            case REMINDER_ID:
                selection = AlarmReminderContract.AlarmReminderEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateReminder(uri, contentValues, selection, selectionArgs);

            case MEDICINE:
                return updateMedicine(uri, contentValues, selection, selectionArgs);
            case MEDICINE_ID:
                selection = MedicineContract.MedicineEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateMedicine(uri, contentValues, selection, selectionArgs);

            case REMINDERMEDICINE:
                return updateReminderMedicine(uri, contentValues, selection, selectionArgs);
            case REMINDERMEDICINE_ID:
                selection = ReminderMedicineContract.ReminderMedicineEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateReminderMedicine(uri, contentValues, selection, selectionArgs);
            case REMINDERMEDICINEFKREM_ID:
                selection = ReminderMedicineContract.ReminderMedicineEntry.KEY_FK_REMINDER_ID+ " IS NULL";
                return updateReminderMedicine(uri, contentValues, selection, null);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }


    private int updateReminderMedicine(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(ReminderMedicineContract.ReminderMedicineEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    private int updateReminder(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(AlarmReminderContract.AlarmReminderEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    private int updateMedicine(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(MedicineContract.MedicineEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

}