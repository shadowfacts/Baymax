package net.shadowfacts.baymax.command.exception

/**
 * @author shadowfacts
 */
class WrongUsageException extends CommandException {

	WrongUsageException() {
	}

	WrongUsageException(String msg) {
		super(msg)
	}

	WrongUsageException(String msg, Throwable t) {
		super(msg, t)
	}

	WrongUsageException(Throwable t) {
		super(t)
	}

}
