package onestopsolns.neurowheels.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import onestopsolns.neurowheels.R;
import onestopsolns.neurowheels.data.MedicineContract;

/**
 * Created by Adnan on 17-04-2018.
 */

public class MedicineAdapter extends CursorAdapter {
    private TextView lblMedName;
    private Button btnEdit, btnDelete;
    private MedicineAdapterListener delegate;

    public  MedicineAdapter(Context mCtx, Cursor c, MedicineAdapterListener listener) {
        super(mCtx, c,0);
        delegate = listener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_medicine_layout,viewGroup,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        lblMedName = (TextView) view.findViewById(R.id.medicinename);
        btnEdit = (Button) view.findViewById(R.id.updatemed);
        btnDelete = (Button) view.findViewById(R.id.deletemed);

        int idColumnIndex = cursor.getColumnIndex(MedicineContract.MedicineEntry._ID);
        int titleColumnIndex = cursor.getColumnIndex(MedicineContract.MedicineEntry.KEY_TITLE);
        final String tempMedTitle = cursor.getString(titleColumnIndex);
        final int medID = cursor.getInt(idColumnIndex);
        if (tempMedTitle != null && !tempMedTitle.isEmpty()){
            lblMedName.setText(tempMedTitle);
        }

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(delegate != null){
                    delegate.onUpdateTapped(medID, tempMedTitle);
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(delegate != null){
                    delegate.onDeleteTapped(medID);
                }
            }
        });
    }

    public interface MedicineAdapterListener{
        void onUpdateTapped(int medID, String medName);
        void onDeleteTapped(int medID);
    }
}

