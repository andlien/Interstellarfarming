package no.expertsinteams.interstellarfarming;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final GridLayout gridView = (GridLayout) findViewById(R.id.mGridView);

        for (int i = 0; i < gridView.getChildCount(); i++) {
            final int finalI = i;
            gridView.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(MowDialog.FIELD_ID_KEY, finalI);
                    DialogFragment fragment = MowDialog.newInstance(bundle);
                    fragment.show(getSupportFragmentManager(), "DIALOG");
                }
            });
        }

    }

    public void mowField(int id) {

    }

}
