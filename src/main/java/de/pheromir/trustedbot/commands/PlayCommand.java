package de.pheromir.trustedbot.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import de.pheromir.trustedbot.Main;
import de.pheromir.trustedbot.Methods;
import de.pheromir.trustedbot.config.GuildConfig;
import de.pheromir.trustedbot.music.Suggestion;
import de.pheromir.trustedbot.tasks.RemoveUserSuggestion;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;

public class PlayCommand extends Command {

	String pattern = "^^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	Pattern compiledPattern = Pattern.compile(pattern);

	public PlayCommand() {
		this.name = "play";
		this.botPermissions = new Permission[] { Permission.VOICE_CONNECT, Permission.VOICE_SPEAK };
		this.guildOnly = true;
		this.help = "Titel zur Wiedergabeschlange hinzufügen.";
		this.category = new Category("Music");
	}

	@Override
	protected void execute(CommandEvent e) {
		VoiceChannel vc = e.getMember().getVoiceState().getChannel();
		AudioManager audioManager = e.getGuild().getAudioManager();
		GuildConfig musicManager = Main.getGuildConfig(e.getGuild());

		if (e.getArgs().isEmpty() && musicManager.player.isPaused()) {
			musicManager.player.setPaused(false);
			e.reply("Wiedergabe fortgesetzt.");
			if (Main.getGuildConfig(e.getGuild()).player.getPlayingTrack() != null) {
				e.getGuild().getAudioManager().openAudioConnection(vc);
			}
			return;
		} else if (e.getArgs().isEmpty()) {
			if (Main.getGuildConfig(e.getGuild()).scheduler.getRequestedTitles().isEmpty()) {
				e.reply("Bitte einen Track angeben (Link / Youtube Suchbegriffe).");
			} else {
				e.reply("Setze Wiedergabe der aktuellen Warteschlange fort.");
				audioManager.openAudioConnection(vc);
				musicManager.scheduler.nextTrack();
			}
			return;
		}

		String toLoad = "";

		if (musicManager.getSuggestions().containsKey(e.getAuthor()) && (e.getArgs().equalsIgnoreCase("1")
				|| e.getArgs().equalsIgnoreCase("2") || e.getArgs().equalsIgnoreCase("3")
				|| e.getArgs().equalsIgnoreCase("4") || e.getArgs().equalsIgnoreCase("5"))) {
			int nr = Integer.parseInt(e.getArgs());
			if (nr <= musicManager.getSuggestions().get(e.getAuthor()).size()) {
				toLoad = "http://youtube.com/watch?v="
						+ musicManager.getSuggestions().get(e.getAuthor()).get(nr - 1).getId();
			}
		}

		if(!Main.spotifyToken.equals("none")) {
			if(e.getArgs().toLowerCase().contains("spotify.com/track/")) {
				Pattern p = Pattern.compile("track\\/(?<trackid>[0-9A-z]+)");
				Matcher m = p.matcher(e.getArgs());
				if(m.find()) {
					String id = m.group("trackid");
					if(id != null && !id.isEmpty()) {
						Unirest.get("https://api.spotify.com/v1/tracks/{id}").routeParam("id", id).header("Authorization", "Bearer "+Main.spotifyToken).asJsonAsync(new Callback<JsonNode>() {

							@Override
							public void cancelled() {
							}

							@Override
							public void completed(HttpResponse<JsonNode> arg0) {
								String title = arg0.getBody().getObject().getString("name");
								String artist = ((JSONObject)arg0.getBody().getObject().getJSONArray("artists").get(0)).getString("name");
								
								try {
									printAndAddSuggestions(getVideoSuggestions(artist+" "+title), e);
								} catch (IOException e1) {
									e1.printStackTrace();
									
								}
								return;
							}

							@Override
							public void failed(UnirestException arg0) {
							}
							
						});
						return;
					}
				}
			
			} else if(e.getArgs().toLowerCase().contains("spotify.com")) {
				e.reply("Es wird derzeit nur die einfache Trackangabe von Spotify unterstützt.\n(spotify.com/track/TRACKID)");
				return;
			}
		}

		if (toLoad.equals("")) {
			Matcher matcher = compiledPattern.matcher(e.getArgs());
			if (!matcher.find()) {
				try {
					printAndAddSuggestions(getVideoSuggestions(e.getArgs()), e);
					return;
				} catch (IOException e1) {
					e1.printStackTrace();
					e.reply("Es ist ein Fehler aufgetreten.");
					return;
				}

			} else {
				toLoad = matcher.group();
			}
		}
		musicManager.player.setPaused(false);

		Main.playerManager.loadItemOrdered(musicManager, toLoad, new AudioLoadResultHandler() {

			@Override
			public void trackLoaded(AudioTrack track) {
				if (track.getDuration() > (1000 * 60 * 60 * 2)
						&& !Main.getExtraUsers().contains(e.getAuthor().getIdLong())) {
					e.reply("Du kannst nur Titel mit einer Länge von bis zu zwei Stunden hinzufügen.");
					return;
				} else if (Main.getGuildConfig(e.getGuild()).scheduler.getRequestedTitles().size() > 10
						&& !Main.getExtraUsers().contains(e.getAuthor().getIdLong())) {
					e.reply("Du kannst keine weiteren Titel hinzufügen, da das Limit von 10 Titeln erreicht wurde.");
					return;
				}
				audioManager.openAudioConnection(vc);
				musicManager.scheduler.queue(track, e.getAuthor());
				e.reply("`" + track.getInfo().title + "` [" + Methods.getTimeString(track.getDuration())
						+ "] wurde zur Warteschlange hinzugefügt.");
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				AudioTrack firstTrack = playlist.getSelectedTrack();
				if (firstTrack == null) {
					firstTrack = playlist.getTracks().get(0);
				}
				audioManager.openAudioConnection(vc);
				musicManager.scheduler.queue(firstTrack, e.getAuthor());
				e.reply("`" + firstTrack.getInfo().title + "` [" + Methods.getTimeString(firstTrack.getDuration())
						+ "] wurde zur Warteschlange hinzugefügt.");
			}

			@Override
			public void noMatches() {
				e.reply("Es konnte kein passender Titel gefunden werden.");
			}

			@Override
			public void loadFailed(FriendlyException throwable) {
				e.reply("Fehler beim Abspielen: " + throwable.getLocalizedMessage());
			}

		});
	}

