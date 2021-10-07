package dada.douDiZhu.data

/**
 * ### 牌的组合
 * 在玩家打牌后，[解析器][deserializeToCard]会把玩家希望出的牌解析为一个[组合][Combination]，
 * 其中包含了合法的组合与[不合法的组合][NotACombination]
 * @param comparableCard 由于组合是可比较大小的，每个子类需要递交一个[用于比较大小的牌][comparableCard]。
例如[三带一][TripleWithSingle] 4445中，用于比较大小的牌就是4
 */
open class Combination(private val comparableCard: Card) {
    fun sameType(another: Combination): Boolean = (this::class == another::class || (this is Bomb && another is Bomb))

    open operator fun compareTo(other: Combination): Int = when {
        comparableCard > other.comparableCard -> 1
        comparableCard == other.comparableCard -> 0
        comparableCard < other.comparableCard -> -1
        else -> 0
    }

}

/**
 * ### 在玩家乱出牌时用的组合
 */
object NotACombination : Combination(Card.NOT_A_CARD)

/**
 * ### 单牌
 */
class Single(private val card: Card) : Combination(card) {
    override fun toString(): String = CardSet(card).toString()
}

fun CardSet.isSingle(): Single? = if (size == 1) Single(get(0)) else null

/**
 * ### 对子
 */
class Double(private val card: Card) : Combination(card) {
    override fun toString(): String = CardSet(card, card).toString()
}

fun CardSet.isDouble(): Double? =
    if (size == 2 && this[1] == this[0])
        Double(this[1])
    else null

/**
 * ### 三张
 */
class Triple(private val card: Card) : Combination(card) {
    override fun toString(): String = CardSet(card, card, card).toString()
}

fun CardSet.isTriple(): Triple? =
    if (size == 3 && all { it == this[0] }) Triple(this[0]) else null

/**
 * ### 炸弹
 * 炸弹单独算接口，因为出牌时不用符合上一个组合的规则
 */
abstract class Bomb(comparableCard: Card) : Combination(comparableCard)

/**
 * ### 普通炸弹
 */
class OrdinaryBomb(private val card: Card) : Bomb(card) {
    override fun toString(): String = CardSet(card, card, card, card).toString()
}

fun CardSet.isBomb(): OrdinaryBomb? =
    if (size == 4 && all { it == this[0] }) OrdinaryBomb(this[0]) else null

/**
 * ### 王炸
 */
class JokerBomb : Bomb(Card.NOT_A_CARD) {
    override fun toString(): String = "[鬼][王]"
}

fun CardSet.isJokerBomb(): JokerBomb? =
    if (size == 2 && Card.COLORFUL_JOKER in this && Card.GREY_JOKER in this)
        JokerBomb()
    else null

/**
 * ### 三带一
 */
class TripleWithSingle(private val triple: Card, private val single: Card) : Combination(triple) {
    override fun toString(): String = CardSet(triple, triple, triple, single).toString()
}

/*
    解析思路：
    1.这个组合必须是size==4，它除去重复项后的set只有2个元素
    2.set中随便挑一个元素first，如果它在this中出现了1次，就是single；如果出现3次，就是triple；如果是其他情况，就啥也不是
    3.下同
 */
fun CardSet.isTripleWithSingle(): TripleWithSingle? {
    val set = toSet()
    return if (size == 4 && set.size == 2) {
        val first = set.toList()[0]
        val second = set.toList()[1]
        var numberOfFirst = 0
        for (card in this) {
            if (card == first) numberOfFirst += 1
        }
        when (numberOfFirst) {
            1 -> TripleWithSingle(second, first)
            3 -> TripleWithSingle(first, second)
            else -> null
        }
    } else null
}

/**
 * ### 三带一对
 */
class TripleWithDouble(private val triple: Card, private val double: Card) : Combination(triple) {
    override fun toString(): String = CardSet(triple, triple, triple, double, double).toString()
}

fun CardSet.isTripleWithDouble(): TripleWithDouble? {
    val set = toSet()
    return if (size == 5 && set.size == 2) {
        val first = set.toList()[0]
        val second = set.toList()[1]
        var numberOfFirst = 0
        for (card in this) {
            if (card == first) numberOfFirst += 1
        }
        when (numberOfFirst) {
            2 -> TripleWithDouble(second, first)
            3 -> TripleWithDouble(first, second)
            else -> null
        }
    } else null
}

/**
 * ### 四带一
 */
class QuadrupleWithSingle(private val quadruple: Card, private val single: Card) : Combination(quadruple) {
    override fun toString(): String = CardSet(quadruple, quadruple, quadruple, quadruple, single).toString()
}

fun CardSet.isQuadrupleWithSingle(): QuadrupleWithSingle? {
    val set = toSet()
    return if (size == 5 && set.size == 2) {
        val first = set.toList()[0]
        val second = set.toList()[1]
        var numberOfFirst = 0
        for (card in this) {
            if (card == first) numberOfFirst += 1
        }
        when (numberOfFirst) {
            1 -> QuadrupleWithSingle(second, first)
            4 -> QuadrupleWithSingle(first, second)
            else -> null
        }
    } else null
}

/**
 * ### 四带二
 */
class QuadrupleWithTwoSingles(private val quadruple: Card, private val rest: CardSet) : Combination(quadruple) {
    override fun toString(): String = CardSet(quadruple, quadruple, quadruple, quadruple, rest[0], rest[1]).toString()
}

