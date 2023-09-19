package de.codingair.tradesystem.ext.audit.commands;

import de.codingair.codingapi.server.commands.builder.BaseComponent;
import de.codingair.codingapi.server.commands.builder.CommandBuilder;
import de.codingair.codingapi.server.commands.builder.CommandComponent;
import de.codingair.codingapi.server.commands.builder.special.MultiCommandComponent;
import de.codingair.tradesystem.ext.audit.TradeAudit;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLog;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogService;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CTradeLog extends CommandBuilder {
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss");

    public CTradeLog() {
        super(TradeAudit.getInstance(), "tradelog", new BaseComponent() {
            @Override
            public void noPermission(CommandSender sender, String s, CommandComponent commandComponent) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("No_Permissions"));
            }

            @Override
            public void onlyFor(boolean players, CommandSender sender, String label, CommandComponent commandComponent) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Only_for_Player"));
            }

            @Override
            public void unknownSubCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Command_TradeLog", new Lang.P("label", label)));
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] strings) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Command_TradeLog", new Lang.P("label", label)));
                return false;
            }
        }.setOnlyPlayers(true), true);

        getBaseComponent().addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    suggestions.add(player.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                Bukkit.getScheduler().runTaskAsynchronously(TradeSystem.getInstance(), () -> {
                    if (!TradeLogService.connected()) {
                        Lang.send(sender, "TradeLog_Disabled", new Lang.P("label", label));
                        return;
                    }

                    List<TradeLog.Entry> log = TradeLogService.getLogMessages(argument);

                    List<String> messages = new ArrayList<>();
                    messages.add("§0");
                    messages.add("§0");
                    messages.add("§7§m                            §c §lTRADE LOG§7 §m                            ");
                    messages.add("§0");

                    if (log.isEmpty()) messages.add("  §c-");
                    else {
                        String p1 = null;
                        String p2 = null;
                        boolean samePlayers = false;

                        for (int i = 0; i < log.size(); i++) {
                            TradeLog.Entry l = log.get(i);
                            boolean last = i + 1 == log.size();

                            String name1 = l.getPlayer1Name();
                            String name2 = l.getPlayer2Name();

                            samePlayers = p1 == null || Objects.equals(p1, name1) && Objects.equals(p2, name2);
                            if (!samePlayers) {
                                messages.add("§0");
                                messages.add("§8§m                               §7 Players: §e" + p1 + " §7& §e" + p2);
                                if (!last) messages.add("§0");
                            }

                            String color = TradeLog.getColorByString(l.getMessage());
                            messages.add("§8" + l.getTimestamp().format(formatter) + " " + color + "» §7" + l.getMessage());

                            p1 = name1;
                            p2 = name2;
                        }

                        if (samePlayers) {
                            messages.add("§0");
                            messages.add("§8§m                               §7 Players: §e" + p1 + " §7& §e" + p2);
                        }
                    }

                    sender.sendMessage(messages.toArray(new String[0]));
                });
                return false;
            }
        });
    }
}
