# DiscordBotBase
DiscordBotBase is a Java project utilizing `JDA`, designed to empower users in creating fully customizable Discord bots through `JSON` configurations. This README provides comprehensive guidance on various aspects of the project, including configuration, commands, components, modules, `ResponseBuilder`, etc.

Please keep in mind that this README does not include every single feature of the project. For more information, please refer to the source code or contact the project's author.
## Table of Contents
1. [Getting started](#getting-started)
2. [Main configuration file](#main-configuration-file)
3. [Commands](#commands)
    - [Subcommands](#subcommands)
    - [Response commands](#response-commands)
4. [Components](#components)
5. [Modules](#modules)
6. [PlaceholderMap](#placeholdermap)
7. [ResponseBuilder](#responsebuilder)
    - [Response structure](#response-structure)
    - [Component (handler) overriding](#component-row-overriding)
8. [EventWaiter and interfaces](#eventwaiter-and-interfaces)
9. [License](#license)

## Getting started
1. Clone the `DiscordBotBase` repository to your local machine.
2. Customize your bot's behavior.
3. Explore examples and showcases in the `configuration` folder.
4. Build and run your bot using Java and the JDA library.

## Main configuration file
The main configuration file (`configuration/config.json`) serves as the cornerstone for customizing your bot's behavior. Here, you can set default values, specify your bot's token, define admins, configure intents, error handlers, database credentials, enable modules etc.

## Commands
To implement a command, create a class extending the `Command` class. Register the command in `CommandManager#registerCommands()`. Additionally, create a JSON file with the command's name in `configuration/commands`. Upon bot startup, this file will be filled with default JSON properties, which are self-explanatory. From here, you are free to customize the command's appearance and functionality.

Command error handlers can be overridden upon configuring command's properties. If no overrides are provided, bot will respond with default error handlers (configured in `configuration/config.json`). Same principle is later applied to option & component handlers.

DiscordBotBase also comes with a few pre-configured commands, such as `reload`, `reboot`, `command`, `diagnostics`, `say` and `guilds`.

`reboot` and `reload` commands are also available directly in bot's console. It is always preferred to use `reboot` rather than forcefully shutting down the bot.

### Subcommands
In order to implement a subcommand, create a command with the name `[parent command]_[subcommand]`. Subcommand's properties should include a parent value referencing the parent command's name.

### Response commands
Response commands require no handler implementation. These can be added to `configuration/responsecommands` and configured from there.

## Components
Components include buttons (`ButtonHandler`), dropdowns (`DropdownHandler`) and modals (`ModalHandler`). Handlers are created similarly to command handlers and are registered in `ComponentManager#registerComponentHandlers()`. 

## Modules
Modules can be enabled from the main configuration file. Each module extends the `Module` class and can register listeners, tasks, both or none. Modules must be registered through `ModuleManager#registerModules()`. Listener modules should use the `registerListener()` method within the `onEnable()` method to register listeners to JDA. Task modules, on the other hand, contain tasks that are repeated at intervals. Use the `registerTask()` method to register tasks.

## PlaceholderMap
`PlaceholderMap` is an object in which different placeholders can be placed. This object accepts a variety of different objects, ranging from primitive types and all numbers to JDA types such as `User`, `Message`, `Channel`, etc.

Below is an example of applying placeholders to a string:
```java
PlaceholderMap map = new PlaceholderMap(ctx);
map.put("color", "red");
String input = "Hello {ctx_user_mention}, apple has {color} color! Here is also uppercase version in case you need it: {color_upper}...";
System.out.println("Applied placeholders: " + map.applyPlaceholders(input));
```

You can also utilize `Placehoderable` interface to directly instruct `PlaceholderMap` which of object's properties should be registered as placeholders.

## ResponseBuilder
The `ResponseBuilder` class handles interactions with users, sending responses in the form of messages or modals. Use the `ResponseBuilder#build()` method to begin response creation process. Once your response has been created, use `#send()` in order to send it. 

`ResponseBuilder` also contains a few specific methods worth explaining:
- `setBonus` - Allows passing JsonObject data to components upon message sending. This data can later be retrieved upon handling component execution. 
- `setComponentConsumer` - Enables the integration of executable consumer to respond when component is used. This feature proves highly advantageous for managing components with diverse appearances yet varying functionalities and same parent handlers, such as confirmation buttons. 
- `setComponentsLimit` - Sets the maximum number of components that can be added to a message.
- `injectDropdownOptions` - Forcefully injects dropdown options into selected dropdown.
- `injectEmbedFields` - Forcefully injects embed fields into response embed. This can be disabled by setting `include_injected_fields` to `false` within response object.

### Response structure
```json
{
  "response": {
    "message": "Hello!",
    "description": "",
    "title": "",
    "title_url": "",
    "footer_text": "",
    "footer_icon_url": "",
    "author_text": "",
    "author_url": "",
    "author_icon_url": "",
    "timestamp": "",
    "thumbnail_url": "",
    "image_url": "",
    "color": "",
    "fields": [
      {
        "name": "",
        "value": "",
        "blank": false,
        "inline": false
      }
    ],
    "include_injected_fields": true, 
    "inject_blank_fields": [
      {
        "index": 0,
        "inline": true
      }
    ],
    "components": [""],
    "modal": "",
    "reactions": [""],
    "delete_after": 0,
    "ephemeral": false,
    "pin": false,
    "crosspost": false
  }
}
```
In cases where a message is supposed to be edited, the placeholder map will automatically receive additional placeholders containing previous message data (including embed data). The response object will have the following additional properties:
```json
{
   "response": {
      "clear_old_message_content": true,
      "clear_old_embed": true,
      "clear_old_files": true,
      "clear_old_components": true,
      "clear_reactions": false
   }
}
```

### Component (handler) overriding

Both component's appearance and handler's behavior can be fully overridden upon sending response object. In case a property is not overridden, the default value will be used.

```json
{
   "response": {
      "message": "Hello!",
      "components": [
         {
            "name": "showcase_button",
            "row_index": 0,
            "style": "SUCCESS",
            "label": "Yes!",
            "ephemeral_defer": false
         },
         {
            "name": "showcase_button",
            "row_index": 1,
            "style": "DANGER",
            "label": "No",
            "ephemeral_defer": true,
            "timeout": 15
         }
      ]
   }
}
```

## EventWaiter and interfaces
The `EventWaiter` class facilitates awaiting events within specific classes without necessitating the registration of separate listener classes. 

Here's an example utilizing this feature in order to await for `MessageReceivedEvent`:

```java
DiscordBot.getInstance().getEventWaiter().waitForEvent(
        MessageReceivedEvent.class, 
        event -> event.getAuthor().getName().equalsIgnoreCase("mrepiko"),
        event -> System.out.println("Hello MrEpiko, how are you today?"),
        10,
        () -> System.out.println("It seems like MrEpiko is not here :(")
);
```

`Configurable` interface should be used when object requires JSON configuration files.

`Cacheable` interface should be used upon `Task` objects that require caching. 

## License
This project is licensed under the [MIT license](LICENSE.md).
