# スマホだけでAPKを作る手順

## 1. GitHubで空のRepositoryを作る
1. Chromeで https://github.com/ を開いてログイン
2. 右上の「+」→「New repository」
3. Repository name: `ClubTrace`
4. Public / Private はどちらでもOK
5. README等は追加せず、空のRepositoryとして作成

## 2. このプロジェクトをアップロード
GitHubのRepository画面で:
1. `Add file` → `Upload files`
2. ZIPそのものではなく、解凍したプロジェクトの中身をアップロード
3. `.github`、`app`、`build.gradle.kts`、`settings.gradle.kts`、
   `gradle.properties` などをRepository直下に置く
4. `Commit changes`

※ Androidのファイル管理アプリでZIPを解凍してからアップロードしてください。

## 3. APKをビルド
`.github/workflows/build-apk.yml` が入っていれば、
mainブランチへのアップロード後に自動でActionsが開始します。

手動で開始する場合:
1. Repositoryの `Actions`
2. `Build Android APK`
3. `Run workflow`
4. `Run workflow`

## 4. APKをスマホへ保存
Actionsのビルドが成功したら:
1. 成功した実行を開く
2. 下部の `Artifacts`
3. `ClubTrace-debug-apk` をタップ
4. ZIPをダウンロードして展開
5. `app-debug.apk` をタップしてインストール

初回だけAndroid側で
「この提供元のアプリを許可」
などのインストール許可が必要になる場合があります。

## 現状
v0.1.1 は v0.1 にGitHub Actionsビルド設定を追加した版です。
アプリ機能自体はまだv0.1相当で、追跡器と動画デコードは未接続です。
