package com.example.pz_9;

import android.app.Activity;
import android.content.ContentUris;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import java.io.IOException;

public class MainActivity extends Activity
        implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    final String LOG_TAG = "myLogs";
    final String DATA_HTTP = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3";
    final String DATA_STREAM = "https://icecast-vgtrk.cdnvideo.ru/mayakfm_mp3_192kbps";
    final String DATA_SD = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            + "/music.mp3";
    final Uri DATA_URI = ContentUris.withAppendedId(
            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 1);

    MediaPlayer mp;
    AudioManager am;
    CheckBox chbLoop;
    TextView tvStatus;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        chbLoop = findViewById(R.id.chbLoop);
        tvStatus = findViewById(R.id.tvStatus);

        chbLoop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton v, boolean c) {
                if (mp != null) mp.setLooping(c);
            }
        });


    }

    public void onClick(View v) {
        int id = v.getId();
        releaseMP();

        try {
            if (id == R.id.btnHttp || id == R.id.btnStream) {
                mp = new MediaPlayer();
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mp.setDataSource(id == R.id.btnHttp ? DATA_HTTP : DATA_STREAM);
                mp.setOnPreparedListener(this);
                mp.setOnCompletionListener(this);
                mp.prepareAsync();
                tvStatus.setText("loading...");
            }
            else if (id == R.id.btnSd) {
                mp = new MediaPlayer();
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mp.setDataSource(DATA_SD);
                mp.setOnCompletionListener(this);
                mp.prepare();
                mp.start();
                tvStatus.setText("SD play");
            }
            else if (id == R.id.btnUri) {
                mp = new MediaPlayer();
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mp.setDataSource(this, DATA_URI);
                mp.setOnCompletionListener(this);
                mp.prepare();
                mp.start();
                tvStatus.setText("URI play");
            }
            else if (id == R.id.btnRaw) {
                // ищем res/raw/music — если файла нет, id = 0
                int rawId = getResources().getIdentifier(
                        "music", "raw", getPackageName());
                if (rawId == 0) {
                    tvStatus.setText("Нет res/raw/music.mp3");
                } else {
                    mp = MediaPlayer.create(this, rawId);
                    if (mp != null) {
                        mp.setOnCompletionListener(this);
                        mp.setLooping(chbLoop.isChecked());
                        mp.start();
                        tvStatus.setText("RAW play");
                    }
                }
            }
            else if (id == R.id.btnPause && mp != null && mp.isPlaying()) {
                mp.pause();
                tvStatus.setText("PAUSE");
            }
            else if (id == R.id.btnResume && mp != null && !mp.isPlaying()) {
                mp.start();
                tvStatus.setText("RESUME");
            }
            else if (id == R.id.btnStop && mp != null) {
                mp.stop();
                tvStatus.setText("STOP");
            }
            else if (id == R.id.btnBackward && mp != null) {
                mp.seekTo(Math.max(0, mp.getCurrentPosition() - 5000));
                tvStatus.setText("<< 5s");
            }
            else if (id == R.id.btnForward && mp != null) {
                mp.seekTo(Math.min(mp.getDuration(), mp.getCurrentPosition() + 5000));
                tvStatus.setText(">> 5s");
            }
            else if (id == R.id.btnInfo && mp != null) {
                String s = "pos=" + mp.getCurrentPosition()
                        + " dur=" + mp.getDuration()
                        + " loop=" + mp.isLooping()
                        + " vol=" + am.getStreamVolume(AudioManager.STREAM_MUSIC);
                Log.d(LOG_TAG, s);
                tvStatus.setText(s);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "err: " + e.getMessage());
        }

        if (mp != null && id != R.id.btnInfo) mp.setLooping(chbLoop.isChecked());
    }

    public void onPrepared(MediaPlayer p) {
        Log.d(LOG_TAG, "onPrepared");
        p.start();
        tvStatus.setText("PLAY");
    }

    public void onCompletion(MediaPlayer p) {
        Log.d(LOG_TAG, "onCompletion");
        tvStatus.setText("END");
    }

    void releaseMP() {
        if (mp != null) {
            try { mp.release(); } catch (Exception ignored) {}
            mp = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMP();
    }

}