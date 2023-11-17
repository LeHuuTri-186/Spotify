package com.example.spotify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements FragmentCallback{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    MusicAdapter downloadSongAdapter, recentlyPlayedSongAdapter,
            popularSongAdapter;
    RecyclerView musicDownload, recentlyPlayed,
            popularSong;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public HomeFragment() {
        // Required empty public constructor
    }
    public static HomeFragment newInstance(String param1) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        LinearLayoutManager recentlyPlayedLM = new LinearLayoutManager(this.getContext());
        LinearLayoutManager musicDownloadLM = new LinearLayoutManager(this.getContext());
        LinearLayoutManager popularSongLM = new LinearLayoutManager(this.getContext());

        recentlyPlayedLM.setOrientation(LinearLayoutManager.HORIZONTAL);
        musicDownloadLM.setOrientation(LinearLayoutManager.HORIZONTAL);
        popularSongLM.setOrientation(LinearLayoutManager.HORIZONTAL);

        popularSong = (RecyclerView) view.findViewById(R.id.popularSongList);
        musicDownload = (RecyclerView) view.findViewById(R.id.downloadMusicList);
        recentlyPlayed = (RecyclerView) view.findViewById(R.id.recentlyPlayed);

        popularSongAdapter = new MusicAdapter(this.getContext(), musicFiles);
        downloadSongAdapter = new MusicAdapter(this.getContext(), musicFiles);
        recentlyPlayedSongAdapter = new MusicAdapter(this.getContext(), musicFiles);

        recentlyPlayed.setLayoutManager(recentlyPlayedLM);
        musicDownload.setLayoutManager(musicDownloadLM);
        popularSong.setLayoutManager(popularSongLM);

        recentlyPlayed.setAdapter(recentlyPlayedSongAdapter);
        musicDownload.setAdapter(downloadSongAdapter);
        popularSong.setAdapter(popularSongAdapter);

        return view;
    }

    @Override
    public void onMessageFromMainToFrag(String sender, MusicFiles musicFiles) {
        if (sender.equals("MAIN")) {
            this.musicFiles.add(musicFiles);
            recentlyPlayedSongAdapter.notifyDataSetChanged();
            popularSongAdapter.notifyDataSetChanged();
            downloadSongAdapter.notifyDataSetChanged();
        }
    }
}