package net.shadowfacts.baymax.module

import com.typesafe.config.Config
import net.dv8tion.jda.events.message.MessageReceivedEvent
import net.shadowfacts.baymax.command.CommandManager
import net.shadowfacts.baymax.command.exception.WrongUsageException
import net.shadowfacts.baymax.module.base.Module

/**
 * @author shadowfacts
 */
class ModuleMeta extends Module {

	private static final def HELP_USAGE = "Gives the help for the given command in a private message. You're really using help to ask for help using help? Usage: help <command>"

	ModuleMeta() {
		super("meta")
	}

	@Override
	void configure(Config config) {
		enabled = true
	}

	@Override
	void init() {
		CommandManager.register("commands", "Lists all available commands in a private message. Usage: commands", this.&handleCommands)
		CommandManager.register("help", HELP_USAGE, this.&handleHelp)
		CommandManager.register("version", "Gets the version of Baymax running. Usage: version", this.&handleVersion)
		CommandManager.register("info", "Gets info about Baymax. Usage: info", this.&handleInfo)
		CommandManager.register("mem", "Gets the current memory usage. Usage: mem", this.&handleMem)
	}

	private static void handleCommands(MessageReceivedEvent event, String[] args) {
		event.author.privateChannel.sendMessage("Commands: " + String.join(", ", CommandManager.commands))
	}

	private static void handleHelp(MessageReceivedEvent event, String[] args) {
		if (args.length != 1) {
			throw new WrongUsageException(HELP_USAGE)
		}
		event.channel.sendMessage(String.format("%s: %s", args[0], CommandManager.getHelp(args[0])))
	}

	private static void handleVersion(MessageReceivedEvent event, String[] args) {
		event.author.privateChannel.sendMessage("1.0.0")
	}

	private static void handleInfo(MessageReceivedEvent event, String[] args) {
		event.author.privateChannel.sendMessage("Baymax is a modular Discord bot.\nhttps://github.com/shadowfacts/Baymax")
	}

	private static void handleMem(MessageReceivedEvent event, String[] args) {
		long max = Runtime.getRuntime().maxMemory()
		long total = Runtime.getRuntime().totalMemory()
		long free = Runtime.getRuntime().freeMemory()
		long used = total - free

		event.author.privateChannel.sendMessage(String.format("Mem: % 2f%% %03d/%03d MB", (double)(used * 100L / max), (long)(used / 1024l / 1024l), (long)(max / 1024l / 1024l)))
		event.author.privateChannel.sendMessage(String.format("Allocated: % 2d%% %03d MB", (long)(total * 100L / max), (long)(total / 1024l / 1024l)))
	}

}
