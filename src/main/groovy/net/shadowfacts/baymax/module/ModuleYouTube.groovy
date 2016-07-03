package net.shadowfacts.baymax.module

import com.google.gson.Gson
import com.typesafe.config.Config
import net.dv8tion.jda.events.message.MessageReceivedEvent
import net.shadowfacts.baymax.command.CommandManager;
import net.shadowfacts.baymax.module.base.Module
import org.slf4j.LoggerFactory

import java.util.regex.Pattern;

/**
 * @author shadowfacts
 */
public class ModuleYouTube extends Module {

	private static final def USAGE = "Searches YouTube and returns the first result. Usage: youtube <query>"
	private static final def USER_AGENT = "Baymax"
	private static final def PATTERN = Pattern.compile("(https?://)?(www\\.)?(youtube\\.com|youtu\\.be)/(watch\\?v=)?(.+)")

	private static final def logger = LoggerFactory.getLogger(ModuleYouTube.class)

	private static final def GSON = new Gson()

	private String apiKey

	ModuleYouTube() {
		super("youtube")
	}

	@Override
	void configure(Config config) {
		super.configure(config)
		apiKey = config.getString("youtube.apiKey")
	}

	@Override
	void init() {
		CommandManager.register("youtube", USAGE, this.&handleCommand)
		CommandManager.registerAlias("youtube", "yt")
	}

	private void handleCommand(MessageReceivedEvent event, String[] args) {
		def query = String.join(" ", args)
		def url = new URI("https", "www.googleapis.com", "/youtube/v3/search", String.format("part=snippet&maxResults=1&type=video&q=%s&key=%s", query, apiKey), null).toURL()

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
			def videoId = item.id.videoId
			def channel = item.snippet.channelTitle
			def title = item.snippet.title
			event.channel.sendMessage(String.format("%s: %s (https://youtu.be/%s)", channel, title, videoId))
		}
	}

	private static class Result {

		Item[] items

		static class Item {
			ID id
			Snippet snippet

			static class ID {
				String videoId
			}

			static class Snippet {
				String channelTitle
				String title
			}
		}
	}

}
