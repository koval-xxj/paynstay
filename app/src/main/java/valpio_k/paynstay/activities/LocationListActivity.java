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
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import valpio_k.paynstay.R;
import valpio_k.paynstay.fragments.DialogWindow;
import valpio_k.paynstay.gn_classes.General;
import valpio_k.paynstay.interfaces.ActivityTasksCallback;
import valpio_k.paynstay.tasks.GetAllParkingTask;

public class LocationListActivity extends AppCompatActivity implements ActivityTasksCallback,
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult> {

    //Location variables

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


    private View mLoginFormView;
    private View mProgressView;
    private HashMap<Integer, HashMap<String, String>> Parkings = new HashMap<>();
    private LinearLayout ParkListView;
    private boolean start_task = true;

    private final int GET_ALL_PARKINGS_TASK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        this.mLoginFormView = findViewById(R.id.login_form);
        this.mProgressView = findViewById(R.id.login_progress);
        this.ParkListView = (LinearLayout) findViewById(R.id.location_list);

        //check user location

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
    }

    @Override
    public void onClick(View v) {

        String tag = v.getTag().toString();
        String [] park_data = tag.split(";");

        switch (park_data[0]) {
            case "more_info":

                Intent TarListIntent = new Intent(this, TariffsListActivity.class);
                TarListIntent.putExtra("parkingInfo", this.Parkings.get(Integer.parseInt(park_data[1])));
                TarListIntent.putExtra("parkId", park_data[1]);
                startActivity(TarListIntent);

                break;
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

    @Override
    public void onTaskFinish(boolean success, String responce, Integer task) {

        if (success) {
            switch (task) {
                case GET_ALL_PARKINGS_TASK:
                    this.start_task = false;
                    this.parceParkingJson(responce);
                    break;
            }
        } else {
            String error_message = responce;
            try {
                JSONObject json = new JSONObject(responce);
                error_message = json.getString("message");
            } catch (Exception e) {
                Log.e("Json Reg Task error", e.getMessage());
            }
            this.showDialog("Message", error_message);
        }

        this.showProgress(false);
    }

    public void showDialog(String title, String message) {
        DialogWindow dialog = new DialogWindow();
        dialog.set_title(title);
        dialog.set_message(message);
        dialog.show(getSupportFragmentManager(), "custom");
    }

    private void parceParkingJson(String ParkListJson) {

        try {
            JSONObject json = new JSONObject(ParkListJson);
            Integer count = json.getInt("count");
            JSONArray items = json.getJSONArray("items");
            HashMap<String, String> Parking;

            for (int i = 0; i < count; i++) {

                final View view = getLayoutInflater().inflate(R.layout.parking_template, null);
                AppCompatTextView ParkTitleView = view.findViewById(R.id.park_title);
                AppCompatTextView ParkAddressView = view.findViewById(R.id.park_address);
                AppCompatTextView ParkStatusView = view.findViewById(R.id.park_status);
                AppCompatTextView ParkCreatedView = view.findViewById(R.id.park_created_date);
                AppCompatTextView ParkTariffQtv = view.findViewById(R.id.park_tarrifs_qtv);
                AppCompatImageView TariffMoreInfo = view.findViewById(R.id.parking_info);

                String parking = items.getString(i);
                json = new JSONObject(parking);

                Integer parkID = json.getInt("parking_id");
                String park_title = json.getString("park_title");
                String park_addess = json.getString("address");
                Integer status = json.isNull("status") ? 0 : json.getInt("status");
                String date_created = json.getString("created");
                String tariffs_qtv = json.getString("tariffs_qty");

                ParkTitleView.setText(park_title);
                ParkAddressView.setText(getString(R.string.PL_Adress) + " " + park_addess);
                String Status = (status > 0) ? getString(R.string.PL_ParkStatusActivated) : getString(R.string.PL_ParkStatusDeactivated);
                ParkStatusView.setText(getString(R.string.PL_Status) + " " + Status);
                ParkCreatedView.setText(getString(R.string.PL_Date_Created) + " " + General.format_date(date_created, "yyyy-MM-dd HH:mm:ss", "EEE, MMM dd, yyyy"));
                ParkTariffQtv.setText(getString(R.string.PL_Tarrifs_qtv) + " " + tariffs_qtv);

                if (Integer.parseInt(tariffs_qtv) > 0) {
                    TariffMoreInfo.setTag("more_info;" + parkID);
                    TariffMoreInfo.setOnClickListener(this);
                } else {
                    TariffMoreInfo.setVisibility(View.GONE);
                }

                this.ParkListView.addView(view);

                Parking = new HashMap<>();
                Parking.put("park_title", park_title);
                Parking.put("address", park_addess);
                Parking.put("places_qty", String.valueOf((json.isNull("places_qty")) ? 0 : json.getInt("places_qty")));
                Parking.put("center_x", String.valueOf((json.isNull("center_x")) ? 0 : json.getDouble("center_x")));
                Parking.put("center_y", String.valueOf((json.isNull("center_y")) ? 0 : json.getDouble("center_y")));
                Parking.put("park_code", json.getString("park_code"));
                Parking.put("status", String.valueOf(status));
                Parking.put("sector_id", String.valueOf((json.isNull("sector_id")) ? 0 : json.getInt("sector_id")));
                Parking.put("date_created", date_created);
                Parking.put("tariffs_qtv", tariffs_qtv);

                this.Parkings.put(parkID, Parking);

            }

        } catch (Exception e) {
            Log.e("ParceParkingListJson", e.getMessage());
        }

    }

    //Location

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
            //Toast.makeText(LocationListActivity.this, "location was already on so detecting location now", Toast.LENGTH_SHORT).show();
            startLocationUpdates();
        }

        if (this.start_task) {
            this.checkLocationSettings();
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

                Toast.makeText(LocationListActivity.this, "Location is already on.", Toast.LENGTH_SHORT).show();
                startLocationUpdates();
                // stopLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    Toast.makeText(LocationListActivity.this, "Location dialog will be open", Toast.LENGTH_SHORT).show();
                    //

                    //move to step 6 in onActivityResult to check what action user has taken on settings dialog
                    status.startResolutionForResult(LocationListActivity.this, REQUEST_CHECK_SETTINGS);
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
        }
    }

    /**
     * Sets the value of the UI fields for the location latitude, longitude and last update time.
     */
    private void updateLocationUI() {
        // if (TextUtils.isEmpty(LocationActivity.this.api_token_name) || TextUtils.isEmpty(LocationActivity.this.api_token_value)) {
        // }
        if (mCurrentLocation != null) {

            //this.GpsParramsText.setText(getString(R.string.tar_list_longitude) + " " + Double.toString(mCurrentLocation.getLongitude()) + "\n" + getString(R.string.tar_list_latitude) + " " + Double.toString(mCurrentLocation.getLatitude()));

            //this.showDialog("test_params", getString(R.string.tar_list_longitude) + " " + Double.toString(mCurrentLocation.getLongitude()) + "\n" + getString(R.string.tar_list_latitude) + " " + Double.toString(mCurrentLocation.getLatitude()));

            LocationListActivity.this.stopLocationUpdates();
            mGoogleApiClient.disconnect();

            //in the end
            this.showProgress(true);
            GetAllParkingTask getPark = new GetAllParkingTask(this, GET_ALL_PARKINGS_TASK, mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude());
            getPark.execute();


        }
    }

}
