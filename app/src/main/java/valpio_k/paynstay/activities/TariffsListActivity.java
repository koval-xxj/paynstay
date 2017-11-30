package valpio_k.paynstay.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import valpio_k.paynstay.R;
import valpio_k.paynstay.fragments.DialogWindow;
import valpio_k.paynstay.gn_classes.General;
import valpio_k.paynstay.interfaces.ActivityTasksCallback;
import valpio_k.paynstay.tasks.GetTariffsTask;

public class TariffsListActivity extends AppCompatActivity implements ActivityTasksCallback {

    private HashMap<String, String> ParkInfo;
    private View mLoginFormView;
    private View mProgressView;
    private LinearLayout TariffsContainer;

    private final int GET_TARIFFS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tariffs_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        this.mLoginFormView = findViewById(R.id.login_form);
        this.mProgressView = findViewById(R.id.login_progress);
        this.TariffsContainer = (LinearLayout) findViewById(R.id.tariffs_container);

        Bundle extras = getIntent().getExtras();
        String parkId = null;
        if (extras != null) {
            this.ParkInfo = (HashMap<String, String>) extras.get("parkingInfo");
            parkId = extras.getString("parkId");
        }

        this.showProgress(true);
        GetTariffsTask get_tariffs = new GetTariffsTask(this, GET_TARIFFS, parkId);
        get_tariffs.execute();

        this.set_parking_info();

    }

    private void set_parking_info() {

        AppCompatTextView title = (AppCompatTextView) findViewById(R.id.park_title);
        AppCompatTextView park_address = (AppCompatTextView) findViewById(R.id.park_address);
        AppCompatTextView park_status = (AppCompatTextView) findViewById(R.id.park_status);
        AppCompatTextView park_created_date = (AppCompatTextView) findViewById(R.id.park_created_date);
        AppCompatTextView park_tarrifs_qtv = (AppCompatTextView) findViewById(R.id.park_tarrifs_qtv);
        AppCompatTextView park_code = (AppCompatTextView) findViewById(R.id.park_code);
        AppCompatTextView position = (AppCompatTextView) findViewById(R.id.park_position);

        if (this.ParkInfo != null) {
            title.setText(this.ParkInfo.get("park_title"));
            park_address.setText(getString(R.string.PL_Adress) + " " + this.ParkInfo.get("address"));
            String Status = (Integer.parseInt(this.ParkInfo.get("status")) > 0) ? getString(R.string.PL_ParkStatusActivated) : getString(R.string.PL_ParkStatusDeactivated);
            park_status.setText(getString(R.string.PL_Status) + " " + Status);
            park_created_date.setText(getString(R.string.PL_Date_Created) + " " + General.format_date(this.ParkInfo.get("date_created"), "yyyy-MM-dd HH:mm:ss", "EEE, MMM dd, yyyy"));
            park_tarrifs_qtv.setText(getString(R.string.PL_Tarrifs_qtv) + " " + this.ParkInfo.get("tariffs_qtv"));
            park_code.setText(getString(R.string.tar_list_park_code) + " " + this.ParkInfo.get("park_code"));
            String location_x = (Double.valueOf(this.ParkInfo.get("center_x")) > 0) ? this.ParkInfo.get("center_x") : getString(R.string.tar_list_position_undefined);
            String location_y = (Double.valueOf(this.ParkInfo.get("center_y")) > 0) ? this.ParkInfo.get("center_y") : getString(R.string.tar_list_position_undefined);
            position.setText(getString(R.string.tar_list_park_position) + " " + getString(R.string.tar_list_longitude) + " " + location_x + " " + getString(R.string.tar_list_latitude) + " " + location_y);
        }

    }

    @Override
    public void onTaskFinish(boolean success, String responce, Integer task) {

        if (success) {
            switch (task) {
                case GET_TARIFFS:
                    this.parce_tariffs_json(responce);
                    this.showProgress(false);
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

    private void parce_tariffs_json(String tariffs_json) {

        try {
            JSONObject json = new JSONObject(tariffs_json);
            Integer count = Integer.parseInt(json.getString("count"));
            JSONArray items = json.getJSONArray("items");

            for (int i = 0; i < count; i++) {
                String tariff = items.getString(i);
                json = new JSONObject(tariff);

                final View view = getLayoutInflater().inflate(R.layout.tariff_tempalte, null);
                AppCompatTextView[] views = {
                        view.findViewById(R.id.tariff_title),
                        view.findViewById(R.id.tariff_valid_from),
                        view.findViewById(R.id.tariff_valid_to),
                        view.findViewById(R.id.tariff_price),
                        view.findViewById(R.id.tariff_max_time)
                };

                String [] values = {
                        json.getString("tariff_title"),
                        getString(R.string.tar_list_valid_from) + " " + json.getString("validity_from"),
                        getString(R.string.tar_list_valid_to) + " " + json.getString("validity_to"),
                        getString(R.string.AP_PricePerHour) + " " + json.getString("price"),
                        getString(R.string.AP_tarriff_max_time) + " " + json.getString("max_time") + " " + json.getString("unit_type"),
                };

                for (int a = 0; a < views.length; a++) {
                    views[a].setText(values[a]);
                }

                this.TariffsContainer.addView(view);
            }

        } catch (Exception e) {
            Log.e("parce_tariffs_json", e.getMessage());
        }

    }

}
