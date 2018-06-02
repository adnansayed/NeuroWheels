package onestopsolns.neurowheels.fragments;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import onestopsolns.neurowheels.R;
import onestopsolns.neurowheels.adapter.MedicineAdapter;
import onestopsolns.neurowheels.data.AlarmReminderDbHelper;
import onestopsolns.neurowheels.data.MedicineContract;
import onestopsolns.neurowheels.data.ReminderMedicineContract;


public class Medicine extends Fragment implements MedicineAdapter.MedicineAdapterListener, LoaderManager.LoaderCallbacks<Cursor> {
    private FloatingActionButton mInsertMed;
    private View rootView;
    private String mMedicine;
    private MedicineAdapter adapter;
    private ListView listMedicine;
    AlarmReminderDbHelper alarmReminderDbHelper = new AlarmReminderDbHelper(getActivity());

    private static final int VEHICLE_LOADER = 0;

    public Medicine() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_medicine, container, false);
        mInsertMed = rootView.findViewById(R.id.addmedbutton);
        listMedicine = rootView.findViewById(R.id.medicinelist);

        LoaderManager loaderManager=getLoaderManager();
        adapter = new MedicineAdapter(getContext(),null, this);
        listMedicine.setAdapter(adapter);

        mInsertMed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
                View mView = getLayoutInflater().inflate(R.layout.dialog_medicine, null);
                final EditText mMedName = mView.findViewById(R.id.medname);
                Button mAdd = mView.findViewById(R.id.insertmed);
                Button mCancel = mView.findViewById(R.id.cancelinsert);
                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                mAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (TextUtils.isEmpty(mMedName.getText().toString())) {
                            Toast.makeText(getContext(), "Please Enter The Medicine Name!", Toast.LENGTH_LONG).show();
                        } else {
                            mMedicine = mMedName.getText().toString();
                            ContentValues values = new ContentValues();
                            values.put(MedicineContract.MedicineEntry.KEY_TITLE, mMedicine);
                            Uri newUri = Medicine.this.getActivity().getContentResolver().insert(MedicineContract.MedicineEntry.CONTENT_URI, values);

                            // Show a toast message depending on whether or not the insertion was successful.
                            if (newUri == null) {
                                // If the new content URI is null, then there was an error with insertion.
                                Toast.makeText(Medicine.this.getContext(), "Error in adding medicine",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Otherwise, the insertion was successful and we can display a toast.
                                Toast.makeText(Medicine.this.getContext(), "Medicine added",
                                        Toast.LENGTH_SHORT).show();
                            }

                            adapter.notifyDataSetChanged();

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

            }
        });

        loaderManager.initLoader(VEHICLE_LOADER,null,this);

        return rootView;
    }

    @Override
    public void onUpdateTapped(final int medID, String medName) {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this.getContext());

        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        View view = inflater.inflate(R.layout.update_med_dialog, null);
        builder.setView(view);


        final EditText editTextName = view.findViewById(R.id.updatemedname);

        editTextName.setText(medName);

        final android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();

        view.findViewById(R.id.updatemed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();

                if (name.isEmpty()) {
                    editTextName.setError("Name can't be blank");
                    editTextName.requestFocus();
                    return;
                }

                ContentValues values = new ContentValues();
                values.put(MedicineContract.MedicineEntry.KEY_TITLE, name);

                Uri uri = ContentUris.withAppendedId(MedicineContract.MedicineEntry.CONTENT_URI, medID);

                int rowsAffected = Medicine.this.getActivity().getContentResolver().update(uri, values, null, null);

                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(Medicine.this.getContext(), "Update Failed",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(Medicine.this.getContext(), "Update Successfull",
                            Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDeleteTapped(final int medID) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this.getContext());
        builder.setTitle("Are you sure?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = ContentUris.withAppendedId(MedicineContract.MedicineEntry.CONTENT_URI, medID);
                int rowsDeleted = Medicine.this.getActivity().getContentResolver().delete(uri, null, null);

                Uri delFKMedID = ContentUris.withAppendedId(ReminderMedicineContract.ReminderMedicineEntry.CONTENT_URI_DELETE_FK_REMID, medID);
                int rowsFKMedIDDeleted= Medicine.this.getActivity().getContentResolver().delete(delFKMedID, null, null);
                Log.d("deleted remID rows", String.valueOf(rowsFKMedIDDeleted));

                if (rowsDeleted == 0) {
                    // If no rows were deleted, then there was an error with the delete.
                    Toast.makeText(Medicine.this.getContext(), "Delete Failed",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the delete was successful and we can display a toast.
                    Toast.makeText(Medicine.this.getContext(), "Deleted successfully",
                            Toast.LENGTH_SHORT).show();
                }

                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                MedicineContract.MedicineEntry._ID,
                MedicineContract.MedicineEntry.KEY_TITLE,
        };

        return new CursorLoader(getActivity(),   // Parent activity context
                MedicineContract.MedicineEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
