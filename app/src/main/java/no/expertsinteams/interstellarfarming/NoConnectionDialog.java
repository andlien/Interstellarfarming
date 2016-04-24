package no.expertsinteams.interstellarfarming;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.net.Socket;

/**
 * Created by Anders on 02.02.2016.
 */
public class NoConnectionDialog extends DialogFragment {

    public static final String TAG = "no_connection_tag";

    public static NoConnectionDialog newInstance(Bundle bundle) {
        NoConnectionDialog fragment = new NoConnectionDialog();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final MainActivity activity = (MainActivity) getActivity();
        Socket mSocket = activity.getNetworkSocket();

        if (mSocket != null && mSocket.isConnected()) {
            dismiss();
        }

        builder.setTitle("Still no connection to server on " + MainActivity.IP + ":" +MainActivity.PORT);
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                dismiss();
                new MainActivity.TriggerConnectionRunner((MainActivity) getActivity(), new Runnable() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                StatusFragment fr = ((StatusFragment) getTargetFragment());
                                fr.handler.post(fr.updateTractorPosition);
                                fr.handler.post(fr.checkConnection);
                            }
                        });
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        ((StatusFragment) getTargetFragment()).handler.post(((StatusFragment) getTargetFragment()).checkConnection);
                    }
                }).start();


            }
        });
        builder.setNegativeButton("Return to start", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                activity.goToStart();
                dismiss();
            }
        });

        return builder.create();
    }
}
