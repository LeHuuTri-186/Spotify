package com.example.spotify;

import static com.example.spotify.ApplicationClass.ACTION_PLAY;
import static com.example.spotify.ApplicationClass.ACTION_PREVIOUS;
import static com.example.spotify.ApplicationClass.CHANNEL_ID_2;
import static com.example.spotify.MainActivity.musicFiles;
import static com.example.spotify.MainActivity.repeatBoolean;
import static com.example.spotify.MainActivity.shuffleBoolean;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Random;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;


public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, ActionPlaying {
    TextView song_name, artist_name, duration_played, duration_total;
    ImageView cover_art, nextBtn, prevBtn, backBtn, shuffleBtn, repeatBtn;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    int position = -1;
    static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    static Uri uri;
    static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread playThread, prevThread, nextThread;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
    //    MusicService musicService;
    MediaSessionCompat mediaSessionCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initViews();
        getIntentMethod();

        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My audio");
        mediaPlayer.setOnCompletionListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser)
                {
                    mediaPlayer.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        });

        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shuffleBoolean)
                {
                    shuffleBoolean = false;
                    shuffleBtn.setImageResource(R.drawable.baseline_shuffle_off);
                }
                else
                {
                    shuffleBoolean = true;
                    shuffleBtn.setImageResource(R.drawable.baseline_shuffle_on_24);
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (repeatBoolean)
                {
                    repeatBoolean = false;
                    repeatBtn.setImageResource(R.drawable.baseline_repeat_off);
                }
                else
                {
                    repeatBoolean = true;
                    repeatBtn.setImageResource(R.drawable.baseline_repeat_on_24);
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(v.getContext(), MainActivity.class);
                intent.putExtra("position", position);
                v.getContext().startActivity(intent);
            }
        });
    }
    @Override
    protected void onResume() {
//        Intent intent = new Intent(this, MusicService.class);
//        bindService(intent, this, BIND_AUTO_CREATE);
        playThreadBtn();
        prevThreadBtn();
        nextThreadBtn();
        super.onResume();
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        unbindService(this);
//    }

    private void prevThreadBtn() {
        prevThread = new Thread()
        {
            @Override
            public void run() {
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();
    }
    public void prevBtnClicked() {
        if (mediaPlayer.isPlaying())
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (shuffleBoolean && !repeatBoolean)
            {
                position = getRandom(listSongs.size() - 1);
            }
            else
            {
                if (!shuffleBoolean && !repeatBoolean)
                {
                    position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position - 1));
                }
            }

            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);

            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekBar.setMax(mediaPlayer.getDuration() / 1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            playPauseBtn.setBackgroundResource(R.drawable.baseline_pause_24);
            mediaPlayer.start();
        }
        else
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (shuffleBoolean && !repeatBoolean)
            {
                position = getRandom(listSongs.size() - 1);
            }
            else
            {
                if (!shuffleBoolean && !repeatBoolean)
                {
                    position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position - 1));
                }
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);

            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            playPauseBtn.setBackgroundResource(R.drawable.baseline_play_arrow_24);
            //mediaPlayer.start();
        }
    }
    public void nextBtnClicked() {
        if (mediaPlayer.isPlaying())
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (shuffleBoolean && !repeatBoolean)
            {
                position = getRandom(listSongs.size() - 1);
            }
            else
            {
                if (!shuffleBoolean && !repeatBoolean)
                {
                    position = ((position + 1) % listSongs.size());
                }
            }

            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);

            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            playPauseBtn.setBackgroundResource(R.drawable.baseline_pause_24);
            mediaPlayer.start();
        }
        else
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (shuffleBoolean && !repeatBoolean)
            {
                position = getRandom(listSongs.size() - 1);
            }
            else
            {
                if (!shuffleBoolean && !repeatBoolean)
                {
                    position = ((position + 1) % listSongs.size());
                }
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            playPauseBtn.setBackgroundResource(R.drawable.baseline_play_arrow_24);
            //mediaPlayer.stop();
        }
    }
    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }
    private void nextThreadBtn() {
        nextThread = new Thread()
        {
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();
    }
    private void playThreadBtn() {
        playThread = new Thread()
        {
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }
    public void playPauseBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            playPauseBtn.setImageResource(R.drawable.baseline_play_arrow_24);
            mediaPlayer.pause();
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
        }
        else
        {
            playPauseBtn.setImageResource(R.drawable.baseline_pause_24);
            mediaPlayer.start();
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
        }
    }
    private String formattedTime(int mCurrentPosition) {
        String totalout = "";
        String totalNew = "";
        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);
        totalout = minutes + ":" + seconds;
        totalNew = minutes + ":"+"0"+seconds;
        if (seconds.length() == 1)
        {
            return totalNew;
        }
        return totalout;
    }
    private void getIntentMethod() {
        position = getIntent().getIntExtra("position", -1);
        listSongs = musicFiles;
        if (listSongs != null)
        {
            playPauseBtn.setImageResource(R.drawable.baseline_pause_24);
            uri = Uri.parse(listSongs.get(position).getPath());
        }
//        if (musicService != null)
//        {
//            musicService.stop();
//            musicService.release();
//            musicService.createMediaPlayer(position);
//            musicService.start();
//        }
//        else
//        {
//            musicService.createMediaPlayer(position);
//            musicService.start();
//        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();
        seekBar.setMax(mediaPlayer.getDuration() / 1000);
        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
        listSongs.get(position).setDuration(String.valueOf(mediaPlayer.getDuration()));
        metaData(uri);


//        Intent intent = new Intent(this, MusicService.class);
//        intent.putExtra("servicePosition", position);
//        startService(intent);

    }
    private void initViews() {
        song_name = findViewById(R.id.song_name);
        artist_name = findViewById(R.id.song_artist);
        duration_played = findViewById(R.id.durationPlayed);
        duration_total = findViewById(R.id.durationTotal);
        cover_art = findViewById(R.id.cover_art);
        nextBtn = findViewById(R.id.id_next);
        prevBtn = findViewById(R.id.id_prev);
        backBtn = findViewById(R.id.back_btn);
        shuffleBtn = findViewById(R.id.id_shuffle);
        repeatBtn = findViewById(R.id.id_repeat);
        playPauseBtn = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seekBar);
    }
    private  void metaData(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;
        duration_total.setText(formattedTime(durationTotal));
        byte[] art = null;
        Bitmap bmp = null;

        final long ONE_MEGABYTE = 1024*1024;
//        Log.e("thumbnail", mFiles.get(position).getAlbum());
        storageReference.child("Thumbnails/" + listSongs.get(position).getAlbum())
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        ImageAnimation(PlayerActivity.this, cover_art, bitmap);
                        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(@Nullable Palette palette) {
                                Palette.Swatch swatch = palette.getDominantSwatch();
                                if (swatch != null)
                                {
                                    ImageView gradient = findViewById(R.id.imageViewGradient);
                                    RelativeLayout mContainer = findViewById(R.id.mContainer);
                                    gradient.setBackgroundResource(R.drawable.gradient_bg);
                                    mContainer.setBackgroundResource(R.drawable.main_bg);
                                    GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                            new int[]{swatch.getRgb(), 0x00000000});
                                    gradient.setBackground(gradientDrawable);
                                    GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                            new int[]{swatch.getRgb(), swatch.getRgb()});
                                    mContainer.setBackground(gradientDrawableBg);
                                    song_name.setTextColor(swatch.getTitleTextColor());
                                    artist_name.setTextColor(swatch.getBodyTextColor());
                                }
                                else
                                {
                                    ImageView gradient = findViewById(R.id.imageViewGradient);
                                    RelativeLayout mContainer = findViewById(R.id.mContainer);
                                    gradient.setBackgroundResource(R.drawable.gradient_bg);
                                    mContainer.setBackgroundResource(R.drawable.main_bg);
                                    GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                            new int[]{0xff000000, 0x00000000});
                                    gradient.setBackground(gradientDrawable);
                                    GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                            new int[]{0xff000000, 0xff000000});
                                    mContainer.setBackground(gradientDrawableBg);
                                    song_name.setTextColor(Color.WHITE);
                                    artist_name.setTextColor(Color.DKGRAY);
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Glide.with(PlayerActivity.this)
                                .asBitmap()
                                .load(R.drawable.anh)
                                .into(cover_art);
                        song_name.setTextColor(Color.WHITE);
                        artist_name.setTextColor(Color.DKGRAY);
                    }
                });

