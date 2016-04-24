package no.expertsinteams.interstellarfarming;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    public static String IP = "10.22.71.122";
    public static int PORT = 9191;

    public static final String MODULE_NAME = "app";

    public static final String MSG_CLOSE = "closed";
    public static final String MSG_START = "start";
    public static final String MSG_ABORT = "abort";
    public static final String MSG_RESUME = "resume";
    public static final String MSG_STOP = "stop";
    public static final String MSG_STATUS = "getstatus";

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
        assert mDrawer != null;
        mDrawer.addDrawerListener(mDrawerToggle);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (networkSocket != null && networkSocket.isConnected()) {
            new TriggerConnectionRunner(this, new Runnable() {
                @Override
                public void run() {

                }
            }, new Runnable() {
                @Override
                public void run() {

                }
            }).start();
        }
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
//                System.out.println(string);
//                if (socket.isClosed()) {
//                    throw new RuntimeException();
//                }
                toServer = new DataOutputStream(socket.getOutputStream());
                toServer.writeBytes(string);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class RecieveStringRunner extends Thread {

        private RecieveStringListener callback;
        private Socket socket;

        public RecieveStringRunner(Socket socket, RecieveStringListener callback) {
            this.callback = callback;
            this.socket = socket;
        }

        @Override
        public void run() {
            BufferedReader fromServer = null;
            try {
                fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String line = fromServer.readLine();
                JSONClass a = new Gson().fromJson(line, JSONClass.class);
                callback.setRecieve(a);

            } catch (IOException e) {
                e.printStackTrace();
            }

            callback.run();
        }
    }

    public static abstract class RecieveStringListener implements Runnable {

        private JSONClass recieve;

        public JSONClass getRecieve() {
            return recieve;
        }

        public void setRecieve(JSONClass recieve) {
            this.recieve = recieve;
        }

        @Override
        public abstract void run();
    }

    public static class TriggerConnectionRunner extends Thread {

        MainActivity activity;
        Runnable callback;
        Runnable error;

        public TriggerConnectionRunner(MainActivity activity, Runnable callback, Runnable error) {
            this.activity = activity;
            this.callback = callback;
            this.error = error;
        }

        @Override
        public void run() {
            try {
                if (activity.getNetworkSocket() == null || !activity.getNetworkSocket().isConnected()) {
                    // open connection (new socket)
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(MainActivity.IP, MainActivity.PORT), 1000);
                    activity.setNetworkSocket(socket);
                } else if (activity.getNetworkSocket().isConnected()) {
                    // close the connection
                    Socket socket = activity.getNetworkSocket();
                    DataOutputStream toServer = new DataOutputStream(socket.getOutputStream());
                    toServer.writeBytes(new Gson().toJson(new JSONClass(
                            MainActivity.MODULE_NAME,
                            MainActivity.MSG_CLOSE,
                            new float[]{0f, 0f},
                            new float[]{0f, 0f},
                            new float[]{0f, 0f})));
                    activity.getNetworkSocket().close();
                    activity.setNetworkSocket(null);
                }

        } catch (IOException e) {
                error.run();
                return;
            }

            callback.run();

        }

    }
}
