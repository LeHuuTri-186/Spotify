package com.example.spotify;

import android.os.Bundle;
import static com.example.spotify.MainActivity.musicFiles;
import java.util.ArrayList;

public interface FragmentCallback {
    public void onMessageFromMainToFrag(String sender, MusicFiles musicFiles);
}
