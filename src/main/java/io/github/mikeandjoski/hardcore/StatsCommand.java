package io.github.mikeandjoski.hardcore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {

    private final StatsManager statsManager;

    public StatsCommand(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerStats stats = statsManager.getPlayerStats(player);
            if (stats != null) {
                StatGUI.openGUI(player, stats);
            } else {
                player.sendMessage("스탯 정보를 불러올 수 없습니다.");
            }
            return true;
        }
        sender.sendMessage("이 명령어는 플레이어만 사용할 수 있습니다.");
        return false;
    }
}
