package no.expertsinteams.interstellarfarming;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by Anders on 02.02.2016.
 */
public class MowDialog extends DialogFragment {

    public static final String FIELD_ID_KEY = "field_id";

    public static MowDialog newInstance(Bundle bundle) {
        MowDialog fragment = new MowDialog();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Confirmation");
        builder.setPositiveButton("Start mowing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((MainActivity) getActivity()).mowField(getArguments().getInt(FIELD_ID_KEY));

                dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });

        return builder.create();
    }
}
