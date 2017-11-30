package valpio_k.paynstay.gn_classes;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by SERHIO on 11.10.2017.
 */

public class ParceCarTypesJson {

    private String CarTypesJson;
    private String[] carTypeNames;
    private Integer[] carTypeIds;

    public ParceCarTypesJson(String json) {
        this.CarTypesJson = json;
        this.parce_json();
    }

    private void parce_json() {
        try {
            JSONObject json = new JSONObject(this.CarTypesJson);

            Integer count = json.getInt("count");
            JSONArray CarArray = json.getJSONArray("items");

            this.carTypeNames = new String[count];
            this.carTypeIds = new Integer[count];

            String sCarType;
            for (int i = 0; i < count; i++) {
                sCarType = CarArray.getString(i);
                json = new JSONObject(sCarType);
                carTypeIds[i] = Integer.parseInt(json.getString("cartype_id"));
                carTypeNames[i] = json.getString("cartype_title");
            }

        } catch (Exception e) {
            Log.e("ParceCarTypesJson", e.getMessage());
        }
    }

    public String[] getCarTypeNames() {
        return this.carTypeNames;
    }

    public Integer[] getCarTypeIds() {
        return this.carTypeIds;
    }

}
