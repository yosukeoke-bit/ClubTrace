# ClubTrace MVP

ゴルフ動画のクラブヘッド軌跡を、Android端末上で作るための最小プロトタイプです。

## MVPの狙い

1. スマホ内の動画を選択
2. 1フレームずつ確認
3. クラブヘッド中心をユーザーが1回タップ
4. OpenCV Lucas–Kanade Optical Flowで追跡
5. 黄色=バックスイング / 水色=ダウンスイングとして軌跡描画
6. 追跡が外れたら、そのフレームで再タップして復帰

## 現在の状態（v0.1 scaffold）

- Android Studioで開けるプロジェクト構成
- 動画選択
- MediaMetadataRetrieverによるフレームプレビュー
- コマ送り
- ヘッド位置タップUI
- OpenCV初期化
- 複数特徴点＋中央値を使うOpticalFlowClubTrackerコア
- 軌跡描画Overlay

### まだ未接続の部分

`MainActivity` の動画デコードと `OpticalFlowClubTracker` をまだ接続していません。

理由:
MediaMetadataRetriever はプレビューには簡単ですが、120fps動画を全フレーム正確に高速デコードする用途には向きません。
本番処理では MediaExtractor + MediaCodec でフレームを順次デコードする構成に切り替える予定です。

## 次の実装

### v0.2
- MediaExtractor + MediaCodec Decoder
- 実フレームをOpenCV Matへ変換
- タップフレームから前後方向にOptical Flow追跡
- 信頼度が下がったら自動停止
- その位置から再タップで再開

### v0.3
- トップ自動検出
- バックスイング=黄色 / ダウンスイング=水色
- Savitzky-Golay等による軌跡平滑化
- 外れ値除去

### v0.4
- MediaCodec Encoderで軌跡付きMP4を書き出し

## 追跡方式

最初からクラブヘッド検出AIに頼りません。

クラブヘッド周辺ROI内に Shi-Tomasi corner を複数抽出し、
Lucas–Kanade Optical Flow で次フレームへ追跡します。

1点追跡ではなく複数点の移動量の中央値を使うことで、
反射・ブレ・シャフトとの重なりに強くする方針です。

## 推奨テスト動画

- 固定カメラ
- 60fps以上（120fps推奨）
- クラブ全体が画面内に入る
- 十分な明るさ
- シャッタースピードが速めだとなお有利

## 開発環境

- Android Studio
- Kotlin
- minSdk 26
- compileSdk 36
- OpenCV Android 5.0.0

