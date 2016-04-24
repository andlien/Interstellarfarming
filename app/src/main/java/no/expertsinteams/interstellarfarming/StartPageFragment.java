package no.expertsinteams.interstellarfarming;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.net.Socket;


/**
 * A simple {@link Fragment} subclass.
 */
public class StartPageFragment extends Fragment {

    private MainActivity activity;

    public static final String CONNECT = "Connect to server";
    public static final String DISCONNECT = "Disconnect from server";

    public static StartPageFragment newInstance(Bundle bundle) {

        StartPageFragment fragment = new StartPageFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    public StartPageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start_page, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = (MainActivity) getActivity();
        Socket mSocket = activity.getNetworkSocket();

        final Button button = (Button) getActivity().findViewById(R.id.setupConnection);
        Button saveip = (Button) activity.findViewById(R.id.save_ip);

        final EditText edit = (EditText) activity.findViewById(R.id.ip_input);
        edit.setText(MainActivity.IP);

        assert saveip != null;
        saveip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.IP = edit.getText().toString();
            }
        });

        button.setText(mSocket != null && mSocket.isConnected()? DISCONNECT : CONNECT);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new MainActivity.TriggerConnectionRunner((MainActivity) getActivity(), new Runnable() {
                    @Override
                    public void run() {
                        button.post(new Runnable() {
                            @Override
                            public void run() {
                                Socket mSocket = activity.getNetworkSocket();
                                button.setText(mSocket != null && mSocket.isConnected() ? DISCONNECT : CONNECT);
                            }
                        });
                    }
                }, new Runnable() {
                    @Override
                    public void run() {

                    }
                }).start();

            }
        });

    }

}
