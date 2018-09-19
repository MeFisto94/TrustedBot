package de.pheromir.discordmusicbot.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import de.pheromir.discordmusicbot.Main;
import net.dv8tion.jda.core.entities.User;

public class ExtraAddCommand extends Command {

	public ExtraAddCommand() {
		this.name = "extraadd";
		this.help = "Extra-Rechte an eine Person vergeben.";
		this.arguments = "<User-Tag/ID>";
		this.guildOnly = false;
		this.ownerCommand = true;
	}

	@Override
	protected void execute(CommandEvent e) {
		String[] args = e.getArgs().split(" ");
		if ((args[0].equals("") || args[0].isEmpty()) && args.length == 1)
			args = new String[0];

		for (String arg : args) {
			Pattern p = Pattern.compile("(\\d+)");
			Matcher m = p.matcher(arg);
			if (m.find()) {
				String id = m.group(1);
				User mem = Main.jda.getUserById(id);
				if (mem == null) {
					e.reply("Es konnte kein entsprechender Nutzer gefunden werden.");
					continue;
				}
				if (Main.getExtraUsers().contains(Long.parseLong(id))) {
					e.reply(mem.getAsMention() + " hat bereits Extra-Rechte.");
					continue;
				} else {
					e.reply(mem.getAsMention() + " hat nun Extra-Rechte.");
					Main.addExtraUser(Long.parseLong(id));
				}
				continue;
			} else {
				e.reply("Es konnte kein entsprechender Nutzer gefunden werden.");
				continue;
			}
		}
	}
}
