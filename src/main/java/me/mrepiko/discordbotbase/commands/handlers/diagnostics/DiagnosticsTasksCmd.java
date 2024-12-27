package me.mrepiko.discordbotbase.commands.handlers.diagnostics;

import me.mrepiko.discordbotbase.DiscordBot;
import me.mrepiko.discordbotbase.commands.types.Command;
import me.mrepiko.discordbotbase.context.interaction.CommandContext;
import me.mrepiko.discordbotbase.mics.ResponseBuilder;
import me.mrepiko.discordbotbase.mics.placeholders.PlaceholderMap;
import me.mrepiko.discordbotbase.tasks.Task;

import java.util.Locale;

public class DiagnosticsTasksCmd extends Command {

    public DiagnosticsTasksCmd() {
        super("diagnostics_tasks");
    }

    @Override
    public void handle(CommandContext ctx) {
        PlaceholderMap map = new PlaceholderMap(ctx);

        StringBuilder tasks = new StringBuilder();
        for (Task task : DiscordBot.getInstance().getTaskManager().getTasks().values()) {
            Task.Type taskType = task.getTaskType();
            map.put("task_id", task.getId());
            map.put("task_type", taskType.name());
            map.put("task_period", task.getPeriod());
            map.put("task_delay", task.getDelay());
            map.put("task_first_execution_timestamp", task.getStartedAtTimestamp());

            if (taskType == Task.Type.SCHEDULED_AT_FIXED_RATE) {
                long currentTimestamp = System.currentTimeMillis();
                long mostRecentExecution = task.getStartedAtTimestamp() * 1000;
                long nextExecution = task.getStartedAtTimestamp() * 1000;
                long periodMillis = task.getPeriod() * 1000;
                while (mostRecentExecution + periodMillis <= currentTimestamp) {
                    mostRecentExecution += periodMillis;
                }
                while (nextExecution <= currentTimestamp) {
                    nextExecution += periodMillis;
                }
                map.put("task_most_recent_execution_timestamp", (mostRecentExecution <= task.getStartedAtTimestamp() * 1000) ? task.getStartedAtTimestamp() : mostRecentExecution / 1000);
                map.put("task_next_execution_timestamp", nextExecution / 1000);
            }

            map.put("task", map.applyPlaceholders(getString(taskType.name().toLowerCase(Locale.ROOT) + "_task_template")));
            tasks.append(map.applyPlaceholders(getString("task_template")));
        }
        map.put("tasks", tasks.toString());

        ResponseBuilder.build(map, getJsonObject("response")).send();
    }

    @Override
    public void setupDefaultConfigurations() {
        expectResponseObject("response");
        expect("scheduled_at_fixed_rate_task_template", "");
        expect("scheduled_with_fixed_delay_task_template", "");
        expect("task_template", "");
    }
}
