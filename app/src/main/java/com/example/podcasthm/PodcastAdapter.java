package com.example.podcasthm;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.List;

public class PodcastAdapter extends RecyclerView.Adapter<PodcastAdapter.PodcastViewHolder> {

    private Context context;
    private List<Podcast> podcastList;
    private MediaPlayer mediaPlayer;
    private int lastPlayingPosition = -1;
    private boolean isPaused = false;
    private Handler seekBarHandler = new Handler();

    public PodcastAdapter(Context context, List<Podcast> podcastList) {
        this.context = context;
        this.podcastList = podcastList;
    }

    @NonNull
    @Override
    public PodcastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_podcast, parent, false);
        return new PodcastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PodcastViewHolder holder, int position) {
        Podcast podcast = podcastList.get(position);
        holder.titleTextView.setText(podcast.getTitle());
        holder.contentView.setText(podcast.getContent());
        Glide.with(context).load(podcast.getThumbnail()).into(holder.thumbnailImageView);

        holder.viewsource.setOnClickListener(v->{
            Intent intent = new Intent(context, Viewsource.class);
            context.startActivity(intent);
        });
        holder.playButton.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                if (lastPlayingPosition == holder.getAdapterPosition() && isPaused) {
                    mediaPlayer.start();
                    isPaused = false;
                    updateSeekBar(holder);
                } else {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    notifyItemChanged(lastPlayingPosition);
                    startMediaPlayer(holder, podcast);
                }
            } else {
                startMediaPlayer(holder, podcast);
            }
        });

        holder.pauseButton.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPaused = true;
            }
        });

        // Reset SeekBar when binding the view
        holder.progressBar.setProgress(0);
    }

    private void startMediaPlayer(PodcastViewHolder holder, Podcast podcast) {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(podcast.getAudio());
            mediaPlayer.prepare();
            mediaPlayer.start();
            lastPlayingPosition = holder.getAdapterPosition();
            notifyItemChanged(lastPlayingPosition);

            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                mediaPlayer = null;
                isPaused = false;
                notifyItemChanged(lastPlayingPosition);
            });

            updateSeekBar(holder);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateSeekBar(PodcastViewHolder holder) {
        holder.progressBar.setMax(mediaPlayer.getDuration());
        seekBarHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    holder.progressBar.setProgress(mediaPlayer.getCurrentPosition());
                    seekBarHandler.postDelayed(this, 1000);
                }
            }
        }, 1000);

        holder.progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    public int getItemCount() {
        return podcastList.size();
    }

    public static class PodcastViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView, contentView;
        ImageView thumbnailImageView;
        Button playButton, pauseButton,viewsource;
        SeekBar progressBar;

        public PodcastViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            thumbnailImageView = itemView.findViewById(R.id.thumbnailImageView);
            contentView = itemView.findViewById(R.id.transcriptContent);
            playButton = itemView.findViewById(R.id.play_button);
            pauseButton = itemView.findViewById(R.id.pause_button);
            progressBar = itemView.findViewById(R.id.progressBar);
            viewsource = itemView.findViewById(R.id.ViewSource);
        }
    }
}
