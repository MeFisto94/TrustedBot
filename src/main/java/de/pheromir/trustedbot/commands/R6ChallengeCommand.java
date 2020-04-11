package de.pheromir.trustedbot.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import de.pheromir.trustedbot.commands.base.TrustedCommand;
import de.pheromir.trustedbot.config.GuildConfig;
import de.pheromir.trustedbot.r6.*;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * The Rainbow Six Random Challenge Command
 *
 * @author MeFisto94
 */
public class R6ChallengeCommand extends TrustedCommand {

    public R6ChallengeCommand() {
        this.name = "r6c";
        this.help = "Create a Rainbow Six Challenge for X PLayers";
        this.arguments = "<atk/def> <Number of Players>";
        this.category = new Category("Miscellaneous");
    }
    @Override
    protected boolean exec(CommandEvent e, @Nullable GuildConfig gc, String[] args, String usage) {
        if (args.length != 2) {
            e.reply(usage);
            return false;
        }

        int numPlayers;
        Side side;

        try {
            numPlayers = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            e.reply("Parser Error!\n" + usage);
            return false;
        }

        if (numPlayers < 0 || numPlayers > 5) {
            e.reply("Invalid Number of Players!\n" + usage);
            return false;
        }

        if (args[0].equalsIgnoreCase("atk")) {
            side = Side.ATTACKERS;
        } else if (args[0].equalsIgnoreCase("def")) {
            side = Side.DEFENDERS;
        } else {
            e.reply("Parser Error: Neither atk nor def found!\n" + usage);
            return false;
        }

        List<ChallengeResult> opList = Arrays.stream(Challenges.values()).map(c ->
            // Challenge to possible Operator List
            new ChallengeResult(c,
                Arrays.stream(Operator.values()).filter((Operator op) -> op.side.equals(side) && c.filter.test(op)).collect(Collectors.toList())
            )
            // Doable with current party.
        ).filter(cr -> cr.eligibleOperators.size() >= numPlayers).collect(Collectors.toList());

        if (opList.isEmpty()) {
            // Should never happen
            e.reply("No Challenges found, I'm sorry :(");
            return false;
        } else {
            Random randy = new Random();
            ChallengeResult res = opList.get(randy.nextInt(opList.size()));

            e.reply(String.format("Do the following Challenge(s): %s\nOperators: %s\n**GameMode: %s**",
                res.challenges.stream().map(String::valueOf).collect(Collectors.joining(" ")),
                res.eligibleOperators.stream().map(String::valueOf).collect(Collectors.joining(", ")),
                GameMode.values()[randy.nextInt(GameMode.values().length)].toString()
            ));
            return true;
        }
    }
}