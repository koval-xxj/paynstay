package valpio_k.paynstay;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import valpio_k.paynstay.activities.AddLocationActivity;
import valpio_k.paynstay.activities.LocationListActivity;
import valpio_k.paynstay.activities.LoginActivity;
import valpio_k.paynstay.gn_classes.SaveDataSharPref;

public class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SaveDataSharPref fileSettings;
    private String user_info;
    private final int ACTIVITY_LOGIN = 1;
    private final int ADD_LOCATION_ACTIVITY = 2;
    private final int LOCATION_LIST_ACTIVITY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_base);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        this.fileSettings = new SaveDataSharPref(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.user_info = this.fileSettings.read_string("user_info");

        if (this.user_info == "") {
            this.go_to_activity(ACTIVITY_LOGIN);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.base, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.add_location:
                this.go_to_activity(ADD_LOCATION_ACTIVITY);
                break;
            case R.id.location_list:
                this.go_to_activity(LOCATION_LIST_ACTIVITY);
                break;
        }

//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        switch (requestCode) {
            case ADD_LOCATION_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    this.go_to_activity(LOCATION_LIST_ACTIVITY);
                }
                break;
        }

    }

    private void go_to_activity(final int activity) {
        switch (activity) {
            case ACTIVITY_LOGIN:
                Intent LogIntent = new Intent(this, LoginActivity.class);
                startActivity(LogIntent);
                break;
            case ADD_LOCATION_ACTIVITY:
                Intent AddLocIntent = new Intent(this, AddLocationActivity.class);
                startActivityForResult(AddLocIntent, ADD_LOCATION_ACTIVITY);
                break;
            case LOCATION_LIST_ACTIVITY:
                Intent LocListIntent = new Intent(this, LocationListActivity.class);
                startActivity(LocListIntent);
                break;
        }
    }
}
