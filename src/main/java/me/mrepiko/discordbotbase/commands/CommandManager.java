package me.mrepiko.discordbotbase.commands;

import lombok.Getter;
import me.mrepiko.discordbotbase.DiscordBot;
import me.mrepiko.discordbotbase.commands.handlers.ShowcaseCmd;
import me.mrepiko.discordbotbase.commands.types.Command;
import me.mrepiko.discordbotbase.commands.types.ResponseCommand;
import me.mrepiko.discordbotbase.context.interaction.CommandContext;
import me.mrepiko.discordbotbase.mics.Constants;
import me.mrepiko.discordbotbase.mics.PlaceholderMap;
import me.mrepiko.discordbotbase.mics.ResponseBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

@Getter
public class CommandManager extends ListenerAdapter {

    private final List<Command> commands = new ArrayList<>();
    private final HashMap<String, HashMap<String, List<net.dv8tion.jda.api.interactions.commands.Command.Choice>>> autocompleteOptions = new HashMap<>();

    public void reload() {
        commands.clear();
        setupCommands();
    }

    private void registerCommands() {
        addCommand(new ShowcaseCmd());
    }

    private void sortSubcommands() {
        for (Command c: commands) {
            if (c.getParentName().isEmpty()) continue;
            Command parent = getCommand(c.getParentName());
            if (parent == null) continue;
            c.setParent(parent);
            parent.addChild(c);
        }
    }

    private void addCommand(Command command) {
        if (contains(command)) return;
        commands.add(command);
    }

    @Nullable
    private Command getCommand(String name) {
        for (Command c: commands) if (c.getName().equalsIgnoreCase(name) || c.getAliases().contains(name)) return c;
        return null;
    }

