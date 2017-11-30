package valpio_k.paynstay.gn_classes;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by SERHIO on 09.10.2017.
 */

public class General {

    public static ArrayList get_hours() {
        ArrayList hours = new ArrayList();

        for (int i = 0; i < 25; i++) {
            String hour = String.valueOf(i);
            hour = (hour.length() < 2) ? "0" + hour + ":00" : hour + ":00";
            hours.add(hour);
        }

        return hours;
    }

    public static String format_date(String date, String format_parce, String return_format) {
        String format_date = null;
        //yyyy-MM-dd HH:mm:ss
        try {
            Date parce_date = new SimpleDateFormat(format_parce).parse(date);
            SimpleDateFormat formater = new SimpleDateFormat(return_format);
            format_date = formater.format(parce_date);
        } catch (Exception e) {
            Log.e("GeneralFormat_date", e.getMessage());
        }

        return format_date;
    }

}
