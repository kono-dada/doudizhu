package dada.douDiZhu

import dada.douDiZhu.command.Command
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeFriendMessages
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.utils.info

object DouDiZhu : KotlinPlugin(
    JvmPluginDescription(
        id = "dada.douDiZhu",
        version = "1.0-SNAPSHOT",
    )
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }

        PlayerData.reload()

        Command.register()

        AbstractPermitteeId.AnyMember(1097639866).permit(Command.permission)

        globalEventChannel().subscribeGroupMessages {
            case("创建游戏") reply {
                launch { Game(group).gameStart() }
                "创建成功！底分：200"
            }
        }
    }
}