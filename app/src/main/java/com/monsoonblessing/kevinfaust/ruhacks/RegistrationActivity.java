package com.monsoonblessing.kevinfaust.ruhacks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegistrationActivity extends AppCompatActivity {

    @BindView(R.id.max_spots_text)
    TextView maxSpotsTextView;
    @BindView(R.id.max_time_text)
    TextView maxTimeTextView;
    @BindView(R.id.hourly_price_text)
    TextView hourlyPriceTextView;
    @BindView(R.id.join_lot_text)
    EditText joinLotText;

    private DatabaseReference parkingLotDatabase;

    // next lot number
    private long nextLotNumber;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        ButterKnife.bind(this);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        parkingLotDatabase = database.child("lots");

        mSharedPreferences = getSharedPreferences("SmartPark", MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();

        if (mSharedPreferences.getString("lot_number", null) != null) {
            launchMain();
        }


        parkingLotDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                nextLotNumber = dataSnapshot.getChildrenCount() + 1;
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });


    }

    @OnClick(R.id.save_button)
    void onSaveButton() {
        String spots = maxSpotsTextView.getText().toString();
        String time = maxTimeTextView.getText().toString();
        String price = hourlyPriceTextView.getText().toString();

        if (spots.length() != 0 && time.length() !=0 && price.length() != 0) {
            // create new lot
            Lot l = new Lot();
            l.setAvailableSpots(Integer.parseInt(spots));
            l.setMaxTime(Integer.parseInt(time));
            l.setHourlyCharge(Double.parseDouble(price));
            l.setLotNumber(nextLotNumber);
            l.setMaxSpots(Integer.parseInt(spots));

            // save lot
            parkingLotDatabase.child(String.valueOf(nextLotNumber)).setValue(l);

            mEditor.putString("lot_number", String.valueOf(nextLotNumber));
            mEditor.apply();

            launchMain();
        }
    }

    public void launchMain() {
        Intent i = new Intent(RegistrationActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @OnClick(R.id.join_lot_btn)
    void onJoinLotButton() {
        if (joinLotText.getText().toString().length() != 0) {
            mEditor.putString("lot_number", joinLotText.getText().toString());
            mEditor.apply();
            launchMain();
        }
    }
}
