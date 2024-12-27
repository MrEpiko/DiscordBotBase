package me.mrepiko.discordbotbase.commands.handlers.diagnostics;

import me.mrepiko.discordbotbase.DiscordBot;
import me.mrepiko.discordbotbase.commands.types.Command;
import me.mrepiko.discordbotbase.context.interaction.CommandContext;
import me.mrepiko.discordbotbase.mics.ResponseBuilder;
import me.mrepiko.discordbotbase.mics.placeholders.PlaceholderMap;
import me.mrepiko.discordbotbase.modules.Module;

public class DiagnosticsModulesCmd extends Command {

    public DiagnosticsModulesCmd() {
        super("diagnostics_modules");
    }

    @Override
    public void handle(CommandContext ctx) {
        PlaceholderMap map = new PlaceholderMap(ctx);

        StringBuilder modules = new StringBuilder();
        for (Module module : DiscordBot.getInstance().getModuleManager().getModules()) {
            map.put("module_name", module.getName());
            map.put("module_status", getString((module.isEnabled()) ? "enabled_template" : "disabled_template"));
            modules.append(map.applyPlaceholders(getString("module_template")));
        }
        map.put("modules", modules.toString());

        ResponseBuilder.build(map, getJsonObject("response")).send();
    }

    @Override
    public void setupDefaultConfigurations() {
        expectResponseObject("response");
        expect("enabled_template", "");
        expect("disabled_template", "");
        expect("module_template", "");
    }
}
