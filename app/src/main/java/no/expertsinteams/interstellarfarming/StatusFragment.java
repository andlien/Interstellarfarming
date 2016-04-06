package no.expertsinteams.interstellarfarming;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

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

    public View tractor;
    public ImageView imageView;

    private Timer timer;
    private LinkedList<TimerTask> taskQueue;
    private boolean isRunning = false;

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

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FrameLayout root = (FrameLayout) getView();

        final MainActivity activity = (MainActivity) getActivity();

        if (activity.getNetworkSocket() == null || !activity.getNetworkSocket().isConnected()) {
            NoConnectionDialog.newInstance(new Bundle()).show(getFragmentManager(), "TAG");
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    InputStream is = getResources().openRawResource(R.raw.send_json);
                    Writer writer = new StringWriter();
                    char[] buffer = new char[1024];
                    try {
                        Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                        int n;
                        while ((n = reader.read(buffer)) != -1) {
                            writer.write(buffer, 0, n);
                        }

                        Socket mSocket = activity.getNetworkSocket();
                        DataOutputStream toSErver = new DataOutputStream(mSocket.getOutputStream());
                        toSErver.writeBytes(String.format(writer.toString(), "ok"));

                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputStream is = getResources().openRawResource(R.raw.send_json);
                Writer writer = new StringWriter();
                char[] buffer = new char[1024];
                try {
                    Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    int n;
                    while ((n = reader.read(buffer)) != -1) {
                        writer.write(buffer, 0, n);
                    }

                    Socket mSocket = activity.getNetworkSocket();
                    DataOutputStream toSErver = new DataOutputStream(mSocket.getOutputStream());
                    toSErver.writeBytes(String.format(writer.toString(), "stop"));

                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }, 5000);



//        imageView = (ImageView) getActivity().findViewById(R.id.farm_image);
//        tractor = root.findViewById(R.id.mTractor);

//        timer = new Timer(false);
//        taskQueue = new LinkedList<>();
//
//        imageView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                    addWaypoint(motionEvent.getX() - tractor.getWidth() / 2, motionEvent.getY() - tractor.getHeight() / 2);
//                    return true;
//                }
//                return false;
//            }
//        });



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

    public void addWaypoint(final float x, final float y) {

        TimerTask timertask = new TimerTask() {
            @Override
            public void run() {
                final TimerTask timertask = this;
                tractor.post(new Runnable() {
                    @Override
                    public void run() {
                        float tractorX = tractor.getX() - imageView.getX();
                        float tractorY = tractor.getY() - imageView.getY();

                        if (Math.sqrt(Math.pow(x - tractorX, 2) + Math.pow(y - tractorY, 2)) < speed) {

                            if (!taskQueue.isEmpty()) {
                                timer.schedule(taskQueue.pop(), 0, 50);
                            } else {
                                isRunning = false;
                            }
                            timertask.cancel();
                            return;
                        }

                        double angle = Math.atan2(y - tractorY, x - tractorX);

                        tractor.setX((float) (tractorX + imageView.getX() + speed * Math.cos(angle)));
                        tractor.setY((float) (tractorY + imageView.getY() + speed * Math.sin(angle)));
                    }
                });
            }

        };

        taskQueue.add(timertask);
        if (!isRunning) {
            isRunning = true;
            timer.schedule(taskQueue.pop(), 0, 50);
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
//        timer.cancel();
    }
}
