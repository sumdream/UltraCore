package com.github.skystardust.ultracore.bungeecord;

import com.github.skystardust.ultracore.bungeecord.commands.MainCommandSpec;
import com.github.skystardust.ultracore.bungeecord.commands.SubCommandSpec;
import com.github.skystardust.ultracore.core.configuration.ConfigurationManager;
import com.github.skystardust.ultracore.core.database.DatabaseListenerRegistry;
import com.github.skystardust.ultracore.core.database.DatabaseRegistry;
import com.github.skystardust.ultracore.core.exceptions.ConfigurationException;
import com.github.skystardust.ultracore.core.exceptions.DatabaseInitException;
import com.github.skystardust.ultracore.core.modules.JarModuleManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

public final class UltraCore extends Plugin {
    private static UltraCore ultraCore;

    public static UltraCore getUltraCore() {
        return ultraCore;
    }

    @Override
    public void onEnable() {
        UltraCore.ultraCore = this;
        MainCommandSpec.newBuilder()
                .addAlias("uc")
                .addAlias("ultracore")
                .withDescription("UltraCore commands")
                .withPermission("ultracore.admin")
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("database")
                        .addAlias("db")
                        .childCommandSpec(SubCommandSpec.newBuilder()
                                .addAlias("l")
                                .addAlias("list")
                                .withCommandSpecExecutor((commandSender, args) -> {
                                    Set<String> keySet = DatabaseRegistry.getPluginDatabaseManagerBaseMap()
                                            .keySet();
                                    DatabaseListenerRegistry.getPluginDatabaseListenerMap()
                                            .keySet()
                                            .stream()
                                            .filter(keySet::contains)
                                            .collect(Collectors.toList())
                                            .forEach(database -> {
                                                commandSender.sendMessage("* " + database);
                                            });
                                    return true;
                                })
                                .build())
                        .childCommandSpec(SubCommandSpec.newBuilder()
                                .addAlias("reload")
                                .addAlias("r")
                                .withCommandSpecExecutor((commandSender, args) -> {
                                    if (args.length < 1) {
                                        return true;
                                    }
                                    try {
                                        DatabaseRegistry.reloadPluginDatabase(args[0]);
                                        sendMessage(commandSender, "重载数据库 " + args[0] + " 成功!");
                                    } catch (DatabaseInitException | ConfigurationException e) {
                                        sendMessage(commandSender, "错误! " + e.getMessage());
                                    }
                                    return true;
                                })
                                .build())
                        .withCommandSpecExecutor((commandSender, args) -> {
                            commandSender.sendMessage("/uc db reload [重载数据库名字] - 重载数据库");
                            commandSender.sendMessage("/uc db list - 列出所有数据库");
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("cfg")
                        .addAlias("config")
                        .childCommandSpec(SubCommandSpec.newBuilder()
                                .addAlias("l")
                                .addAlias("list")
                                .withCommandSpecExecutor((commandSender, args) -> {
                                    ConfigurationManager.PLUGIN_INSTANCE_CONFIGURATION_MANAGER_MAP
                                            .forEach((plugin, cfgM) -> {
                                                sendMessage(commandSender, "--------" + plugin.getName() + "--------");
                                                cfgM.getData().forEach((key, value) -> {
                                                    sendMessage(commandSender, "* " + key);
                                                });
                                                sendMessage(commandSender, "-------------------");
                                            });
                                    return true;
                                })
                                .build())
                        .childCommandSpec(SubCommandSpec.newBuilder()
                                .addAlias("r")
                                .addAlias("reload")
                                .withCommandSpecExecutor((commandSender, args) -> {
                                    if (args.length < 1) {
                                        return true;
                                    }
                                    ConfigurationManager.PLUGIN_INSTANCE_CONFIGURATION_MANAGER_MAP.forEach((plugin, cfgM) -> {
                                        if (plugin.getName().equalsIgnoreCase(args[0])) {
                                            try {
                                                cfgM.reloadFiles();
                                                sendMessage(commandSender, "重载完成!");
                                            } catch (ConfigurationException e) {
                                                sendMessage(commandSender, "重载失败! " + e.getLocalizedMessage());
                                            }

                                        }
                                    });
                                    return true;
                                })
                                .build())
                        .withCommandSpecExecutor((commandSender, args) -> {
                            sendMessage(commandSender, "/uc cfg reload [插件名字] - 重载配置文件");
                            sendMessage(commandSender, "/uc cfg list - 列出所有配置文件");
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("mod")
                        .addAlias("module")
                        .childCommandSpec(SubCommandSpec.newBuilder()
                                .addAlias("l")
                                .addAlias("list")
                                .withCommandSpecExecutor((commandSender, args) -> {
                                    JarModuleManager.getModulesMap()
                                            .forEach((name, instance) -> {
                                                sendMessage(commandSender, "--------" + name + "--------");
                                            });
                                    return true;
                                })
                                .build())
                        .childCommandSpec(SubCommandSpec.newBuilder()
                                .addAlias("r")
                                .addAlias("reload")
                                .withCommandSpecExecutor((commandSender, args) -> {
                                    if (args.length < 1) {
                                        return true;
                                    }
                                    commandSender.sendMessage("重载Module: " + JarModuleManager.reloadModule(args[0]));
                                    return true;
                                })
                                .build())
                        .childCommandSpec(SubCommandSpec.newBuilder()
                                .addAlias("l")
                                .addAlias("load")
                                .withCommandSpecExecutor((commandSender, args) -> {
                                    if (args.length < 1) {
                                        return true;
                                    }
                                    if (JarModuleManager.loadModule(new File("./uc-modules", args[0] + ".jar"))) {
                                        commandSender.sendMessage("加载 " + args[0] + " 已经成功!");
                                        return true;
                                    }
                                    commandSender.sendMessage("无法加载 " + args[0]);
                                    return true;
                                })
                                .build())
                        .childCommandSpec(SubCommandSpec.newBuilder()
                                .addAlias("ul")
                                .addAlias("unload")
                                .withCommandSpecExecutor((commandSender, args) -> {
                                    if (args.length < 1) {
                                        return true;
                                    }
                                    if (JarModuleManager.unloadModule(args[0]) != null) {
                                        commandSender.sendMessage("卸载 " + args[0] + " 已经成功!");
                                        return true;
                                    }
                                    commandSender.sendMessage("无法卸载 " + args[0]);
                                    return true;
                                })
                                .build())
                        .withCommandSpecExecutor((commandSender, args) -> {
                            sendMessage(commandSender, "/uc mod reload [名称] - 重载Module");
                            sendMessage(commandSender, "/uc mod load [文件名] - 加载文件");
                            sendMessage(commandSender, "/uc mod unload [名称] - 卸载Module");
                            sendMessage(commandSender, "/uc mod list - 列出所有已经加载的Module");
                            return true;
                        })
                        .build())
                .withCommandSpecExecutor((commandSender, args) -> {
                    commandSender.sendMessage("/uc database - 数据库相关");
                    commandSender.sendMessage("/uc config - 配置文件相关");
                    commandSender.sendMessage("/uc mod - Module相关");
                    return true;
                })
                .build()
                .register();
        JarModuleManager.loadModules();
    }

    public void sendMessage(CommandSender commandSender, String string) {
        commandSender.sendMessage(new TextComponent(string));
    }
}
