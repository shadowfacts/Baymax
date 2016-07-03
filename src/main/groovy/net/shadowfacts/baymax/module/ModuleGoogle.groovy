package net.shadowfacts.baymax.module

import com.google.gson.Gson
import com.typesafe.config.Config
import net.dv8tion.jda.events.message.MessageReceivedEvent
import net.shadowfacts.baymax.command.CommandManager
import net.shadowfacts.baymax.module.base.Module
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author shadowfacts
 */
class ModuleGoogle extends Module {

	private static final def USAGE = "Googles the given query and returns the first result. Usage: google <query>"
	private static final def CSE = "007161902339625765643:7svtbrbi34i"
	private static final def USER_AGENT = "Baymax"

	private static final Logger logger = LoggerFactory.getLogger(ModuleGoogle.class)

	private static final def GSON = new Gson()

	private String apiKey

	ModuleGoogle() {
		super("google")
	}

	@Override
	void configure(Config config) {
		super.configure(config)
		apiKey = config.getString("google.apiKey")
	}

	@Override
	void init() {
		CommandManager.register("google", USAGE, this.&handle)
		CommandManager.registerAlias("google", "g")
	}

	private void handle(MessageReceivedEvent event, String[] args) {
		def query = String.join(" ", args)
		def url = new URI("https", "www.googleapis.com", "/customsearch/v1", String.format("q=%s&num=1&cx=%s&key=%s", query, CSE, apiKey), null).toURL()

		logger.debug("Sending request to %s", url.toString())

		def con = (HttpURLConnection)url.openConnection()
		con.setRequestMethod("GET")
		con.setRequestProperty("User-Agent", USER_AGENT)

		logger.debug("Response code: %d", con.responseCode)

		def input = con.inputStream
		def result = GSON.fromJson(new InputStreamReader(input), Result.class)
		input.close()

		if (result.items.length == 0) {
			event.channel.sendMessage("No result for " + query)
		} else {
			def item = result.items[0]
			def snippet = item.snippet.length() > 100 ? item.snippet.substring(0, 98) + "..." : item.snippet
			snippet = snippet.replaceAll("\\n", "")
			event.channel.sendMessage(String.format("%s (%s): %s", item.title, item.link, snippet))
		}

	}

	private static class Result {

		Item[] items

		static class Item {
			String title
			String link
			String snippet
		}

	}

}
