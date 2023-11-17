package com.example.spotify;

import android.os.Bundle;

public interface MainCallback {
    public void onMessageFromFragToMain(String sender, Bundle bundle);
}
