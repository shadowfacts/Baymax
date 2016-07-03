package net.shadowfacts.baymax.module

import com.typesafe.config.Config
import net.dv8tion.jda.events.message.MessageReceivedEvent
import net.shadowfacts.baymax.Baymax
import net.shadowfacts.baymax.command.CommandManager
import net.shadowfacts.baymax.command.exception.IllegalUsageException
import net.shadowfacts.baymax.module.base.Module
import net.shadowfacts.baymax.module.base.ModuleManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author shadowfacts
 */
class ModuleAdmin extends Module {

	private static final def RC_USAGE = "Reloads the Baymax configuration. Admin only. Usage: reloadConfig"

	private static final Logger logger = LoggerFactory.getLogger(ModuleAdmin.class)

	ModuleAdmin() {
		super("admin")
	}

	@Override
	void configure(Config config) {
		enabled = true
	}

	@Override
	void init() {
		CommandManager.register("reloadConfig", RC_USAGE, this.&reloadConfig)
		CommandManager.register("stop", "Stops Baymax. Admin only.", this.&stop)
	}

	private static void reloadConfig(MessageReceivedEvent event, String[] args) {
		if (Baymax.config.getStringList("baymax.admins").contains(event.author.id)) {
			Baymax.loadConfig()
			ModuleManager.configure()
			event.channel.sendMessage("Baymax configuration reloaded, some settings may not apply until restart")
		} else {
			throw new IllegalUsageException("reloadConfig is admin only")
		}
	}

	private static void stop(MessageReceivedEvent event, String[] args) {
		if (Baymax.config.getStringList("baymax.admins").contains(event.author.id)) {
			event.channel.sendMessage("Stopping Baymax")
			logger.info("Shutting down the JDA")
			Baymax.jda.shutdown(true)
			logger.info("Exiting Baymax")
			System.exit(0)
		} else {
			throw new IllegalUsageException("stop is admin only")
		}
	}

}
