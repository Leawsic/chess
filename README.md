# Chess

Fabric 1.20.1 board-game mod with a shared 3x3 board multiblock.

## Games

- Gomoku: local, multiplayer, edit mode, and AI opponents with selectable player color.
- Go: 19x19 rules with captures, ko prevention, pass/end-game scoring, multiplayer, and AI opponents with selectable player color.
- Xiangqi: legal move validation, check/checkmate and stalemate handling, multiplayer, and AI opponents with selectable player color.

## Controls

- Use the game mode buttons on the Gomoku board screen to switch between Gomoku and Go before placing any stones.
- The AI button cycles between off, player black, and player white on an empty board. During an active AI game, press it to leave AI mode.
- Go supports passing and explicit score settlement. Two consecutive passes also settle the game.
- Xiangqi shows the origin and destination of the latest move; Gomoku and Go mark the latest stone with a red dot.

## Development

```text
gradlew.bat compileJava
gradlew.bat runDatagen
gradlew.bat build
```
