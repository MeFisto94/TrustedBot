package de.pheromir.trustedbot.commands.base;

import java.awt.Color;

import org.json.JSONException;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import de.pheromir.trustedbot.Methods;
import de.pheromir.trustedbot.Exceptions.HttpErrorException;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;

public abstract class RandomImageCommand extends Command {

	protected String BASE_URL;
	protected String jsonKey;

	public RandomImageCommand() {
		this.name = "";
		this.botPermissions = new Permission[] { Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS };
		this.help = "Zeigt ein zufälliges Bild";
		this.guildOnly = false;
		this.category = new Category("Fun");
	}

	@Override
	protected void execute(CommandEvent e) {
		String imgUrl;
		try {
			imgUrl = Methods.httpRequestJSON(BASE_URL).getString(jsonKey);
			e.reply(new EmbedBuilder().setImage(imgUrl).setColor(e.getChannelType() == ChannelType.TEXT
					? e.getSelfMember().getColor()
					: Color.BLUE).build());
		} catch (JSONException | HttpErrorException e1) {
			e.reply("Es ist ein Fehler aufgetreten.");
			e1.printStackTrace();
		}
	}

}