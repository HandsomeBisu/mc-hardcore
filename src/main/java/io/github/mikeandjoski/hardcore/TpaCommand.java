package io.github.mikeandjoski.hardcore;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpaCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("이 명령어는 플레이어만 사용할 수 있습니다.");
            return true;
        }

        Player player = (Player) sender;

        // Check if the player has an outgoing request
        if (Hardcore.getTpaManager().hasSentRequest(player)) {
            player.sendMessage(ChatColor.RED + "이미 보낸 텔레포트 요청이 있습니다. 응답을 기다리거나 /tpacancel 로 취소하세요.");
            return true;
        }

        TpaGUI.openGUI(player);
        return true;
    }
}
