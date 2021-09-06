package dada.douDiZhu

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object PlayerData : AutoSavePluginData("playerData") {
    var data: MutableMap<Long, CustomData> by value()
}

@Serializable
data class CustomData(
    var winTimes: Int,
    var gameTimes: Int
)