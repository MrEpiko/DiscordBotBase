# DiscordBotBase
DiscordBotBase is a Java project utilizing JDA, designed to empower users in creating fully customizable Discord bots through JSON configurations. This README provides comprehensive guidance on various aspects of the project, including configuration, commands, components, modules, and the ResponseBuilder.

## Table of Contents
1. [Getting started](#getting-started)
2. [Main configuration file](#main-configuration-file)
3. [Commands](#commands)
    - [Command structure](#command-structure)
    - [Subcommands](#subcommands)
    - [Response commands](#response-commands)
4. [Components](#components)
    - [Button component structure](#button-component-structure)
    - [Dropdown component structure](#dropdown-component-structure)
    - [Modal component structure](#modal-component-structure)
5. [Modules](#modules)
6. [ResponseBuilder](#responsebuilder)
    - [Response structure](#response-structure)
    - [Component row overriding](#component-row-overriding)
    - [Component appearance overriding](#component-appearance-overriding)
7. [License](#license)

## Getting started
1. Clone the `DiscordBotBase` repository to your local machine.
2. Customize your bot's behavior.
3. Explore examples and showcases in the `configuration` folder.
4. Build and run your bot using Java and the JDA library.

## Main configuration file
The main configuration file (`configuration/config.json`) serves as the cornerstone for customizing your bot's behavior. Here, you can set default values, specify your bot's token, define admins, configure intents, error handlers, database credentials, enable modules etc.

## Commands
To implement a command, create a class extending the `Command` class. Register the command in `CommandManager#registerCommands()`. Additionally, create a JSON file with the command's name in `configuration/commands`. Upon bot startup, this file will be filled with default JSON properties, which are self-explanatory. From here, you are free to customize the command's appearance and functionality.

Command error handlers can be overridden upon configuring command's properties. If no overrides are provided, bot will respond with default error handlers (configured in `configuration/config.json`). Same principle is later applied to component handlers. 

### Command structure
```json
{
  "properties": {
    "cooldown": 1,
    "required_roles": [],
    "required_users": [],
    "required_channels": [], 
    "required_permissions": [],
    "required_channel_permissions": [],
    "error_handlers": {
      "cooldown": {},
      "reserved_for_role": {}, 
      "reserved_for_user": {},
      "reserved_for_channel": {},
      "reserved_for_admin": {},
      "missing_permissions": {},
      "missing_channel_permissions": {},
      "disabled": {}
    },
    "description": "N/A",
    "admin": false,
    "aliases": [],
    "global": false,
    "guilds": [],
    "ephemeral_defer": false,
    "parent": "",
    "options": [
      {
        "name": "",
        "type": "STRING",
        "description": "N/A",
        "required": false,
        "autocomplete": false,
        "choices": [
          {
            "name": "",
            "value": ""
          }
        ]
      }
    ]
  }
}
```

### Subcommands
In order to implement a subcommand, create a command with the name `[parent command]_[subcommand]`. Subcommand's properties should include a parent value referencing the parent command's name.

### Response commands
Response commands require no handler implementation. These can be added to `configuration/responsecommands` and configured from there.

## Components
Components include buttons (`ButtonHandler`), dropdowns (`DropdownHandler`) and modals (`ModalHandler`). Handlers are created similarly to command handlers and are registered in `ComponentManager#registerComponentHandlers()`. 

### Button component structure
```json
{
  "properties": {
    "label": "",
    "style": "",
    "emoji": "",
    "cooldown": 0,
    "admin": false,
    "ephemeral_defer": false,
    "required_roles": [],
    "required_users": [],
    "required_channels": [],
    "required_permissions": [],
    "required_channel_permissions": [],
    "error_handlers": {
      "cooldown": {},
      "reserved_for_role": {},
      "reserved_for_user": {},
      "reserved_for_channel": {},
      "reserved_for_admin": {},
      "missing_permissions": {},
      "missing_channel_permissions": {},
      "disabled": {}
    },
    "timeout": 0,
    "delete_after_timeout": 0,
    "row_index": 0,
    "disable_once_used": true
  }
}
```
### Dropdown component structure
```json
{
  "properties": {
    "placeholder": "",
    "min_options": 0,
    "max_options": 1,
    "options": [],
    "cooldown": 0,
    "admin": false,
    "ephemeral_defer": false,
    "required_roles": [],
    "required_users": [],
    "required_channels": [],
    "required_permissions": [],
    "required_channel_permissions": [],
    "error_handlers": {
      "cooldown": {},
      "reserved_for_role": {},
      "reserved_for_user": {},
      "reserved_for_channel": {},
      "reserved_for_admin": {},
      "missing_permissions": {},
      "missing_channel_permissions": {},
      "disabled": {}
    },
    "timeout": 0,
    "delete_after_timeout": 0,
    "row_index": 0,
    "disable_once_used": true
  }
}
```
### Modal component structure
```json
{
  "properties": {
    "title": "",
    "fields": [],
    "cooldown": 0,
    "admin": false,
    "ephemeral_defer": false,
    "required_roles": [],
    "required_users": [],
    "required_channels": [], 
    "required_permissions": [],
    "required_channel_permissions": [],
    "error_handlers": {
      "cooldown": {},
      "reserved_for_role": {},
      "reserved_for_user": {},
      "reserved_for_channel": {},
      "reserved_for_admin": {},
      "missing_permissions": {},
      "missing_channel_permissions": {}, 
      "disabled": {}
    }
  }
}
```
## Modules
Modules can be enabled from the main configuration file. Each module extends the `Module` class and can register listeners, tasks, both or none. Modules must be registered through `ModuleManager#registerModules()`. Listener modules should use the `registerListener()` method within the `onEnable()` method to register listeners to JDA. Task modules, on the other hand, contain tasks that are repeated at intervals. Use the `registerTask()` method to register tasks.

## ResponseBuilder
The `ResponseBuilder` class handles interactions with users, sending responses in the form of messages or modals. Use the `ResponseBuilder#buildAndSend()` method to send responses. The bonus value allows passing data to components upon message sending. 

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
          "blank": true,
          "inline": false
        }
    ],
    "components": [""],
    "modal": "",
    "delete_after": 0,
    "ephemeral": false,
    "pin": false
  }
}
```

### Component row overriding
```json
{
   "response": {
      "message": "Hello!",
      "components": [
         {
            "name": "showcase_button",
            "row_index": 0
         },
         {
            "name": "second_showcase_button",
            "row_index": 1
         }
      ]
   }
}
```

### Component appearance overriding
```json
{
   "response": {
      "message": "Hello!",
      "components": [
         {
            "name": "showcase_button",
            "style": "DANGER",
            "label": "Hello!"
         }
      ]
   }
}
```

## License
This project is licensed under the [MIT license](LICENSE.md).
