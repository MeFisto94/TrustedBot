package de.pheromir.discordmusicbot.commands;

import de.pheromir.discordmusicbot.commands.base.RandomImageCommand;

public class PatCommand extends RandomImageCommand {

	public PatCommand() {
		this.BASE_URL = "https://nekos.life/api/" + "pat";
		this.jsonKey = "url";
		this.name = "pat";
		this.help = "Zeigt ein zufälliges Pat-Gif";
	}

}
