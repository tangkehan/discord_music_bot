package org.example;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import java.util.HashMap;
import java.util.Map;

public class Bot {
    public static String botToken = null;
    public static void main( String[] args ) throws Exception
    {

        JDA jda = JDABuilder.createDefault(botToken).build();
    }


    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager playerManager;
    private Bot(){
        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerLocalSource(playerManager);
        AudioSourceManagers.registerRemoteSources(playerManager);
    }



}


