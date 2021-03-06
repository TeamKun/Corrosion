package net.kunmc.lab.corrosion.command;

import net.kunmc.lab.corrosion.config.ConfigManager;
import net.kunmc.lab.corrosion.game.CorrosionBlockManager;
import net.kunmc.lab.corrosion.game.CorrosionManager;
import net.kunmc.lab.corrosion.game.GameManager;
import net.kunmc.lab.corrosion.util.DecolationConst;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.kunmc.lab.corrosion.command.CommandConst.CONFIG_START_RANGE;

public class CommandController implements CommandExecutor, TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String input = args[args.length - 1];
            String[] target = {CommandConst.START, CommandConst.STOP, CommandConst.PAUSE, CommandConst.CONFIG_SET,
                    CommandConst.CONFIG_RELOAD, CommandConst.SHOW};
            completions.addAll(Arrays.asList(target).stream()
                    .filter(e -> e.startsWith(input)).collect(Collectors.toList()));
        } else if (args.length == 2 && args[0].equals(CommandConst.CONFIG_SET)) {
            String input = args[args.length - 1];
            String[] target = {CommandConst.CONFIG_CORROSION_DEATH, CommandConst.CONFIG_START_RANGE,
                    CommandConst.CONFIG_UPDATE_BLOCK_TICK, CommandConst.CONFIG_UPDATE_BLOCK_MAX_NUM,
                    CommandConst.CONFIG_UPDATE_BLOCK_PRUNING_RATIO, CommandConst.CONFIG_PLAYER};
            completions.addAll(Arrays.asList(target).stream()
                    .filter(e -> e.startsWith(input)).collect(Collectors.toList()));
        } else if (args.length == 3 && args[0].equals(CommandConst.CONFIG_SET) &&
                (args[1].equals(CommandConst.CONFIG_UPDATE_BLOCK_TICK) ||
                        args[1].equals(CommandConst.CONFIG_UPDATE_BLOCK_MAX_NUM) ||
                        args[1].equals(CommandConst.CONFIG_UPDATE_BLOCK_PRUNING_RATIO) ||
                        args[1].equals(CommandConst.CONFIG_START_RANGE))) {
            completions.add("<Number>");
        } else if (args.length == 3 && args[0].equals(CommandConst.CONFIG_SET) && args[1].equals(CommandConst.CONFIG_PLAYER)) {
            List<String> name = Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
            completions.addAll(name.stream().filter(e -> e.startsWith(args[2])).collect(Collectors.toList()));
        } else if (args.length == 3 && args[0].equals(CommandConst.CONFIG_SET) &&
                (args[1].equals(CommandConst.CONFIG_CORROSION_DEATH))) {
            String input = args[args.length - 1];
            List<String> target = new ArrayList<>();
            if (ConfigManager.booleanConfig.get(args[1])) {
                target.add("off");
            } else {
                target.add("on");
            }
            completions.addAll(target.stream()
                    .filter(e -> e.startsWith(input)).collect(Collectors.toList()));
        }
        return completions;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String commandName = args[0];
        switch (commandName) {
            case CommandConst.START:
                if (GameManager.isRunning()) {
                    sender.sendMessage(DecolationConst.RED + "??????????????????????????????");
                    return true;
                }
                if (!checkArgsNum(sender, args.length, 1)) return true;
                Player p = null;
                if (sender instanceof Player) {
                    p = (Player) sender;
                }
                GameManager.controller(GameManager.GameMode.MODE_START, p);
                sender.sendMessage(DecolationConst.GREEN + "???????????????");
                break;
            case CommandConst.STOP:
                if (!GameManager.isRunning() && !GameManager.isPause()) {
                    sender.sendMessage(DecolationConst.RED + "???????????????????????????");
                    return true;
                }
                if (!checkArgsNum(sender, args.length, 1)) return true;
                GameManager.controller(GameManager.GameMode.MODE_NEUTRAL, null);
                sender.sendMessage(DecolationConst.GREEN + "???????????????");
                break;
            case CommandConst.PAUSE:
                if (!GameManager.isRunning()) {
                    sender.sendMessage(DecolationConst.RED + "???????????????????????????");
                    return true;
                }
                if (!checkArgsNum(sender, args.length, 1)) return true;
                GameManager.controller(GameManager.GameMode.MODE_PAUSE, null);
                sender.sendMessage(DecolationConst.GREEN + "?????????????????????????????????????????????????????????????????????/cor stop???????????????????????????");
                break;
            case CommandConst.CONFIG_RELOAD:
                if (!checkArgsNum(sender, args.length, 1)) return true;
                ConfigManager.loadConfig(true);
                CorrosionManager.changeUpdateBlockTick();
                sender.sendMessage(DecolationConst.GREEN + "?????????????????????????????????");
                break;
            case CommandConst.CONFIG_SET:
                switch (args[1]) {
                    case CommandConst.CONFIG_UPDATE_BLOCK_TICK:
                        setIntegerConfig(sender, args, 3, CommandConst.CONFIG_UPDATE_BLOCK_TICK);
                        CorrosionManager.changeUpdateBlockTick();
                        break;
                    case CommandConst.CONFIG_UPDATE_BLOCK_MAX_NUM:
                        setIntegerConfig(sender, args, 3, CommandConst.CONFIG_UPDATE_BLOCK_MAX_NUM);
                        break;
                    case CONFIG_START_RANGE:
                        setIntegerConfig(sender, args, 3, CONFIG_START_RANGE);
                        break;
                    case CommandConst.CONFIG_UPDATE_BLOCK_PRUNING_RATIO:
                        setDoubleConfig(sender, args, 3, CommandConst.CONFIG_UPDATE_BLOCK_PRUNING_RATIO);
                        break;
                    case CommandConst.CONFIG_CORROSION_DEATH:
                        setBooleanConfig(sender, args, 3, CommandConst.CONFIG_CORROSION_DEATH);
                        break;
                    case CommandConst.CONFIG_PLAYER:
                        checkArgsNum(sender, args.length, 3);
                        String name = args[2];
                        if (Bukkit.selectEntities(sender, name).isEmpty()) {
                            sender.sendMessage(DecolationConst.RED + "????????????????????????????????????");
                            return true;
                        }
                        if (ConfigManager.stringConfig.get(CommandConst.CONFIG_PLAYER).equals(name)) {
                            sender.sendMessage(DecolationConst.AQUA + "??????????????????????????????????????????????????????");
                            return true;
                        }
                        ConfigManager.stringConfig.put(CommandConst.CONFIG_PLAYER, name);
                        ConfigManager.setConfig(CommandConst.CONFIG_PLAYER);
                        String configName = CommandConst.CONFIG_PLAYER;
                        sender.sendMessage(DecolationConst.GREEN + configName + "?????????" + ConfigManager.stringConfig.get(configName) + "?????????????????????");
                        break;
                    default:
                        sender.sendMessage(DecolationConst.RED + "?????????????????????????????????");
                        sendUsage(sender);
                }
                break;
            case CommandConst.SHOW:
                if (!checkArgsNum(sender, args.length, 1)) return true;
                sender.sendMessage(DecolationConst.GREEN + "???????????????");
                List<String> switchList = new ArrayList<>();
                for (Map.Entry<String, Boolean> target : ConfigManager.booleanConfig.entrySet()) {
                    if (target.getValue()) switchList.add(target.getKey());
                }
                String prefix = "  ";
                for (Map.Entry<String, Integer> param : ConfigManager.integerConfig.entrySet()) {
                    sender.sendMessage(String.format("%s%s: %s", prefix, param.getKey(), param.getValue()));
                }
                for (Map.Entry<String, Double> param : ConfigManager.doubleConfig.entrySet()) {
                    sender.sendMessage(String.format("%s%s: %.1f", prefix, param.getKey(), param.getValue()));
                }
                for (Map.Entry<String, String> param : ConfigManager.stringConfig.entrySet()) {
                    sender.sendMessage(String.format("%s%s: %s", prefix, param.getKey(), param.getValue()));
                }
                sender.sendMessage(String.format("%sswitch: ", prefix) + switchList);
                sender.sendMessage(String.format("%s???????????????????????????: %s", prefix, CorrosionBlockManager.currentSearchCorrosionBlockList.size()));
                break;
            default:
                sender.sendMessage(DecolationConst.RED + "?????????????????????????????????");
                sendUsage(sender);
        }
        return true;
    }

    private void sendUsage(CommandSender sender) {
        String usagePrefix = String.format("  /%s ", CommandConst.MAIN);
        String descPrefix = "  ";
        sender.sendMessage(DecolationConst.GREEN + "Usage:");
        sender.sendMessage(String.format("%s%s"
                , usagePrefix, CommandConst.START));
        sender.sendMessage(String.format("%s??????", descPrefix));
        sender.sendMessage(String.format("%s%s"
                , usagePrefix, CommandConst.STOP));
        sender.sendMessage(String.format("%s??????", descPrefix));
        sender.sendMessage(String.format("%s%s"
                , usagePrefix, CommandConst.CONFIG_RELOAD));
        sender.sendMessage(String.format("%s???????????????????????????", descPrefix));
        sender.sendMessage(String.format("%s%s %s <number>"
                , usagePrefix, CommandConst.CONFIG_SET, CommandConst.CONFIG_UPDATE_BLOCK_TICK));
        sender.sendMessage(String.format("%s?????????????????????(Tick)", descPrefix));
        sender.sendMessage(String.format("%s%s %s <number>"
                , usagePrefix, CommandConst.CONFIG_SET, CommandConst.CONFIG_UPDATE_BLOCK_MAX_NUM));
        sender.sendMessage(String.format("%s????????????????????????????????????????????????????????????????????????????????????", descPrefix));
        sender.sendMessage(String.format("%s%s %s <number>"
                , usagePrefix, CommandConst.CONFIG_SET, CommandConst.CONFIG_UPDATE_BLOCK_PRUNING_RATIO));
        sender.sendMessage(String.format("%s???????????????????????????????????????????????????", descPrefix));
        sender.sendMessage(String.format("%s%s %s <number>"
                , usagePrefix, CommandConst.CONFIG_SET, CONFIG_START_RANGE));
        sender.sendMessage(String.format("%sstart???????????????????????????????????????", descPrefix));
        sender.sendMessage(String.format("%s%s %s <on|off>"
                , usagePrefix, CommandConst.CONFIG_SET, CommandConst.CONFIG_CORROSION_DEATH));
        sender.sendMessage(String.format("%s??????????????????????????????????????????????????????????????????", descPrefix));
        sender.sendMessage(String.format("%s%s"
                , usagePrefix, CommandConst.SHOW));
        sender.sendMessage(String.format("%s???????????????????????????????????????", descPrefix));

    }

    private int validateInteger(CommandSender sender, String target) {
        // ??????????????????-1?????????
        int num;
        try {
            num = Integer.parseInt(target);
        } catch (NumberFormatException e) {
            sender.sendMessage(DecolationConst.RED + "?????????????????????????????????");
            return -1;
        }
        if (num < 0) {
            sender.sendMessage(DecolationConst.RED + "0??????????????????????????????????????????");
            return -1;
        }
        return num;
    }

    private double validateDouble(CommandSender sender, String target) {
        // ??????????????????-1?????????
        double num;
        try {
            num = Double.parseDouble(target);
        } catch (NumberFormatException e) {
            sender.sendMessage(DecolationConst.RED + "???????????????????????????????????????");
            return -1;
        }
        if (num < 0 || num > 1) {
            sender.sendMessage(DecolationConst.RED + "0??????1??????????????????????????????????????????");
            return -1;
        }
        return num;
    }


    private boolean checkArgsNum(CommandSender sender, int argsLength, int validLength) {
        if (argsLength != validLength) {
            if (validLength == 1) {
                sender.sendMessage(DecolationConst.RED + "????????????????????????????????????");
                sendUsage(sender);
            } else {
                sender.sendMessage(DecolationConst.RED + "???????????????????????????");
                sendUsage(sender);
            }
            return false;
        }
        return true;
    }

    private boolean setDoubleConfig(CommandSender sender, String[] args, int validLength, String configName) {
        if (!checkArgsNum(sender, args.length, validLength)) return false;
        double ret = validateDouble(sender, args[2]);
        if ((int) ret == -1) return false;

        ConfigManager.doubleConfig.put(configName, ret);
        ConfigManager.setConfig(configName);
        sender.sendMessage(DecolationConst.GREEN + configName + "?????????" + ConfigManager.doubleConfig.get(configName) + "?????????????????????");
        return true;
    }

    private boolean setIntegerConfig(CommandSender sender, String[] args, int validLength, String configName) {
        if (!checkArgsNum(sender, args.length, validLength)) return false;
        int ret = validateInteger(sender, args[2]);
        if (ret == -1) return false;

        ConfigManager.integerConfig.put(configName, ret);
        ConfigManager.setConfig(configName);
        sender.sendMessage(DecolationConst.GREEN + configName + "?????????" + ConfigManager.integerConfig.get(configName) + "?????????????????????");
        return true;
    }

    private boolean setBooleanConfig(CommandSender sender, String[] args, int validLength, String configName) {
        if (!checkArgsNum(sender, args.length, validLength)) return false;
        if (validateSwitch(sender, configName, args[2]) == -1) return false;

        boolean setSwitch = false;
        if (args[2].equals("on")) {
            setSwitch = true;
        }

        ConfigManager.booleanConfig.put(configName, setSwitch);
        ConfigManager.setConfig(configName);
        String out = "off";
        if (ConfigManager.booleanConfig.get(configName)) {
            out = "on";
        }
        sender.sendMessage(DecolationConst.GREEN + configName + "?????????" + out + "?????????????????????");
        return true;
    }

    private int validateSwitch(CommandSender sender, String key, String value) {
        // on, off???????????????
        if (!value.equals("on") && !value.equals("off")) {
            sender.sendMessage(DecolationConst.RED + "on/off????????????????????????");
            return -1;
        }
        if (ConfigManager.booleanConfig.get(key) && value.equals("on")) {
            sender.sendMessage(DecolationConst.RED + "?????????on??????");
            return -1;
        }
        if (!ConfigManager.booleanConfig.get(key) && value.equals("off")) {
            sender.sendMessage(DecolationConst.RED + "?????????off??????");
            return -1;
        }

        return 0;
    }
}
