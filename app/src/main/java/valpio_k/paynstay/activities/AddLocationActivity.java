package valpio_k.paynstay.activities;


import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import stfalcon.universalpickerdialog.UniversalPickerDialog;
import valpio_k.paynstay.R;
import valpio_k.paynstay.fragments.DialogWindow;
import valpio_k.paynstay.gn_classes.General;
import valpio_k.paynstay.gn_classes.PriceControl;
import valpio_k.paynstay.gn_classes.SaveDataSharPref;
import valpio_k.paynstay.interfaces.ActivityTasksCallback;
import valpio_k.paynstay.tasks.FillParkingTask;

/**
 * step1 :buildGoogleApiClient
 * step2: createLocationRequest
 * step3: buildLocationSettingsRequest
 * step 4:on click of button detect location check the location is on/off using checkLocationSettings method.
 * step 5:on click of the options in location dialog
 * step 6 :depending on the action taken on dialog (startResolution for result and check the action in onActivityResult
 */
public class AddLocationActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult>,
        View.OnClickListener,
        UniversalPickerDialog.OnPickListener,
        ActivityTasksCallback {


    boolean mRepeat;

    protected static final String TAG = "AddLocationActivity";

    //Any random number you can take
    public static final int REQUEST_PERMISSION_LOCATION = 10;

    /**
     * Constant used in the location settings dialog.
     */
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    protected LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;

    int RQS_GooglePlayServices = 0;

    private LinearLayout AddLocStepOne;
    private LinearLayout AddLocStepTwo;
    private AppCompatTextView GpsParramsText;
    private AutoCompleteTextView barcodeView;
    private LinearLayout tarriffs_layout;
    private AppCompatImageView gps_values_result;
    private ArrayList Hours;
    private AppCompatTextView currentTimeElem;

    //Tarriffs manage
    private AppCompatImageView AddNewTarriff;
    private Integer TarriffsAutoIncrement = 0;
    private HashMap <Integer, View> TarriffsConteiners;
    //private ParceCarTypesJson CarTypes;

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final int KEY_SINGLE_PICK = 1;
    private final int GET_ALL_CAR_TYPES_TASK = 2;
    private final int FILL_PARKING_TASK = 3;

    private View mLoginFormView;
    private View mProgressView;

    @Override
    protected void onStart() {
        super.onStart();

        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(this);
        if (resultCode == ConnectionResult.SUCCESS) {
            mGoogleApiClient.connect();
        } else {
            googleAPI.getErrorDialog(this, resultCode, RQS_GooglePlayServices);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            //  Toast.makeText(FusedLocationWithSettingsDialog.this, "location was already on so detecting location now", Toast.LENGTH_SHORT).show();
            startLocationUpdates();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        this.AddLocStepOne = (LinearLayout) findViewById(R.id.add_location_step_one);
        this.AddLocStepTwo = (LinearLayout) findViewById(R.id.add_location_step_two);
        this.GpsParramsText = (AppCompatTextView) findViewById(R.id.gps_params_text);
        this.barcodeView = (AutoCompleteTextView) findViewById(R.id.qr_code);
        this.tarriffs_layout = (LinearLayout) findViewById(R.id.tarriffs_layout);
        this.gps_values_result = (AppCompatImageView) findViewById(R.id.gps_values_result);
        this.AddNewTarriff = (AppCompatImageView) findViewById(R.id.add_price_for_car_type);

        findViewById(R.id.btn_detect_fused_location).setOnClickListener(this);
        findViewById(R.id.btn_scan_qr).setOnClickListener(this);
        findViewById(R.id.add_park_submit).setOnClickListener(this);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //total six textviews

        //tv_city = (TextView) findViewById(R.id.tv_city);
        //tv_pincode = (TextView) findViewById(R.id.tv_pincode);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        ////
        mRepeat = true;

        // Kick off the process of building the GoogleApiClient, LocationRequest, and
        // LocationSettingsRequest objects.

        //step 1
        buildGoogleApiClient();

        //step 2
        createLocationRequest();

        //step 3
        buildLocationSettingsRequest();

        this.AddNewTarriff.setOnClickListener(this);
        this.Hours = General.get_hours();
        this.TarriffsConteiners = new HashMap<>();
        this.mLoginFormView = findViewById(R.id.login_form);
        this.mProgressView = findViewById(R.id.login_progress);

//        GetAllCarTypesTask get_car_task = new GetAllCarTypesTask(this, GET_ALL_CAR_TYPES_TASK);
//        get_car_task.execute((Void) null);
    }

    @Override
    public void onClick(View v) {

        boolean flag = true;

        switch (v.getId()) {
            case R.id.btn_detect_fused_location:
                mGoogleApiClient.connect();
                checkLocationSettings();
                flag = false;
                break;
            case R.id.btn_scan_qr:
                // launch barcode activity.
                AppCompatCheckBox useFlash = (AppCompatCheckBox) findViewById(R.id.use_flash);
                Intent intent = new Intent(this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());

                startActivityForResult(intent, RC_BARCODE_CAPTURE);
                flag = false;
                break;
            case R.id.add_price_for_car_type:
                this.add_new_tarriff();
                flag = false;
                break;
            case R.id.add_park_submit:
                this.checking_before_submit();
                flag = false;
                break;

        }

        if (!flag) {
            return;
        }

        String[] elemData = v.getTag().toString().split(";");

        switch (elemData[0]) {
            case "tariff_time_from":
            case "tariff_time_to":
            case "max_time":
                this.currentTimeElem = (AppCompatTextView) v;
                showDefaultPicker(R.string.title_select_tarriff_time, KEY_SINGLE_PICK, new UniversalPickerDialog.Input(0, this.Hours));
                break;
            case "deleteTarriff":
                Integer ViewKey = Integer.parseInt(elemData[1]);
                this.tarriffs_layout.removeView(this.TarriffsConteiners.get(ViewKey));
                this.TarriffsConteiners.remove(ViewKey);
                break;
        }

    }

    //step 1
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    //step 2
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    //step 3
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    //step 4
    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
        } else {
            goAndDetectLocation();
        }

    }

    public void goAndDetectLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = true;
                //     setButtonsEnabledState();
            }
        });
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = false;
                //   setButtonsEnabledState();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    goAndDetectLocation();
                }
                break;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.i(TAG, "Connected to GoogleApiClient");

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                return;
            }
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            // LocationActivity.this.stopLocationUpdates();
            // updateLocationUI(mRepeat);
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateLocationUI();
        Toast.makeText(this, "Location Updated.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    /**
     * Invoked when settings dialog is opened and action taken
     *
     * @param locationSettingsResult This below OnResult will be used by settings dialog actions.
     */

    //step 5
    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {

        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");

                Toast.makeText(AddLocationActivity.this, "Location is already on.", Toast.LENGTH_SHORT).show();
                startLocationUpdates();
                // stopLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    Toast.makeText(AddLocationActivity.this, "Location dialog will be open", Toast.LENGTH_SHORT).show();
                    //

                    //move to step 6 in onActivityResult to check what action user has taken on settings dialog
                    status.startResolutionForResult(AddLocationActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }


    /**
     * This OnActivityResult will listen when
     * case LocationSettingsStatusCodes.RESOLUTION_REQUIRED: is called on the above OnResult
     */
    //step 6:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        break;
                }
                break;
            case RC_BARCODE_CAPTURE:
                if (resultCode == CommonStatusCodes.SUCCESS) {
                    if (data != null) {
                        Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                        barcodeView.setText(barcode.displayValue);
                        Log.d(TAG, "Barcode read: " + barcode.displayValue);
                    } else {
                        //statusMessage.setText(R.string.barcode_failure);
                        Log.d(TAG, "No barcode captured, intent data is null");
                    }
                } else {
                    this.showDialog("Error", String.format(getString(R.string.barcode_error), CommonStatusCodes.getStatusCodeString(resultCode)));
//                    statusMessage.setText(String.format(getString(R.string.barcode_error),
//                            CommonStatusCodes.getStatusCodeString(resultCode)));
                }
                break;
        }
    }

    /**
     * Sets the value of the UI fields for the location latitude, longitude and last update time.
     */
    private void updateLocationUI() {
        // if (TextUtils.isEmpty(LocationActivity.this.api_token_name) || TextUtils.isEmpty(LocationActivity.this.api_token_value)) {
        // }
        if (mCurrentLocation != null) {
            //TextView data_text = (TextView) findViewById(R.id.txt_data);

            this.GpsParramsText.setText(getString(R.string.tar_list_longitude) + " " + Double.toString(mCurrentLocation.getLongitude()) + "\n" + getString(R.string.tar_list_latitude) + " " + Double.toString(mCurrentLocation.getLatitude()));
            this.gps_values_result.setBackgroundResource(R.drawable.img_checkmark_orange);


            //this.GPSTask = new GPSLocationTask(this, Double.toString(mCurrentLocation.getLatitude()), Double.toString(mCurrentLocation.getLongitude()));

            //this.GPSTask.execute();


            AddLocationActivity.this.stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
    }

    // Navigation

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            boolean result = false;

            switch (item.getItemId()) {
                case R.id.navigation_step_one:
                    AddLocationActivity.this.setLayoutVisibility(true);
                    result = true;
                    break;
                case R.id.navigation_step_two:
                    AddLocationActivity.this.setLayoutVisibility(false);
                    result = true;
                    break;
//                case R.id.navigation_home:
//                    //mTextMessage.setText(R.string.title_home);
//                    return true;
//                case R.id.navigation_dashboard:
//                    //mTextMessage.setText(R.string.title_dashboard);
//                    return true;
//                case R.id.navigation_notifications:
//                    //mTextMessage.setText(R.string.title_notifications);
//                    return true;
            }
            return result;
        }

    };

    private void setLayoutVisibility(boolean step_one) {
        Integer StepOne = (step_one) ? View.VISIBLE : View.GONE;
        Integer StepTwo = (step_one) ? View.GONE : View.VISIBLE;

        this.AddLocStepOne.setVisibility(StepOne);
        this.AddLocStepTwo.setVisibility(StepTwo);
    }

    public void showDialog(String title, String message) {
        DialogWindow dialog = new DialogWindow();
        dialog.set_title(title);
        dialog.set_message(message);
        dialog.show(getSupportFragmentManager(), "custom");
    }

    @Override
    public void onPick(int[] selectedValues, int key) {
        this.currentTimeElem.setText(String.valueOf(this.Hours.get(selectedValues[0])));

        //String str = list.get(selectedValues[0]);
        //Object obj = array[selectedValues[0]];

    /*do some logic*/
    }

    private void showDefaultPicker(@StringRes int title, int key, UniversalPickerDialog.Input... inputs) {
        new UniversalPickerDialog.Builder(this)
                .setTitle(title)
                .setListener(this)
                .setInputs(inputs)
                .setKey(key)
                .show();
    }

    private void add_new_tarriff () {

        this.TarriffsAutoIncrement++;

        final View view = getLayoutInflater().inflate(R.layout.tarriff_container, null);
        AppCompatTextView TimeFrom = view.findViewById(R.id.first_tariff_time_from);
        TimeFrom.setTag("tariff_time_from;" + this.TarriffsAutoIncrement);
        TimeFrom.setOnClickListener(this);

        AppCompatTextView TimeTo = view.findViewById(R.id.first_tariff_time_to);
        TimeTo.setTag("tariff_time_to;" + this.TarriffsAutoIncrement);
        TimeTo.setOnClickListener(this);

//        if (this.CarTypes != null) {
//            String[] data = this.CarTypes.getCarTypeNames();
//            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//            Spinner CarType = view.findViewById(R.id.car_type_spinner);
//            CarType.setTag("car_type_spinner;" + this.TarriffsAutoIncrement);
//            CarType.setAdapter(adapter);
//            // spinner title
//            CarType.setPrompt("Car Type");
//        }

        AutoCompleteTextView price = view.findViewById(R.id.tarriff_price_per_hour);
        price.setTag("tarriff_price;" + this.TarriffsAutoIncrement);
        price.addTextChangedListener(new PriceControl(price));

        AppCompatImageView deleteTarriff = view.findViewById(R.id.delete_tarriff);
        deleteTarriff.setTag("deleteTarriff;" + this.TarriffsAutoIncrement);
        deleteTarriff.setOnClickListener(this);

        AppCompatTextView max_time = view.findViewById(R.id.max_time_view);
        max_time.setTag("max_time;" + this.TarriffsAutoIncrement);
        max_time.setOnClickListener(this);

        this.tarriffs_layout.addView(view);
        this.TarriffsConteiners.put(this.TarriffsAutoIncrement, view);

    }

    private void checking_before_submit() {
        if (this.mCurrentLocation == null) {
            this.showDialog("Notification", "Location is not defined.");
            return;
        }
        if (TextUtils.isEmpty(this.barcodeView.getText().toString())) {
            this.showDialog("Notification", "Qr code is not defined.");
            this.barcodeView.requestFocus();
            return;
        }

        boolean flag = true;

        flag = (!this.check_required_fields((AutoCompleteTextView) findViewById(R.id.park_title))) ? false : flag;
        flag = (!this.check_required_fields((AutoCompleteTextView) findViewById(R.id.park_address))) ? false : flag;
        flag = (!this.check_required_fields((AutoCompleteTextView) findViewById(R.id.park_palces_qtv))) ? false : flag;

        for (Object tariffID : this.TarriffsConteiners.keySet()) {
            View TarriffView = this.TarriffsConteiners.get(tariffID);
            flag = (!this.check_required_fields((AutoCompleteTextView) TarriffView.findViewById(R.id.tarriff_price_per_hour))) ? false : flag;
            if (!flag) {
                continue;
            }
//            Spinner carType = TarriffView.findViewById(R.id.car_type_spinner);
//            Log.d("SpinerCarType", carType.getSelectedItem().toString());
        }

        if (!flag) {
            return;
        }

        this.save_parking_data();

    }

    private boolean check_required_fields(AutoCompleteTextView textView) {
        boolean flag = true;

        if (TextUtils.isEmpty(textView.getText().toString())) {
            textView.setError(getString(R.string.error_field_required));
            textView.requestFocus();
            flag = false;
        }

        return flag;
    }

    @Override
    public void onTaskFinish(boolean success, String responce, Integer task) {

        if (success) {
            switch (task) {
                case GET_ALL_CAR_TYPES_TASK:
                    //this.CarTypes = new ParceCarTypesJson(responce);
                    break;
                case FILL_PARKING_TASK:
                    String message = "Added successfully";
                    try {
                        JSONObject json = new JSONObject(responce);
                        message = json.getString("success");
                    } catch (Exception e) {
                        Log.e("Json Reg Task success", e.getMessage());
                    }
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                    break;
            }
        } else {
            String error_message = responce;
            try {
                JSONObject json = new JSONObject(responce);
                error_message = (json.has("message")) ? json.getString("message") : json.getString("error");
            } catch (Exception e) {
                Log.e("Json Reg Task error", e.getMessage());
            }
            this.showDialog("Message", error_message);
        }

        this.showProgress(false);

    }

    private void save_parking_data() {

        SaveDataSharPref data = new SaveDataSharPref(this);
        String user_info = data.read_string("user_info");

        try {

            JSONObject json = new JSONObject(user_info);
            String managerId = json.getString("user_id");

            json = new JSONObject();

            json.put("park_title", ((AutoCompleteTextView) findViewById(R.id.park_title)).getText());
            json.put("address", ((AutoCompleteTextView) findViewById(R.id.park_address)).getText());
            json.put("places_qty", ((AutoCompleteTextView) findViewById(R.id.park_palces_qtv)).getText());
            json.put("center_x", this.mCurrentLocation.getLongitude());
            json.put("center_y", this.mCurrentLocation.getLatitude());
            json.put("park_code", ((AutoCompleteTextView) findViewById(R.id.qr_code)).getText());
            json.put("status", 1);
            json.put("manager_id", managerId);

            JSONObject tariffs = new JSONObject();
            tariffs.put("count", this.TarriffsConteiners.size());
            JSONArray items = new JSONArray();
            JSONObject tariff;

            for (Object tariffID : this.TarriffsConteiners.keySet()) {
                View TarriffView = this.TarriffsConteiners.get(tariffID);
                tariff = new JSONObject();
                tariff.put("tariff_title", "");
                tariff.put("validity_from", ((AppCompatTextView)TarriffView.findViewById(R.id.first_tariff_time_from)).getText());
                tariff.put("validity_to", ((AppCompatTextView)TarriffView.findViewById(R.id.first_tariff_time_to)).getText());
                tariff.put("price", ((AutoCompleteTextView)TarriffView.findViewById(R.id.tarriff_price_per_hour)).getText());
                String max_time = ((AppCompatTextView)TarriffView.findViewById(R.id.max_time_view)).getText().toString();
                tariff.put("max_time", max_time.replace(":", "."));
                tariff.put("unit_type", "hour");
                items.put(tariff);
            }

            tariffs.put("items", items);
            json.put("tariffs", tariffs);

            Log.d("parking_json", json.toString());
            this.showProgress(true);
            FillParkingTask fillPark = new FillParkingTask(this, FILL_PARKING_TASK, json.toString());
            fillPark.execute();

        } catch (Exception e) {
            Log.e("saveParkingDataJson", e.getMessage());
        }

    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
