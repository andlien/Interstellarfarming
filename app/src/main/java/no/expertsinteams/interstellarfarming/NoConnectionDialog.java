package no.expertsinteams.interstellarfarming;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.net.Socket;

/**
 * Created by Anders on 02.02.2016.
 */
public class NoConnectionDialog extends DialogFragment {

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

        builder.setTitle("No connection to server on " + MainActivity.IP + ":" +MainActivity.PORT);
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                new MainActivity.TriggerConnectionRunner((MainActivity) getActivity(), new Runnable() {
                    @Override
                    public void run() {
                        NoConnectionDialog.newInstance(new Bundle()).show(activity.getSupportFragmentManager(), "TAG");
                    }
                }).start();

                dismiss();

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