    private boolean contains(Command command) {
        for (Command c: commands) if (c.getName().toLowerCase(Locale.ROOT).equalsIgnoreCase(command.getName().toLowerCase(Locale.ROOT))) return true;
        return false;
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void setupCommands() {
        registerCommands();
        sortSubcommands();
        File folder = new File(Constants.COMMAND_CONFIGURATION_FOLDER_PATH);
        File responseFolder = new File(Constants.RESPONSE_COMMAND_CONFIGURATION_FOLDER_PATH);
        if (!folder.exists()) {
            folder.mkdir();
            return;
        }
        if (responseFolder.exists()) responseFolder.mkdir();

        Set<Command> commandsToBeRegistered = new HashSet<>();
        List<CommandData> commandDataList = new ArrayList<>();
        HashMap<String, List<CommandData>> guildSpecificCommands = new HashMap<>();

        HashMap<String, Boolean> fileQueue = new HashMap<>();
        for (File f: Objects.requireNonNull(folder.listFiles())) fileQueue.put(f.getName().substring(0, f.getName().length() - 5), false);
        for (File f: Objects.requireNonNull(responseFolder.listFiles())) fileQueue.put(f.getName().substring(0, f.getName().length() - 5), true);

        for (Map.Entry<String, Boolean> e: fileQueue.entrySet()) {
            String name = e.getKey();
            boolean responseCommand = e.getValue();
            Command command = (responseCommand) ? new ResponseCommand(name) : getCommand(name);
            if (command == null || command.getCommandConfig() == null || command.getParent() != null) {
                commandsToBeRegistered.add(command);
                continue;
            }
            List<String> allCommandAppearances = new ArrayList<>(command.getAliases());
            allCommandAppearances.add(name);
            commandsToBeRegistered.add(command);

            if (command.isGlobal()) {
                for (String a: allCommandAppearances) {
                    if (command.isHideOriginalName() && a.equalsIgnoreCase(name)) continue;
                    SlashCommandData data = Commands.slash(a, command.getDescription())
                            .setGuildOnly(false)
                            .addOptions(command.getOptionsList().stream().map(Command.Option::getOptionData).toList())
                            .setDefaultPermissions((command.isAdmin()) ? DefaultMemberPermissions.DISABLED : DefaultMemberPermissions.ENABLED);
                    for (Command c: command.getChildren()) data.addSubcommands(new SubcommandData(c.getName().replace("_", "").replace(command.getName(), ""), c.getDescription()).addOptions(c.getOptionsList().stream().map(Command.Option::getOptionData).toList()));
                    commandDataList.add(data);
                }
            } else {
                if (command.getGuilds().isEmpty()) {
                    for (String a: allCommandAppearances) {
                        if (command.isHideOriginalName() && a.equalsIgnoreCase(name)) continue;
                        SlashCommandData data = Commands.slash(a, command.getDescription())
                                .setGuildOnly(true)
                                .addOptions(command.getOptionsList().stream().map(Command.Option::getOptionData).toList())
                                .setDefaultPermissions((command.isAdmin()) ? DefaultMemberPermissions.DISABLED : DefaultMemberPermissions.ENABLED);
                        for (Command c: command.getChildren()) data.addSubcommands(new SubcommandData(c.getName().replace("_", "").replace(command.getName(), ""), c.getDescription()).addOptions(c.getOptionsList().stream().map(Command.Option::getOptionData).toList()));
                        commandDataList.add(data);
                    }
                } else {
                    for (String guildId : command.getGuilds()) {
                        List<CommandData> previousCommands = new ArrayList<>(guildSpecificCommands.getOrDefault(guildId, new ArrayList<>()));
                        for (String a : allCommandAppearances) {
                            if (command.isHideOriginalName() && a.equalsIgnoreCase(name)) continue;
                            SlashCommandData data = Commands.slash(a, command.getDescription())
                                    .setGuildOnly(true)
                                    .addOptions(command.getOptionsList().stream().map(Command.Option::getOptionData).toList())
                                    .setDefaultPermissions((command.isAdmin()) ? DefaultMemberPermissions.DISABLED : DefaultMemberPermissions.ENABLED);
                            for (Command c : command.getChildren()) data.addSubcommands(new SubcommandData(c.getName().replace("_", "").replace(command.getName(), ""), c.getDescription()).addOptions(c.getOptionsList().stream().map(Command.Option::getOptionData).toList()));
                            previousCommands.add(data);
                        }
                        guildSpecificCommands.put(guildId, previousCommands);
                    }
                }
            }

            if (command.getAutocompleteOptions().size() > 0) for (String a: allCommandAppearances) autocompleteOptions.put(a, command.getAutocompleteOptions());
            System.out.println(((responseCommand) ? "[ResponseCommand]" : "[Command]") +
                    " " +
                    name +
                    " has been registered " +
                    ((command.isGlobal()) ? "globally" : "in " + ((command.getGuilds().size() == 0) ? DiscordBot.getInstance().getJda().getGuilds().size() : command.getGuilds().size()) + " guild(s)") +
                    ((command.getChildren().isEmpty()) ? "." : " with " + command.getChildren().size() + " subcommand(s).")
            );
        }

        List<String> overrideCommandNames = commandDataList.stream().map(CommandData::getName).toList();
        JDA jda = DiscordBot.getInstance().getJda();
        jda.retrieveCommands().queue(retrievedCommands -> {
            for (net.dv8tion.jda.api.interactions.commands.Command c: retrievedCommands) if (!overrideCommandNames.contains(c.getName())) jda.deleteCommandById(c.getId()).queue(d -> {}, d -> {});
            jda.updateCommands().addCommands(commandDataList).queue();
        });
        for (Map.Entry<String, List<CommandData>> e: guildSpecificCommands.entrySet()) {
            Guild guild = jda.getGuildById(e.getKey());
            if (guild == null) continue;
            List<String> overrideGuildCommandNames = e.getValue().stream().map(CommandData::getName).toList();
            guild.retrieveCommands().queue(retrieveCommands -> {
                for (net.dv8tion.jda.api.interactions.commands.Command c: retrieveCommands) if (!overrideGuildCommandNames.contains(c.getName())) jda.deleteCommandById(c.getId()).queue(d -> {}, d -> {});
                guild.updateCommands().addCommands(e.getValue()).queue();
            });
        }
        commands.clear();
        commands.addAll(commandsToBeRegistered);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Command command = getCommand(event.getFullCommandName().replace(" ", "_"));
        if (command == null) return;

        User user = event.getUser();
        CommandContext ctx = new CommandContext(event, command);
        PlaceholderMap map = new PlaceholderMap(ctx);
        DiscordBot instance = DiscordBot.getInstance();

        if (!command.isEnabled()) {
            ResponseBuilder.build(map, command.getErrorHandlers().get("disabled")).send();
            return;
        }
        if (command.isAdmin() && !instance.getAdmins().contains(user.getId())) {
            ResponseBuilder.build(map, command.getErrorHandlers().get("reserved_for_admin")).send();
            return;
        }
        Long currentCooldown = command.getCooldowns().getOrDefault(user.getId(), 0L);
        if (currentCooldown > 0) {
            if (System.currentTimeMillis() - currentCooldown < command.getCooldown() * 1000) {
                map.put("cooldown_time_left", String.valueOf(Math.round((double) currentCooldown / 1000 + command.getCooldown() - (double) System.currentTimeMillis() / 1000)));
                ResponseBuilder.build(map, command.getErrorHandlers().get("cooldown")).send();
                return;
            } else command.getCooldowns().remove(user.getId());
        }
        if (event.getMember() != null && !command.getRequiredRoles().isEmpty()) {
            boolean hasRole = false;
            for (Role r: event.getMember().getRoles()) {
                if (command.getRequiredRoles().contains(r.getId())) {
                    hasRole = true;
                    break;
                }
            }
            if (!hasRole) {
                ResponseBuilder.build(map, command.getErrorHandlers().get("reserved_for_role")).send();
                return;
            }
        }
        if (!command.getRequiredUsers().isEmpty() && !command.getRequiredUsers().contains(user.getId())) {
            ResponseBuilder.build(map, command.getErrorHandlers().get("reserved_for_user")).send();
            return;
        }
        if (event.isFromGuild() && !command.getRequiredChannels().isEmpty() && !command.getRequiredChannels().contains(event.getChannelId())) {
            ResponseBuilder.build(map, command.getErrorHandlers().get("reserved_for_channel")).send();
            return;
        }
        if (event.getMember() != null && !command.getRequiredPermissions().isEmpty() && !event.getMember().hasPermission(command.getRequiredPermissions())) {
            ResponseBuilder.build(map, command.getErrorHandlers().get("missing_permissions")).send();
            return;
        }
        if (event.getMember() != null && !command.getRequiredChannelPermissions().isEmpty() && !event.getMember().hasPermission((GuildMessageChannel) event.getChannel(), command.getRequiredChannelPermissions())) {
            ResponseBuilder.build(map, command.getErrorHandlers().get("missing_channel_permissions")).send();
            return;
        }

        for (Command.Option option: command.getOptionsList()) {
            for (OptionMapping optionMapping : event.getOptions()) {
                if (option.getOptionData().getName().equalsIgnoreCase(optionMapping.getName())) {
                    map.put("option", option.getOptionData().getName());
                    if (!option.isEnabled()) {
                        ResponseBuilder.build(map, option.getErrorHandlers().get("disabled")).send();
                        return;
                    }
                    if (option.isAdmin() && !instance.getAdmins().contains(user.getId())) {
                        ResponseBuilder.build(map, option.getErrorHandlers().get("reserved_for_admin")).send();
                        return;
                    }
                    if (event.getMember() != null && !option.getRequiredRoles().isEmpty()) {
                        boolean hasRole = false;
                        for (Role r: event.getMember().getRoles()) {
                            if (option.getRequiredRoles().contains(r.getId())) {
                                hasRole = true;
                                break;
                            }
                        }
                        if (!hasRole) {
                            ResponseBuilder.build(map, option.getErrorHandlers().get("reserved_for_role")).send();
                            return;
                        }
                    }
                    if (!option.getRequiredUsers().isEmpty() && !option.getRequiredUsers().contains(user.getId())) {
                        ResponseBuilder.build(map, option.getErrorHandlers().get("reserved_for_user")).send();
                        return;
                    }
                    if (event.isFromGuild() && !option.getRequiredChannels().isEmpty() && !option.getRequiredChannels().contains(event.getChannelId())) {
                        ResponseBuilder.build(map, option.getErrorHandlers().get("reserved_for_channel")).send();
                        return;
                    }
                    if (event.getMember() != null && !option.getRequiredPermissions().isEmpty() && !event.getMember().hasPermission(option.getRequiredPermissions())) {
                        ResponseBuilder.build(map, option.getErrorHandlers().get("missing_permissions")).send();
                        return;
                    }
                    if (event.getMember() != null && !option.getRequiredChannelPermissions().isEmpty() && !event.getMember().hasPermission((GuildMessageChannel) event.getChannel(), command.getRequiredChannelPermissions())) {
                        ResponseBuilder.build(map, option.getErrorHandlers().get("missing_channel_permissions")).send();
                        return;
                    }
                }
            }
        }

        if (command.getCooldown() > 0) command.getCooldowns().put(user.getId(), System.currentTimeMillis());
        if (command.isDefer()) ctx.getCallback().deferReply(command.isEphemeralDefer()).queue();
        command.handle(ctx);
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        List<net.dv8tion.jda.api.interactions.commands.Command.Choice> choices = new ArrayList<>(autocompleteOptions.getOrDefault(event.getName(), new HashMap<>()).getOrDefault(event.getFocusedOption().getName(), new ArrayList<>()));
        if (choices.isEmpty()) return;
        event.replyChoices(choices
                .stream()
                .filter(x -> x.getAsString().startsWith(event.getFocusedOption().getValue()))
                .map(x -> new net.dv8tion.jda.api.interactions.commands.Command.Choice(x.getName(), x.getAsString()))
                .toList()
        ).queue(x -> {}, x -> {});
    }
}
