package valpio_k.paynstay.gn_classes;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by SERHIO on 08.09.2017.
 */

public class SaveDataSharPref {

    private SharedPreferences mSettings;
    private String APP_PREFERENCES = "";

    public SaveDataSharPref(Context context) {
        mSettings = context.getSharedPreferences(APP_PREFERENCES, context.MODE_PRIVATE);
    }

    public String read_string(String key) {
        return (this.mSettings.contains(key)) ? mSettings.getString(key, "0") : "";
    }

    public void put_string(String key, String data) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(key, data);
        editor.apply();
    }

}
