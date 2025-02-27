package com.example.unipiaudiobook;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatsFragment extends Fragment {



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public StatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StatsFragment newInstance(String param1, String param2) {
        StatsFragment fragment = new StatsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    //In orfer to Dispaly the Stats in the stats fragment
    TextView fulltime2;
    TextView fulltime4;

    TextView FavAuthor;
    TextView FavBook;

    Button MoralListen;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_stats, container, false);

        fulltime2=view.findViewById(R.id.tv_total_listening_time2);
        fulltime4=view.findViewById(R.id.tv_total_listening_time4);

        FavAuthor=view.findViewById(R.id.tv_books_completed1);
        FavBook=view.findViewById(R.id.tv_most_listened_genre1);

        MoralListen=view.findViewById(R.id.moralb);

        // Gets the data from the passed bundle
        Bundle bundle = getArguments();
        long time = bundle.getLong("time");
        String Author= bundle.getString("Author");
        String Bookname=bundle.getString("Book");
        String Moral=bundle.getString("Moral");
        //Toast.makeText(getActivity().getApplicationContext(), String.valueOf(time).toString(),Toast.LENGTH_SHORT).show();

        // Calculate minutes and seconds
        long minutes = time / 60;
        long seconds = time % 60;

        // Sets the derived data (type String) in the
        // TextView2 for min
        // TextView4 for seconds
        fulltime2.setText(String.valueOf(minutes));
        fulltime4.setText(String.valueOf(seconds));
        FavAuthor.setText(Author);
        FavBook.setText(Bookname);
        MoralListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This makes a dialog with message if user presses okay he logs out
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(Moral)
                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        return view ;
    }



}