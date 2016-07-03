package net.shadowfacts.baymax.module

import net.dv8tion.jda.events.message.MessageReceivedEvent
import net.shadowfacts.baymax.command.CommandManager
import net.shadowfacts.baymax.command.exception.WrongUsageException
import net.shadowfacts.baymax.module.base.Module

import java.time.OffsetDateTime

/**
 * @author shadowfacts
 */
class ModulePing extends Module {

	private static final def USAGE = "Responds with the time between sending the message and the bot receiving it. Usage: ping"

	ModulePing() {
		super("ping")
	}

	@Override
	void init() {
		CommandManager.register("ping", USAGE, this.&handle)
	}

	private static void handle(MessageReceivedEvent event, String[] args) {
		if (args.length != 0) throw new WrongUsageException(USAGE)

		def now = OffsetDateTime.now().toInstant()
		def sent = event.message.time.toInstant()

		event.channel.sendMessage(String.format("Pong. Time: %ss", (now.toEpochMilli() - sent.toEpochMilli()) / 1000f))
	}

}
