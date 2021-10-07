package dada.douDiZhu

import dada.douDiZhu.data.*
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content

/**
 * ### 游戏类
 * 主要分为[准备][prepare]、[发牌][faPai]、[抢地主][qiangDiZhu]、[开始][startDouDiZhu]、[结算][settle]五个阶段
 *
 * [Game][Game]本身继承[CompletableJob]，在[Game][Game]内各种各样的Job都是自身的ChildJob。
 *
 * 每一个阶段创建一个ChildJob来构建一个新的eventChannel，以便在接收到特定指令的时候结束掉整个时间通道
 *
 * **在[prepare]与[settle]中，需要用到CoinManager来查询明乃币，更改明乃币。**
 *
 * @param gameGroup 游戏所在的群
 * @param basicBet 底分
 */
class Game(private val gameGroup: Group, private val basicBet: Int = 200) : CompletableJob by SupervisorJob() {
    private var diZhuPai = CardSet()

    private val table = Table()

    private var magnification = 1

    /**
     * ### 游戏的入口
     * 在游戏被创建时，即开启一个“开始游戏”和“结束游戏”的监听。前着可以拦截二次的创建指令，后者可以随时使游戏结束。
     */
    suspend fun gameStart() {
        coroutineScope {
            val channel = globalEventChannel()
                .parentJob(this@Game)
                .filterIsInstance<GroupMessageEvent>()
                .filter { event: GroupMessageEvent -> event.group == gameGroup }

            // 当游戏存在时拦截创建游戏的指令
            channel.subscribeGroupMessages(priority = EventPriority.HIGH) {
                (case("创建游戏") and sentFrom(gameGroup)) reply {
                    this.intercept()
                    "已经有一个游戏了"
                }
                //强制结束游戏
                //只有Config中的admin可以结束游戏
                (case("结束游戏") and sentFrom(gameGroup)) {
                    if (sender.id in Config.admin) {
                        group.sendMessage("结束成功")
                        this@Game.cancel()
                    }
                }
                "当前玩家" reply { "当前玩家：${table.players.map { it.nick }}" }
            }
        }
        prepare()
    }

    /**
     * ### 准备
     * 玩家从此处进入游戏
     */
    private suspend fun prepare() {
        var started = false
        /*
            创建一个job用于终结订阅器，这个job是this的子job
         */
        val prepareJob = Job(this)
        val scopedChannel = coroutineScope {
            globalEventChannel().parentJob(prepareJob)
                .filterIsInstance<GroupMessageEvent>()
                .filter { event: GroupMessageEvent -> event.group == gameGroup }
        }
        val job = scopedChannel.subscribeGroupMessages {
            (case("上桌") and sentFrom(gameGroup)) reply {
                if (!sender.enough(200)) {
                    "你的point不够200个哦，你没钱了"
                } else if (table.enter(sender)) {
                    "加入成功\n当前玩家：${table.players.map { it.nick }}"
                } else {
                    "人满了或你已经在游戏中了，无法加入"
                }
            }
            case("下桌") {
                if (sender in table.players) {
                    table.players.remove(sender)
                    subject.sendMessage("<${sender.nick}>下桌成功")
                }
            }
            (case("开始游戏")) reply {
                if (sender in table.players) {
                    if (table.isFull()) {
                        started = true
                        prepareJob.cancel()
                    } else {
                        "还没满人，无法开始\n" + "当前玩家：${table.players.map { it.nick }}"
                    }
                }
            }
        }
        //等待job结束，即成功开始游戏
        job.join()
        if (started) faPai()
    }

    private suspend fun faPai() {
        val cardSet =
            (Card.cards() + Card.cards() + Card.cards() + Card.cards() + Card.jokers()).shuffled().toCardSet()
        diZhuPai = cardSet.subList(51, 54).toCardSet()
        cardSet.removeAt(53)
        cardSet.removeAt(52)
        cardSet.removeAt(51)
        table.handCard = mutableMapOf(
            Pair(table.players[0], HandCards(cardSet.subList(0, 17))),
            Pair(table.players[1], HandCards(cardSet.subList(17, 34))),
            Pair(table.players[2], HandCards(cardSet.subList(34, 51)))
        )
        for (player in table.players) {
            player.sendMessage(player.handCards().toSortedString())
        }
        qiangDiZhu()
    }

    /**
     * 逻辑：
     * 1.创建一个迭代器，轮换地返回一个玩家
     * 2.在迭代时，每前进一个玩家，打开一次监听任务，监听其是否抢地主，直到有答复
     * 3.关闭job
     */
    private suspend fun qiangDiZhu() {
        var qiang = false
        for (player in table.players) {
            if (this.isActive)
                reply(At(player) + PlainText("轮到你抢地主了，是否要抢地主？"))

            val qiangDiZhuJob = Job(this)

            val gameEventChannel = coroutineScope {
                globalEventChannel()
                    .parentJob(qiangDiZhuJob)
                    .filterIsInstance<GroupMessageEvent>()
                    .filter { event: GroupMessageEvent -> event.sender == player }
            }
            //2
            val job = if (this.isActive) {
                gameEventChannel.subscribeGroupMessages {
                    (case("抢地主") or case("我抢") or case("抢")){
                        qiang = true
                        qiangDiZhuJob.cancel()
                    }
                    (case("不抢")){
                        qiangDiZhuJob.cancel()
                    }
                }
            } else {
                return
            }

            job.join()
            if (qiang) {
                reply(At(player) + PlainText("当上了地主"))
                reply("地主牌是$diZhuPai")
                table.index = table.players.indexOf(player)
                table.diZhu = player
                table.nongMin = table.players.mapNotNull { if (it != table.diZhu) it else null }
                player.handCards().addAll(diZhuPai)
                player.sendMessage("你当上了地主，你的地主牌是$diZhuPai")
                player.sendMessage("你现在有\n${player.handCards().toSortedString()}")
                break
            }
        }
        if (qiang)
            startDouDiZhu()  //如果有人抢地主就开始
        else
            faPai()  //如果无人抢地主就重新发牌
    }

