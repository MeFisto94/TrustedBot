package de.pheromir.trustedbot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import de.pheromir.trustedbot.Main;
import de.pheromir.trustedbot.config.GuildConfig;
import net.dv8tion.jda.core.Permission;

public class SkipCommand extends Command {

	public SkipCommand() {
		this.name = "skip";
		this.help = "Musiktrack überspringen";
		this.guildOnly = true;
		this.category = new Category("Music");
	}

	@Override
	protected void execute(CommandEvent e) {
		GuildConfig m = Main.getGuildConfig(e.getGuild());

		if (e.getArgs().isEmpty()) {
			if (!Main.getGuildConfig(e.getGuild()).getDJs().contains(e.getAuthor().getIdLong())
					&& m.scheduler.getCurrentRequester() != e.getAuthor()
					&& !e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
				e.reply("Du kannst nur Songs skippen, die du selbst hinzugefügt hast.");
				return;
			}
			m.scheduler.nextTrack();
			e.reactSuccess();
		} else {
			int index = Integer.MAX_VALUE;
			try {
				index = Integer.parseInt(e.getArgs()) - 1;
			} catch (NumberFormatException ex) {
				e.reply("Bitte eine gültige Zahl angeben.");
				return;
			}
			if (index >= m.scheduler.getRequestedTitles().size()) {
				e.reply("Bitte eine gültige Zahl angeben.");
			}
			if (!Main.getGuildConfig(e.getGuild()).getDJs().contains(e.getAuthor().getIdLong()) && !e.isOwner()
					&& m.scheduler.getRequestedTitles().get(index).getRequestor() != e.getAuthor()
					&& !e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
				e.reply("Du kannst nur Songs skippen, die du selbst hinzugefügt hast.");
				return;
			}
			String title = m.scheduler.getRequestedTitles().get(index).getTrack().getInfo().title;
			if (m.scheduler.skipTrackNr(index)) {
				e.reply("`" + title + "` wurde übersprungen.");
			} else {
				e.reply("Hoppala, da ist wohl etwas schiefgegangen");
			}
		}
	}

}