package mani.itachi.memories.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.net.URL;

import mani.itachi.memories.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AboutUsFragment extends Fragment {

    TextView github,google;

    String url;

    public AboutUsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setTitle("About Us");
        View view = inflater.inflate(R.layout.fragment_about_us, container, false);
        github = (TextView) view.findViewById(R.id.about_github);
        google = (TextView) view.findViewById(R.id.about_google);
        github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                url="https://github.com/Mani-Itachi";
                openUrl();
            }
        });
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                url="https://plus.google.com/+ManikantaJunioreinstein";
                openUrl();
            }
        });
        return view;
    }

    void openUrl(){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

}
