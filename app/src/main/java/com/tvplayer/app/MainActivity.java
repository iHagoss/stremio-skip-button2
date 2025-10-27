package com.tvplayer.app;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;

public class MainActivity extends AppCompatActivity {
    
    private StyledPlayerView playerView;
    private ExoPlayer player;
    private Button skipIntroButton;
    private Button skipCreditsButton;
    
    private SkipMarkers skipMarkers;
    private Handler updateHandler;
    private boolean introAutoSkipped = false;
    
    private static final String VIDEO_URL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
    private static final String MARKERS_API_URL = "https://your-api-endpoint.com/skip-markers.json";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        playerView = findViewById(R.id.player_view);
        skipIntroButton = findViewById(R.id.skip_intro_button);
        skipCreditsButton = findViewById(R.id.skip_credits_button);
        
        updateHandler = new Handler();
        
        initializePlayer();
        setupSkipButtons();
        fetchSkipMarkers();
    }
    
    private void initializePlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(VIDEO_URL));
        player.setMediaItem(mediaItem);
        player.prepare();
        player.setPlayWhenReady(true);
        
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
            }
        });
        
        startPositionUpdates();
    }
    
    private void setupSkipButtons() {
        skipIntroButton.setOnClickListener(v -> {
            if (skipMarkers != null && skipMarkers.intro != null) {
                player.seekTo(skipMarkers.intro.end * 1000);
                skipIntroButton.setVisibility(View.GONE);
            }
        });
        
        skipCreditsButton.setOnClickListener(v -> {
            if (skipMarkers != null && skipMarkers.credits != null) {
                player.seekTo(player.getDuration());
                skipCreditsButton.setVisibility(View.GONE);
            }
        });
    }
    
    private void fetchSkipMarkers() {
        ApiService apiService = new ApiService();
        apiService.fetchSkipMarkers(MARKERS_API_URL, new ApiService.SkipMarkersCallback() {
            @Override
            public void onSuccess(SkipMarkers markers) {
                skipMarkers = markers;
            }
            
            @Override
            public void onError(Exception e) {
                skipMarkers = createDefaultMarkers();
            }
        });
    }
    
    private SkipMarkers createDefaultMarkers() {
        SkipMarkers markers = new SkipMarkers();
        markers.intro = new SkipMarkers.TimeRange();
        markers.intro.start = 0;
        markers.intro.end = 90;
        
        markers.credits = new SkipMarkers.TimeRange();
        markers.credits.start = 2500;
        markers.credits.end = 2700;
        
        return markers;
    }
    
    
    private void startPositionUpdates() {
        updateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateSkipButtonsVisibility();
                updateHandler.postDelayed(this, 500);
            }
        }, 500);
    }
    
    private void updateSkipButtonsVisibility() {
        if (player == null || skipMarkers == null) {
            return;
        }
        
        long currentPositionSeconds = player.getCurrentPosition() / 1000;
        
        if (skipMarkers.intro != null) {
            boolean inIntroRange = currentPositionSeconds >= skipMarkers.intro.start && 
                                   currentPositionSeconds < skipMarkers.intro.end;
            
            if (inIntroRange && !introAutoSkipped) {
                player.seekTo(skipMarkers.intro.end * 1000);
                introAutoSkipped = true;
            }
            
            skipIntroButton.setVisibility(inIntroRange ? View.VISIBLE : View.GONE);
        }
        
        if (skipMarkers.credits != null) {
            boolean inCreditsRange = currentPositionSeconds >= skipMarkers.credits.start && 
                                     currentPositionSeconds < skipMarkers.credits.end;
            skipCreditsButton.setVisibility(inCreditsRange ? View.VISIBLE : View.GONE);
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateHandler.removeCallbacksAndMessages(null);
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
