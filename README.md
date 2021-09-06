# doudizhu

一个能在QQ上斗地主的mirai插件。

使用方法：

* 首先你要熟知[mirai console](https://github.com/mamoe/mirai-console/tree/03ebfd2278e9e8f051ce7f2786fb9a33efd2dbeb)的使用方法，成功在console上运行一个bot。

* 将本插件添加至plugins文件夹。
* 在**游戏群**内发送“创建游戏”，即可创建一个游戏。
* 创建游戏后，发送“上桌”即可加入游戏
* 当上桌人数达3人后，任意玩家发送“开始游戏”即可开始斗地主。出牌阶段，发送“/<你要出的牌>”在与bot的私聊或者群聊中即可出牌。如“/10jqka”就表示出了一个顺子。
* 当**管理员**发送“结束游戏”时，游戏会被强制结束。

插件的特性：

* 覆盖全部的斗地主规则。
* 能够自动识别玩家出的牌是否合法。
* 在私聊中告知玩家所剩的牌。

**指令**：

* 添加群为游戏群：在console输入指令"/dc addgroup <群号>"即可。如"/dc addgroup 123456789"。
* 添加管理员：在console输入指令"/dc addadmin <QQ号>"即可。如"/dc addgroup 123456789"

