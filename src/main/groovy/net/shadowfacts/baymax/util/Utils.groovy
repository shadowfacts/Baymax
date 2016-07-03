package net.shadowfacts.baymax.util

import net.dv8tion.jda.entities.Guild
import net.dv8tion.jda.entities.User

/**
 * @author shadowfacts
 */
class Utils {

	static String getName(User user, Guild guild) {
		return guild == null ? user.username : guild.getNicknameForUser(user) == null ? user.username : guild.getNicknameForUser(user)
	}

}
