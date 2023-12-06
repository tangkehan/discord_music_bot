package example;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_VOICE_STATES;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.

public class Main extends ListenerAdapter {
    public static String botToken ="MTE4MTM5MDQwMjQyMzYxOTY0OA.Gu_PzZ.SBFFlilbCsRCEZUAOtYV4J-baJ-iFVNcjbZiqQ";
    public static String googleApi = "AIzaSyBazEFolqIsm8d7F5JvJkcKMIS-SB5ahcc";
    public static String ownerID = "945162326598877204";
    private static YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        return new YouTube.Builder(httpTransport, jsonFactory, new HttpRequestInitializer() {
            public void initialize(com.google.api.client.http.HttpRequest request) throws IOException {}
        }).setApplicationName("YouTubeAPI").build();
    }

    private static List<String> searchVideos(YouTube youtubeService, String query) throws IOException {
        YouTube.Search.List search = youtubeService.search().list("id");
        search.setKey(googleApi);
        search.setQ(query);
        search.setType("video");
        search.setMaxResults(5L); // Number of results to retrieve

        SearchListResponse searchResponse = search.execute();
        List<SearchResult> searchResults = searchResponse.getItems();

        // Extract video URLs from search results
        List<String> videoUrls = new ArrayList<>();
        for (SearchResult searchResult : searchResults) {
            videoUrls.add("https://www.youtube.com/watch?v=" + searchResult.getId().getVideoId());
        }

        return videoUrls;
    }

    public static void main(String[] args) {

        JDA jda = JDABuilder.create(botToken, GUILD_MESSAGES, GUILD_VOICE_STATES)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new Main())
                .build();

    }

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;


    private Main() {
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {

        guild.getAudioManager().closeAudioConnection();
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager, guild);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] command = event.getMessage().getContentRaw().split(" ", 2);


        if ("$play".equals(command[0])) {
            if(command[1].contains("http")){
                loadAndPlay(event.getChannel().asTextChannel(), command[1], event);
            }else{
                try {
                    YouTube youtubeService = getService();
                    List<String> videoUrls = searchVideos(youtubeService, command[1]);
                    loadAndPlay(event.getChannel().asTextChannel(), videoUrls.get(0), event);
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
            }

        } else if ("$skip".equals(command[0])) {
            skipTrack(event.getChannel().asTextChannel());
        } else if ("$shutdown".equals(command[0]) && ownerID.equals(event.getAuthor().getId())){
            event.getJDA().shutdown();
            //send instruction
        } else if ("$instruction".equals(command[0])) {
            sendInstructions(event.getChannel().asTextChannel());
        } else if ("$list".equals(command[0])) {
            // list the queue
            listQueue(event.getChannel().asTextChannel());
        }
        super.onMessageReceived(event);
    }



    private void loadAndPlay(final TextChannel channel, final String trackUrl, final MessageReceivedEvent event) {
// set this musicManager to final
        final GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();

                play(channel.getGuild(), musicManager, track, event);
//                musicManager.scheduler.listQueue(channel);

            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();

                play(channel.getGuild(), musicManager, firstTrack, event);
//                musicManager.scheduler.listQueue(channel);

            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found by " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
    }

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track, MessageReceivedEvent event) {
        if (connectToFirstVoiceChannel(guild.getAudioManager(), event)){
            musicManager.scheduler.queue(track);
        }
    }

    private void skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();
        channel.sendMessage("Skipped to next track.").queue();
    }

    private static boolean connectToFirstVoiceChannel(AudioManager audioManager, MessageReceivedEvent event) {
        if (!audioManager.isConnected()) {
            try{
                VoiceChannel voiceChannel = event.getMember().getVoiceState().getChannel().asVoiceChannel();
                audioManager.openAudioConnection(voiceChannel);
                return true;
            }catch(Exception e){
                event.getChannel().asTextChannel().sendMessage("couldn't connect to channel").queue();
            }
        }
        return false;
    }

    // Add new Feature
    private void sendInstructions(TextChannel channel) {
        String instructions =
                "**$play**: Play music. Usage: `$play <song>`\n" +
                "**$skip**: Skip the current track.\n" +
                "**$shutdown**: Shutdown the bot (Owner only).\n";
        channel.sendMessage(instructions).queue();
    }

    private void listQueue(TextChannel textChannel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(textChannel.getGuild());
        musicManager.scheduler.listQueue(textChannel);
    }
}