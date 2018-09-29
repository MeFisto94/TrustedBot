package de.pheromir.trustedbot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import de.pheromir.trustedbot.Main;
import de.pheromir.trustedbot.config.GuildConfig;
import de.pheromir.trustedbot.music.QueueTrack;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class ResumeCommand extends Command {

	public ResumeCommand() {
		this.name = "resume";
		this.aliases = new String[] { "unpause" };
		this.help = "Musikwiedergabe fortsetzen";
		this.guildOnly = true;
		this.category = new Category("Music");
	}

	@Override
	protected void execute(CommandEvent e) {
		if (!Main.getGuildConfig(e.getGuild()).getDJs().contains(e.getAuthor().getIdLong())
				&& !e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			e.reply("Du musst mind. ein DJ sein um den Bot wieder starten zu können.");
			return;
		}
		GuildConfig musicManager = Main.getGuildConfig(e.getGuild());

		if (musicManager.player.getPlayingTrack() != null
				&& musicManager.player.getPlayingTrack().getDuration() == Long.MAX_VALUE) {
			musicManager.scheduler.setCurrentTrack(new QueueTrack(musicManager.player.getPlayingTrack().makeClone(),
					musicManager.scheduler.getCurrentRequester()));
			musicManager.player.startTrack(musicManager.player.getPlayingTrack().makeClone(), false);
		}
		musicManager.player.setPaused(false);
		if (musicManager.player.getPlayingTrack() != null && !e.getGuild().getAudioManager().isConnected()) {
			VoiceChannel vc = e.getMember().getVoiceState().getChannel();
			if (vc != null) {
				e.getGuild().getAudioManager().openAudioConnection(vc);
			}
		}
		e.reactSuccess();
	}
}