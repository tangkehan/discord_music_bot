package example;
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
        System.out.println("nextTrack called");
        AudioTrack track = queue.poll();
        if (track != null) {
            player.startTrack(track, false);
        }
    }

//    when a track end, we call the nextTrack function
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        System.out.println("Track ended: " + track.getInfo().title + ", End reason: " + endReason);

        if (endReason.mayStartNext || endReason == AudioTrackEndReason.REPLACED) {
            System.out.println("Triggering next track.");
            nextTrack();
        } else {
            System.out.println("Track ended, but not triggering next track.");
        }
    }

    public boolean isQueueEmpty() {
        return queue.isEmpty();
    }

    public AudioTrack peekQueue() {
        return this.queue.peek();
    }

}
