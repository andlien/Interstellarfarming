package no.expertsinteams.interstellarfarming;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.Socket;


/**
 * A simple {@link Fragment} subclass.
 */
public class StatusFragment extends Fragment {

    public static final String RUNNING_TAG = "RUNNING";
    public static final String NOT_RUNNING_TAG = "NOT_RUNNING";

    private View tractor;
    private ImageView imageView;

    private Button stopOrResume;
    private Button abort;

    private Gson gson;
    Handler handler;

    float oldX;
    float oldY;

    private boolean first = true;

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
            final MainActivity activity = ((MainActivity)getActivity());
            new MainActivity.RecieveStringRunner(((MainActivity) getActivity()).getNetworkSocket(), new MainActivity.RecieveStringListener() {
                @Override
                public void run() {
                    final JSONClass recv = getRecieve();
                    if (recv != null) {
                        tractor.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!(tractor.getX() == 0f && tractor.getY() == 0f) &&
                                        (oldX != recv.area[0] || oldY != recv.area[1])) {
                                    System.out.println("IAM RUNNING");

                                    if (stopOrResume.getTag().equals(NOT_RUNNING_TAG)) {
                                        System.out.println("ADDING KEEP SCREEN ON");
                                        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                    }
                                    stopOrResume.setTag(RUNNING_TAG);

                                    stopOrResume.setVisibility(View.VISIBLE);
                                    abort.setVisibility(View.VISIBLE);

                                    tractor.setX(recv.area[0] * imageView.getWidth() + imageView.getX());
                                    tractor.setY(recv.area[1] * imageView.getHeight() + imageView.getY());
                                } else if (tractor.getX() == 0f && tractor.getY() == 0f) {
                                    tractor.setX(recv.area[0] * imageView.getWidth() + imageView.getX());
                                    tractor.setY(recv.area[1] * imageView.getHeight() + imageView.getY());
                                } else {

                                    if (stopOrResume.getTag().equals(RUNNING_TAG)) {
                                        System.out.println("CLEARING KEEP SCREEN ON");
                                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                    }

                                    System.out.println("IAM NOT RUNNING");
                                    stopOrResume.setTag(NOT_RUNNING_TAG);
                                }

                                oldX = recv.area[0];
                                oldY = recv.area[1];

                                tractor.setVisibility(View.VISIBLE);

                                handler.post(updateTractorPosition);
                            }
                        });

                    } else {
                        Socket socket = activity.getNetworkSocket();
                        if (socket != null) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            activity.setNetworkSocket(null);
                        }
                    }
                }
            }).start();
        }
    };

    Runnable checkConnection = new Runnable() {
        @Override
        public void run() {
            // check every 5 seconds
            handler.postDelayed(checkConnection, 5000);

            Socket mSocket = ((MainActivity) getActivity()).getNetworkSocket();
            if (mSocket == null || !mSocket.isConnected()) {
                handler.removeCallbacksAndMessages(null);
                NoConnectionDialog dialog = NoConnectionDialog.newInstance(new Bundle());
                dialog.setTargetFragment(StatusFragment.this, -1);
                dialog.show(getFragmentManager(), NoConnectionDialog.TAG);
            }

        }
    };

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FrameLayout root = (FrameLayout) getView();

        gson = new Gson();
        handler = new Handler();

        final MainActivity activity = (MainActivity) getActivity();

        imageView = (ImageView) root.findViewById(R.id.farm_image);
        tractor = root.findViewById(R.id.mTractor);
        stopOrResume = (Button) root.findViewById(R.id.stopOrResume);
        abort = (Button) root.findViewById(R.id.abort);

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, final MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP &&
                        !stopOrResume.getTag().equals(RUNNING_TAG)) {

                    tractor.setVisibility(View.VISIBLE);

                    System.out.println("Sent START: x=" + event.getX() / imageView.getWidth() + " and y=" + event.getY() / imageView.getHeight());

                    new MainActivity.SendStringRunner(activity.getNetworkSocket(),
                            gson.toJson(new JSONClass(
                                    MainActivity.MODULE_NAME,
                                    MainActivity.MSG_START,
                                    new float[]{event.getX() / imageView.getWidth(), event.getY() / imageView.getHeight()},
                                    new float[]{0f, 0f},
                                    new float[]{0f, 0f}))).start();


                }
                return true;
            }
        });

        stopOrResume.setTag(NOT_RUNNING_TAG);
        stopOrResume.setText("Pause/Resume");
        abort.setText("Abort");
        stopOrResume.setVisibility(View.VISIBLE);
        abort.setVisibility(View.VISIBLE);

        if (!stopOrResume.hasOnClickListeners()) {
            stopOrResume.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("Pressed stopOrResume, Tag=" + stopOrResume.getTag());
                    if (stopOrResume.getTag().equals(RUNNING_TAG)) {
                        new MainActivity.SendStringRunner(activity.getNetworkSocket(),
                                gson.toJson(new JSONClass(
                                        MainActivity.MODULE_NAME,
                                        MainActivity.MSG_STOP,
                                        new float[]{0f, 0f},
                                        new float[]{0f, 0f},
                                        new float[]{0f, 0f}))).start();
                    } else {
                        new MainActivity.SendStringRunner(activity.getNetworkSocket(),
                                gson.toJson(new JSONClass(
                                        MainActivity.MODULE_NAME,
                                        MainActivity.MSG_RESUME,
                                        new float[]{0f, 0f},
                                        new float[]{0f, 0f},
                                        new float[]{0f, 0f}))).start();
                    }

                }
            });
        }

        if (!abort.hasOnClickListeners()) {
            abort.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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

        handler.post(checkConnection);

        if (activity.getNetworkSocket() != null && activity.getNetworkSocket().isConnected()) {
            handler.post(updateTractorPosition);
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
        handler.removeCallbacksAndMessages(null);
    }
}
