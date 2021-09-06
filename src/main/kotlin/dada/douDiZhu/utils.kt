package dada.douDiZhu

import net.mamoe.mirai.contact.User

val User.douDiZhuData
    get() = PlayerData.data.getOrPut(this.id) { CustomData(0, 0) }

val User.winTimes
    get() = douDiZhuData.winTimes

val User.gameTimes
    get() = douDiZhuData.gameTimes

val User.winRate
    get() = winTimes.toFloat() / gameTimes.toFloat()