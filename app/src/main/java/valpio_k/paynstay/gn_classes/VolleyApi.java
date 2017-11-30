package valpio_k.paynstay.gn_classes;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import valpio_k.paynstay.R;
import valpio_k.paynstay.interfaces.VolleyResponse;

/**
 * Created by SERHIO on 08.09.2017.
 */

public class VolleyApi {

    private String api_token_name;
    private String api_token_value;
    private Map ApiUrls;
    private SaveDataSharPref SavedUserData;

    private Activity CurActivity;
    private VolleyResponse VolleyResp;
    private Context context;

    private Map curApiParams;

    public VolleyApi(Context context, VolleyResponse response) {
        this.context = context;
        this.CurActivity = (Activity) context;
        this.VolleyResp = response;
        this.save_api_urls();
        this.get_api_tokens();
    }

    private void save_api_urls() {
        Map<String, String> urls = new HashMap<>();
        urls.put("Auth", "http://naviapi.valpio-k.com/v1_0/auth");
        urls.put("ManagerLogin", "http://naviapi.valpio-k.com/v1_0/user/manager/login");
        urls.put("ForgotPass", "http://naviapi.valpio-k.com/v1_0/user/manager/forgot");
        urls.put("GetCarTypes", "http://naviapi.valpio-k.com/v1_0/cartype/list");
        urls.put("GetParkingList", "http://naviapi.valpio-k.com/v1_0/parking/location/list");
        urls.put("GetTariffList", "http://naviapi.valpio-k.com/v1_0/tariff/list");
        urls.put("FillEmptyParking", "http://naviapi.valpio-k.com/v1_0/parking/fill");
        this.ApiUrls = urls;
    }

    private void get_api_tokens() {
        this.SavedUserData = new SaveDataSharPref(context);
        this.api_token_name = this.SavedUserData.read_string("api_token_name");
        this.api_token_value = this.SavedUserData.read_string("api_token_value");
    }

    private void getTokens(final String method, final Map params) {

        this.curApiParams = params;

        Map<String, String> token_params = new HashMap<>();
        token_params.put("login", this.context.getString(R.string.AL));
        token_params.put("password", this.context.getString(R.string.AP));

        this.send_api_request("Auth", token_params, method);

    }

    public void send_request(final String method, final Map params) {
        if (!this.ApiUrls.containsKey(method)) {
            return;
        }
        this.send_api_request(method, params, null);
    }

    private void send_api_request(final String method, final Map params, final String secMethod) {

        if ((TextUtils.isEmpty(this.api_token_name) || TextUtils.isEmpty(this.api_token_value)) && method != "Auth") {
            this.getTokens(method, params);
            return;
        }

        RequestQueue queue = com.android.volley.toolbox.Volley.newRequestQueue(this.CurActivity);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, (String) this.ApiUrls.get(method),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        switch (method) {
                            case "Auth":
                                try {
                                    JSONObject json = new JSONObject(response);
                                    api_token_name = json.getString("token-name");
                                    api_token_value = json.getString("token-value");
                                    SavedUserData.put_string("api_token_name", api_token_name);
                                    SavedUserData.put_string("api_token_value", api_token_value);
                                } catch (Exception e) {
                                    Log.e("JSON_register", e.getMessage());
                                }

                                if (!TextUtils.isEmpty(secMethod)) {
                                    VolleyApi.this.send_api_request(secMethod, VolleyApi.this.curApiParams, null);
                                }
                                break;
                            default:
                                VolleyApi.this.VolleyResp.VolleyResponseData(200, response, method);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error.networkResponse == null) {
                            VolleyApi.this.VolleyResp.VolleyResponseData(418, VolleyApi.this.context.getString(R.string.no_internet_connection), method);
                            return;
                        }

                        switch (error.networkResponse.statusCode) {
                            case 401:
                                VolleyApi.this.getTokens(method, params);
                                break;
                            default:
                                Log.d("Resp_Error_416_method", method);
                                Log.e("Resp_Error_416", "That didn't work: " + error.networkResponse.statusCode);
                                Log.d("Resp_Error_416", "Data: " + new String(error.networkResponse.data));
                                Log.d("Resp_Error_416", "Headers:" + error.networkResponse.headers);
                                VolleyApi.this.VolleyResp.VolleyResponseData(error.networkResponse.statusCode, new String(error.networkResponse.data), method);
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();
                if (headers == null || headers.equals(Collections.emptyMap())) {
                    headers = new HashMap<>();
                }

                if (method != "Auth") {
                    headers.put("token-name", VolleyApi.this.api_token_name);
                    headers.put("token-value", VolleyApi.this.api_token_value);
                }

                return headers;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

}
