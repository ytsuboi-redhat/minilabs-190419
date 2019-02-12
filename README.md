# base-app

## 構成
  - todo-backend
    - Spring Boot によるバックエンドアプリケーションとコンテナ
      - Spring Boot: 2.1.1.RELEASE
        - Spring Data JDBC
  - todo-frontend
    - Vue.js によるフロントエンドアプリケーションとコンテナ
      - Vue.js: 2.5.17
        - Vue CLI: 
      - Nginx: 1.14.0
        - フロントエンドをホスティング
        - バックエンドへの通信をリバースプロキシ
  - todo-mysql
    - MySQL によるデータベースコンテナ
      - MySQL: 5.7
  - todo-at
    - Acceptance Test 実行用プロジェクト
  - ci
    - CI パイプライン関連コンテナ
      - Jenkins
      - SonarQube

## アプリケーションの動作確認

### 前提
  - Node.js (>= 8.11.0) が利用可能であること
  - JDK (= 1.8.0) が利用可能であること
  - Apache Maven (>= 3.3.9) が利用可能であること
  - Docker が利用可能であること
  - Docker Compose が利用可能であること

### 手順
  - {PWD} = 本README.mdが存在するディレクトリ
  
  1. フロントエンドビルド用に Vue CLI をインストール
  ```
  $ npm install -g @vue/cli
  ```
  2. フロントエンドのソースコードをビルド
  ```
  $ cd {PWD}/todo-frontend
  $ npm install
  $ npm run build
  ```
  3. バックエンドのソースコードをビルド
  ```
  $ cd {PWD}/todo-backend
  $ mvn clean package
  ```
  4. フロントエンド・バックエンド・MySQL各コンテナをビルドして起動
  ```
  $ cd {PWD}
  $ docker-compose up --build
  ```
  5. `http://localhost/todo` にアクセス

## 演習を実施する前に

リポジトリには以下のブランチが存在します。
  - master
    - ログインと CRUD のリファレンス実装（テスト含む）
  - scaffold
    - ログインのみの実装（テスト含む）

ATDDの演習は scaffold ブランチで実施してください。

## CIパイプラインの構築

### 前提
  - Git クライアントが利用可能であること
  - Git にワークショップで利用するためのリポジトリが用意され、当ソースコードが push されていること
  - Docker が利用可能であること
  - Docker Compose が利用可能であること

### 手順
  - {PWD} = 本README.mdが存在するディレクトリ
  - {GIT_URL} = Git サーバのURL
  - {GIT_REPO_NAME} = 事前に準備いただいているワークショップ用リポジトリ名
  
  1. hostsファイルの `127.0.0.1` に `workshop-sonar` を追加
  ```
  $ sudo vi /etc/hosts
  ======
  127.0.0.1      localhost workshop-sonar
  ```
  2. Jenkins・SonarQube各コンテナをビルドして起動
      - ログ（標準出力）に出力される初期パスワードを保存しておく
  ```
  $ cd {PWD}/ci
  $ docker-compose up --build
  ...snip...
  workshop-jenkins | *************************************************************
  workshop-jenkins | *************************************************************
  workshop-jenkins | *************************************************************
  workshop-jenkins |
  workshop-jenkins | Jenkins initial setup is required. An admin user has been created and a password generated.
  workshop-jenkins | Please use the following password to proceed to installation:
  workshop-jenkins |
  workshop-jenkins | 8b53e5f0056f4d00be763f121a29a9f3
  workshop-jenkins |
  workshop-jenkins | This may also be found at: /var/jenkins_home/secrets/initialAdminPassword
  workshop-jenkins |
  workshop-jenkins | *************************************************************
  workshop-jenkins | *************************************************************
  ...snip...
  workshop-jenkins | INFO: Jenkins is fully up and running
  ...snip...
  workshop-sonar | 2018.12.26 02:06:27 INFO  app[][o.s.a.SchedulerImpl] SonarQube is up
  ```
  3. Jenkins (http://localhost:1080) にアクセスし、画面を通して初期設定を実施
     - 初期パスワードを入力
     - Suggested Plugin を選択
       - プラグインはインストール済みのためすぐに完了
     - 管理ユーザは適当に作成
       - メールアドレスも適当に（メール通知は利用しない）
     - Jenkins URL はそのまま
  4. `Jenkinsの管理 -> システム設定 -> SonarQube servers` でSonarQubeを設定
     - SonarQube installations で「Add SonarQube」をクリックする
     - 表示されたフォームに以下の情報を入力
       - Name: default
       - Server URL: http://workshop-sonar:9000
     - 画面下部「保存」をクリックする
  5. `Jenkinsの管理 -> Global Tool Configuration` で以下のツールを設定
     - `SonarQube Scanner`
       - インストール済みSonarQube Scanner で「SonarQube Scanner追加」をクリックする
       - 表示されたフォームに以下の情報を入力
         - Name: sonarqube-scanner
         - 自動インストール: Yes
         - "Install from Maven Central"
           - バージョン: SonarQube Scanner 3.2.x
     - `Maven`
       - インストール済みMaven で「Maven追加」をクリックする
       - 表示されたフォームに以下の情報を入力
         - 名前: maven3.6.0
         - 自動インストール: Yes
         - "Apacheからインストール"
           - バージョン: 3.6.0
     - `NodeJS`
       - インストール済みNodeJS で「NodeJS追加」をクリックする
       - 表示されたフォームに以下の情報を入力
         - 名前: Node 10.14.x
         - 自動インストール: Yes
         - "Install from nodejs.org"
           - Version: NodeJS 10.14.2
           - Force 32bit architecture: No
           - Global npm packages to install: @vue/cli
           - Global npm packages refresh hours: 72
     - 画面下部「Save」をクリックする
  6. Jenkinsのトップ画面から新規ジョブ作成
     - "Enter an item name" で "todo-app" と入力、パイプラインを選択して「OK」をクリックする
     - 遷移先にて以下の情報を選択/入力
       - パイプライン
         - 定義: Pipeline script from SCM
         - SCM: Git
           - リポジトリ
             - リポジトリURL: {GIT_URL}/{GIT_REPO_NAME}.git
             - 認証情報: なし
           - ビルドするブランチ
             - ブランチ指定子: */master
         - Script Path: Jenkinsfile
     - 画面下部「保存」をクリックする
  7. todo-appジョブ画面からビルド実行
  8. アンドン（行灯）表示用ビューを設定する
     - Jeinkinsのトップ画面からNew Viewをクリックする
       - ビュー名に "Build Monitor" と入力
       - "Build Monitor View" をラジオボタンを選択
       - 「OK」をクリックする
     - 遷移先にて以下の情報を選択
       - Job Filters: Jobs
         - todo-app にチェック
       - 「保存」をクリックする

以上