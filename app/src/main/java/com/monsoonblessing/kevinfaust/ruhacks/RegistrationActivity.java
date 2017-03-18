package com.monsoonblessing.kevinfaust.ruhacks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegistrationActivity extends AppCompatActivity {

    @BindView(R.id.max_spots_text)
    TextView maxSpotsTextView;
    @BindView(R.id.hourly_price_text)
    TextView hourlyPriceTextView;

    private DatabaseReference parkingLotDatabase;

    // hardcoded number for this lot
    public static int LOT_NUMBER = 11;

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
    }

    @OnClick(R.id.save_button)
    void onSaveButton() {
        String spots = maxSpotsTextView.getText().toString();
        String price = hourlyPriceTextView.getText().toString();

        if (spots.length() != 0 && price.length() != 0) {
            // create new lot
            Lot l = new Lot();
            l.setAvailableSpots(Integer.parseInt(spots));
            l.setHourlyCharge(Double.parseDouble(price));
            l.setLotNumber(LOT_NUMBER);
            l.setMaxSpots(Integer.parseInt(spots));

            // save lot
            parkingLotDatabase.child(String.valueOf(LOT_NUMBER)).setValue(l);

            mEditor.putString("lot_number", "temp");
            mEditor.apply();

            launchMain();
        }
    }

    public void launchMain() {
        Intent i = new Intent(RegistrationActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
