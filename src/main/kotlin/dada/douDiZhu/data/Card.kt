package dada.douDiZhu.data

/**
 * ### 牌
 * @param value 牌的值，可以看做牌的大小关系
 * @param id 牌的字符串形式
 */
enum class Card(val value: Int, val id: String) {
    THREE(1, "3"),
    FOUR(2, "4"),
    FIVE(3, "5"),
    SIX(4, "6"),
    SEVEN(5, "7"),
    EIGHT(6, "8"),
    NINE(7, "9"),
    TEN(8, "10"),
    J(9, "J"),
    Q(10, "Q"),
    K(11, "K"),
    A(12, "A"),
    TWO(13, "2"),
    GREY_JOKER(14, "鬼"),
    COLORFUL_JOKER(15, "王"),

    //在玩家乱出牌的时候用的牌类型
    NOT_A_CARD(100, "");

    internal companion object {
        internal fun deserializeFromString(id: String): Card =
            values().firstOrNull { it.id == id.toUpperCase() } ?: NOT_A_CARD

        internal fun deserializeFromInt(value: Int): Card =
            values().firstOrNull { it.value == value } ?: NOT_A_CARD

        internal fun cards(): CardSet =
            CardSet(A, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, J, Q, K)

        internal fun jokers(): CardSet = CardSet(GREY_JOKER, COLORFUL_JOKER)
    }
}