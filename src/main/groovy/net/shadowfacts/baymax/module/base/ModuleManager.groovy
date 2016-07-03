package net.shadowfacts.baymax.module.base

import net.shadowfacts.baymax.config.ConfigManager
import net.shadowfacts.shadowlib.util.ClasspathUtils
import org.reflections.Reflections
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author shadowfacts
 */
class ModuleManager {

	private static final Logger logger = LoggerFactory.getLogger(ModuleManager.class)

	private static def modules = new ArrayList<Module>()

	static void loadModules() {
		loadJars()

		Set<Class<? extends Module>> classes = new Reflections("").getSubTypesOf(Module.class)
		for (def it in classes) {
			try {
				def module = it.newInstance()
				modules.add(module)
			} catch (ReflectiveOperationException e) {
				logger.error("Couldn't load module", e)
			}
		}
	}

	static void loadJars() {
		def dir = new File("modules")
		if (dir.exists()) {
			def files = dir.listFiles()

			files.each({
				if (it.name.endsWith(".jar")) {
					ClasspathUtils.addFileToClasspath(it)
				}
			})

		} else {
			dir.mkdirs()
		}
	}

	static void configure() {
		for (Module module : modules) {
			module.configure(ConfigManager.getConfig(module.name))
		}
	}

	static void init() {
		for (Module module : modules) {
			if (module.enabled) {
				module.init()
			}
		}
	}

}
