package me.mrepiko.discordbotbase.mics;

import com.google.gson.JsonObject;

public class Constants {

    public static final int HOURS_IN_DAY = 24;
    public static final int MINUTES_IN_DAY = 1440;
    public static final int SECONDS_IN_DAY = 86400;

    public static final int MINUTES_IN_HOUR = 60;
    public static final int SECONDS_IN_HOUR = 3600;

    public static final int SECONDS_IN_MINUTE = 60;
    public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

    public static final String CONFIGURATION_FOLDER_PATH = "configuration";
    public static final String MAIN_CONFIG_FILE_PATH = CONFIGURATION_FOLDER_PATH + "/config.json";
    public static final String MODULE_CONFIGURATION_FOLDER_PATH = CONFIGURATION_FOLDER_PATH + "/modules";
    public static final String COMMAND_CONFIGURATION_FOLDER_PATH = CONFIGURATION_FOLDER_PATH + "/commands";
    public static final String RESPONSE_COMMAND_CONFIGURATION_FOLDER_PATH = CONFIGURATION_FOLDER_PATH + "/responsecommands";
    public static final String COMPONENT_CONFIGURATION_FOLDER_PATH = CONFIGURATION_FOLDER_PATH + "/components";

    public static JsonObject getDropdownOptionTemplate() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("label", "");
        jsonObject.addProperty("value", "");
        jsonObject.addProperty("description", "");
        jsonObject.addProperty("emoji", "");
        return jsonObject;
    }

    public static JsonObject getEmbedFieldTemplate() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("blank", false);
        jsonObject.addProperty("inline", false);
        jsonObject.addProperty("name", "");
        jsonObject.addProperty("value", "");
        return jsonObject;
    }

    public static final String COMMAND_STRUCTURE = """
            {
                       \s
              "properties": {
                "cooldown": 1,
                "required_roles": [],
                "required_users": [],
                "required_channels": [],
                "required_permissions": [],
                "required_channel_permissions": [],
                "required_bot_permissions": [],
                "error_handlers": {
                    "cooldown": {},
                    "reserved_for_role": {},
                    "reserved_for_user": {},
                    "reserved_for_channel": {},
                    "reserved_for_admin": {},
                    "missing_permissions": {},
                    "missing_channel_permissions": {},
                    "missing_bot_permissions": {},
                    "cannot_talk": {},
                    "disabled": {}
                },
                "description": "N/A",
                "admin": false,
                "talk": false,
                "aliases": [],
                "global": false,
                "guilds": [],
                "ephemeral_defer": false,
                "parent": "",
                "enabled": true,
                "hide_original_name": false,
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
              }
                       \s
            }
           \s""";

    public static final String COMPONENT_STRUCTURE = """
            {
                       \s
              "properties": {
                "label": "",
                "style": "",
                "emoji": "",
                "enabled": true,
                "cooldown": 0,
                "admin": false,
                "talk": false,
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
                    "reserved_for_invoker": {},
                    "missing_permissions": {},
                    "missing_channel_permissions": {},
                    "cannot_talk": {},
                    "disabled": {}
                },
                "timeout": 0,
                "delete_after_timeout": 0,
                "row_index": 0,
                "disable_once_used": false,
                "disable_all_once_used": false,
                "invoker_only": false
              },
                       \s
              "properties": {
                "placeholder": "",
                "min_options": 0,
                "max_options": 1,
                "options": [
                  {
                    "label": "",
                    "value": "",
                    "description": "",
                    "emoji": ""
                  }
                ],
                "enabled": true,
                "cooldown": 0,
                "admin": false,
                "talk": false,
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
                    "reserved_for_invoker": {},
                    "missing_permissions": {},
                    "missing_channel_permissions": {},
                    "cannot_talk": {},
                    "disabled": {}
                },
                "timeout": 0,
                "delete_after_timeout": 0,
                "row_index": 0,
                "disable_once_used": false,
                "disable_all_once_used": false,
                "invoker_only": false
              },
                       \s
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
                "enabled": true,
                "cooldown": 0,
                "admin": false,
                "talk": false,
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
                    "cannot_talk": {},
                    "disabled": {}
                }
              }
                       \s
            }
           \s""";

}
