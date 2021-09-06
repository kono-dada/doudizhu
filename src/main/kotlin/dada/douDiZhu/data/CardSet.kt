package dada.douDiZhu.data


/**
 * ### 牌的集合
 * 牌的集合，继承[ArrayList]
 * 可以传入多张牌，也可以传入一个包含了牌的列表
 */
open class CardSet : ArrayList<Card> {
    constructor(vararg elements: Card) : super(elements.toMutableList())
    constructor(list:List<Card>) : super(list)

    //手牌的包含关系
    //用来判断玩家是否拥有他想要打出的手牌
    infix fun have(another: CardSet): Boolean {
        val copy = ArrayList<Card>(this)
        another.forEach {
            if (!copy.remove(it)) return false
        }
        return true
    }

    fun sortByValue() = sortBy { it.value }

    /**
     * 排序后变成字符串
     */
    fun toSortedString(): String {
        sortByValue()
        var str = ""
        forEach {
            str += "[${it.id}]"
        }
        return str
    }

    /**
     * 不排序变成字符串
     */
    override fun toString(): String {
        var str = ""
        forEach {
            str += "[${it.id}]"
        }
        return str
    }

    override fun equals(other: Any?): Boolean {
        return if (other is CardSet) {
            this have other && other have this
        } else false
    }

    //没什么卵用，但是不写会有警告，看着不爽
    override fun hashCode(): Int {
        return 0
    }

}

/**
 * ### 玩家的手牌
 */
class HandCards(elements: MutableList<Card>) : CardSet(elements) {

    //出牌
    fun play(cardSet: CardSet) {
        cardSet.forEach {
            this.remove(it)
        }
    }
}


fun List<Card>.toCardSet(): CardSet {
    return CardSet(*this.toTypedArray())
}

/**
 * 懒得写四带两张的解析，干脆把全部四带两张的枚举了吧
 */
object AllOfQuadrupleWithTwoSingles : ArrayList<CardSet>() {
    init {
        for (i in (1..13)) {
            val qua = Card.deserializeFromInt(i)
            for (j in (1..15)) {
                for (k in (1..15)) {
                    add(CardSet(qua, qua, qua, qua, Card.deserializeFromInt(j), Card.deserializeFromInt(k)))
                }
            }
        }
    }
}