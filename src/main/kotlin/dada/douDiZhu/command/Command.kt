package dada.douDiZhu.command

import dada.douDiZhu.*
import net.mamoe.mirai.console.command.*
import kotlin.math.log

object Command : CompositeCommand(
    DouDiZhu,
    "d",
) {
    //查询玩家的胜率
    @SubCommand("me")
    suspend fun UserCommandSender.me() {
        subject.sendMessage("<${user.nick}>总共进行了${user.gameTimes}场游戏，获胜${user.winTimes}场，胜率${user.winRate}")
    }
}

object DouDiZhuConsoleCommand : CompositeCommand(
    DouDiZhu,
    "doudizhucommand", "dc"
) {
    @SubCommand("addadmin")
    suspend fun ConsoleCommandSender.addadmin(id: Long) {
        Config.admin.add(id)
        sendMessage("OK")
    }

    @SubCommand("addgroup")
    suspend fun ConsoleCommandSender.addgroup(id: Long) {
        Config.groups.add(id)
        sendMessage("OK")
    }
}