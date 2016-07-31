package net.shadowfacts.baymax.module.mcserver

import com.typesafe.config.Config
import net.dv8tion.jda.events.message.MessageReceivedEvent
import net.shadowfacts.baymax.command.CommandManager
import net.shadowfacts.baymax.command.exception.WrongUsageException
import net.shadowfacts.baymax.module.base.Module
import org.slf4j.LoggerFactory

/**
 * @author shadowfacts
 */
class ModuleMCServer extends Module {

	private static final def SERVER_USAGE = "Queries the status of a Minecraft server. Usage: mcserver <address>"
	private static final def AUTH_USAGE = "Queries the status of the Minecraft authentication servers. Usage: mcauth"

	private static final def logger = LoggerFactory.getLogger(ModuleMCServer.class)

	private boolean server
	private boolean auth

	ModuleMCServer() {
		super("mcserver")
	}

	@Override
	void configure(Config config) {
		super.configure(config)

		server = config.getBoolean("mcserver.server")
		auth = config.getBoolean("mcserver.auth")
	}

	@Override
	void init() {
		if (server) CommandManager.register("mcserver", SERVER_USAGE, this.&handleServer)
		if (auth) CommandManager.register("mcauth", AUTH_USAGE, this.&handleAuth)
	}

	private static void handleServer(MessageReceivedEvent event, String[] args) {
		if (args.length != 1) {
			throw new WrongUsageException(SERVER_USAGE)
		}

		def addr = args[0]
		def bits = addr.split(":")
		int port = bits.length > 1 ? Integer.parseInt(bits.last()) : 25565
		def host = bits.dropRight(1).join(":")
		def thread = new Thread({
			def packet = new PacketPing(new InetSocketAddress(host, port))
			try {
				def res = packet.fetch()

				event.channel.sendMessage("${addr}: ${res.description.text}, ${res.time}ms, ${res.players.online}/${res.players.max} players")

			} catch (Exception e) {
				event.channel.sendMessage("No response from ${addr}")
			}
		})
		thread.start()
	}

	private static void handleAuth(MessageReceivedEvent event, String[] args) {
		if (args.length != 0) {
			throw new WrongUsageException(SERVER_USAGE)
		}
		def thread = new Thread({
			try {
				def result = AuthStatus.check()
				event.channel.sendMessage("Minecraft authentication server status: ${result}")
			} catch (Exception e) {
				event.channel.sendMessage("Could not check Minecraft authentication server status")
			}
		})
		thread.start()
	}

}
