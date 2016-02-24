package no.expertsinteams.interstellarfarming;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {


    private GridLayout gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final DrawerLayout mDrawer = (DrawerLayout) findViewById(R.id.root);
        NavigationView navigation = (NavigationView) findViewById(R.id.navigation);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, 0, 0);
        mDrawer.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();

        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.startPage:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, StartPageFragment.newInstance(new Bundle()))
                                .commit();
                        break;
                    case R.id.statusPage:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, StatusFragment.newInstance(new Bundle()))
                                .commit();
                        break;
                    default:
                        return false;
                }

                mDrawer.closeDrawer(Gravity.LEFT);
                return true;
            }
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, StatusFragment.newInstance(new Bundle()))
                .commit();

    }

    public void mowField(final int id) {

        final Snackbar mSnack = Snackbar.make(gridView, "Mowing " + id, Snackbar.LENGTH_INDEFINITE);
        mSnack.show();

        new CountDownTimer(30000, 1000) {

            @Override
            public void onTick(long l) {
                mSnack.setText("Mowing " + id + ", time left: " + l/1000);
            }

            @Override
            public void onFinish() {
                mSnack.dismiss();
            }
        }.start();
    }

}
