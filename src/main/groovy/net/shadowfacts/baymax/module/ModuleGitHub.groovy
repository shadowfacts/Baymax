package net.shadowfacts.baymax.module

import com.typesafe.config.Config
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent
import net.shadowfacts.baymax.Baymax
import net.shadowfacts.baymax.Listener
import net.shadowfacts.baymax.module.base.Module
import org.kohsuke.github.GitHub

import java.util.regex.Pattern

/**
 * @author shadowfacts
 */
class ModuleGitHub extends Module {

	private static final def MATCHER = Pattern.compile("(\\w+/\\w+)?#(\\d+)")

	private def repos = new HashMap<String, Map<String, String>>()
	private GitHub gh

	ModuleGitHub() {
		super("github")
	}

	@Override
	void configure(Config config) {
		super.configure(config)
		def servers = Baymax.config.getStringList("baymax.discord.servers")
		servers.each({
			def path = "github.servers." + it
			if (config.hasPath(path)) {
				def server = new HashMap<String, String>()
				repos.put(it, server)
				Config inner = config.getConfig(path)
				inner.entrySet().each({
					server.put(it.key, (String)it.value.unwrapped())
					it.value.unwrapped()
				})
			}
		})

		gh = GitHub.connectAnonymously()
	}

	@Override
	void init() {
		Listener.INSTANCE.register(GuildMessageReceivedEvent.class, this.&handle)
	}

	private void handle(GuildMessageReceivedEvent event) {
		if (event.author.username != event.JDA.selfInfo.username) {
			def matcher = MATCHER.matcher(event.message.content)
			if (matcher.find()) {
				String repo

				if (matcher.group(1) != null) {
					repo = matcher.group(1)
				} else {
					repo = repos.get(event.guild.id).get(event.channel.name)
				}

				int number
				try {
					number = Integer.parseInt(matcher.group(2))
				} catch (NumberFormatException ignored) { return }

				def issue = gh.getRepository(repo).getIssue(number)
				event.channel.sendMessage(String.format("%s#%d: %s (%s)", repo, number, issue.title, issue.htmlUrl))
			}
		}
	}

}
