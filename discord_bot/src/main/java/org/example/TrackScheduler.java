package org.example;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final BlockingDeque<AudioTrack> queue;
    private final Guild guild;

    public TrackScheduler(AudioPlayer player, Guild guild) {
        this.player = player;
        this.guild = guild;
        this.queue = new LinkedBlockingDeque<>();

    }

//    if there's no track started, the queue will offer new track
    public void queue(AudioTrack track){
        if (!this.player.startTrack(track, true)){
            this.queue.offer(track);
        }
    }

    // list all the songs in the queue
    public void listQueue(TextChannel channel) {
        StringBuilder queueList = new StringBuilder("Current Queue:\n");

        for (AudioTrack track : queue) {
            queueList.append(track.getInfo().title).append("\n");
        }

        channel.sendMessage(queueList.toString()).queue();
    }

//    get to the next track
    public void nextTrack(){

        AudioTrack track = queue.poll();

        if (track != null) {
            this.player.startTrack(track, false);
        } else {
            this.player.stopTrack();
            this.guild.getAudioManager().closeAudioConnection();
        }

//        this.player.startTrack(this.queue.poll(), false);
//        if (!player.startTrack(queue.poll(), false) && queue.isEmpty()){
//            this.guild.getAudioManager().closeAudioConnection();
//        }
    }

//    when a track end, we call the nextTrack function
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext){
            nextTrack();
        }
    }

}
