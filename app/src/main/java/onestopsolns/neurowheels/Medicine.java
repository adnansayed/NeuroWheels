package onestopsolns.neurowheels;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import onestopsolns.neurowheels.adapter.MedicineAdapter;
import onestopsolns.neurowheels.model.MedicineModel;


public class Medicine extends Fragment {
    private Button mInsertMed;
    private View rootView;
    private String mMedicine;
    SQLiteDatabase mDatabase;
    public static final String DATABASE_NAME = "mymedicinedatabase";
    private List<MedicineModel> medicineList;
    private MedicineAdapter adapter;
    private ListView listMedicine;

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
        medicineList = new ArrayList<>();
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
                            addMedicine(mMedicine);
                            adapter.clear();
                            adapter.notifyDataSetChanged();
                            showMedicineFromDatabase();
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


        mDatabase = getActivity().openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
        createMedicineTable();

        showMedicineFromDatabase();
        return rootView;
    }

    private void createMedicineTable() {
        mDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS medicine (\n" +
                        "    id INTEGER NOT NULL CONSTRAINT medicine_pk PRIMARY KEY AUTOINCREMENT,\n" +
                        "    name varchar(200) NOT NULL\n" +
                        ");"
        );
    }

    private void addMedicine(String name) {

        String insertSQL = "INSERT INTO medicine \n" +
                "(name)\n" +
                "VALUES \n" +
                "(?);";

        //using the same method execsql for inserting values
        //this time it has two parameters
        //first is the sql string and second is the parameters that is to be binded with the query
        try {
            mDatabase.execSQL(insertSQL, new String[]{name});
        } catch (Exception ex) {
            Log.d("mytag", ex.toString());
        }

        Toast.makeText(getActivity(), "Medicine Added Successfully", Toast.LENGTH_SHORT).show();
    }


    private void showMedicineFromDatabase() {

        //we used rawQuery(sql, selectionargs) for fetching all the employees
        Cursor cursorMedicine = mDatabase.rawQuery("SELECT * FROM medicine", null);

        //if the cursor has some data
        if (cursorMedicine.moveToFirst()) {
            //looping through all the records
            do {
                //pushing each record in the employee list
                medicineList.add(new MedicineModel(
                        cursorMedicine.getInt(0),
                        cursorMedicine.getString(1)
                ));
            } while (cursorMedicine.moveToNext());
        }
        //closing the cursor
        cursorMedicine.close();

        //creating the adapter object
        adapter = new MedicineAdapter(getActivity(), R.layout.list_medicine_layout, medicineList, mDatabase);

        //adding the adapter to listview
        listMedicine.setAdapter(adapter);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
