package valpio_k.paynstay.tasks;

import android.content.Context;
import android.os.AsyncTask;

import java.util.HashMap;
import java.util.Map;

import valpio_k.paynstay.gn_classes.VolleyApi;
import valpio_k.paynstay.interfaces.ActivityTasksCallback;
import valpio_k.paynstay.interfaces.VolleyResponse;

/**
 * Created by SERHIO on 11.10.2017.
 */

public class GetAllCarTypesTask extends AsyncTask<Void, Void, Boolean> implements VolleyResponse {

    private Context context;
    private ActivityTasksCallback ATC;
    private Integer TaskID;

    public GetAllCarTypesTask(Context context, Integer taskID) {
        this.context = context;
        this.TaskID = taskID;
        this.ATC = (ActivityTasksCallback) context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        this.getCarTypes();
        return true;
    }

    private void getCarTypes() {
        VolleyApi api = new VolleyApi(this.context, this);
        String method = "GetCarTypes";
        Map<String, String> params = new HashMap<>();
        api.send_request(method, params);
    }

    public void VolleyResponseData(Integer header, String json_response, String method) {
        boolean success = (header == 200) ? true : false;
        this.ATC.onTaskFinish(success, json_response, this.TaskID);
    }

}
