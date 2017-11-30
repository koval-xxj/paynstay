package valpio_k.paynstay.fragments;

/**
 * Created by SERHIO on 05.09.2017.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class DialogWindow extends DialogFragment {

    private String message = "Error";
    private String title = "Message";

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder
                .setTitle(this.title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(this.message)
                .setPositiveButton("OK", null)
                //.setNegativeButton("Отмена", null)
                .create();
    }

    public void set_message(String message) {
        this.message = message;
    }

    public void set_title(String title) {
        this.title = title;
    }

}
