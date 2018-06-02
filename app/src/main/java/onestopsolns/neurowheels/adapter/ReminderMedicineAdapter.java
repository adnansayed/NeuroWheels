package onestopsolns.neurowheels.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import onestopsolns.neurowheels.R;
import onestopsolns.neurowheels.data.MedicineContract;
import onestopsolns.neurowheels.data.ReminderMedicineContract;

public class ReminderMedicineAdapter extends CursorAdapter {
    private ReminderMedicineAdapterListener delegate;
    private TextView qtyTxtView;
    private TextView medNameTxtView;
    private ImageButton closeImgBtn;

    public ReminderMedicineAdapter(Context mCtx, Cursor c, ReminderMedicineAdapterListener delegate){
        super(mCtx, c, 0);
        this.delegate = delegate;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.reminder_medicine_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        qtyTxtView = (TextView) view.findViewById(R.id.lblMedicineQty);
        medNameTxtView = (TextView) view.findViewById(R.id.lblMedicineName);
        closeImgBtn = (ImageButton) view.findViewById(R.id.remMedBtnDelete);

        int idColumnIndex = cursor.getColumnIndex(ReminderMedicineContract.ReminderMedicineEntry._ID);
        int titleColumnIndex = cursor.getColumnIndex(MedicineContract.MedicineEntry.KEY_TITLE);
        final int qtyColumnIndex = cursor.getColumnIndex(ReminderMedicineContract.ReminderMedicineEntry.KEY_QUANTITY);
        int fkMedIDColumnIndex = cursor.getColumnIndex(ReminderMedicineContract.ReminderMedicineEntry.KEY_FK_MEDICINE_ID);
        final String tempMedTitle = cursor.getString(titleColumnIndex);
        final int remMedID = cursor.getInt(idColumnIndex);
        final int remMedQty = cursor.getInt(qtyColumnIndex);
        final int remMedFKMedID = cursor.getInt(fkMedIDColumnIndex);
        if (tempMedTitle != null && !tempMedTitle.isEmpty()){
            medNameTxtView.setText(tempMedTitle);
        }

        qtyTxtView.setText(context.getString(R.string.medicine_quantity_format,remMedQty));

        closeImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (delegate != null) {
                    delegate.onItemDeleteTapped(remMedID, remMedFKMedID);
                }

            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (delegate != null) {
                    delegate.onItemClicked(remMedID,tempMedTitle,remMedQty);
                }
            }
        });
    }

    public interface ReminderMedicineAdapterListener{
        void onItemClicked(int remMedID, String medName, int medQty);
        void onItemDeleteTapped(int remMedID, int remFKMedID);
    }
}
