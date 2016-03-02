package no.expertsinteams.interstellarfarming;


import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class StatusFragment extends Fragment {

    public static final float speed = 5.0f;

    public View tractor;

    private TimerTask timertask = null;

    public static StatusFragment newInstance(Bundle args) {
        StatusFragment fragment = new StatusFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public StatusFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        return inflater.inflate(R.layout.fragment_status, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FrameLayout root = (FrameLayout) getView();

        tractor = root.findViewById(R.id.mTractor);

        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    goToPoint(motionEvent.getX() - tractor.getWidth() / 2, motionEvent.getY() - tractor.getHeight() / 2);
                    return true;
                }
                return false;
            }
        });

    }

    public void goToPoint(final float x, final float y) {

        if (timertask != null) {
            timertask.cancel();
        }

        final Handler mHandler = new Handler();
        Timer timer = new Timer(false);
        timertask = new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        float tractorX = tractor.getX();
                        float tractorY = tractor.getY();

                        if (Math.sqrt(Math.pow(x - tractorX, 2) + Math.pow(y - tractorY, 2)) < 2) {
                            timertask.cancel();
                        }

                        double angle = Math.atan2(y - tractorY, x - tractorX);

                        tractor.setX((float) (tractorX + speed * Math.cos(angle)));
                        tractor.setY((float) (tractorY + speed * Math.sin(angle)));
                    }
                });
            }
        };

        timer.schedule(timertask, 0, 200);


    }


}
