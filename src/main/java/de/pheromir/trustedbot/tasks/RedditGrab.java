package de.pheromir.trustedbot.tasks;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
import org.json.JSONObject;

import de.pheromir.trustedbot.Main;
import de.pheromir.trustedbot.Methods;
import de.pheromir.trustedbot.config.GuildConfig;
import de.pheromir.trustedbot.exceptions.HttpErrorException;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.entities.TextChannel;

public class RedditGrab implements Runnable {

	@Override
	public void run() {
		Thread.currentThread().setName("Reddit-Task");
		for (String subreddit : GuildConfig.getRedditList().keySet()) {
			try {
				JSONObject res;
				try {
				res = Methods.httpRequestJSON(String.format("https://www.reddit.com/r/%s/hot/.json", subreddit));
				} catch (InterruptedException | HttpErrorException | ExecutionException | TimeoutException e) {
					Main.LOG.error("", e);
					continue;
				}
				if (!res.has("data"))
					continue;
				JSONObject data = res.getJSONObject("data");
				if (!data.has("children"))
					continue;
				JSONArray children = data.getJSONArray("children");
				if (children.length() == 0)
					continue;
				for (Object postObject : children) {
					JSONObject post = ((JSONObject) postObject).getJSONObject("data");
					String contentUrl = post.getString("url");
					if (contentUrl.isEmpty())
						continue;

					String sub = post.getString("subreddit");
					String title = post.getString("title");
					String link = "https://www.reddit.com" + post.getString("permalink");
					String author = post.getString("author");
					int score = post.getInt("score");

					EmbedBuilder emb = new EmbedBuilder();
					emb.setAuthor(author);
					emb.addField(new Field("Score", score + "", false));
					emb.setFooter("/r/" + sub, Main.jda.getSelfUser().getAvatarUrl());
					emb.setTitle(title.length() > 256 ? title.substring(0, 256) : title, link);

					for (Long chId : GuildConfig.getRedditList().get(subreddit)) {
						TextChannel c = Main.jda.getTextChannelById(chId);

						if (!GuildConfig.RedditPosthistoryContains(contentUrl)) {
							if (contentUrl.contains(".jpg") || contentUrl.contains(".png")
									|| contentUrl.contains(".jpeg")) {
								emb.setImage(contentUrl);
								c.sendMessage(emb.build()).complete();
							} else {
								c.sendMessage(emb.build()).complete();
								c.sendMessage(contentUrl).complete();
							}
						}
						c = null;
					}
					GuildConfig.addSubredditPostHistory(contentUrl);
					Thread.sleep(500L);
				}
				res = null;
			} catch (InterruptedException e) {
				Main.LOG.error("", e);
				continue;
			}
		}
	}
}
