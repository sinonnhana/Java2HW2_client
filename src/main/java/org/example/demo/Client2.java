package org.example.demo;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Client2 extends Application {

    public static void main(String[] args) {
        // 启动JavaFX应用
        launch(ApplicationLauncher.class, args);

    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // 初始化游戏并启动应用界面
        ApplicationLauncher.launchApp(primaryStage);
    }
}
