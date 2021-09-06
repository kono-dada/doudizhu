package dada.douDiZhu

import dada.douDiZhu.data.HandCards
import net.mamoe.mirai.contact.Member

/**
 * ### 牌桌
 * 记录玩家数据(手牌等)，构造迭代器以进行周而复始的回合制游戏
 */
class Table : Iterable<Member> {
    val players = mutableListOf<Member>()

    var index = 0

    lateinit var diZhu: Member
    lateinit var nongMin: List<Member>

    /*
       构建玩家到手牌的映射
     */
    lateinit var handCard: MutableMap<Member, HandCards>

    fun enter(player: Member): Boolean {
        return if (!isFull() && player !in players) {
            players.add(player)
            true
        } else {
            false
        }
    }

    fun isFull(): Boolean {
        return players.size == 3
    }

    override fun iterator(): Iterator<Member> {
        return TableIterator(players, index)
    }

    class TableIterator(private val players: List<Member>, private var index: Int = 0) : Iterator<Member> {
        override fun next(): Member {
            val p = players[index]
            if (index < 2) {
                index++
            } else index = 0
            return p
        }

        override fun hasNext(): Boolean {
            return true
        }
    }
}