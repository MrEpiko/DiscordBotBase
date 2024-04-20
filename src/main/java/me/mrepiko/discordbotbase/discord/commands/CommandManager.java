package me.mrepiko.discordbotbase.discord.commands;

import com.google.gson.JsonObject;
import lombok.Getter;
import me.mrepiko.discordbotbase.discord.DiscordBot;
import me.mrepiko.discordbotbase.discord.commands.handlers.ShowcaseCmd;
import me.mrepiko.discordbotbase.discord.commands.types.Command;
import me.mrepiko.discordbotbase.discord.commands.types.ResponseCommand;
import me.mrepiko.discordbotbase.discord.context.interaction.CommandContext;
import me.mrepiko.discordbotbase.discord.mics.Constants;
import me.mrepiko.discordbotbase.discord.mics.PlaceholderMap;
import me.mrepiko.discordbotbase.discord.mics.ResponseBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
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
            if (command == null || command.getCommandConfig() == null || command.getParent() != null) continue;
            List<String> allCommandAppearances = new ArrayList<>(command.getAliases());
            allCommandAppearances.add(name);
            commandsToBeRegistered.add(command);

            if (command.isGlobal()) {
                for (String a: allCommandAppearances) {
                    if (command.isHideOriginalName() && a.equalsIgnoreCase(name)) continue;
                    SlashCommandData data = Commands.slash(a, command.getDescription())
                            .setGuildOnly(false)
                            .addOptions(command.getOptionDataList())
                            .setDefaultPermissions((command.isAdmin()) ? DefaultMemberPermissions.DISABLED : DefaultMemberPermissions.ENABLED);
                    for (Command c: command.getChildren()) data.addSubcommands(new SubcommandData(c.getName().replace("_", "").replace(command.getName(), ""), c.getDescription()));
                    commandDataList.add(data);
                }
            } else {
                if (command.getGuilds().isEmpty()) {
                    for (String a: allCommandAppearances) {
                        if (command.isHideOriginalName() && a.equalsIgnoreCase(name)) continue;
                        SlashCommandData data = Commands.slash(a, command.getDescription())
                                .setGuildOnly(true)
                                .addOptions(command.getOptionDataList())
                                .setDefaultPermissions((command.isAdmin()) ? DefaultMemberPermissions.DISABLED : DefaultMemberPermissions.ENABLED);
                        for (Command c: command.getChildren()) data.addSubcommands(new SubcommandData(c.getName().replace("_", "").replace(command.getName(), ""), c.getDescription()));
                        commandDataList.add(data);
                    }
                } else {
                    for (String guildId : command.getGuilds()) {
                        List<CommandData> previousCommands = new ArrayList<>(guildSpecificCommands.getOrDefault(guildId, new ArrayList<>()));
                        for (String a : allCommandAppearances) {
                            if (command.isHideOriginalName() && a.equalsIgnoreCase(name)) continue;
                            SlashCommandData data = Commands.slash(a, command.getDescription())
                                    .setGuildOnly(true)
                                    .addOptions(command.getOptionDataList())
                                    .setDefaultPermissions((command.isAdmin()) ? DefaultMemberPermissions.DISABLED : DefaultMemberPermissions.ENABLED);
                            for (Command c : command.getChildren())
                                data.addSubcommands(new SubcommandData(c.getName().replace("_", "").replace(command.getName(), ""), c.getDescription()));
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
                    ((command.isGlobal()) ? "globally" : "in " + command.getGuilds().size() + " guild(s)") +
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
            ResponseBuilder.buildAndSend(map, command.getErrorHandlers().get("disabled"));
            return;
        }
        if (command.isAdmin() && !instance.getAdmins().contains(user.getId())) {
            ResponseBuilder.buildAndSend(map, command.getErrorHandlers().get("reserved_for_admin"));
            return;
        }
        Long currentCooldown = command.getCooldowns().getOrDefault(user.getId(), 0L);
        if (currentCooldown > 0) {
            if (System.currentTimeMillis() - currentCooldown < command.getCooldown() * 1000) {
                map.put("cooldown_time_left", String.valueOf(Math.round((double) currentCooldown / 1000 + command.getCooldown() - (double) System.currentTimeMillis() / 1000)));
                ResponseBuilder.buildAndSend(map, command.getErrorHandlers().get("cooldown"));
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
                ResponseBuilder.buildAndSend(map, command.getErrorHandlers().get("reserved_for_role"));
                return;
            }
        }
        if (!command.getRequiredUsers().isEmpty() && !command.getRequiredUsers().contains(user.getId())) {
            ResponseBuilder.buildAndSend(map, command.getErrorHandlers().get("reserved_for_user"));
            return;
        }
        if (event.isFromGuild() && !command.getRequiredChannels().isEmpty() && !command.getRequiredChannels().contains(event.getChannelId())) {
            ResponseBuilder.buildAndSend(map, command.getErrorHandlers().get("reserved_for_channel"));
            return;
        }
        if (event.getMember() != null && !command.getRequiredPermissions().isEmpty() && !event.getMember().hasPermission(command.getRequiredPermissions())) {
            ResponseBuilder.buildAndSend(map, command.getErrorHandlers().get("missing_permissions"));
            return;
        }
        for (Permission x : event.getMember().getPermissions((GuildMessageChannel) event.getChannel())) {
            System.out.println(x);
        }
        if (event.getMember() != null && !command.getRequiredChannelPermissions().isEmpty() && !event.getMember().hasPermission((GuildMessageChannel) event.getChannel(), command.getRequiredChannelPermissions())) {
            ResponseBuilder.buildAndSend(map, command.getErrorHandlers().get("missing_channel_permissions"));
            return;
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
