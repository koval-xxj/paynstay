package valpio_k.paynstay.tasks;

import android.content.Context;
import android.os.AsyncTask;

import java.util.HashMap;
import java.util.Map;

import valpio_k.paynstay.gn_classes.VolleyApi;
import valpio_k.paynstay.interfaces.ActivityTasksCallback;
import valpio_k.paynstay.interfaces.VolleyResponse;

/**
 * Created by SERHIO on 04.10.2017.
 */

public class GetNewPassTask extends AsyncTask<Void, Void, Boolean> implements VolleyResponse {

    private String mEmail;

    private Context context;
    private ActivityTasksCallback ATC;
    private VolleyApi Api;
    private Integer TaskID;

    public GetNewPassTask(Context context, String email, Integer TaskID) {
        this.context = context;
        this.mEmail = email;
        this.TaskID = TaskID;
        this.ATC = (ActivityTasksCallback) context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        this.Api = new VolleyApi(this.context, this);
        this.get_new_pass();
        return true;
    }

    private void get_new_pass() {
        String method = "ForgotPass";

        Map<String, String> params = new HashMap<>();
        params.put("user_email", this.mEmail);

        this.Api.send_request(method, params);
    }

    public void VolleyResponseData(Integer header, String json_response, String method) {
        boolean success = (header == 200) ? true : false;
        this.ATC.onTaskFinish(success, json_response, this.TaskID);
    }

}
