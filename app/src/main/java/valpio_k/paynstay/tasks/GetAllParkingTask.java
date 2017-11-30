package valpio_k.paynstay.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import valpio_k.paynstay.gn_classes.SaveDataSharPref;
import valpio_k.paynstay.gn_classes.VolleyApi;
import valpio_k.paynstay.interfaces.ActivityTasksCallback;
import valpio_k.paynstay.interfaces.VolleyResponse;

/**
 * Created by SERHIO on 12.10.2017.
 */

public class GetAllParkingTask extends AsyncTask implements VolleyResponse {

    private Context context;
    private ActivityTasksCallback ATC;
    private Integer TaskID;
    private Double longitude;
    private Double latitude;

    public GetAllParkingTask(Context context, Integer taskID, Double longitude, Double latitude) {
        this.context = context;
        this.TaskID = taskID;
        this.ATC = (ActivityTasksCallback) context;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    @Override
    protected Boolean doInBackground(Object[] params) {
        this.getAllParkings();
        return true;
    }

    private void getAllParkings() {
        VolleyApi api = new VolleyApi(this.context, this);
        String method = "GetParkingList";
        Map<String, String> params = new HashMap<>();
        SaveDataSharPref data = new SaveDataSharPref(this.context);
        String user_info = data.read_string("user_info");
        String ManagerId = null;

        try {
            JSONObject json = new JSONObject(user_info);
            ManagerId = json.getString("user_id");
        } catch (Exception e) {
            Log.e("GetAllParkingTaskJson", e.getMessage());
        }

        if (ManagerId != null) {
            params.put("manager_id", ManagerId);
            params.put("latitude", String.valueOf(this.latitude));
            params.put("longitude", String.valueOf(this.longitude));
            api.send_request(method, params);
        }

    }

    public void VolleyResponseData(Integer header, String json_response, String method) {
        boolean success = (header == 200) ? true : false;
        this.ATC.onTaskFinish(success, json_response, this.TaskID);
    }

}
