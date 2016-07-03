package net.shadowfacts.baymax.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import net.shadowfacts.baymax.Baymax
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author shadowfacts
 */
class ConfigManager {

	private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class)

	public static Config getConfig(String name) {
		def configFile = new File(Baymax.configDir, name + ".conf")
		if (!configFile.exists()) {
			try {
				configFile.createNewFile()
				def input = getClass().getResourceAsStream(name + "-reference.conf")
				if (input != null) {

				}
			} catch (IOException e) {
				logger.error("Couldn't load config for " + name, e)
			}
		}
		return ConfigFactory.parseFile(configFile).withFallback(ConfigFactory.load(name + "-reference.conf"))
	}

}
