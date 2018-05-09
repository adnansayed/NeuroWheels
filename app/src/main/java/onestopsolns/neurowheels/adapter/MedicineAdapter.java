package onestopsolns.neurowheels.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


import onestopsolns.neurowheels.R;
import onestopsolns.neurowheels.model.MedicineModel;

/**
 * Created by Adnan on 17-04-2018.
 */

public class MedicineAdapter extends ArrayAdapter<MedicineModel> {
    Context mCtx;
    int mLayoutRes;
    List<MedicineModel> medicineList;
    SQLiteDatabase mDatabase;

    public MedicineAdapter(Context mCtx, int mLayoutRes, List<MedicineModel> medicineList, SQLiteDatabase mDatabase) {
        super(mCtx, mLayoutRes, medicineList);
        this.mCtx = mCtx;
        this.mLayoutRes = mLayoutRes;
        this.medicineList = medicineList;
        this.mDatabase = mDatabase;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(mLayoutRes, null);

        //getting medicine of the specified position
        final MedicineModel medicine = medicineList.get(position);

        //getting Views
        TextView textViewName = view.findViewById(R.id.medicinename);

        //adding data to views
        textViewName.setText(medicine.getName());

        //we will use these buttons later for update and delete operation
        Button buttonDelete = view.findViewById(R.id.deletemed);
        Button buttonEdit = view.findViewById(R.id.updatemed);

        //adding a clicklistener to button
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateMedicine(medicine);
            }
        });

        //the delete operation
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
                builder.setTitle("Are you sure?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String sql = "DELETE FROM medicine WHERE id = ?";
                        mDatabase.execSQL(sql, new Integer[]{medicine.getId()});
                        reloadEmployeesFromDatabase();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        return view;
    }


    private void updateMedicine(final MedicineModel medicine) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);

        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.update_med_dialog, null);
        builder.setView(view);


        final EditText editTextName = view.findViewById(R.id.updatemedname);

        editTextName.setText(medicine.getName());

        final AlertDialog dialog = builder.create();
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

                String sql = "UPDATE medicine \n" +
                        "SET name = ?\n" +
                        "WHERE id = ?;\n";

                mDatabase.execSQL(sql, new String[]{name, String.valueOf(medicine.getId())});
                Toast.makeText(mCtx, "Employee Updated", Toast.LENGTH_SHORT).show();
                reloadEmployeesFromDatabase();

                dialog.dismiss();
            }
        });
    }


    private void reloadEmployeesFromDatabase() {
        Cursor cursorMedicine = mDatabase.rawQuery("SELECT * FROM medicine", null);
        if (cursorMedicine.moveToFirst()) {
            medicineList.clear();
            do {
                medicineList.add(new MedicineModel(
                        cursorMedicine.getInt(0),
                        cursorMedicine.getString(1)
                ));
            } while (cursorMedicine.moveToNext());
        }
        cursorMedicine.close();
        notifyDataSetChanged();
    }
}
