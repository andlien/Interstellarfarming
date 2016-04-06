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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    public static final String IP = "10.22.71.69";
    public static final int PORT = 9191;

    public static final String MSG_CLOSE = "closed";
    public static final String MSG_START = "start";
    public static final String MSG_ABORT = "abort";
    public static final String MSG_RESUME = "resume";
    public static final String MSG_STOP = "stop";

    private GridLayout gridView;

    private Socket networkSocket;
    public NavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final DrawerLayout mDrawer = (DrawerLayout) findViewById(R.id.root);

        navigation = (NavigationView) findViewById(R.id.navigation);

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
                        goToStart();
                        break;
                    case R.id.statusPage:
                        goToStatus();
                        break;
                    default:
                        return false;
                }

                mDrawer.closeDrawer(Gravity.LEFT);
                return true;
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, StartPageFragment.newInstance(new Bundle()))
                    .commit();
        }
    }

    public void goToStart() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, StartPageFragment.newInstance(new Bundle()))
                .commit();
    }

    public void goToStatus() {
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

    public void setNetworkSocket(Socket networkSocket) {
        this.networkSocket = networkSocket;
    }

    public Socket getNetworkSocket() {
        return networkSocket;
    }

    public String getRawJSON(int id) {
        InputStream is = getResources().openRawResource(R.raw.send_json);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return writer.toString();
    }

    public static class SendStringRunner extends Thread {

        private String string;
        private Socket socket;

        public SendStringRunner(Socket socket, String string) {
            this.string = string;
            this.socket = socket;
        }

        @Override
        public void run() {
            DataOutputStream toServer = null;
            try {
                toServer = new DataOutputStream(socket.getOutputStream());
                toServer.writeBytes(string);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    toServer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class TriggerConnectionRunner extends Thread {

        MainActivity activity;
        Runnable callback;

        public TriggerConnectionRunner(MainActivity activity, Runnable callback) {
            this.activity = activity;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                if (activity.getNetworkSocket() == null) {
                    // open connection (new socket)
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(MainActivity.IP, MainActivity.PORT), 2000);
                    activity.setNetworkSocket(socket);
                } else if (activity.getNetworkSocket().isConnected()) {
                    // close the connection
                    Socket socket = activity.getNetworkSocket();
                    DataOutputStream toServer = new DataOutputStream(socket.getOutputStream());
                    toServer.writeBytes(String.format(activity.getRawJSON(R.raw.send_json), MSG_CLOSE));
                    activity.getNetworkSocket().close();
                    activity.setNetworkSocket(null);
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            callback.run();

        }

    }
}
