/**
 * Copyright (C) 2011 DThielke <dave.thielke@gmail.com>
 *
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 **/

package me.taylorkelly.mywarp.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.permissions.WarpPermissions;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler {

    private WarpPermissions warpPermissions;
    protected Map<String, Command> commands;

    public CommandHandler(MyWarp plugin) {
        warpPermissions = MyWarp.getWarpPermissions();
        commands = new LinkedHashMap<String, Command>();
    }

    public void addCommand(Command command) {
        commands.put(command.getName().toLowerCase(), command);
    }

    public void removeCommand(Command command) {
        commands.remove(command.getName().toLowerCase());
    }

    public Command getCommand(String name) {
        return commands.get(name.toLowerCase());
    }

    public List<Command> getCommands() {
        return new ArrayList<Command>(commands.values());
    }

    public boolean dispatch(CommandSender sender,
            org.bukkit.command.Command command, String label, String[] args) {

        String[] arguments;
        if (args.length < 1) {
            arguments = new String[] { "help" };
        } else {
            arguments = args;
        }

        for (int argsIncluded = arguments.length; argsIncluded >= 0; argsIncluded--) {
            StringBuilder identifierBuilder = new StringBuilder();
            for (int i = 0; i < argsIncluded; i++) {
                identifierBuilder.append(' ').append(arguments[i]);
            }

            String identifier = identifierBuilder.toString().trim();
            if (identifier.isEmpty()) {
                identifier = "warp";
            }

            for (Command cmd : commands.values()) {
                if (cmd.isIdentifier(sender, identifier)) {
                    String[] realArgs = Arrays.copyOfRange(arguments,
                            argsIncluded, arguments.length);

                    if (!cmd.isInProgress(sender)) {
                        if (realArgs.length < cmd.getMinArguments()
                                || realArgs.length > cmd.getMaxArguments()) {
                            displayCommandHelp(cmd, sender);
                            return true;
                        } else if (realArgs.length > 0
                                && "?".equals(realArgs[0])) {
                            displayCommandHelp(cmd, sender);
                            return true;
                        }
                    }

                    if (cmd.isPlayerOnly() && !(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED
                                + LanguageManager.getString("error.playerOnly"));
                        return true;
                    }

                    if (!hasPermission(sender, cmd.getPermission())) {
                        sender.sendMessage(ChatColor.RED
                                + LanguageManager
                                        .getString("error.noPermission"));
                        return true;
                    }

                    try {
                        cmd.execute(sender, identifier, realArgs);
                    } catch (CommandException e) {
                        sender.sendMessage(ChatColor.RED + e.getMessage());
                    }
                    return true;
                }
            }
        }
        return true;
    }

    private void displayCommandHelp(Command cmd, CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD
                + LanguageManager.getColorlessString("help.command") + ": "
                + ChatColor.WHITE + cmd.getName());
        sender.sendMessage(ChatColor.GOLD
                + LanguageManager.getColorlessString("help.description") + ": "
                + ChatColor.WHITE + cmd.getDescription());
        sender.sendMessage(ChatColor.GOLD
                + LanguageManager.getColorlessString("help.usage") + ": "
                + ChatColor.WHITE + "/warp "
                + StringUtils.join(cmd.getIdentifiers(), '|') + " " + ChatColor.GRAY
                + cmd.getUsage());
        if (cmd.getNotes() != null) {
            for (String note : cmd.getNotes()) {
                sender.sendMessage(ChatColor.ITALIC + note);
            }
        }
    }

    public boolean hasPermission(CommandSender sender, String permString) {
        if (permString == null || permString.isEmpty()) {
            return true;
        }

        if (warpPermissions != null) {
            return warpPermissions.hasPermission(sender, permString);
        }
        return sender.hasPermission(permString);
    }
}
