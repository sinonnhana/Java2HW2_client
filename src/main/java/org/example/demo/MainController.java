package org.example.demo;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class MainController {
    @FXML
    private TextField usernameField; // 对应 FXML 文件中的 usernameField
    @FXML
    private PasswordField passwordField; // 对应 FXML 文件中的 passwordField
    @FXML
    private TextField boardSizeField;
    @FXML
    private VBox playerListContainer; // FXML 中的 VBox 容器

    private static Stage primaryStage;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public static boolean isloggedin = false;
    public static boolean gameStart = false;

    public void setStage(Stage stage) {
        primaryStage = stage;
    }

    public void initialize(Socket socket, ObjectOutputStream out, ObjectInputStream in) {
        this.socket = socket;
        this.out = out;
        this.in = in;
        if (playerListContainer != null) {
            loadPlayers();
        }


    }

    private void loadPlayers() {
        // 示例玩家数据
        List<Player> players = List.of(
                new Player("Alice", "Online", List.of("Bob - Win", "Charlie - Lose")),
                new Player("Bob", "Online", List.of("Alice - Lose", "Charlie - Win")),
                new Player("Charlie", "Online", List.of("Alice - Win", "Bob - Lose"))
        );

        // 清空容器
        playerListContainer.getChildren().clear();

        // 动态生成玩家卡片
        for (Player player : players) {
            VBox card = createPlayerCard(player);
            playerListContainer.getChildren().add(card);
        }
    }

    private VBox createPlayerCard(Player player) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: rgba(20, 30, 48, 0.9);"
                + "-fx-padding: 15; -fx-background-radius: 15; -fx-border-radius: 15;"
                + "-fx-border-width: 2; -fx-border-color: linear-gradient(to right, #06B6D4, #3A3D40);");

        Text username = new Text("Username: " + player.getUsername());
        username.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: linear-gradient(to right, #06B6D4, #00C9A7);");

        Text status = new Text("Status: " + player.getStatus());
        status.setStyle("-fx-font-size: 16px; -fx-fill: #A9FBD7;");

        Text records = new Text("Game Records: " + String.join(", ", player.getGameRecords()));
        records.setStyle("-fx-font-size: 14px; -fx-fill: #CCCCCC;");

        Button challengeButton = new Button("Challenge");
        challengeButton.setStyle("-fx-font-size: 14px;"
                + "-fx-background-color: linear-gradient(to right, #06B6D4, #00C9A7);"
                + "-fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 20;"
                + "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0.8, 0, 0);");
        challengeButton.setOnAction(event -> challengePlayer(player.getUsername()));

        card.getChildren().addAll(username, status, records, challengeButton);
        return card;
    }

    private void challengePlayer(String username) {
        // 模拟挑战逻辑
        System.out.println("Challenging player: " + username);
    }

    public void handleLogin() {
        loadScene("Login.fxml");
    }

    public void handleRegister() {
        loadScene("Register.fxml");
    }
    public void handleBoardSizeConfirm(){
        String boardSize = boardSizeField.getText();
        // 校验输入并传递给服务器或游戏逻辑
        System.out.println("Board size selected: " + boardSize);
        try {
            out.writeObject(boardSize);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleLoginSubmit() {
        // 获取用户名和密码并发送到服务器
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            out.writeObject("LOGIN");
            out.writeObject(username);
            out.writeObject(password);
            out.flush();

            String response = (String) in.readObject();
            if ("SUCCESS".equals(response)) {
                Platform.runLater(() -> {
                    loadScene("hall.fxml");
                  //  isloggedin = true;
                });
            } else {
                showAlert("Login Failed", "Invalid credentials", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Connection failed", Alert.AlertType.ERROR);
        }
    }

    public void handleRegisterSubmit() {
        // 获取用户名和密码并发送到服务器
        String username = usernameField.getText();
        String password = passwordField.getText();
        try {
            out.writeObject("REGISTER");
            out.writeObject(username);
            out.writeObject(password);
            out.flush();

            String response = (String) in.readObject();
            if ("SUCCESS".equals(response)) {
                Platform.runLater(() -> {
                    showAlert("Register Successful", "Please log in", Alert.AlertType.INFORMATION);
                    loadScene("Login.fxml");
                });
            } else {
                showAlert("Register Failed", "Username already exists", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Connection failed", Alert.AlertType.ERROR);
        }
    }

    public void loadScene(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            MainController controller = loader.getController();
            controller.initialize(this.socket, this.out, this.in);
            Platform.runLater(() -> primaryStage.setScene(new Scene(root)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void handleBack(){
        Platform.runLater(() -> {
            loadScene("MainMenu.fxml");
        });
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }




    public void chooseMatch(){
        try {
//            out.writeObject("choose");
//            out.flush();
            loadScene("SelectOpponent.fxml");
        }catch(Exception e){
            showAlert("Error", "Connection failed", Alert.AlertType.ERROR);
        }
    }

    public void showUsers() {
        try {
//            out.writeObject("showUsers");
//            out.flush();
            loadScene("UserList.fxml");
        }catch(Exception e){
            showAlert("Error", "Connection failed", Alert.AlertType.ERROR);
        }
    }
    public void showUsersBack() {
        try {
            loadScene("hall.fxml");
        }catch(Exception e){
            showAlert("Error", "Connection failed", Alert.AlertType.ERROR);
        }
    }

    public void suijiMatch() {
        try {
            out.writeObject("suiji");
            out.flush();
            String response = (String) in.readObject();
            if ("SUCCESS".equals(response)) {
                gameStart = true;
            }
        }catch(Exception e){
            showAlert("Error", "Connection failed", Alert.AlertType.ERROR);
        }
    }
}
