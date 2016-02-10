package no.expertsinteams.interstellarfarming;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by Anders on 03.02.2016.
 */
public class GridViewAdapter extends BaseAdapter {

    private Activity mActivity;

    public GridViewAdapter(Activity activity) {
        this.mActivity = activity;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Object getItem(int i) {
        switch (i) {
            case 0:
                return Color.RED;
            case 1:
                return Color.BLUE;
            case 2:
                return Color.YELLOW;
            case 3:
                return Color.GREEN;
            default:
                return null;
        }
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = new View(mActivity);
        v.setBackgroundColor((int) getItem(i));
        return v;
    }
}