    private suspend fun startDouDiZhu() {
        var isRunning = true

        var lastCardSet = CardSet()
        var lastCombination: Combination = NotACombination
        var lastPlayer = table.players[table.index] //上次出牌的玩家。如果连续两个玩家没出牌，就知道该更新牌型了

        /*
        玩家轮流出牌。
         */
        for (player in table.iterator()) {
            if (player == lastPlayer) lastCombination = NotACombination

            if (this.isActive)
                reply(At(player) + "轮到你出牌了")
            val startJob = Job(this)

            val gameEventChannel = coroutineScope {
                globalEventChannel()
                    .parentJob(startJob)
                    .filterIsInstance<MessageEvent>()
                    .filter { event: MessageEvent -> event.sender.id == player.id }
            }

            /*
            玩家每次发送以“/“开头的消息，都视为一次出牌请求。出牌请求有多种处理结果，包括
            1.玩家并没有要出的牌
            2.玩家出牌不符合规则(与上家出的牌不是同类型的，或者比上家出的小)
            3.玩家把所有的牌都出完了，赢得游戏
            4.玩家顺利出牌，并进入下家的回合
            5.玩家跳过
            以上结果都会迎来onEvent的结束，但只有情况4和情况5会顺利进入下一家的回合
            其中，情况3会直接进入settle环节
             */
            val job = if (this.isActive) {
                gameEventChannel.subscribeAlways<MessageEvent> playCard@{
                    if (message.content[0].toString() == "/") {
                        val rawCardsString = message.content.substring(1)
                        val deserializedCards = rawCardsString.deserializeToCard()
                        if (!(player.handCards() have deserializedCards)) {
                            player.sendMessage("没在你的牌中找到你想出的牌哦")
                            return@playCard
                        }
                        val comb = deserializedCards.findCombination() //玩家想出的牌
                        if (comb == NotACombination) {
                            player.sendMessage("没看懂你想要出什么牌啦")
                            return@playCard
                        }
                        /*
                            有可能可以出牌的情况：
                            1.牌权回到自己手上
                            2.出了和上一次相同牌型并且比他大
                            3.上一次不是炸弹，但这次是炸弹
                         */
                        if (
                            lastCombination == NotACombination
                            || (lastCombination.sameType(comb)
                                    && comb > lastCombination
                                    && deserializedCards.size == lastCardSet.size)
                            || (lastCombination !is Bomb && comb is Bomb)
                        ) {
                            player.play(deserializedCards)
                            /*
                                炸弹有特殊回复，并且翻倍
                             */
                            if (comb is Bomb) {
                                magnification *= 2
                                reply("炸弹！<${player.nick}>出了$comb")
                                reply("当前倍率：$magnification")
                            } else reply("<${player.nick}>出了$comb")
                            if (player.handCards().size == 2) reply("<${player.nick}>只剩两张牌了哦！感觉ta要赢了")
                            if (player.handCards().size == 1) reply("<${player.nick}>只剩一张牌了哦！感觉ta要赢了")

                            //获胜判断
                            if (player.handCards().size == 0) {
                                settle(player)
                                isRunning = false
                                startJob.cancel()
                                return@playCard
                            }
                            player.sendMessage("你还剩\n ${player.handCards()}")

                            lastCardSet = deserializedCards
                            lastCombination = comb
                            lastPlayer = player
                            startJob.cancel()
                            return@playCard
                        }
                        player.sendMessage("你出的牌貌似不符合规则哦")
                        return@playCard
                    }

                    if (message.content == "要不起" || message.content == "不要" || message.content == "过") {
                        if (lastPlayer == player) player.sendMessage("这是你的回合，不可以不出哦")
                        else {
                            reply("<${player.nick}>选择了不出")
                            startJob.cancel()
                        }
                    }
                }
            }else return
            job.join()
            if (!isRunning) {
                this.cancel()
                break
            }
        }
    }

    /*
    获胜结算
    斗地主的获胜结算依赖CoinManager，即明乃币的管理系统。
     */
    private suspend fun settle(winner: Member) {
        //获胜场次，总场次的变化
        winner.douDiZhuData.winTimes += 1
        for (player in table.players) {
            player.douDiZhuData.gameTimes += 1
        }

        val amount = basicBet * magnification
        if (winner == table.diZhu) {
            winner.addPoints(amount * 2)
            table.nongMin.forEach {
                it.pay(amount)
            }
            reply(
                "地主赢了\n" +
                        "<${winner.nick}>赢得了${amount * 2}个point\n" + "\n" +
                        "<${table.nongMin[0]}.nick>、<${table.nongMin[1].nick}>输掉了${amount}个point"
            )
        } else {
            table.diZhu.pay(amount * 2)
            table.nongMin.forEach {
                it.addPoints(amount)
            }
            reply(
                "农民赢了\n" +
                        "<${table.nongMin[0].nick}>、<${table.nongMin[1].nick}>赢得了了${amount}个point" + "\n" +
                        "<${table.diZhu.nick}>输掉了${amount * 2}个个point\n"
            )
        }
    }

    //工具函数，别忘了
    private fun Member.handCards(): HandCards = table.handCard[this]!!

    private fun Member.play(cardSet: CardSet) = handCards().play(cardSet)

    private suspend fun reply(msg: Message) {
        gameGroup.sendMessage(msg)
    }

    private suspend fun reply(msg: String) {
        gameGroup.sendMessage(msg)
    }

}
