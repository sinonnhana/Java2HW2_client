package org.example.demo;

import java.io.*;
import javafx.animation.PauseTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.paint.Color;


import java.util.Arrays;
import java.util.Objects;

public class Controller {


    @FXML
    private Label scoreLabel1;
    @FXML
    private Label scoreLabel2;
    @FXML
    private Label dialogLabel;
    @FXML
    private Pane linePane;

    @FXML
    private GridPane gameBoard;

    public static Game game;

    int[] position = new int[3];

    int[] record = new int[9];

    int counter = 0;

    private final IntegerProperty score = new SimpleIntegerProperty(0);
    private final IntegerProperty scoreOppo = new SimpleIntegerProperty(0);
    private final StringProperty dialog = new SimpleStringProperty(" ");


    @FXML
    public void initialize() {
        scoreLabel1.textProperty().bind(score.asString());
        scoreLabel2.textProperty().bind(scoreOppo.asString());
        dialogLabel.textProperty().bind(dialog);
        linePane.setMouseTransparent(true);
    }


    public void showDialogMessage_2s(String message) {
        Platform.runLater(() -> {
            dialog.set(message);
            dialogLabel.setVisible(true);        // 使对话框可见

            // 使用 Timeline 在 2 秒后隐藏对话框
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> dialogLabel.setVisible(false)));
            timeline.setCycleCount(1);           // 只执行一次
            timeline.play();
        });
    }

    public void showDialogMessage(String message) {
        Platform.runLater(() -> {
            dialog.set(message);
            dialogLabel.setVisible(true);        // 使对话框可见
        });
    }

    public void cancelDialogMessage() {
        Platform.runLater(() -> {
            dialogLabel.setVisible(false);
        });
    }

    public void drawLineBetweenCells(int row1, int col1, int row2, int col2) {
        Node cell1 = getNodeByRowColumnIndex(row1, col1, gameBoard);
        Node cell2 = getNodeByRowColumnIndex(row2, col2, gameBoard);

        if (cell1 != null && cell2 != null) {
            // 计算起始和终止点相对于 linePane 的位置
            double startX = cell1.getLayoutX() + cell1.getBoundsInParent().getWidth() / 2;
            double startY = cell1.getLayoutY() + cell1.getBoundsInParent().getHeight() / 2;
            double endX = cell2.getLayoutX() + cell2.getBoundsInParent().getWidth() / 2;
            double endY = cell2.getLayoutY() + cell2.getBoundsInParent().getHeight() / 2;

            // 创建一条线
            Line line = new Line(startX, startY, endX, endY);
            line.setStroke(Color.RED);
            line.setStrokeWidth(2);

            // 在 JavaFX 应用线程中更新 UI
            Platform.runLater(() -> {
                linePane.getChildren().add(line); // 将线添加到 linePane 中

                // 0.5 秒后移除线条
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
                delay.setOnFinished(event -> linePane.getChildren().remove(line));
                delay.play();
            });
        }
    }


    public void createGameBoard() {
        gameBoard.getChildren().clear();
        for (int row = 0; row < game.row; row++) {
            for (int col = 0; col < game.col; col++) {
                Button button = new Button();
                button.setPrefSize(40, 40);
                ImageView imageView = addContent(game.board[row][col]);
                imageView.setFitWidth(30);
                imageView.setFitHeight(30);
                imageView.setPreserveRatio(true);
                button.setGraphic(imageView);
                int finalRow = row;
                int finalCol = col;
                button.setOnAction( _ -> handleButtonPress(finalRow, finalCol));
                gameBoard.add(button, col, row);
            }
        }
    }

    public int[] oneRound(){
        counter = 0;
        Arrays.fill(record, 0);
        while (counter < 2) {
            // 等待计数器达到2
            try {
                Thread.sleep(100); // 短暂休眠，避免循环过于频繁
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return record;
    }


    private void handleButtonPress(int row, int col) {
        System.out.println("Button pressed at: " + row + ", " + col);
        if(position[0] == 0){
            position[1] = row;
            position[2] = col;
            position[0] = 1;
        }else{
            record = game.judge(position[1], position[2], row, col);
            position[0] = 0;
            if(record[0] != 0){
                System.out.println("delete: (" + position[1] + "," + position[2] + "),(" + row + "," + col + ")");
                // TODO: handle the grid deletion logic
                if (game.board[row][col] != 0) {
                    score.set(score.get() + 1);
                    game.board[position[1]][position[2]] = 0;
                    game.board[row][col] = 0;

                    updateButtonImage(position[1], position[2], game.board[position[1]][position[2]]);
                    updateButtonImage(row, col, game.board[row][col]);

                    if (record[0] == 1){
                        drawLineBetweenCells(record[1], record[2], record[3], record[4]);
                    }else if (record[0] == 2){
                        drawLineBetweenCells(record[1], record[2], record[3], record[4]);
                        drawLineBetweenCells(record[3], record[4], record[5], record[6]);
                    }else if (record[0] == 3){
                        drawLineBetweenCells(record[1], record[2], record[3], record[4]);
                        drawLineBetweenCells(record[3], record[4], record[5], record[6]);
                        drawLineBetweenCells(record[5], record[6], record[7], record[8]);
                    }

                }
            }
        }
        counter++;
    }

    public void disableAllButtons() {
        for (Node node : gameBoard.getChildren()) {
            if (node instanceof Button) {
                node.setDisable(true); // 设置按钮不可用
            }
        }
    }

    public void enableAllButtons() {
        for (Node node : gameBoard.getChildren()) {
            if (node instanceof Button) {
                node.setDisable(false); // 设置按钮不可用
            }
        }
    }

    public void update(int[] arr){
        Platform.runLater(() -> {
            int flag = arr[0];
            game.board[arr[1]][arr[2]] = 0;
            game.board[arr[2*flag+1]][arr[2*flag+2]] = 0;
            updateButtonImage(arr[1], arr[2],game.board[arr[1]][arr[2]]);
            updateButtonImage(arr[2*flag+1], arr[2*flag+2],game.board[arr[2*flag+1]][arr[2*flag+2]]);
            scoreOppo.set(scoreOppo.get()+1);

        });
        if (arr[0] == 1){
            drawLineBetweenCells(arr[1], arr[2], arr[3], arr[4]);
        }else if (arr[0] == 2){
            drawLineBetweenCells(arr[1], arr[2], arr[3], arr[4]);
            drawLineBetweenCells(arr[3], arr[4], arr[5], arr[6]);
        }else if (arr[0] == 3){
            drawLineBetweenCells(arr[1], arr[2], arr[3], arr[4]);
            drawLineBetweenCells(arr[3], arr[4], arr[5], arr[6]);
            drawLineBetweenCells(arr[5], arr[6], arr[7], arr[8]);
        }
    }

    private void updateButtonImage(int row, int col, int content) {
        Button button = (Button) getNodeByRowColumnIndex(row, col, gameBoard);
        if (button != null) {
            ImageView imageView = addContent(content);
            imageView.setFitWidth(30);
            imageView.setFitHeight(30);
            imageView.setPreserveRatio(true);
            button.setGraphic(imageView);
        }
    }

    private Node getNodeByRowColumnIndex(int row, int col, GridPane gridPane) {
        for (Node node : gridPane.getChildren()) {
            Integer rowIndex = GridPane.getRowIndex(node);
            Integer columnIndex = GridPane.getColumnIndex(node);

            if (rowIndex != null && columnIndex != null && rowIndex == row && columnIndex == col) {
                return node;
            }
        }
        return null;
    }

    @FXML
    private void handleReset() {

    }

    public ImageView addContent(int content){
        return switch (content) {
            case 0 -> new ImageView(imageCarambola);
            case 1 -> new ImageView(imageApple);
            case 2 -> new ImageView(imageMango);
            case 3 -> new ImageView(imageBlueberry);
            case 4 -> new ImageView(imageCherry);
            case 5 -> new ImageView(imageGrape);
            case 6 -> new ImageView(imageKiwi);
            case 7 -> new ImageView(imageOrange);
            case 8 -> new ImageView(imagePeach);
            case 9 -> new ImageView(imagePear);
            case 10 -> new ImageView(imagePineapple);
            case 11 -> new ImageView(imageWatermelon);
            default -> new ImageView(imageCarambola);
        };
    }

    public static Image imageApple = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/apple.png")).toExternalForm());
    public static Image imageMango = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/mango.png")).toExternalForm());
    public static Image imageBlueberry = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/blueberry.png")).toExternalForm());
    public static Image imageCherry = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/cherry.png")).toExternalForm());
    public static Image imageGrape = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/grape.png")).toExternalForm());
    public static Image imageCarambola = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/carambola.png")).toExternalForm());
    public static Image imageKiwi = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/kiwi.png")).toExternalForm());
    public static Image imageOrange = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/orange.png")).toExternalForm());
    public static Image imagePeach = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/peach.png")).toExternalForm());
    public static Image imagePear = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/pear.png")).toExternalForm());
    public static Image imagePineapple = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/pineapple.png")).toExternalForm());
    public static Image imageWatermelon = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/watermelon.png")).toExternalForm());

    public void resets(ActionEvent actionEvent) throws IOException {
//        FXMLLoader fxmlLoader = new FXMLLoader(ApplicationLauncher.class.getResource("MainMenu.fxml"));
//        VBox root = fxmlLoader.load();
//        MainController Maincontroller = fxmlLoader.getController();
//        Maincontroller.initialize(socket,out,in);
//        Maincontroller.setStage(stage);
//        Platform.runLater(() -> {
//            alert = new Alert(Alert.AlertType.INFORMATION);
//            Scene scene = new Scene(root);
//            stage.setTitle("连连看游戏 - 主界面 ");
//            stage.setScene(scene);
//            stage.show();
//        });
    }
}
