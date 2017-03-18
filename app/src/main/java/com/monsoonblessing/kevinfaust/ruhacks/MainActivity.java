package com.monsoonblessing.kevinfaust.ruhacks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.picasso.Picasso;

import org.openalpr.OpenALPR;
import org.openalpr.model.Results;
import org.openalpr.model.ResultsError;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE = 100;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String ANDROID_DATA_DIR;
    private String openAlprConfFile;
    private static File destination;

    @BindView(R.id.result_text)
    TextView resultTextView;
    @BindView(R.id.license_image)
    ImageView imageView;
    @BindView(R.id.creditCardText)
    TextView creditCardTextView;
    @BindView(R.id.credit_card_prompt_layout)
    LinearLayout creditCardLayoutPromptLinearLayout;
    @BindView(R.id.time_spent_report)
    LinearLayout timeSpentReportLinearLayout;
    @BindView(R.id.timeSpent)
    TextView timeSpentText;
    @BindView(R.id.costCharged)
    TextView costChargedText;
    @BindView(R.id.textView2)
    TextView licensePlateText;
    @BindView(R.id.lot_availability_text)
    TextView lotAvailabilityTextView;
    @BindView(R.id.CVVText)
    EditText cvvText;
    @BindView(R.id.expiryDateText)
    EditText expiryDateText;

    private DatabaseReference vehiclesDatabase;
    private DatabaseReference parkingLotDatabase;

    private String licensePlate;
    private Double ocrAccuracy;

    // contains stuff related to parking lot like available spots and price
    private Lot lot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        SharedPreferences sharedPreferences = getSharedPreferences("SmartPark", MODE_PRIVATE);
        String lotNumber = sharedPreferences.getString("lot_number", null);

        // stuff for license recognition
        ANDROID_DATA_DIR = this.getApplicationInfo().dataDir;
        openAlprConfFile = ANDROID_DATA_DIR + File.separatorChar + "runtime_data" + File.separatorChar + "openalpr.conf";

        // create two database references
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        vehiclesDatabase = database.child("vehicles");
        parkingLotDatabase = database.child("lots").child(lotNumber);

        // dynamic updating lot space text
        parkingLotDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lot = dataSnapshot.getValue(Lot.class);
                int availableSpots = lot.getAvailableSpots();
                int maximumSpots = lot.getMaxSpots();
                lotAvailabilityTextView.setText("Lot availability: " + availableSpots + " / " + maximumSpots);

                if (((float) availableSpots/maximumSpots) >= 0.60) {
                    lotAvailabilityTextView.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.md_green_300));
                } else if (((float) availableSpots/maximumSpots) >= 0.20) {
                    lotAvailabilityTextView.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.md_orange_500));
                } else {
                    lotAvailabilityTextView.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.md_red_600));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // check if we have storage permission and proceed to take picture
        if (!PermissionManager.hasStoragePermission(MainActivity.this)) {
            PermissionManager.requestPermission(MainActivity.this, PermissionManager.STORAGE_PERMISSION, PermissionManager.PERMISSION_STORAGE_CODE);
            finish();
            // check if we have camera permission
        } else if (!PermissionManager.hasCameraPermission(MainActivity.this)) {
            PermissionManager.requestPermission(MainActivity.this, PermissionManager.CAMERA_PERMISSION, PermissionManager.PERMISSION_CAMERA_CODE);
            finish();
        }

    }

    // PROCESS LICENSE PLATE BUTTON
    @OnClick(R.id.process_license_plate)
    void onProcessLicensePlate() {
        // make layout fresh
        licensePlateText.setText("License Plate");
        creditCardLayoutPromptLinearLayout.setVisibility(View.GONE);
        timeSpentReportLinearLayout.setVisibility(View.GONE);

        // take picture of license plate and see whether car is leaving or entering
        takePicture();
    }

    @OnClick(R.id.verifyCreditCard)
    void onVerifyCreditCard() {

        // check if spots open
        if (lot.getAvailableSpots() == 0) {
            Toast.makeText(this, "No spots available", Toast.LENGTH_SHORT).show();
        } else {

            // get credit card number, expiry, and cvv numbers
            String creditCardNum = creditCardTextView.getText().toString();
            String expiryDate = expiryDateText.getText().toString();
            String cvvNum = cvvText.getText().toString();

            // make sure credit card field not blank
            if (creditCardNum.length() != 0 && expiryDate.length() != 0 && cvvNum.length() != 0) {
                // verify credit card is legit ...
                Toast.makeText(this, "Making sure card is valid ...", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "It is valid", Toast.LENGTH_SHORT).show();

                // create new credit card object
                CreditCard card = new CreditCard();
                card.setCardNumber(Integer.parseInt(creditCardNum));
                card.setCardExpiry(expiryDate);
                card.setCardSecurityNumber(Integer.parseInt(cvvNum));

                // create new vehicle object and store in database
                Vehicle v = new Vehicle();
                v.setPlateNumber(licensePlate);
                v.setOcrAccuracy(ocrAccuracy);
                v.setCreditCard(card);
                v.setTimeIn(System.currentTimeMillis());
                v.setTimeOut(null);
                vehiclesDatabase.child(licensePlate).setValue(v);
                Toast.makeText(MainActivity.this, "Successfully registered license in system", Toast.LENGTH_SHORT).show();

                // valid credit card given so we can now hide the prompt
                creditCardLayoutPromptLinearLayout.setVisibility(View.GONE);

                // subtract an available spot
                decreaseSpaceAvailability();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            final ProgressDialog progress = ProgressDialog.show(this, "Loading", "Processing license...", true);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 10;

            // Picasso requires permission.WRITE_EXTERNAL_STORAGE
            Picasso.with(MainActivity.this).load(destination).fit().centerCrop().into(imageView);
            resultTextView.setText("Processing");

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    String result = OpenALPR.Factory.create(MainActivity.this, ANDROID_DATA_DIR).recognizeWithCountryRegionNConfig("us", "", destination.getAbsolutePath(), openAlprConfFile, 10);

                    try {
                        final Results results = new Gson().fromJson(result, Results.class);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (results == null || results.getResults() == null || results.getResults().size() == 0) {
                                    Toast.makeText(MainActivity.this, "It was not possible to detect the licence plate.", Toast.LENGTH_LONG).show();
                                    resultTextView.setVisibility(View.VISIBLE);
                                    resultTextView.setText("It was not possible to detect the licence plate.");
                                } else {
                                    resultTextView.setVisibility(View.INVISIBLE);

                                    // get license plate and accuracy
                                    licensePlate = results.getResults().get(0).getPlate();
                                    ocrAccuracy = results.getResults().get(0).getConfidence();

                                    licensePlateText.setText("License Plate: " + licensePlate + "\nAccuracy: " + String.format("%.2f", ocrAccuracy));

                                    // check if we have this license in our system or not
                                    vehiclesDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(licensePlate)) {
                                                // we have it in our system

                                                // we will show their stats
                                                timeSpentReportLinearLayout.setVisibility(View.VISIBLE);

                                                // see how long they were in the parking lot for
                                                vehiclesDatabase.child(licensePlate).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        Vehicle vehicle = dataSnapshot.getValue(Vehicle.class);
                                                        // calculate time inside (in seconds)
                                                        long timeSpent = (System.currentTimeMillis() - vehicle.getTimeIn()) / 1000;
                                                        double amountCharged = ((double) timeSpent / 60) / 60 * lot.getHourlyCharge();


                                                        CreditCard card = vehicle.getCreditCard();
                                                        /**
                                                         *
                                                         * CHARGE CREDIT CARD
                                                         *
                                                         */

                                                        // display to screen
                                                        Log.d(TAG, "Time spent: " + timeSpent);
                                                        Log.d(TAG, "Amount charged: " + amountCharged);
                                                        timeSpentText.setText("Time spent: " + String.format("%.2f", (double) timeSpent) + " seconds");
                                                        costChargedText.setText("Amount charged: $" + String.format("%.2f", amountCharged));

                                                        // remove vehicle from database
                                                        vehiclesDatabase.child(licensePlate).setValue(null);

                                                        // free a space in the lot
                                                        increaseSpaceAvailability();
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });


                                            } else {
                                                // its a new vehicle. get credit card info before letting them in
                                                creditCardLayoutPromptLinearLayout.setVisibility(View.VISIBLE);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });


                                }
                            }
                        });

                    } catch (JsonSyntaxException exception) {
                        final ResultsError resultsError = new Gson().fromJson(result, ResultsError.class);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                resultTextView.setVisibility(View.VISIBLE);
                                resultTextView.setText(resultsError.getMsg());
                            }
                        });
                    }

                    progress.dismiss();
                }
            });
        }
    }


    public String dateToString(Date date, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format, Locale.getDefault());

        return df.format(date);
    }

    public void takePicture() {
        // Use a folder to store all results
        File folder = new File(Environment.getExternalStorageDirectory() + "/OpenALPR/");
        if (!folder.exists()) {
            folder.mkdir();
        }

        // Generate the path for the next photo
        String name = dateToString(new Date(), "yyyy-MM-dd-hh-mm-ss");
        destination = new File(folder, name + ".jpg");

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(destination));
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (destination != null) {// Picasso does not seem to have an issue with a null value, but to be safe
            Picasso.with(MainActivity.this).load(destination).fit().centerCrop().into(imageView);
        }
    }

    public void decreaseSpaceAvailability() {
        lot.decreaseAvailableSpots();
        parkingLotDatabase.setValue(lot);
    }

    public void increaseSpaceAvailability() {
        lot.increaseAvailableSpots();
        parkingLotDatabase.setValue(lot);
    }
}