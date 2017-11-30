package valpio_k.paynstay.tasks;

import android.content.Context;
import android.os.AsyncTask;

import java.util.HashMap;
import java.util.Map;

import valpio_k.paynstay.gn_classes.VolleyApi;
import valpio_k.paynstay.interfaces.ActivityTasksCallback;
import valpio_k.paynstay.interfaces.VolleyResponse;

/**
 * Created by KS on 09.09.2017.
 */

public class UserLoginTask extends AsyncTask <Void, Void, Boolean> implements VolleyResponse {

    private String mEmail;
    private String mPassword;

    private Context context;
    private ActivityTasksCallback ATC;
    private VolleyApi Api;
    private Integer TaskID;

    public UserLoginTask(Context context, String email, String password, Integer TaskId) {
        this.mEmail = email;
        this.mPassword = password;
        this.context = context;
        this.ATC = (ActivityTasksCallback) context;
        this.TaskID = TaskId;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        this.Api = new VolleyApi(this.context, this);
        this.sign_in();
        return true;
    }

    private void sign_in() {
        String method = "ManagerLogin";

        Map<String, String> params = new HashMap<>();
        params.put("user_email", this.mEmail);
        params.put("user_pass", this.mPassword);

        this.Api.send_request(method, params);
    }

    public void VolleyResponseData(Integer header, String json_response, String method) {
        boolean success = (header == 200) ? true : false;
        this.ATC.onTaskFinish(success, json_response, this.TaskID);
    }

}