//        if (art != null) {
//            bmp = BitmapFactory.decodeByteArray(art, 0, art.length);
//            ImageAnimation(this, cover_art, bitmap);
//            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
//                @Override
//                public void onGenerated(@Nullable Palette palette) {
//                    Palette.Swatch swatch = palette.getDominantSwatch();
//                    if (swatch != null)
//                    {
//                        ImageView gradient = findViewById(R.id.imageViewGradient);
//                        RelativeLayout mContainer = findViewById(R.id.mContainer);
//                        gradient.setBackgroundResource(R.drawable.gradient_bg);
//                        mContainer.setBackgroundResource(R.drawable.main_bg);
//                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
//                                new int[]{swatch.getRgb(), 0x00000000});
//                        gradient.setBackground(gradientDrawable);
//                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
//                                new int[]{swatch.getRgb(), swatch.getRgb()});
//                        mContainer.setBackground(gradientDrawableBg);
//                        song_name.setTextColor(swatch.getTitleTextColor());
//                        artist_name.setTextColor(swatch.getBodyTextColor());
//                    }
//                    else
//                    {
//                        ImageView gradient = findViewById(R.id.imageViewGradient);
//                        RelativeLayout mContainer = findViewById(R.id.mContainer);
//                        gradient.setBackgroundResource(R.drawable.gradient_bg);
//                        mContainer.setBackgroundResource(R.drawable.main_bg);
//                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
//                                new int[]{0xff000000, 0x00000000});
//                        gradient.setBackground(gradientDrawable);
//                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
//                                new int[]{0xff000000, 0xff000000});
//                        mContainer.setBackground(gradientDrawableBg);
//                        song_name.setTextColor(Color.WHITE);
//                        artist_name.setTextColor(Color.DKGRAY);
//                    }
//                }
//            });
//        }
//        else
//        {
//            Glide.with(this)
//                    .asBitmap()
//                    .load(R.drawable.anh)
//                    .into(cover_art);
//            song_name.setTextColor(Color.WHITE);
//            artist_name.setTextColor(Color.DKGRAY);
//        }
    }
    public void ImageAnimation(Context context, ImageView imageView, Bitmap bitmap) {
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        nextBtnClicked();
        if (mediaPlayer != null)
        {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);
        }
    }

