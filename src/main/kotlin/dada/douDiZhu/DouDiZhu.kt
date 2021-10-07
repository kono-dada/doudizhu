package dada.douDiZhu

import dada.douDiZhu.command.Command
import dada.douDiZhu.command.DouDiZhuConsoleCommand
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.globalEventChannel
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
        Config.reload()

        Command.register()
        DouDiZhuConsoleCommand.register()

        Config.groups.forEach {
            AbstractPermitteeId.AnyMember(it).permit(Command.permission)
        }

        globalEventChannel().subscribeGroupMessages {
            case("创建游戏"){
                //只有允许的群聊可以玩斗地主
                if (group.id in Config.groups) {
                    launch { Game(group).gameStart() }
                    subject.sendMessage("创建成功（底分：200）！发送“上桌”即可参与游戏")
                }
            }
        }
    }
}