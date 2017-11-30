package valpio_k.paynstay.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import valpio_k.paynstay.gn_classes.VolleyApi;
import valpio_k.paynstay.interfaces.ActivityTasksCallback;
import valpio_k.paynstay.interfaces.VolleyResponse;

/**
 * Created by SERHIO on 13.10.2017.
 */

public class FillParkingTask extends AsyncTask implements VolleyResponse {

    private Context context;
    private ActivityTasksCallback ATC;
    private Integer TaskID;
    private String JsonParkData;

    public FillParkingTask(Context context, Integer taskID, String JsonParkData) {
        this.context = context;
        this.TaskID = taskID;
        this.ATC = (ActivityTasksCallback) context;
        this.JsonParkData = JsonParkData;
    }

    @Override
    protected Boolean doInBackground(Object[] params) {
        this.fill_empty_park();
        return true;
    }

    private void fill_empty_park() {
        VolleyApi api = new VolleyApi(this.context, this);
        String method = "FillEmptyParking";
        Map<String, String> params = new HashMap<>();

        try {

            JSONObject json = new JSONObject(this.JsonParkData);
            params.put("park_title", json.getString("park_title"));
            params.put("address", json.getString("address"));
            params.put("places_qty", json.getString("places_qty"));
            params.put("center_x", String.valueOf(json.getDouble("center_x")));
            params.put("center_y", String.valueOf(json.getDouble("center_y")));
            params.put("park_code", json.getString("park_code"));
            params.put("manager_id", json.getString("manager_id"));
            params.put("tariffs", json.getJSONObject("tariffs").toString());
            api.send_request(method, params);

        } catch (Exception e) {
            Log.e("FillParkingTaskJson", e.getMessage());
        }

    }

    public void VolleyResponseData(Integer header, String json_response, String method) {
        boolean success = (header == 200) ? true : false;
        this.ATC.onTaskFinish(success, json_response, this.TaskID);
    }

}
