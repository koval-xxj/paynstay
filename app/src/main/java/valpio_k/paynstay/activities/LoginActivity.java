package valpio_k.paynstay.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import valpio_k.paynstay.R;
import valpio_k.paynstay.fragments.DialogWindow;
import valpio_k.paynstay.gn_classes.SaveDataSharPref;
import valpio_k.paynstay.interfaces.ActivityTasksCallback;
import valpio_k.paynstay.tasks.GetNewPassTask;
import valpio_k.paynstay.tasks.UserLoginTask;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>, ActivityTasksCallback, View.OnClickListener {

    /**
     * Id to identity READ_CONTACTS permission request.
     */

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private final int TASK_LOGIN = 1;
    private final int TASK_FORGOT_PASS = 2;

    // UI references.

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private AppCompatTextView mLoginNoticeView;
    private String [] aEmailNotice;
    private AppCompatButton btnSignIn;
    private AppCompatButton btnForgotPass;
    private AppCompatButton btnPassSignIn;
    private String email;
    private Integer ExitApp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        this.mLoginNoticeView = (AppCompatTextView) findViewById(R.id.login_notice);
        this.btnSignIn = (AppCompatButton) findViewById(R.id.email_sign_in_button);
        this.btnForgotPass = (AppCompatButton) findViewById(R.id.forgot_password);
        this.btnPassSignIn = (AppCompatButton) findViewById(R.id.pass_sign_in);
        this.btnSignIn.setOnClickListener(this);
        this.btnForgotPass.setOnClickListener(this);
        this.btnPassSignIn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //finish();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        //mEmailView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        //String email = mEmailView.getText().toString();

        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (!this.check_email()) {
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(this, this.email, password, TASK_LOGIN);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return Pattern.matches("^([a-z0-9_-]+\\.)*[a-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,6}$", email);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        //addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    public void showDialog(String title, String message) {
        DialogWindow dialog = new DialogWindow();
        dialog.set_title(title);
        dialog.set_message(message);
        dialog.show(getSupportFragmentManager(), "custom");
    }

    @Override
    public void onTaskFinish(boolean success, String responce, Integer task) {
        mAuthTask = null;
        showProgress(false);

        if (success) {

            switch (task) {
                case TASK_LOGIN:
                    SaveDataSharPref data = new SaveDataSharPref(this);
                    data.put_string("user_info", responce);
                    finish();
                    break;
                case TASK_FORGOT_PASS:
                    this.mPasswordView.setVisibility(View.VISIBLE);
                    this.btnForgotPass.setVisibility(View.VISIBLE);
                    this.btnSignIn.setVisibility(View.VISIBLE);
                    this.btnPassSignIn.setVisibility(View.GONE);
                    this.mLoginNoticeView.setText(String.valueOf(this.aEmailNotice[1]));
                    break;
            }

        } else {
            String error_message = responce;
            try {
                JSONObject json = new JSONObject(responce);
                error_message = json.getString("error");
            } catch (Exception e) {
                Log.e("Json Reg Task error", e.getMessage());
            }
            this.showDialog("Message", error_message);

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.email_sign_in_button:
                attemptLogin();
                break;
            case R.id.forgot_password:
                this.mPasswordView.setVisibility(View.GONE);
                this.btnForgotPass.setVisibility(View.GONE);
                this.btnSignIn.setVisibility(View.GONE);
                this.btnPassSignIn.setVisibility(View.VISIBLE);
                this.aEmailNotice = getResources().getStringArray(R.array.enter_email_notice);
                this.mLoginNoticeView.setText(String.valueOf(this.aEmailNotice[0]));
                this.mLoginNoticeView.setVisibility(View.VISIBLE);
                break;
            case R.id.pass_sign_in:
                this.get_new_password();
                break;
        }
    }

    private void get_new_password() {
        if (!this.check_email()) {
            this.mEmailView.requestFocus();
            return;
        }

        showProgress(true);
        GetNewPassTask passTask = new GetNewPassTask(this, this.email, TASK_FORGOT_PASS);
        passTask.execute((Void) null);
    }

    private boolean check_email() {
        boolean check = true;

        this.email = mEmailView.getText().toString();

        if (TextUtils.isEmpty(this.email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            check = false;
        } else if (!this.isEmailValid(this.email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            check = false;
        }

        return check;
    }

    @Override
    public void onBackPressed()
    {
        if (this.ExitApp < 1) {
            this.ExitApp++;
            Toast.makeText(this, R.string.click_exit_app, Toast.LENGTH_LONG).show();
            return;
        }

        moveTaskToBack(true);
        finish();
        System.runFinalizersOnExit(true);
        System.exit(0);
    }

}

