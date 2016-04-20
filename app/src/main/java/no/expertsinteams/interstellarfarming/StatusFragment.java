package no.expertsinteams.interstellarfarming;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class StatusFragment extends Fragment {

    public static final float speed = 4.0f;
    public final int intervall = 500;

    public static final String RUNNING_TAG = "RUNNING";
    public static final String PAUSED_TAG = "PAUSED";
    public static final String NOT_RUNNING_TAG = "NOT_RUNNING";

    private View tractor;
    private ImageView imageView;
    private ProgressBar mProgressbar;

    private Button stopOrResume;
    private Button abort;

    private Gson gson;
    private Handler handler;

    public static StatusFragment newInstance(Bundle args) {
        StatusFragment fragment = new StatusFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public StatusFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_status, container, false);
    }

    Runnable updateTractorPosition = new Runnable() {
        @Override
        public void run() {
            new MainActivity.RecieveStringRunner(((MainActivity) getActivity()).getNetworkSocket(), new MainActivity.RecieveStringListener() {
                @Override
                public void run() {
                    final JSONClass recv = getRecieve();
                    tractor.post(new Runnable() {
                        @Override
                        public void run() {
                            tractor.setX(recv.area[0]);
                            tractor.setY(recv.area[1]);

                            mProgressbar.setProgress((int) recv.waypoints_x[0]);
                        }
                    });
                }
            });

            //run again in <intervall>ms
            handler.postDelayed(updateTractorPosition, intervall);
        }
    };

    Runnable checkConnection = new Runnable() {
        @Override
        public void run() {
            Socket mSocket = ((MainActivity) getActivity()).getNetworkSocket();
            if (mSocket == null || !mSocket.isConnected()) {
                NoConnectionDialog.newInstance(new Bundle()).show(getFragmentManager(), NoConnectionDialog.TAG);
            }

            // check every 5 seconds
            handler.postDelayed(checkConnection, 5000);
        }
    };

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FrameLayout root = (FrameLayout) getView();

        gson = new Gson();
        handler = new Handler();

        final MainActivity activity = (MainActivity) getActivity();

        checkConnection.run();

        mProgressbar = (ProgressBar) root.findViewById(R.id.show_progress);

        imageView = (ImageView) root.findViewById(R.id.farm_image);
        tractor = root.findViewById(R.id.mTractor);

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {

                    mProgressbar.setVisibility(View.VISIBLE);
                    tractor.setVisibility(View.VISIBLE);
                    stopOrResume.setVisibility(View.VISIBLE);
                    abort.setVisibility(View.VISIBLE);

                    stopOrResume.setText("Pause");
                    abort.setText("Abort");
                    stopOrResume.setTag(RUNNING_TAG);
                    abort.setTag(RUNNING_TAG);

                    mProgressbar.setX(event.getX());
                    mProgressbar.setY(event.getY());

                    updateTractorPosition.run();

                    new MainActivity.SendStringRunner(activity.getNetworkSocket(),
                            gson.toJson(new JSONClass(
                                    MainActivity.MODULE_NAME,
                                    MainActivity.MSG_START,
                                    new float[]{event.getX(), event.getY()},
                                    new float[]{0f, 0f},
                                    new float[]{0f, 0f}))).start();


                    return true;
                }
                return false;
            }
        });

        stopOrResume = (Button) root.findViewById(R.id.stopOrResume);
        abort = (Button) root.findViewById(R.id.abort);

        if (!stopOrResume.hasOnClickListeners()) {
            stopOrResume.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handler.removeCallbacks(updateTractorPosition);
                    stopOrResume.setTag(PAUSED_TAG);
                    new MainActivity.SendStringRunner(activity.getNetworkSocket(),
                            gson.toJson(new JSONClass(
                                    MainActivity.MODULE_NAME,
                                    MainActivity.MSG_STOP,
                                    new float[]{0f, 0f},
                                    new float[]{0f, 0f},
                                    new float[]{0f, 0f}))).start();
                }
            });
        }

        if (!abort.hasOnClickListeners()) {
            abort.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handler.removeCallbacks(updateTractorPosition);
                    abort.setTag(NOT_RUNNING_TAG);
                    new MainActivity.SendStringRunner(activity.getNetworkSocket(),
                            gson.toJson(new JSONClass(
                                    MainActivity.MODULE_NAME,
                                    MainActivity.MSG_ABORT,
                                    new float[]{0f, 0f},
                                    new float[]{0f, 0f},
                                    new float[]{0f, 0f}))).start();
                }
            });
        }

        if (savedInstanceState != null) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    tractor.setX(savedInstanceState.getFloat("tractor_x") * imageView.getWidth());
                    tractor.setY(savedInstanceState.getFloat("tractor_y") * imageView.getHeight());
                }
            });
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putFloat("tractor_x", (tractor.getX() - imageView.getX()) / imageView.getWidth());
        outState.putFloat("tractor_y", (tractor.getY() - imageView.getY()) / imageView.getHeight());
        super.onSaveInstanceState(outState);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(null);
    }
}
