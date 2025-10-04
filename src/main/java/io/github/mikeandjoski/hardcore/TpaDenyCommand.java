package io.github.mikeandjoski.hardcore;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpaDenyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("이 명령어는 플레이어만 사용할 수 있습니다.");
            return true;
        }

        Player target = (Player) sender;
        TpaManager tpaManager = Hardcore.getTpaManager();

        if (!tpaManager.hasRequest(target)) {
            target.sendMessage(ChatColor.RED + "당신에게 온 텔레포트 요청이 없습니다.");
            return true;
        }

        Player requester = tpaManager.getRequester(target);

        if (requester == null || !requester.isOnline()) {
            target.sendMessage(ChatColor.RED + "요청자가 오프라인이거나 존재하지 않습니다.");
            tpaManager.removeRequest(target); // Clean up expired request
            return true;
        }

        target.sendMessage(ChatColor.RED + requester.getName() + "님의 텔레포트 요청을 거절했습니다.");
        requester.sendMessage(ChatColor.RED + target.getName() + "님이 당신의 텔레포트 요청을 거절했습니다.");

        tpaManager.removeRequest(target); // Remove the request after denial
        return true;
    }
}
