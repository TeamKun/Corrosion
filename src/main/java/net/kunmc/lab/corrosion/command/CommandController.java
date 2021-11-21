package net.kunmc.lab.corrosion.command;

import com.sun.tools.internal.jxc.ap.Const;
import net.kunmc.lab.corrosion.config.ConfigManager;
import net.kunmc.lab.corrosion.game.GameManager;
import net.kunmc.lab.corrosion.util.DecolationConst;
import org.bukkit.command.*;

import java.util.*;
import java.util.stream.Collectors;

public class CommandController implements CommandExecutor, TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String input = args[args.length-1];
            String[] target = {CommandConst.START, CommandConst.STOP, CommandConst.CONFIG_SET,
                   CommandConst.CONFIG_RELOAD, CommandConst.SHOW};
            completions.addAll(Arrays.asList(target).stream()
                    .filter(e -> e.startsWith(input)).collect(Collectors.toList()));
        } else if (args.length == 2 && args[0].equals(CommandConst.CONFIG_SET)) {
            String input = args[args.length-1];
            String[] target = {CommandConst.CONFIG_CORROSION_DEATH, CommandConst.CONFIG_UPDATE_BLOCK_TIME};
            completions.addAll(Arrays.asList(target).stream()
                    .filter(e -> e.startsWith(input)).collect(Collectors.toList()));
        } else if (args.length == 3 && args[0].equals(CommandConst.CONFIG_SET) &&
                args[1].equals(CommandConst.CONFIG_UPDATE_BLOCK_TIME)) {
            completions.add("<Number>");
        } else if (args.length == 3 && args[0].equals(CommandConst.CONFIG_SET) &&
                args[1].equals(CommandConst.CONFIG_CORROSION_DEATH)) {
            String input = args[args.length - 1];
            String[] target = {"true", "false"};
            completions.addAll(Arrays.asList(target).stream()
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
                    sender.sendMessage(DecolationConst.RED + "すでに開始しています");
                    return true;
                }
                if (!checkArgsNum(sender, args.length, 1)) return true;
                GameManager.controller(GameManager.GameMode.MODE_START);
                sender.sendMessage(DecolationConst.GREEN + "開始します");
                break;
            case CommandConst.STOP:
                if (!GameManager.isRunning()) {
                    sender.sendMessage(DecolationConst.RED + "開始されていません");
                    return true;
                }
                if (!checkArgsNum(sender, args.length, 1)) return true;
                GameManager.controller(GameManager.GameMode.MODE_NEUTRAL);
                sender.sendMessage(DecolationConst.GREEN + "終了します");
                break;
            case CommandConst.CONFIG_RELOAD:
                if (!checkArgsNum(sender, args.length, 1)) return true;
                ConfigManager.loadConfig(true);
                sender.sendMessage(DecolationConst.GREEN + "設定をリロードしました");
                break;
            case CommandConst.CONFIG_SET:
                switch (args[1]) {
                    case CommandConst.CONFIG_UPDATE_BLOCK_TIME:
                        setIntConfig(sender, args, 3, CommandConst.CONFIG_UPDATE_BLOCK_TIME);
                        break;
                    case CommandConst.CONFIG_CORROSION_DEATH:
                        setBooleanConfig(sender, args, 3, CommandConst.CONFIG_CORROSION_DEATH);
                        break;
                    default:
                        sender.sendMessage(DecolationConst.RED + "存在しない設定項目です");
                        sendUsage(sender);
                }
                break;
            case CommandConst.SHOW:
                if (!checkArgsNum(sender, args.length, 1)) return true;
                sender.sendMessage(DecolationConst.GREEN + "設定値一覧");
                List<String> switchList = new ArrayList<>();
                for (Map.Entry<String, Boolean> target : ConfigManager.booleanConfig.entrySet()) {
                    if ( target.getValue()) switchList.add( target.getKey());
                }
                String prefix = "  ";
                for (Map.Entry<String, Integer> param : ConfigManager.integerConfig.entrySet()) {
                    sender.sendMessage(String.format("%s%s: %s", prefix, param.getKey(), param.getValue()));
                }
                sender.sendMessage(String.format("%sswitch: ", prefix) + switchList);
                break;
            default:
                sender.sendMessage(DecolationConst.RED + "存在しないコマンドです");
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
        sender.sendMessage(String.format("%s開始", descPrefix));
        sender.sendMessage(String.format("%s%s"
                , usagePrefix, CommandConst.STOP));
        sender.sendMessage(String.format("%s終了", descPrefix));
        sender.sendMessage(String.format("%s%s"
                , usagePrefix, CommandConst.CONFIG_RELOAD));
        sender.sendMessage(String.format("%sコンフィグリロード", descPrefix));
        sender.sendMessage(String.format("%s%s %s <number>"
                , usagePrefix, CommandConst.CONFIG_SET, CommandConst.CONFIG_UPDATE_BLOCK_TIME));
        sender.sendMessage(String.format("%s腐食更新の間隔(秒)", descPrefix));
        sender.sendMessage(String.format("%s%s %s <on|off>"
                , usagePrefix, CommandConst.CONFIG_CORROSION_DEATH));
        sender.sendMessage(String.format("%s腐食ブロックに触れた場合に死ぬかどうかを変更", descPrefix));
        sender.sendMessage(String.format("%s%s"
                , usagePrefix, CommandConst.SHOW));
        sender.sendMessage(String.format("%s設定などゲームの状態を確認", descPrefix));

    }

    private int validateNum(CommandSender sender, String target) {
        // 不正な場合は-1を返す
        int num;
        try {
            num = Integer.parseInt(target);
        } catch (NumberFormatException e) {
            sender.sendMessage(DecolationConst.RED + "整数以外が入力されています");
            return -1;
        }
        if (num < 0) {
            sender.sendMessage(DecolationConst.RED + "0以上の整数を入力してください");
            return -1;
        }
        return num;
    }

    private boolean checkArgsNum(CommandSender sender, int argsLength, int validLength) {
        if (argsLength != validLength) {
            if (validLength == 1) {
                sender.sendMessage(DecolationConst.RED + "引数が不要なコマンドです");
            } else {
                sender.sendMessage(DecolationConst.RED + "引数の数が不正です");
            }
            return false;
        }
        return true;
    }

    private boolean setIntConfig(CommandSender sender, String[] args, int validLength, String configName){
        if (!checkArgsNum(sender, args.length, validLength)) return false;
        int ret = validateNum(sender, args[2]);
        if (ret == -1) return false;

        ConfigManager.setConfig(configName);
        sender.sendMessage(DecolationConst.GREEN + configName + "の値を" + ConfigManager.integerConfig.get(configName) + "に変更しました");
        return true;
    }

    private boolean setBooleanConfig(CommandSender sender, String[] args, int validLength, String configName){
        if (!checkArgsNum(sender, args.length, validLength)) return false;
        if (validateNum(sender, args[2]) == -1) return false;
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
        sender.sendMessage(DecolationConst.GREEN + configName + "の値を" + out + "に変更しました");
        return true;
    }

    private int validateSwitch(CommandSender sender, String key, String value){
        // on, offの設定確認
        if (!value.equals("on") && value.equals("off")) {
            sender.sendMessage(DecolationConst.RED + "on/offのみ有効です");
            return -1;
        }
        if (ConfigManager.booleanConfig.get(key) && value.equals("on")) {
            sender.sendMessage(DecolationConst.RED + "すでにonです");
            return -1;
        }
        if (!ConfigManager.booleanConfig.get(key) && value.equals("off")) {
            sender.sendMessage(DecolationConst.RED + "すでにoffです");
            return -1;
        }

        return 0;
    }
}
