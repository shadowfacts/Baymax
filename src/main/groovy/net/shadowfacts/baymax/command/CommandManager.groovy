package net.shadowfacts.baymax.command

import net.dv8tion.jda.events.message.MessageReceivedEvent
import net.shadowfacts.baymax.Baymax
import net.shadowfacts.baymax.command.exception.CommandException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.function.BiConsumer

/**
 * @author shadowfacts
 */
class CommandManager {

	private static final Logger logger = LoggerFactory.getLogger(CommandManager.class)

	private static final def handlerMap = new HashMap<String, BiConsumer<MessageReceivedEvent, String[]>>()
	private static final def helpMap = new HashMap<String, String>()

	static void register(String command, String help, BiConsumer<MessageReceivedEvent, String[]> handler) {
		if (handlerMap.containsKey(command)) throw new IllegalArgumentException(String.format("Command %s is already registered", command))
		handlerMap.put(command, handler)
		helpMap.put(command, help)
	}

	static void registerAlias(String original, String... aliases) {
		aliases.each({
			handlerMap.put(it, handlerMap.get(original))
			helpMap.put(it, helpMap.get(original))
		})
	}

	static void onMessageReceived(MessageReceivedEvent event) {
		if (event.message.content.startsWith(Baymax.config.getString("baymax.prefix"))) {
			def bits = event.message.content.split(" ");
			def command = bits[0].substring(1)
			def args = Arrays.copyOfRange(bits, 1, bits.length)

			logger.trace("Handling command %s with args %s", command, Arrays.toString(args))

			if (handlerMap.containsKey(command)) {

				try {
					handlerMap.get(command).accept(event, args)
				} catch (CommandException e) {
					event.getChannel().sendMessage("Error: " + e.message)
				} catch (RuntimeException e) {
					event.getChannel().sendMessage("Exception thrown: " + e.message)
					e.printStackTrace()
				}

			} else {
				logger.info("User %s request nonexistent command %s", event.authorNick, command)
				event.channel.sendMessage("No such command " + command)
			}
		}
	}

	static String getHelp(String command) {
		return helpMap.get(command)
	}

	static Set<String> getCommands() {
		return handlerMap.keySet()
	}

}
