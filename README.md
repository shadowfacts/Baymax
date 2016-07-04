# Baymax
A modular Discord bot.

This respository contains the core modules of Baymax.

# Developing Modules
Baymax Core is available on [my Maven](http://mvn.rx14.co.uk/shadowfacts/net/shadowfacts/Baymax/). 

## Gradle usage
```
repositories {
	maven {
		name "shadowfacts"
		"url "http://mvn.rx14.co.uk/shadowfacts/
	}
}

dependencies {
	compile "net.shadowfacts:Baymax:1.0.0"
}
```

## Modules
Create a class that extends `net.shadowfacts.baymax.module.base.Module`. Create a no-args constructor that calls the super constructor with the name of the module. Override `configure` to configure your module and override `init` to run code during Baymax's initialization phase. All classes extending `Module` will be registered automatically.

### Configuration
In your resources, include a file called `NAME-reference.conf` which is the [TypeSafe config file](https://github.com/typesafehub/config/) for your module where `NAME` is the name you passed into the `Module` constructor.

### Initialization
The `init` method is called during Baymax's initialization phase when all the modules are being configured and set up. Use this to register commands and event handlers.

**Note:** Modules are initialized before the JDA instance is created. Register a handler for `ReadyEvent` if you need to run code after the JDA is initialized. 

### Commands
Commands are registered through the [`CommandManager`](https://github.com/shadowfacts/Baymax/blob/master/src/main/groovy/net/shadowfacts/baymax/command/CommandManager.groovy) using `CommandManager.register`. `register` takes three argument: the name of the command, the usage string of the command (for use by the `help` command), and the function to call when the command is run by a user. 

See the [ping module and its command](https://github.com/shadowfacts/Baymax/blob/master/src/main/groovy/net/shadowfacts/baymax/module/ModulePing.groovy) for a simple example of a command.

### Events
Event handlers are registered using `Listener.INSTANCE.register` which accepts two parameters: the class of the event and a function that consumes that event.

See the [temperature module](https://github.com/shadowfacts/Baymax/blob/master/src/main/groovy/net/shadowfacts/baymax/module/ModuleTemperature.groovy#L40) for a simple example of an event handler.
