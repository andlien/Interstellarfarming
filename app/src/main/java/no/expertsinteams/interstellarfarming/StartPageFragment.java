package no.expertsinteams.interstellarfarming;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.RequestQueue;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.AccessibleObject;
import java.net.Socket;


/**
 * A simple {@link Fragment} subclass.
 */
public class StartPageFragment extends Fragment {

    private RequestQueue queue;
    private MainActivity activity;

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

        Button button = (Button) getActivity().findViewById(R.id.setupConnection);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        InputStream is = getResources().openRawResource(R.raw.test);
                        Writer writer = new StringWriter();
                        char[] buffer = new char[1024];
                        try {
                            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                            int n;
                            while ((n = reader.read(buffer)) != -1) {
                                writer.write(buffer, 0, n);
                            }

                            activity.setNetworkSocket(new Socket("10.22.71.69", 9191));
                            DataOutputStream toServer = new DataOutputStream(activity.getNetworkSocket().getOutputStream());
                            toServer.writeBytes(writer.toString());

                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                is.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });

    }

}
