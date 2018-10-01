package de.pheromir.trustedbot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import de.pheromir.trustedbot.Main;

public class AliasCmdsCommand extends Command {

	public AliasCmdsCommand() {
		this.name = "aliases";
		this.help = "List all aliases for the current guild.";
		this.guildOnly = true;
		this.category = new Category("Command Aliases");
	}

	@Override
	protected void execute(CommandEvent e) {
		String[] args = e.getArgs().split(" ");
		if ((args[0].equals("") || args[0].isEmpty()) && args.length == 1)
			args = new String[0];

		String cmds = "There are currently the following aliases: ";
		StringBuilder sb = new StringBuilder();
		for (String str : Main.getGuildConfig(e.getGuild()).getAliasCommands().keySet()) {
			sb.append("`" + str + "`, ");
		}
		e.reply(cmds + sb.toString().substring(0, sb.length() - 2));
	}
}