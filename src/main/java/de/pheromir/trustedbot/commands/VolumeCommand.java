package de.pheromir.trustedbot.commands;

import com.jagrosh.jdautilities.command.CommandEvent;

import de.pheromir.trustedbot.Main;
import de.pheromir.trustedbot.commands.base.TrustedCommand;
import de.pheromir.trustedbot.config.GuildConfig;

public class VolumeCommand extends TrustedCommand {

	public VolumeCommand() {
		this.name = "volume";
		this.arguments = "<1-100>";
		this.aliases = new String[] { "vol" };
		this.help = "Set the playback volume. (currently only for selected users available)";
		this.guildOnly = true;
		this.category = new Category("Music");
	}

	@Override
	protected boolean exec(CommandEvent e, GuildConfig gc, String[] args, String usage) {
		if (args.length == 0) {
			e.reply("Current volume: " + gc.player.getVolume());
			return false;
		}
		if (!Main.getExtraUsers().contains(e.getAuthor().getIdLong())) {
			e.reply("For performance reasons, this command is only available for selected users. Sorry.\n"
					+ "You can control the volume in your discord-client by rightclicking me.");
			return false;
		}
		if (!gc.getDJs().contains(e.getAuthor().getIdLong())) {
			e.reply("You need DJ permissions to use this command.");
			return false;
		}

		int vol = 100;
		try {
			vol = Integer.parseInt(args[0]);
			if (vol < 1 || vol > 100) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException ex) {
			e.reply("Invalid value. Please specify a value between 1-100.");
			return false;
		}
		gc.setVolume(vol);
		e.reactSuccess();
		return true;
	}

}
