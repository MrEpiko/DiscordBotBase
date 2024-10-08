package me.mrepiko.discordbotbase.mics;

public class Constants {

    public static final String CONFIGURATION_FOLDER_PATH = "configuration";
    public static final String MAIN_CONFIG_FILE_PATH = CONFIGURATION_FOLDER_PATH + "/config.json";
    public static final String MODULE_CONFIGURATION_FOLDER_PATH = CONFIGURATION_FOLDER_PATH + "/modules";
    public static final String COMMAND_CONFIGURATION_FOLDER_PATH = CONFIGURATION_FOLDER_PATH + "/commands";
    public static final String RESPONSE_COMMAND_CONFIGURATION_FOLDER_PATH = CONFIGURATION_FOLDER_PATH + "/responsecommands";
    public static final String COMPONENT_CONFIGURATION_FOLDER_PATH = CONFIGURATION_FOLDER_PATH + "/components";

    public static final String COMMAND_STRUCTURE = """
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
                    "required_roles": [],
                    "required_users": [],
                    "required_channels": [],
                    "required_permissions": [],
                    "required_channel_permissions": [],
                    "admin": false,
                    "enabled": true,
                    "error_handlers": {
                        "reserved_for_role": {},
                        "reserved_for_user": {},
                        "reserved_for_channel": {},
                        "reserved_for_admin": {},
                        "missing_permissions": {},
                        "missing_channel_permissions": {},
                        "disabled": {}
                    },
                    "choices": [
                      {
                        "name": "",
                        "value": ""
                      }
                    ]
                  }
                ]
              },
                        
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
                "reactions": [],
                "ephemeral": false,
                "pin": false
              }
                        
            }
            """;

    public static final String COMPONENT_STRUCTURE = """
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
                "disable_once_used": false,
                "disable_all_once_used": false
              },
                        
              "properties": {
                "placeholder": "",
                "min_options": 0,
                "max_options": 1,
                "options": [
                  {
                    "name": "",
                    "value": "",
                    "description": "",
                    "emoji": ""
                  }
                ],
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
                "disable_once_used": false,
                "disable_all_once_used": false
              },
                        
              "properties": {
                "title": "",
                "fields": [
                  {
                    "id": "",
                    "label": "",
                    "style": "",
                    "min_length": 0,
                    "max_length": 1,
                    "placeholder": "",
                    "value": "",
                    "required": false
                  }
                ],
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
            """;

}
