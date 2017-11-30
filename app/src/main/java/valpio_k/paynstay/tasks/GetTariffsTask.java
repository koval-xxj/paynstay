package valpio_k.paynstay.tasks;

import android.content.Context;
import android.os.AsyncTask;

import java.util.HashMap;
import java.util.Map;

import valpio_k.paynstay.gn_classes.VolleyApi;
import valpio_k.paynstay.interfaces.ActivityTasksCallback;
import valpio_k.paynstay.interfaces.VolleyResponse;

/**
 * Created by SERHIO on 12.10.2017.
 */

public class GetTariffsTask extends AsyncTask implements VolleyResponse {

    private Context context;
    private ActivityTasksCallback ATC;
    private Integer TaskID;
    private String ParkId;

    public GetTariffsTask(Context context, Integer taskID, String parkId) {
        this.context = context;
        this.TaskID = taskID;
        this.ATC = (ActivityTasksCallback) context;
        this.ParkId = parkId;
    }

    @Override
    protected Boolean doInBackground(Object[] params) {
        this.getTariffs();
        return true;
    }

    private void getTariffs() {
        VolleyApi api = new VolleyApi(this.context, this);
        String method = "GetTariffList";

        Map<String, String> params = new HashMap<>();
        params.put("parking_id", this.ParkId);
        api.send_request(method, params);

    }

    public void VolleyResponseData(Integer header, String json_response, String method) {
        boolean success = (header == 200) ? true : false;
        this.ATC.onTaskFinish(success, json_response, this.TaskID);
    }

}