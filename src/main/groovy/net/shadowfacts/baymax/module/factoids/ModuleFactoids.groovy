package net.shadowfacts.baymax.module.factoids

import com.typesafe.config.Config
import net.dv8tion.jda.events.message.MessageReceivedEvent
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent
import net.shadowfacts.baymax.Listener
import net.shadowfacts.baymax.command.CommandManager
import net.shadowfacts.baymax.command.exception.WrongUsageException
import net.shadowfacts.baymax.module.base.Module

/**
 * @author shadowfacts
 */
class ModuleFactoids extends Module {

	public static final String USAGE = "Sets/removes factoids. Usage: factoid <set|remove> <id> [value]"

	private String prefix

	ModuleFactoids() {
		super("factoids")
	}

	@Override
	void configure(Config config) {
		super.configure(config)
		prefix = config.getString("factoids.prefix")
	}

	@Override
	void init() {
		FactoidManager.init()
		Listener.INSTANCE.register(GuildMessageReceivedEvent.class, this.&handle)
		CommandManager.register("factoid", USAGE, this.&handleCommand)
	}

	private void handle(GuildMessageReceivedEvent event) {
		if (event.message.content.startsWith(prefix)) {
			def id = event.message.content.substring(1)
			def factoid = FactoidManager.instance.get(event.guild.id, id)
			if (factoid != null) {
				event.channel.sendMessage(id + ": " + factoid)
			}
		}
	}

	private static void handleCommand(MessageReceivedEvent event, String[] args) {
		if (event.isPrivate()) {
			throw new WrongUsageException("factoid can only be used in server channels")
		}
		if (args.length == 0) {
			throw new WrongUsageException(USAGE)
		}
		def command = args[0]
		if (command == "set") {
			if (args.length < 3) {
				throw new WrongUsageException(USAGE)
			}
			def factoid = String.join(" ", Arrays.copyOfRange(args, 2, args.length))
			FactoidManager.instance.set(event.guild.id, args[1], factoid)
			event.channel.sendMessage("Set factoid " + args[1] + " to: " + factoid)
		} else if (command == "remove") {
			if (args.length != 2) {
				throw new WrongUsageException(USAGE)
			}
			FactoidManager.instance.remove(event.guild.id, args[1])
			event.channel.sendMessage("Removed factoid " + args[1])
		} else {
			throw new WrongUsageException(USAGE)
		}
	}

}
