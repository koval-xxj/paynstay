package valpio_k.paynstay.gn_classes;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;

/**
 * Created by SERHIO on 11.10.2017.
 */

public class PriceControl implements TextWatcher {

    private AutoCompleteTextView control_view;
    private boolean set = false;
    private Integer price_first_part = 0;

    public PriceControl (AutoCompleteTextView view) {
        this.control_view = view;
    }

    @Override
    public void afterTextChanged(Editable s) {
        // Прописываем то, что надо выполнить после изменения текста
        //Log.d("TextWatcher", s.toString());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //Log.d("TextWatcher", "" + s);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        String price = String.valueOf(s);
        String[] elemData = price.split(".");

        Integer firstPart = (elemData.length > 0) ? elemData[0].length() : this.price_first_part;

        if (price.contains(".") && (firstPart != this.price_first_part || !this.set)) {
            this.set = true;
            this.price_first_part = firstPart;
            Integer numb = price.length() + 2;
            this.control_view.setFilters(new InputFilter[]{new InputFilter.LengthFilter(numb)});
        }

        if (!price.contains(".") && this.set) {
            this.set = false;
            this.control_view.setFilters(new InputFilter[]{});
        }
    }

}
