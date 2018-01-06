package abdullahhafeez.me.prototype3.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import abdullahhafeez.me.prototype3.R;

/**
 * Created by Abdullah on 6/19/2017.
 */

public class Category2 extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.category2_layout, container, false);

        return view;
    }

}
