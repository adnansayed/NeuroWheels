package onestopsolns.neurowheels.fragments;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import onestopsolns.neurowheels.R;
import onestopsolns.neurowheels.adapter.ReminderMedicineAdapter;
import onestopsolns.neurowheels.data.AlarmReminderContract;
import onestopsolns.neurowheels.data.MedicineContract;
import onestopsolns.neurowheels.data.ReminderMedicineContract;
import onestopsolns.neurowheels.reminder.AlarmScheduler;

public class AddAlarm extends AppCompatActivity implements
        TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener,
        ReminderMedicineAdapter.ReminderMedicineAdapterListener{

    private static final int EXISTING_VEHICLE_LOADER = 0;
    private static final int REM_MED_LOADER = 202;


    private Toolbar mToolbar;
    private TextView mDateText, mTimeText, mRepeatText, mRepeatNoText, mRepeatTypeText;
    private FloatingActionButton mFAB1;
    private FloatingActionButton mFAB2;
    private FloatingActionButton addMedFAB;
    private ListView medListView;
    private Calendar mCalendar;
    private int mYear, mMonth, mHour, mMinute, mDay;
    private long mRepeatTime;
    private Switch mRepeatSwitch;
    private String mTime;
    private String mDate;
    private String mRepeat;
    private String mRepeatNo;
    private String mRepeatType;
    private String mActive;
    private Set<Integer> ignoreList = new HashSet<Integer>();
    private Uri mCurrentReminderUri;
    private boolean mVehicleHasChanged = false;
    private ReminderMedicineAdapter remMedAdapter;
    private boolean isInitialRemMedLoad = true;

    // Values for orientation change
    private static final String KEY_TITLE = "title_key";
    private static final String KEY_TIME = "time_key";
    private static final String KEY_DATE = "date_key";
    private static final String KEY_REPEAT = "repeat_key";
    private static final String KEY_REPEAT_NO = "repeat_no_key";
    private static final String KEY_REPEAT_TYPE = "repeat_type_key";
    private static final String KEY_ACTIVE = "active_key";


    // Constant values in milliseconds
    private static final long milMinute = 60000L;
    private static final long milHour = 3600000L;
    private static final long milDay = 86400000L;
    private static final long milWeek = 604800000L;
    private static final long milMonth = 2592000000L;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mVehicleHasChanged = true;
            return false;
        }
    };

    private LoaderManager.LoaderCallbacks<Cursor> alarmRemLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            String[] projection = {
                    AlarmReminderContract.AlarmReminderEntry._ID,
                    AlarmReminderContract.AlarmReminderEntry.KEY_TITLE,
                    AlarmReminderContract.AlarmReminderEntry.KEY_DATE,
                    AlarmReminderContract.AlarmReminderEntry.KEY_TIME,
                    AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT,
                    AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_NO,
                    AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE,
                    AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE,
            };

            // This loader will execute the ContentProvider's query method on a background thread
            return new CursorLoader(AddAlarm.this,   // Parent activity context
                    mCurrentReminderUri,         // Query the content URI for the current reminder
                    projection,             // Columns to include in the resulting Cursor
                    null,                   // No selection clause
                    null,                   // No selection arguments
                    null);                  // Default sort order
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            if (cursor == null || cursor.getCount() < 1) {
                return;
            }

            // Proceed with moving to the first row of the cursor and reading data from it
            // (This should be the only row in the cursor)
            if (cursor.moveToFirst()) {
                int titleColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE);
                int dateColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_DATE);
                int timeColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_TIME);
                int repeatColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT);
                int repeatNoColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_NO);
                int repeatTypeColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE);
                int activeColumnIndex = cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE);

                // Extract out the value from the Cursor for the given column index
                String title = cursor.getString(titleColumnIndex);
                String date = cursor.getString(dateColumnIndex);
                String time = cursor.getString(timeColumnIndex);
                String repeat = cursor.getString(repeatColumnIndex);
                String repeatNo = cursor.getString(repeatNoColumnIndex);
                String repeatType = cursor.getString(repeatTypeColumnIndex);
                String active = cursor.getString(activeColumnIndex);



                mDateText.setText(date);
                mTimeText.setText(time);
                mRepeatNoText.setText(repeatNo);
                mRepeatTypeText.setText(repeatType);
                mRepeatText.setText("Every " + repeatNo + " " + repeatType + "(s)");
                // Setup up active buttons
                // Setup repeat switch
                if (repeat.equals("false")) {
                    mRepeatSwitch.setChecked(false);
                    mRepeatText.setText(R.string.repeat_off);

                } else if (repeat.equals("true")) {
                    mRepeatSwitch.setChecked(true);
                }

            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    private LoaderManager.LoaderCallbacks<Cursor> remMedLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            String[] projection = {
                    ReminderMedicineContract.ReminderMedicineEntry._ID,
                    MedicineContract.MedicineEntry.KEY_TITLE,
                    ReminderMedicineContract.ReminderMedicineEntry.KEY_QUANTITY
            };

            // This loader will execute the ContentProvider's query method on a background thread
            if (mCurrentReminderUri == null){
                return new CursorLoader(AddAlarm.this,   // Parent activity context
                        ReminderMedicineContract.ReminderMedicineEntry.CONTENT_URI_JOIN_NULL,
                        projection,             // Columns to include in the resulting Cursor
                        null,                   // No selection clause
                        null,                   // No selection arguments
                        null);                  // Default sort order
            }
            else{
                Uri remRelatedURI = ContentUris.withAppendedId(ReminderMedicineContract.ReminderMedicineEntry.CONTENT_URI_JOIN, (int)ContentUris.parseId(mCurrentReminderUri));
                return new CursorLoader(AddAlarm.this,   // Parent activity context
                        remRelatedURI,
                        projection,             // Columns to include in the resulting Cursor
                        null,                   // No selection clause
                        null,                   // No selection arguments
                        null);                  // Default sort order
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            if(remMedAdapter != null){
                remMedAdapter.swapCursor(cursor);
                int fkMedIDPos = cursor.getColumnIndex(ReminderMedicineContract.ReminderMedicineEntry.KEY_FK_MEDICINE_ID);
                if(isInitialRemMedLoad){
                    isInitialRemMedLoad =false;
                    for (int i= 0; i< remMedAdapter.getCount();i++){
                        if(remMedAdapter.getItem(i) instanceof Cursor){
                            ignoreList.add(((Cursor)remMedAdapter.getItem(i)).getInt(fkMedIDPos));
                        }
                    }
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            if(remMedAdapter != null){
                remMedAdapter.swapCursor(null);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        Intent intent = getIntent();
        mCurrentReminderUri = intent.getData();

        if (mCurrentReminderUri == null) {

            setTitle(getString(R.string.editor_activity_title_new_reminder));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a reminder that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {

            setTitle(getString(R.string.editor_activity_title_edit_reminder));


            getLoaderManager().initLoader(EXISTING_VEHICLE_LOADER, null, alarmRemLoaderCallback);
        }




        // Initialize Views
        mToolbar = (Toolbar) findViewById(R.id.al_toolbar);
        mDateText = (TextView) findViewById(R.id.set_date);
        mTimeText = (TextView) findViewById(R.id.set_time);
        mRepeatText = (TextView) findViewById(R.id.set_repeat);
        mRepeatNoText = (TextView) findViewById(R.id.set_repeat_no);
        mRepeatTypeText = (TextView) findViewById(R.id.set_repeat_type);
        mRepeatSwitch = (Switch) findViewById(R.id.repeat_switch);
        mFAB1 = (FloatingActionButton) findViewById(R.id.starred1);
        mFAB2 = (FloatingActionButton) findViewById(R.id.starred2);
        addMedFAB = (FloatingActionButton) findViewById(R.id.addMedFAB);
        medListView = (ListView) findViewById(R.id.reminderMedList);


        remMedAdapter = new ReminderMedicineAdapter(this,null,this);
        medListView.setAdapter(remMedAdapter);
        getLoaderManager().initLoader(REM_MED_LOADER,null, remMedLoaderCallback);


        // Initialize default values
        mActive = "true";
        mRepeat = "true";
        mRepeatNo = Integer.toString(1);
        mRepeatType = "Hour";

        mCalendar = Calendar.getInstance();
        mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        mMinute = mCalendar.get(Calendar.MINUTE);
        mYear = mCalendar.get(Calendar.YEAR);
        mMonth = mCalendar.get(Calendar.MONTH) + 1;
        mDay = mCalendar.get(Calendar.DATE);

        mDate = mDay + "/" + mMonth + "/" + mYear;
        mTime = mHour + ":" + mMinute;

//        region addMedFAB

        addMedFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(AddAlarm.this);
                View mView = getLayoutInflater().inflate(R.layout.medicine_quantity_dialog,null);
                final TextInputEditText mMedQty = mView.findViewById(R.id.editTextQuantity);
                final Spinner medSpinner = mView.findViewById(R.id.spinnerMedName);
                Button mAdd = mView.findViewById(R.id.medQtyBtnAdd);
                Button mCancel = mView.findViewById(R.id.medQtyBtnCancel);
                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();


                LoaderManager.LoaderCallbacks<Cursor> medSpinnerCursorCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
                    @Override
                    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                        String[] projection = {
                                MedicineContract.MedicineEntry._ID,
                                MedicineContract.MedicineEntry.KEY_TITLE,
                        };


                        StringBuilder selectionArguments = new StringBuilder();
                        for (int element: ignoreList) {
                            if (selectionArguments.toString().isEmpty()){
                                selectionArguments.append(String.valueOf(element));
                            }
                            else{
                                selectionArguments.append(",").append(String.valueOf(element));
                            }
                        }
                        String selection = MedicineContract.MedicineEntry._ID + " NOT IN (" + selectionArguments.toString() + ")";

                        if (ignoreList.isEmpty()) {
                            return new CursorLoader(AddAlarm.this.getBaseContext(), MedicineContract.MedicineEntry.CONTENT_URI, projection, null, null, null);
                        }
                        else{
                            return new CursorLoader(AddAlarm.this.getBaseContext(), MedicineContract.MedicineEntry.CONTENT_URI, projection, selection,null , null);
                        }
                    }

                    @Override
                    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                        SimpleCursorAdapter adapter = new SimpleCursorAdapter(AddAlarm.this.getBaseContext(), android.R.layout.simple_spinner_item, cursor, new String[]{MedicineContract.MedicineEntry.KEY_TITLE},new int[]{android.R.id.text1}, 0);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        medSpinner.setAdapter(adapter);
                    }

                    @Override
                    public void onLoaderReset(Loader<Cursor> loader) {
                        medSpinner.setAdapter(null);
                    }
                };

                final LoaderManager loaderManager = AddAlarm.this.getLoaderManager();
                loaderManager.initLoader(101,null,medSpinnerCursorCallback);

                mAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (TextUtils.isEmpty(mMedQty.getText().toString())) {
                            Toast.makeText(AddAlarm.this.getBaseContext(), "Please Enter The Quantity!", Toast.LENGTH_SHORT).show();
                        }
                        else if (Integer.parseInt(mMedQty.getText().toString()) == 0){
                            Toast.makeText(AddAlarm.this.getBaseContext(), "Please enter a non zero quantity!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            if (medSpinner.getSelectedItem() != null && medSpinner.getSelectedItem() instanceof Cursor){
                                ContentValues values = new ContentValues();
                                values.put(ReminderMedicineContract.ReminderMedicineEntry.KEY_FK_MEDICINE_ID,((Cursor) medSpinner.getSelectedItem()).getInt(0));
                                values.put(ReminderMedicineContract.ReminderMedicineEntry.KEY_QUANTITY, Integer.parseInt(mMedQty.getText().toString()));
                                if (mCurrentReminderUri != null){
                                    values.put(ReminderMedicineContract.ReminderMedicineEntry.KEY_FK_REMINDER_ID,(int)ContentUris.parseId(mCurrentReminderUri));
                                }
                                Uri uri = AddAlarm.this.getContentResolver().insert(ReminderMedicineContract.ReminderMedicineEntry.CONTENT_URI,values);
                                if (uri != null){
                                    ignoreList.add(((Cursor) medSpinner.getSelectedItem()).getInt(0));
                                    loaderManager.destroyLoader(101);
                                    getLoaderManager().destroyLoader(REM_MED_LOADER);
                                    getLoaderManager().initLoader(REM_MED_LOADER,null, remMedLoaderCallback);
                                    dialog.dismiss();
                                }
                            }
                            else{
                                if (medSpinner.getAdapter().getCount() <= 0){
                                    Toast.makeText(AddAlarm.this,"Select an item",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(AddAlarm.this,"No medicines present add some first",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });

                mCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loaderManager.destroyLoader(101);
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

//        endregion

        // Setup TextViews using reminder values
        mDateText.setText(mDate);
        mTimeText.setText(mTime);
        mRepeatNoText.setText(mRepeatNo);
        mRepeatTypeText.setText(mRepeatType);
        mRepeatText.setText("Every " + mRepeatNo + " " + mRepeatType + "(s)");

        // To save state on device rotation
        if (savedInstanceState != null) {
            String savedTime = savedInstanceState.getString(KEY_TIME);
            mTimeText.setText(savedTime);
            mTime = savedTime;

            String savedDate = savedInstanceState.getString(KEY_DATE);
            mDateText.setText(savedDate);
            mDate = savedDate;

            String saveRepeat = savedInstanceState.getString(KEY_REPEAT);
            mRepeatText.setText(saveRepeat);
            mRepeat = saveRepeat;

            String savedRepeatNo = savedInstanceState.getString(KEY_REPEAT_NO);
            mRepeatNoText.setText(savedRepeatNo);
            mRepeatNo = savedRepeatNo;

            String savedRepeatType = savedInstanceState.getString(KEY_REPEAT_TYPE);
            mRepeatTypeText.setText(savedRepeatType);
            mRepeatType = savedRepeatType;

            mActive = savedInstanceState.getString(KEY_ACTIVE);
        }

        // Setup up active buttons
        if (mActive.equals("false")) {
            mFAB1.setVisibility(View.VISIBLE);
            mFAB2.setVisibility(View.GONE);

        } else if (mActive.equals("true")) {
            mFAB1.setVisibility(View.GONE);
            mFAB2.setVisibility(View.VISIBLE);
        }

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.title_activity_add_reminder);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }
    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence(KEY_TIME, mTimeText.getText());
        outState.putCharSequence(KEY_DATE, mDateText.getText());
        outState.putCharSequence(KEY_REPEAT, mRepeatText.getText());
        outState.putCharSequence(KEY_REPEAT_NO, mRepeatNoText.getText());
        outState.putCharSequence(KEY_REPEAT_TYPE, mRepeatTypeText.getText());
        outState.putCharSequence(KEY_ACTIVE, mActive);
    }

    // On clicking Time picker
    public void setTime(View v){
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                false
        );
        tpd.setThemeDark(false);
        tpd.show(getFragmentManager(), "Timepickerdialog");
    }

    // On clicking Date picker
    public void setDate(View v){
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.show(getFragmentManager(), "Datepickerdialog");
    }

    // Obtain time from time picker
    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        mHour = hourOfDay;
        mMinute = minute;
        if (minute < 10) {
            mTime = hourOfDay + ":" + "0" + minute;
        } else {
            mTime = hourOfDay + ":" + minute;
        }
        mTimeText.setText(mTime);
    }

    // Obtain date from date picker
    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        monthOfYear ++;
        mDay = dayOfMonth;
        mMonth = monthOfYear;
        mYear = year;
        mDate = dayOfMonth + "/" + monthOfYear + "/" + year;
        mDateText.setText(mDate);
    }

    // On clicking the active button
    public void selectFab1(View v) {
        mFAB1 = (FloatingActionButton) findViewById(R.id.starred1);
        mFAB1.setVisibility(View.GONE);
        mFAB2 = (FloatingActionButton) findViewById(R.id.starred2);
        mFAB2.setVisibility(View.VISIBLE);
        mActive = "true";
    }

    // On clicking the inactive button
    public void selectFab2(View v) {
        mFAB2 = (FloatingActionButton) findViewById(R.id.starred2);
        mFAB2.setVisibility(View.GONE);
        mFAB1 = (FloatingActionButton) findViewById(R.id.starred1);
        mFAB1.setVisibility(View.VISIBLE);
        mActive = "false";
    }

    // On clicking the repeat switch
    public void onSwitchRepeat(View view) {
        boolean on = ((Switch) view).isChecked();
        if (on) {
            mRepeat = "true";
            mRepeatText.setText("Every " + mRepeatNo + " " + mRepeatType + "(s)");
        } else {
            mRepeat = "false";
            mRepeatText.setText(R.string.repeat_off);
        }
    }

    // On clicking repeat type button
    public void selectRepeatType(View v){
        final String[] items = new String[5];

        items[0] = "Minute";
        items[1] = "Hour";
        items[2] = "Day";
        items[3] = "Week";
        items[4] = "Month";

        // Create List Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Type");
        builder.setItems(items, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {

                mRepeatType = items[item];
                mRepeatTypeText.setText(mRepeatType);
                mRepeatText.setText("Every " + mRepeatNo + " " + mRepeatType + "(s)");
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // On clicking repeat interval button
    public void setRepeatNo(View v){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Enter Number");

        // Create EditText box to input repeat number
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alert.setView(input);
        alert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        if (input.getText().toString().length() == 0) {
                            mRepeatNo = Integer.toString(1);
                            mRepeatNoText.setText(mRepeatNo);
                            mRepeatText.setText("Every " + mRepeatNo + " " + mRepeatType + "(s)");
                        }
                        else {
                            mRepeatNo = input.getText().toString().trim();
                            mRepeatNoText.setText(mRepeatNo);
                            mRepeatText.setText("Every " + mRepeatNo + " " + mRepeatType + "(s)");
                        }
                    }
                });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // do nothing
            }
        });
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_add_reminder, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new reminder, hide the "Delete" menu item.
        if (mCurrentReminderUri == null) {
            MenuItem menuItem = menu.findItem(R.id.discard_reminder);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.save_reminder:

                if(remMedAdapter.getCount() != 0) {
                    saveReminder();
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.discard_reminder:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the reminder hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mVehicleHasChanged) {
                    onBackPressed();
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                onBackPressed();
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the reminder.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the reminder.
                deleteReminder();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the reminder.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteReminder() {
        // Only perform the delete if this is an existing reminder.
        if (mCurrentReminderUri != null) {
            // Call the ContentResolver to delete the reminder at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentreminderUri
            // content URI already identifies the reminder that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentReminderUri, null, null);

            Uri delFKRemID = ContentUris.withAppendedId(ReminderMedicineContract.ReminderMedicineEntry.CONTENT_URI_DELETE_FK_REMID, ContentUris.parseId(mCurrentReminderUri));
            int rowsFKRemIDDeleted= getContentResolver().delete(delFKRemID, null, null);
            Log.d("deleted remID rows", String.valueOf(rowsFKRemIDDeleted));

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_reminder_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_reminder_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    // On clicking the save button
    public void saveReminder(){

     /*   if (mCurrentReminderUri == null ) {
            // Since no fields were modified, we can return early without creating a new reminder.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }
*/
        ContentValues values = new ContentValues();
        SimpleDateFormat dateFormatter = new SimpleDateFormat();
        dateFormatter.applyPattern("dd-MMM-YYYY, HH:ss");

        StringBuilder reminderName = new StringBuilder();
        reminderName.append("Reminder ").append(dateFormatter.format(new Date()));

        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE, reminderName.toString());
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_DATE, mDate);
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_TIME, mTime);
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT, mRepeat);
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_NO, mRepeatNo);
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_REPEAT_TYPE, mRepeatType);
        values.put(AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE, mActive);


        // Set up calender for creating the notification
        mCalendar.set(Calendar.MONTH, --mMonth);
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, mDay);
        mCalendar.set(Calendar.HOUR_OF_DAY, mHour);
        mCalendar.set(Calendar.MINUTE, mMinute);
        mCalendar.set(Calendar.SECOND, 0);

        long selectedTimestamp =  mCalendar.getTimeInMillis();

        // Check repeat type
        if (mRepeatType.equals("Minute")) {
            mRepeatTime = Integer.parseInt(mRepeatNo) * milMinute;
        } else if (mRepeatType.equals("Hour")) {
            mRepeatTime = Integer.parseInt(mRepeatNo) * milHour;
        } else if (mRepeatType.equals("Day")) {
            mRepeatTime = Integer.parseInt(mRepeatNo) * milDay;
        } else if (mRepeatType.equals("Week")) {
            mRepeatTime = Integer.parseInt(mRepeatNo) * milWeek;
        } else if (mRepeatType.equals("Month")) {
            mRepeatTime = Integer.parseInt(mRepeatNo) * milMonth;
        }

        if (mCurrentReminderUri == null) {
            // This is a NEW reminder, so insert a new reminder into the provider,
            // returning the content URI for the new reminder.
            Uri newUri = getContentResolver().insert(AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_reminder_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.

                try {
                    ContentValues values1 = new ContentValues();
                    values1.put(ReminderMedicineContract.ReminderMedicineEntry.KEY_FK_REMINDER_ID, (int) ContentUris.parseId(newUri));
                    int retRows = getContentResolver().update(ReminderMedicineContract.ReminderMedicineEntry.CONTENT_URI_NULL_ID, values1, null, null);

                    Log.d("updates", "remmed rows updated:" + String.valueOf(retRows));
                }
                catch (Exception e1){
                    e1.printStackTrace();
                }

                Toast.makeText(this, getString(R.string.editor_insert_reminder_successful),
                        Toast.LENGTH_SHORT).show();
                mCurrentReminderUri = newUri;
            }
        } else {

            int rowsAffected = getContentResolver().update(mCurrentReminderUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_reminder_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_reminder_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Create a new notification
        if (mActive.equals("true")) {
            if (mRepeat.equals("true")) {
                new AlarmScheduler().setRepeatAlarm(getApplicationContext(), selectedTimestamp, mCurrentReminderUri, mRepeatTime);
            } else if (mRepeat.equals("false")) {
                new AlarmScheduler().setAlarm(getApplicationContext(), selectedTimestamp, mCurrentReminderUri);
            }

            Toast.makeText(this, "Alarm time is " + selectedTimestamp,
                    Toast.LENGTH_LONG).show();
        }

        // Create toast to confirm new reminder
        Toast.makeText(getApplicationContext(), "Saved",
                Toast.LENGTH_SHORT).show();

    }

    // On pressing the back button
    @Override
    public void onBackPressed() {
        if(mCurrentReminderUri == null){
            int rows = getContentResolver().delete(ReminderMedicineContract.ReminderMedicineEntry.CONTENT_URI_DELETE_NULL,null,null);
            Log.d("null rows deleted", String.valueOf(rows));
        }
        super.onBackPressed();
    }

    @Override
    public void onItemClicked(final int remMedID, String medName, int medQty) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(AddAlarm.this);
        View mView = getLayoutInflater().inflate(R.layout.medicine_quantity_dialog,null);
        final TextInputEditText mMedQty = mView.findViewById(R.id.editTextQuantity);
        final Spinner medSpinner = mView.findViewById(R.id.spinnerMedName);
        TextView medNameTextView = mView.findViewById(R.id.textViewMedName);
        Button mAdd = mView.findViewById(R.id.medQtyBtnAdd);
        Button mCancel = mView.findViewById(R.id.medQtyBtnCancel);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();

        medSpinner.setVisibility(View.GONE);
        medNameTextView.setVisibility(View.VISIBLE);

        medNameTextView.setText(medName);
        mMedQty.setText(String.valueOf(medQty));

        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mMedQty.getText().toString())) {
                    Toast.makeText(AddAlarm.this.getBaseContext(), "Please Enter The Quantity!", Toast.LENGTH_SHORT).show();
                }
                else if (Integer.parseInt(mMedQty.getText().toString()) == 0){
                    Toast.makeText(AddAlarm.this.getBaseContext(), "Please enter a non zero quantity!", Toast.LENGTH_SHORT).show();
                }
                else {
                    ContentValues values = new ContentValues();
                    values.put(ReminderMedicineContract.ReminderMedicineEntry.KEY_QUANTITY, Integer.parseInt(mMedQty.getText().toString()));
                    int rowsUpdated = getContentResolver().update(ContentUris.withAppendedId(ReminderMedicineContract.ReminderMedicineEntry.CONTENT_URI,remMedID),values,null,null);
                    Log.d("RemMedRows updated", String.valueOf(rowsUpdated));
                    getLoaderManager().destroyLoader(REM_MED_LOADER);
                    getLoaderManager().initLoader(REM_MED_LOADER,null, remMedLoaderCallback);
                    dialog.dismiss();
                }
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onItemDeleteTapped(final int remMedID, int remFKMedID) {
        int rowsDeleted = getContentResolver().delete(ContentUris.withAppendedId(ReminderMedicineContract.ReminderMedicineEntry.CONTENT_URI,remMedID),null, null);
        Log.d("RemMedRows deleted", String.valueOf(rowsDeleted));
        for( Object i : ignoreList){
            if (i instanceof  Integer){
                if (((int)i) == remFKMedID){
                    ignoreList.remove(i);
                    break;
                }
            }
        }
        getLoaderManager().destroyLoader(REM_MED_LOADER);
        getLoaderManager().initLoader(REM_MED_LOADER,null, remMedLoaderCallback);
    }
}
