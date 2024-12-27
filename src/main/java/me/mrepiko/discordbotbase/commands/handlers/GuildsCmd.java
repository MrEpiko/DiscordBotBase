package me.mrepiko.discordbotbase.commands.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.mrepiko.discordbotbase.commands.types.Command;
import me.mrepiko.discordbotbase.context.interaction.CommandContext;
import me.mrepiko.discordbotbase.mics.ResponseBuilder;
import me.mrepiko.discordbotbase.mics.placeholders.PlaceholderMap;
import me.mrepiko.discordbotbase.mics.placeholders.Placeholderable;
import me.mrepiko.discordbotbase.mics.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GuildsCmd extends Command {

    private final Logger LOGGER = LoggerFactory.getLogger(GuildsCmd.class);

    public GuildsCmd() {
        super("guilds");
    }

    @Override
    public void handle(CommandContext ctx) {
        PlaceholderMap map = new PlaceholderMap(ctx);
        map.put("guild_count", ctx.getJDA().getGuilds().size());

        List<SimpleGuild> guilds = new ArrayList<>();
        int totalMembers = 0;

        for (Guild guild : ctx.getJDA().getGuilds()) {
            guilds.add(new SimpleGuild(guild.getName(), guild.getId(), guild.getMemberCount(), guild.getSelfMember().getTimeJoined(), guild.getOwnerId()));
            totalMembers += guild.getMemberCount();
        }

        StringBuilder guildsSortedByMemberCount = new StringBuilder();
        StringBuilder guildsSortedByJoinTimestamp = new StringBuilder();

        guilds.sort((a, b) -> b.getMemberCount() - a.getMemberCount());
        for (SimpleGuild guild : guilds) {
            map.put("guild", guild);
            guildsSortedByMemberCount.append(map.applyPlaceholders(getString("member_count_template")));
        }

        guilds.sort(Comparator.comparing(SimpleGuild::getJoinedAt));
        Collections.reverse(guilds);
        for (SimpleGuild guild : guilds) {
            map.put("guild", guild);
            guildsSortedByJoinTimestamp.append(map.applyPlaceholders(getString("join_timestamp_template")));
        }

        map.put("member_count", totalMembers);
        map.put("member_count_guilds_key", Utils.uploadAndGet(guildsSortedByMemberCount.toString()));
        map.put("join_timestamp_guilds_key", Utils.uploadAndGet(guildsSortedByJoinTimestamp.toString()));

        ResponseBuilder.build(map, getJsonObject("response")).send();
    }

    @Override
    public void setupDefaultConfigurations() {
        expectResponseObject("response");
        expect("member_count_template", "");
        expect("join_timestamp_template", "");
    }

    @AllArgsConstructor
    @Getter
    public static class SimpleGuild implements Placeholderable {
        private String name;
        private String id;
        private int memberCount;
        private OffsetDateTime joinedAt;
        private String ownerId;

        @Override
        public PlaceholderMap getPlaceholderMap() {
            PlaceholderMap map = new PlaceholderMap();
            map.put("name", name);
            map.put("id", id);
            map.put("member_count", memberCount);
            map.put("joined_at_timestamp", joinedAt.toInstant().getEpochSecond());
            map.put("joined_at", joinedAt);
            map.put("owner_id", ownerId);
            return map;
        }
    }

}
