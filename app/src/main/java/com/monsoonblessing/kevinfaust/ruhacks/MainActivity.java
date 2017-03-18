package com.monsoonblessing.kevinfaust.ruhacks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
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
    private String ANDROID_DATA_DIR;
    private String openAlprConfFile;
    private static File destination;

    @BindView(R.id.result_text)
    TextView resultTextView;
    @BindView(R.id.license_image)
    ImageView imageView;
    @BindView(R.id.creditCardText)
    TextView creditCardTextView;

    private DynamoDBMapper mapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // stuff for license recognition
        ANDROID_DATA_DIR = this.getApplicationInfo().dataDir;
        openAlprConfFile = ANDROID_DATA_DIR + File.separatorChar + "runtime_data" + File.separatorChar + "openalpr.conf";


        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-west-2:3d91c865-8a4a-437a-8816-ae5e9c8578a1", // Identity Pool ID
                Regions.US_WEST_2 // Region
        );
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(ddbClient);
    }

//    @OnClick(R.id.take_picture)
//    void onTakePicture() {
//        // check if we have storage permission
//        if (!PermissionManager.hasStoragePermission(MainActivity.this)) {
//            PermissionManager.requestPermission(MainActivity.this, PermissionManager.STORAGE_PERMISSION, PermissionManager.PERMISSION_STORAGE_CODE);
//            // check if we have camera permission
//        } else if (!PermissionManager.hasCameraPermission(MainActivity.this)) {
//            PermissionManager.requestPermission(MainActivity.this, PermissionManager.CAMERA_PERMISSION, PermissionManager.PERMISSION_CAMERA_CODE);
//        } else {
//            takePicture();
//        }
//    }

    @OnClick(R.id.verifyCreditCard)
    void onVerifyCreditCard() {
        // make sure credit card field not blank
        if (creditCardTextView.getText().length() != 0) {
            // verify credit card is legit ...

            // check if we have storage permission and proceed to take picture
            if (!PermissionManager.hasStoragePermission(MainActivity.this)) {
                PermissionManager.requestPermission(MainActivity.this, PermissionManager.STORAGE_PERMISSION, PermissionManager.PERMISSION_STORAGE_CODE);
                // check if we have camera permission
            } else if (!PermissionManager.hasCameraPermission(MainActivity.this)) {
                PermissionManager.requestPermission(MainActivity.this, PermissionManager.CAMERA_PERMISSION, PermissionManager.PERMISSION_CAMERA_CODE);
            } else {
                takePicture();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            final ProgressDialog progress = ProgressDialog.show(this, "Loading", "Parsing result...", true);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 10;

            // Picasso requires permission.WRITE_EXTERNAL_STORAGE
            Picasso.with(MainActivity.this).load(destination).fit().centerCrop().into(imageView);
            resultTextView.setText("Processing");

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    String result = OpenALPR.Factory.create(MainActivity.this, ANDROID_DATA_DIR).recognizeWithCountryRegionNConfig("us", "", destination.getAbsolutePath(), openAlprConfFile, 10);

                    Log.d("OPEN ALPR", result);

                    try {
                        final Results results = new Gson().fromJson(result, Results.class);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (results == null || results.getResults() == null || results.getResults().size() == 0) {
                                    Toast.makeText(MainActivity.this, "It was not possible to detect the licence plate.", Toast.LENGTH_LONG).show();
                                    resultTextView.setText("It was not possible to detect the licence plate.");
                                } else {
                                    resultTextView.setText("Plate: " + results.getResults().get(0).getPlate()
                                            // Trim confidence to two decimal places
                                            + "\nConfidence: " + String.format("%.2f", results.getResults().get(0).getConfidence()) + "%"
                                            // Convert processing time to seconds and trim to two decimal places
                                            + "\nProcessing time: " + String.format("%.2f", ((results.getProcessingTimeMs() / 1000.0) % 60)) + " seconds");
                                }
                            }
                        });

                    } catch (JsonSyntaxException exception) {
                        final ResultsError resultsError = new Gson().fromJson(result, ResultsError.class);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
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
}