	public static List<SearchResult> getVideoSuggestions(String search) throws IOException {
		YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
				new HttpRequestInitializer() {

					public void initialize(HttpRequest request) throws IOException {

					}
				}).setApplicationName("DiscordBot").build();

		YouTube.Search.List searchRequest;
		searchRequest = youtube.search().list("snippet");
		searchRequest.setMaxResults((long) 5);
		searchRequest.setType("video");
		searchRequest.setQ(search);
		searchRequest.setKey(Main.youtubeKey);
		SearchListResponse listResponse = searchRequest.execute();
		List<SearchResult> videoList = listResponse.getItems();
		return videoList;
	}

	public static void printAndAddSuggestions(List<SearchResult> videoList, CommandEvent e) {
		MessageBuilder mes = new MessageBuilder();
		mes.append("**Titelauswahl:**");
		EmbedBuilder m = new EmbedBuilder();
		m.setColor(e.getGuild().getSelfMember().getColor());
		ArrayList<Suggestion> suggests = new ArrayList<>();
		for (int i = 0; i < (videoList.size() >= 5 ? 5 : videoList.size()); i++) {
			suggests.add(new Suggestion(videoList.get(i).getSnippet().getTitle(),
					videoList.get(i).getId().getVideoId()));
			m.appendDescription("**[" + (i + 1) + "]** " + videoList.get(i).getSnippet().getTitle() + " *["
					+ Methods.getTimeString(Methods.getYoutubeDuration(videoList.get(i).getId().getVideoId()))
					+ "]*\n\n");
		}
		Main.getGuildConfig(e.getGuild()).getSuggestions().put(e.getAuthor(), suggests);
		Executors.newScheduledThreadPool(1).schedule(new RemoveUserSuggestion(e.getGuild(),
				e.getAuthor()), 5, TimeUnit.MINUTES);
		m.setFooter("Titel auswählen: !play [Nr] (Vorschläge 5 min. gültig)", e.getJDA().getSelfUser().getAvatarUrl());
		mes.setEmbed(m.build());
		e.reply(mes.build());
	}

}