//    @Override
//    public void onServiceConnected(ComponentName name, IBinder service) {
//        MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
//        musicService = myBinder.getService();
//        Toast.makeText(this, "Connected" + musicService, Toast.LENGTH_SHORT).show();
//        seekBar.setMax(musicService.getDuration() / 1000);
//        metaData(uri);
//        song_name.setText(listSongs.get(position).getTitle());
//        artist_name.setText(listSongs.get(position).getArtist());
//        musicService.onCompleted();
//    }
//
//    @Override
//    public void onServiceDisconnected(ComponentName name) {
//        musicService = null;
//    }

//    void showNotification (int playPauseBtn) {
//        Intent intent = new Intent(this, PlayerActivity.class);
//        @SuppressLint("UnspecifiedImmutableFlag")
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
//        Intent prevIntent = new Intent(this, NotificationReceiver.class)
//                .setAction(ACTION_PREVIOUS);
//        @SuppressLint("UnspecifiedImmutableFlag")
//        PendingIntent prevPending = PendingIntent
//                .getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
//                .setAction(ACTION_PLAY);
//        @SuppressLint("UnspecifiedImmutableFlag")
//        PendingIntent pausePending = PendingIntent
//                .getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        Intent nextIntent = new Intent(this, NotificationReceiver.class)
//                .setAction(ACTION_PREVIOUS);
//        @SuppressLint("UnspecifiedImmutableFlag")
//        PendingIntent nextPending = PendingIntent
//                .getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        byte[] picture = null;
//        picture = getAlbumArt(musicFiles.get(position).getPath());
//        Bitmap thumb = null;
//        if (picture != null) {
//            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
//        } else {
//            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.anh);
//        }
//        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
//                .setSmallIcon(playPauseBtn)
//                .setLargeIcon(thumb)
//                .setContentTitle(musicFiles.get(position).getTitle())
//                .setContentText(musicFiles.get(position).getArtist())
//                .addAction(R.drawable.baseline_skip_previous_24, "Previous", prevPending)
//                .addAction(playPauseBtn, "Pause", pausePending)
//                .addAction(R.drawable.baseline_skip_next_24, "Next", nextPending)
//                .setStyle(new Notification.MediaStyle()
//                        .setMediaSession(mediaSessionCompat.getSessionToken()))
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setOnlyAlertOnce(true)
//                .build();
//        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        notificationManager.notify(0, notification);
//    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        return art;
    }
}