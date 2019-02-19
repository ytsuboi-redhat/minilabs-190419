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

以上
