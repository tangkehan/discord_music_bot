package org.example;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final BlockingDeque<AudioTrack> queue;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingDeque<>();

    }

//    if there's no track started, the queue will offer new track
    public void queue(AudioTrack track){
        boolean canStartTrack = this.player.startTrack(track, true);
        if (!canStartTrack){
            this.queue.offer(track);
        }
    }

    public AudioTrack getNextTrack() {
        return queue.peek();
    }

    // list all the songs in the queue
    public void listQueue(TextChannel channel) {
        StringBuilder queueList = new StringBuilder("Current Queue:\n");

        for (AudioTrack track : queue) {
            queueList.append(track.getInfo().title).append("\n");
        }

        channel.sendMessage(queueList.toString()).queue();
    }


    //get to the next track
    public boolean nextTrack(){
        AudioTrack nextTrack = queue.poll();
        if (nextTrack == null){
            player.stopTrack();
            return false;
        }
        boolean canStart = player.startTrack(nextTrack, false);
        return true;
    }

//    public void nextTrack(){
//        AudioTrack nextTrack = queue.poll();
//        boolean canStart = player.startTrack(nextTrack, false);
//
//    }


//    when a track end, we call the nextTrack function
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext){
            nextTrack();
        }
    }

}
