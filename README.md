## Corrosion
腐食ブロック（紫のコンクリートブロック）が広がっていくプラグイン

 ## 動作環境
- Minecraft 1.16.5
- PaperMC 1.16.5

## コマンド

- cor
  - start
    
    ゲームを開始する
    
  - stop

  　ゲームを終了する
  
  - pause
  
  　腐食を停止する
  　
  - setConfig 
  
     - updateBlockTick [数値(0以上の値)]
  
        腐食発生間隔を設定（数値はTick）
  
     - updateBlockMaxNum [数値(0以上の値)]
  
        腐食できるブロックのおおよその最大数を設定  
  
        最大数を超えると次の腐食化ブロックの数*updateBlockPruningRatioの数に腐食数がリセットされる
  
     - updateBlockPruningRatio [数値(0~1の値)]
  
        腐食ブロックの増殖数リセット時に残すブロックの割合
  
     - startRange [数値(0以上の値)]
  
        start実行時に読み取るブロックの範囲
  
     - player [Player名]
  
        腐食ブロックが追いかけるプレイヤー名
  
     - death [on|off]
  
        onにすると腐食ブロックにプレイヤーが乗った時にプレイヤーが死亡する
  
  - reloadConfig
  
     コンフィグリロード
  
  - showStatus
  
     設定値や状況を表示

## デフォルト値

| 設定名                  | デフォルト値 |
| ----------------------- | ------------ |
| updateBlockTick         | 40           |
| updateBlockMaxNum       | 2000         |
| updateBlockPruningRatio | 0.2          |
| startRange              | 10           |
| player                  | roadhog_kun  |
| death                   | on           |

