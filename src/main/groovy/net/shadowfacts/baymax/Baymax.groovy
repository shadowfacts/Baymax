package net.shadowfacts.baymax

import com.typesafe.config.Config
import net.dv8tion.jda.JDA
import net.dv8tion.jda.JDABuilder
import net.dv8tion.jda.events.message.MessageReceivedEvent
import net.shadowfacts.baymax.command.CommandManager
import net.shadowfacts.baymax.config.ConfigManager
import net.shadowfacts.baymax.module.base.ModuleManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author shadowfacts
 */
class Baymax {

	private static final Logger logger = LoggerFactory.getLogger(Baymax.class)

	static File configDir
	static Config config
	static JDA jda

	static void main(String... args) {
//		TODO: copy default config

		loadConfig()

		def builder = new JDABuilder()
		builder.botToken = config.getString("baymax.discord.token")
		builder.addListener(Listener.INSTANCE)

		logger.info("Loading modules")
		ModuleManager.loadModules()
		logger.info("Configuring modules")
		ModuleManager.configure()
		logger.info("Initializing modules")
		ModuleManager.init()

		Listener.INSTANCE.register(MessageReceivedEvent.class, CommandManager.&onMessageReceived)

		logger.info("Building JDA")

		jda = builder.buildBlocking()
	}

	static void loadConfig() {
		configDir = new File("config/")
		if (!configDir.exists()) configDir.mkdirs()
		config = ConfigManager.getConfig("Baymax")
	}

}
