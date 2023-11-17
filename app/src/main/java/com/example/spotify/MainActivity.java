package com.example.spotify;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MainCallback{
    static ArrayList<MusicFiles> musicFiles;
    static boolean shuffleBoolean = false, repeatBoolean = false;
    static boolean isPlaying = false;
    BottomNavigationView bottomNavigationView;
    FragmentTransaction ft;
    HomeFragment homeFragment;
    SearchFragment searchFragment;
    PlaylistFragment playlistFragment;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottomNavigationView);
            musicFiles = new ArrayList<>();
            homeFragment = homeFragment.newInstance("home-fragment");
            searchFragment = searchFragment.newInstance("search-fragment");
            playlistFragment = playlistFragment.newInstance("playlist-fragment");
            db = FirebaseFirestore.getInstance();

            db.collection("Music").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            // after getting the data we are calling on success method
                            // and inside this method we are checking if the received
                            // query snapshot is empty or not.
                            if (!queryDocumentSnapshots.isEmpty()) {
                                // if the snapshot is not empty we are
                                // hiding our progress bar and adding
                                // our data in a list.
                                //                            loadingPB.setVisibility(View.GONE);
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot d : list) {
                                    // after getting this list we are passing
                                    // that list to our object class.

                                    //                                MusicFiles c = d.toObject(MusicFiles.class);

                                    String title = d.getString("name");
                                    String artist = d.getString("singer");
                                    //                                String path = d.getString("source");
                                    String album = d.getString("thumbnailName");
                                    String id = d.getString("id");
                                    String duration = "";
                                    String path = d.getString("source");

                                    MusicFiles c = new MusicFiles(path, title, artist, album, duration, id);
                                    //                                Log.e("Duration: ", c.getDuration());

                                    // and we will pass this object class
                                    // inside our arraylist which we have
                                    // created for recycler view.
                                    musicFiles.add(c);
                                    homeFragment.onMessageFromMainToFrag("MAIN", c);
                                }
                                // after adding the data to recycler view.
                                // we are calling recycler view notifyDataSetChanged
                                // method to notify that data has been changed in recycler view.
                            } else {
                                // if the snapshot is empty we are displaying a toast message.
                                Toast.makeText(MainActivity.this, "Empty", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // if we do not get any data or any error we are displaying
                            // a toast message that we do not get any data
                            Toast.makeText(MainActivity.this, "Fail", Toast.LENGTH_SHORT).show();
                        }
                    });

            bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.Home)
                    {
                        ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.mainFrameContainer, homeFragment);
                        ft.commit();
                        return true;
                    }
                    if (id == R.id.Search)
                    {
                        ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.mainFrameContainer, searchFragment);
                        ft.commit();
                        return true;
                    }
                    if (id == R.id.Playlist)
                    {
                        ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.mainFrameContainer, playlistFragment);
                        ft.commit();
                        return true;
                    }
                    return false;
                }
            });

            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrameContainer, homeFragment);
            ft.commit();

        } catch (Exception e)
        {
            Log.d("myTag", e.toString());
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onMessageFromFragToMain(String sender, Bundle bundle)
    {
        if (sender.equals("HOME-FRAG"))
        {

        }

        if (sender.equals("PLAYLIST-FRAG"))
        {

        }

        if (sender.equals("SEARCH-FRAG"))
        {

        }

        if (sender.equals("PLAY-BAR"))
        {

        }

    }
}

//class musicData {
//    static ArrayList<MusicFiles> musicFiles;
//    static boolean shuffleBoolean = false, repeatBoolean = false;
//    FragmentTransaction ft;
//    HomeFragment homeFragment;
//    RecyclerView recyclerView;
//    MusicAdapter musicAdapter;
//    FirebaseFirestore db = FirebaseFirestore.getInstance();
//    public interface dataContact{
//        public void getData(MusicFiles musicFiles);
//    }
//
//    public void dataGetter(dataContact contact)
//    {
//
//        db.collection("Music").get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        // after getting the data we are calling on success method
//                        // and inside this method we are checking if the received
//                        // query snapshot is empty or not.
//                        if (!queryDocumentSnapshots.isEmpty()) {
//                            // if the snapshot is not empty we are
//                            // hiding our progress bar and adding
//                            // our data in a list.
//                            //                            loadingPB.setVisibility(View.GONE);
//                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
//                            for (DocumentSnapshot d : list) {
//                                // after getting this list we are passing
//                                // that list to our object class.
//
//                                //                                MusicFiles c = d.toObject(MusicFiles.class);
//
//                                String title = d.getString("name");
//                                String artist = d.getString("singer");
//                                //                                String path = d.getString("source");
//                                String album = d.getString("thumbnailName");
//                                String id = d.getString("id");
//                                String duration = "";
//                                String path = d.getString("source");
//
//                                MusicFiles c = new MusicFiles(path, title, artist, album, duration, id);
//                                //                                Log.e("Duration: ", c.getDuration());
//
//                                // and we will pass this object class
//                                // inside our arraylist which we have
//                                // created for recycler view.
//                                musicFiles.add(c);
//
//                                //                                Toast.makeText(MainActivity.this, "Music name:" + c.getTitle(), Toast.LENGTH_SHORT).show();
//                            }
//                            // after adding the data to recycler view.
//                            // we are calling recycler view notifyDataSetChanged
//                            // method to notify that data has been changed in recycler view.
//                        } else {
//                            // if the snapshot is empty we are displaying a toast message.
//                        }
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        // if we do not get any data or any error we are displaying
//                        // a toast message that we do not get any data
//                    }
//                });
//    }
//}
