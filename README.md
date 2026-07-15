# Chess / 棋类

A Fabric 1.20.1 board-game mod built around shared 3x3 board multiblocks.

一个基于 Fabric 1.20.1 的棋类 Mod，使用共用的 3x3 多方块棋盘结构。

## Games / 棋类模式

| Game / 模式 | Features / 功能 |
| --- | --- |
| Gomoku / 五子棋 | Local play, multiplayer, edit mode, selectable-color AI, and a latest-stone marker. / 单人、联机、编辑模式、可选执色的人机对战，以及最近落子标记。 |
| Go / 围棋 | 19x19 board, captures, ko prevention, pass and score settlement, multiplayer, and selectable-color AI. / 19x19 棋盘、提子、打劫禁点、停手与目数结算、联机，以及可选执色的人机对战。 |
| Xiangqi / 象棋 | Legal move validation, check, checkmate and stalemate handling, multiplayer, selectable-color AI, and latest-move markers. / 合法走子校验、将军、将死与困毙判定、联机、可选执色的人机对战，以及最近走子标记。 |

## Playing / 对局说明

### Gomoku and Go / 五子棋与围棋

- Switch between Gomoku and Go with the mode buttons before placing any stones. Changing modes starts a fresh board. / 请在落子前用模式按钮切换五子棋或围棋；切换模式会开启新棋局。
- On an empty board, the AI button cycles through off, player black, and player white. During an active AI game, press it to leave AI mode. / 空棋盘时，AI 按钮依次切换关闭、玩家执黑、玩家执白。人机对局进行中，点击该按钮可退出人机模式。
- In Go, the AI only selects legal moves and considers captures, liberties, connected stones, local pressure, and the current ko point. / 围棋 AI 仅选择合法落点，并考虑提子、气、连通棋块、局部压迫与当前打劫禁点。
- Use `Pass` to pass in Go. Two consecutive passes settle the game; the host can also use `Finish Score` to settle manually. / 围棋中使用“停手”跳过回合。双方连续停手会自动结算；房主也可使用“结算目数”手动结束。
- The newest stone is marked with a compact red dot. / 最近落下的棋子会显示紧凑的红色标记点。

### Xiangqi / 象棋

- Select a piece, then select a legal destination. Illegal moves appear as temporary on-screen notices. / 先选择棋子，再选择合法目标位置；非法走子会以屏幕临时提示显示。
- The board shows the latest move's origin and destination with highlighted frames. / 棋盘会用高亮方框标出最近一步的起点和终点。
- Check, checkmate, stalemate, and capturing the general are handled by the game rules. / 游戏规则会处理将军、将死、困毙和吃将。

## Multiplayer / 联机对局

- The first player to use a board becomes the host. A second player can join from the board screen. / 首位使用棋盘的玩家成为房主；第二位玩家可在棋盘界面加入。
- The host chooses player colors before a multiplayer game begins. Leaving a game releases the seat but keeps the board state. / 联机开始前由房主选择双方棋色。离开对局会释放席位，但保留棋盘局面。
- AI mode is disabled when a second player joins. / 第二位玩家加入后会自动关闭人机模式。

## Development / 开发

Requirements: Java 21 and the Gradle wrapper included in this repository.

环境要求：Java 21，以及本仓库内置的 Gradle Wrapper。

```text
gradlew.bat compileJava
gradlew.bat runDatagen
gradlew.bat build
```

`runDatagen` refreshes generated language, model, and loot-table data.

`runDatagen` 会刷新生成的语言、模型和战利品表数据。