/*
    旧逻辑：
    1.产生一个去除重复项的set
    2.遍历这个set，如果其中一个card在this中出现了4次，则认为这是一个四带二
    3.用qua记录出现了四次的牌，剩下的塞进rest

    新逻辑：
    1.构建一个包含所有可能的四带二的集合，检查是否存在
 */
fun CardSet.isQuadrupleWithTwoSingles(): QuadrupleWithTwoSingles? {
    for (cardSet in AllOfQuadrupleWithTwoSingles) {
        if (this == cardSet) return QuadrupleWithTwoSingles(cardSet[0], cardSet.subList(4, 6).toCardSet())
    }
    return null
}

/**
 * ### 顺子
 */
class Smooth(private val start: Card, private val end: Card) : Combination(start) {
    override fun toString(): String {
        val cardSet = CardSet()
        for (i in (start.value..end.value)) {
            cardSet += Card.deserializeFromInt(i)
        }
        return cardSet.toString()
    }
}

/*
    解析思路：
    1.先把牌排序，最后一张不能超过“2”
    2.如果每个牌都比前一个牌大1，就是顺子，否则不是顺子
    3.连对也是一样
 */
fun CardSet.isSmooth(): Smooth? {
    sortByValue()
    return if (this[lastIndex].value <= 12 && this.size >= 5) {
        for (i in (0 until size - 1)) {
            if (this[i + 1].value - this[i].value != 1) return null
        }
        return Smooth(this[0], this[lastIndex])
    } else null
}

/**
 * ### 连对
 */
class DoubleSeries(private val start: Card, private val end: Card) : Combination(start) {
    override fun toString(): String {
        val cardSet = CardSet()
        for (i in (start.value..end.value)) {
            cardSet += Card.deserializeFromInt(i)
            cardSet += Card.deserializeFromInt(i)
        }
        return cardSet.toString()
    }
}

/*
    类似上面的算法，这次步长为2
 */
fun CardSet.isDoubleSeries(): DoubleSeries? {
    sortByValue()
    return if (this[lastIndex].value <= 12 && this.size >= 6) {
        for (i in ((0..size - 3) step 2)) {
            if (
                this[i + 2].value - this[i].value != 1 ||
                this[i] != this[i + 1] ||
                this[i + 2].value != this[i + 3].value
            ) return null
        }
        return DoubleSeries(this[0], this[lastIndex])
    } else null
}

/**
 * ### 连三
 */
class TripleSeries(private val triple: CardSet) : Combination(triple[0]) {
    override fun toString(): String {
        val cardSet = CardSet()
        triple.sortByValue()
        for (i in triple) cardSet.addAll(CardSet(i, i, i))
        return cardSet.toString()
    }
}

fun CardSet.isTripleSeries(): TripleSeries? {
    val set = toSet().toList().toCardSet()
    for (i in set) if (count { it == i } != 3) return null
    for (i in (0 until set.size - 1)) if (set[i + 1].value - set[i].value != 1) return null
    return TripleSeries(set)
}

/**
 * ### 飞机
 */
class PlaneWithSingle(private val triple: CardSet, private val single: CardSet) : Combination(triple[0]) {
    override fun toString(): String {
        val cardSet = CardSet()
        for (i in triple)
            cardSet.addAll(CardSet(i, i, i))
        cardSet.sortByValue()
        for (i in single)
            cardSet.add(i)
        return cardSet.toString()
    }
}

fun CardSet.isPlaneWithSingle(): PlaneWithSingle? {
    if (size % 4 != 0) return null
    val single = this.subList(size - size / 4, lastIndex + 1).toCardSet()
    val triple = this.subList(0, size - size / 4).toCardSet()
    //以下三句：判断triple的部分是否合法
    val tripleSet = triple.toSet().toList().toCardSet()
    for (card in tripleSet) if (triple.count { card == it } != 3) return null
    for (i in (0 until tripleSet.size - 1)) if (tripleSet[i + 1].value - tripleSet[i].value != 1) return null

    return PlaneWithSingle(tripleSet, single)
}

/**
 * ### 飞机带一对
 */
class PlaneWithDouble(private val triple: CardSet, private val double: CardSet) : Combination(triple[0]) {
    override fun toString(): String {
        val cardSet = CardSet()
        for (i in triple)
            cardSet.addAll(CardSet(i, i, i))
        cardSet.sortByValue()
        for (i in double)
            cardSet.addAll(CardSet(i, i))
        return cardSet.toString()
    }
}

fun CardSet.isPlaneWithDouble(): PlaneWithDouble? {
    if (size % 5 != 0) return null
    val double = this.subList(size - (size / 5) * 2, lastIndex + 1).toCardSet()
    val triple = this.subList(0, size - (size / 5) * 2).toCardSet()
    val tripleSet = triple.toSet().toList().toCardSet()
    for (card in tripleSet) if (triple.count { card == it } != 3) return null
    for (i in (0 until tripleSet.size - 1)) if (tripleSet[i + 1].value - tripleSet[i].value != 1) return null

    val doubleSet = double.toSet().toList().toCardSet()
    for (card in doubleSet) if (double.count { card == it } != 3) return null
    return PlaneWithDouble(tripleSet, doubleSet)
}