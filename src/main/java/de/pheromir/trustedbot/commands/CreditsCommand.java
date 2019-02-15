package de.pheromir.trustedbot.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import de.pheromir.trustedbot.Main;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Member;

public class CreditsCommand extends Command {
	
	public CreditsCommand() {
		this.name = "credits";
		this.help = "Show your Credits";
		this.guildOnly = true;
		this.category = new Category("Money");
	}

	@Override
	protected void execute(CommandEvent e) {
		if(e.getChannelType() == ChannelType.TEXT && Main.getGuildConfig(e.getGuild()).isCommandDisabled(this.name)) {
			e.reply(Main.COMMAND_DISABLED);
			return;
		}
		
		if(!e.getArgs().isEmpty() && e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			Pattern p = Pattern.compile("(\\d+)");
			Matcher m = p.matcher(e.getArgs());
			if (!m.find()) {
				e.reply("The specified user couldn't be found.");
			}
			String id = m.group(1);
			Member mem = e.getGuild().getMemberById(id);
			if (mem == null) {
				e.reply("The specified user couldn't be found.");
				return;
			}
			e.reply(mem.getEffectiveName()+" has **" + Main.getGuildConfig(e.getGuild()).getUserCredits(mem.getUser().getIdLong()) + "** credits.");
		} else {
			e.reply(e.getMember().getEffectiveName()+", you have **" + Main.getGuildConfig(e.getGuild()).getUserCredits(e.getMember().getUser().getIdLong()) + "** credits.");
		}
	}

}
