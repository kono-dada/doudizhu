package dada.douDiZhu.command

import dada.douDiZhu.DouDiZhu
import dada.douDiZhu.gameTimes
import dada.douDiZhu.winRate
import dada.douDiZhu.winTimes
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.UserCommandSender

object Command:CompositeCommand(
    DouDiZhu,
    "d",
) {
    //查询玩家的胜率
    @SubCommand("me")
    suspend fun UserCommandSender.me(){
        subject.sendMessage("<${user.nick}>总共进行了${user.gameTimes}场游戏，获胜${user.winTimes}场，胜率${user.winRate}")
    }
}