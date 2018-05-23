package com.example.ppere.proy_android_consumidor_fiware;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by ppere on 31/10/2017.
 */

public class StatusFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity)getActivity()).FragmentStatusId=getId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vi=inflater.inflate(R.layout.status_layout, container, false);

        TextView ll = (TextView) vi.findViewById(R.id.TextViewEstado);
        if (ll != null) {
            ll.setGravity(Gravity.LEFT);
            ll.append(((MainActivity)getActivity()).getLOG());
        }

        ScrollView ll1 = (ScrollView) vi.findViewById(R.id.scrollView);
        if (ll1 != null) {
            ll1.fullScroll(View.FOCUS_DOWN);

        }

        return vi;

    }
}


