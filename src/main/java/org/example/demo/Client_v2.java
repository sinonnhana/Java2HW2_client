package org.example.demo;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class Client_v2 extends Application {
    private static Alert alert;
    private static int player;
    private static TextInputDialog dialog;
    private static Boolean connected = true;

    @Override
    public void start(Stage stage) throws IOException {
        launchApp(stage);
    }

    public static void launchApp(Stage stage) {
        new Thread(() -> {
            try {
                Socket socket = new Socket("localhost", 12345);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());


                FXMLLoader fxmlLoader = new FXMLLoader(ApplicationLauncher.class.getResource("MainMenu.fxml"));
                VBox root = fxmlLoader.load();
                MainController Maincontroller = fxmlLoader.getController();
                Maincontroller.initialize(socket,out,in);
                Maincontroller.setStage(stage);
                Platform.runLater(() -> {
                    alert = new Alert(Alert.AlertType.INFORMATION);
                    Scene scene = new Scene(root);
                    stage.setTitle("连连看游戏 - 主界面 ");
                    stage.setScene(scene);
                    stage.show();
                });

                while (!MainController.gameStart){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
                System.out.println("login");


                //while (true) {
                    String waitMessage = (String) in.readObject();
                    System.out.println("server: " + waitMessage);
                    Maincontroller.loadScene("WaitingDialog.fxml");
                    // 等待服务器发送玩家信息并关闭 alert

                    String playerInfo;
                    while (true) {
                        playerInfo = (String) in.readObject();
                        if ("ping".equals(playerInfo)) {
                            System.out.println("Received heartbeat from server: " + playerInfo);
                            out.writeObject("pong");
                            out.flush();
                            System.out.println("pong");
                        } else {
                            System.out.println("Received message from server: " + playerInfo);
                            break;
                        }
                    }

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    System.out.println("收到玩家信息：" + playerInfo);

                    AtomicReference<String> size = new AtomicReference<>("");
                    if (playerInfo.split(" ")[3].equals("1.")) {
                        Maincontroller.loadScene("Player1BoardSelection.fxml");
                        player = 1;
                    } else {
                        System.out.println("match successfully");
                        Maincontroller.loadScene("Player2Waiting.fxml");
                        player = 2;
                    }

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    initGameUI(stage, in, out, playerInfo);
               // }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                handleServerDisconnection("Server connection lost. Exiting game.", stage);
            }
        }).start();
    }

    private static void initGameUI(Stage stage, ObjectInputStream in, ObjectOutputStream out, String playerInfo) throws IOException, ClassNotFoundException {
        try {

            GameState gameState = (GameState) in.readObject();
            Controller.game = new Game(gameState.getBoard());


            FXMLLoader fxmlLoader = new FXMLLoader(ApplicationLauncher.class.getResource("board.fxml"));
            VBox root = fxmlLoader.load();
            Controller controller = fxmlLoader.getController();
            controller.createGameBoard();

            Platform.runLater(() -> {
                alert.close();
                System.out.println("create game stage");
                Scene scene = new Scene(root);
                stage.setTitle("连连看游戏 - " + playerInfo);
                stage.setScene(scene);
                stage.show();
            });

            startGameLoop(controller, in, out, stage);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            handleServerDisconnection("Server connection lost. Exiting game.", stage);
        }
    }

    private static void startGameLoop(Controller controller, ObjectInputStream in, ObjectOutputStream out, Stage stage) {
        new Thread(() -> {
            try {
                System.out.println("------game start------");
                while (true) {
                    int[] record;
                    if (player == 1) {
                        controller.enableAllButtons();
                        System.out.println("start your round");
                        record = controller.oneRound();
                        int flag = record[0];

                        System.out.println("you try to delete:");
                        for (int i = 0; i < record.length; i++) {
                            System.out.print(record[i] + " ");
                        }
                        if (flag != 0) {
                            controller.showDialogMessage_2s("You deleted (" + record[1] + "," + record[2] + "),(" + record[2 * flag + 1] + "," + record[2 * flag + 2] + ") successfully");
                        } else {
                            controller.showDialogMessage_2s("error match!");

                        }
                        for (int x = 0; x < 9; x++) {
                            out.writeInt(record[x]);
                            out.flush();
                        }

                        System.out.println("end your round");
                        controller.disableAllButtons();


                        int state = in.readInt();
                        System.out.println("state:" + state);
                        Thread.sleep(1000);

                        if (state == 1) {
                            controller.showDialogMessage_2s("player1 win the game!");
                            controller.disableAllButtons();
                            break;
                        } else if (state == 2) {
                            controller.showDialogMessage_2s("player2 win the game!");
                            controller.disableAllButtons();
                            break;
                        } else if (state == 3) {
                            controller.showDialogMessage_2s("a tie!");
                            controller.disableAllButtons();
                            break;
                        } else if (state == 0) {
                            controller.showDialogMessage_2s("game continue, opponent's turn");
                        }

                        Object obj = in.readObject();
                        int[] arr = new int[0];
                        if (obj instanceof String) {
                            System.out.println((String) obj);
                            handleOpponentLeft(stage);
                            return;
                        }else {
                            arr = (int[]) obj;
                        }


                        flag = arr[0];
                        System.out.println("updating------");
                        if (flag != 0) {
                            controller.showDialogMessage_2s("Your opponent deleted (" + arr[1] + "," + arr[2] + "),(" + arr[2 * flag + 1] + "," + arr[2 * flag + 2] + ") successfully");
                            controller.update(arr);
                        } else {
                            controller.showDialogMessage_2s("your opponent has a error match!");
                        }

                        state = in.readInt();
                        System.out.println("state:" + state);
                        Thread.sleep(1000);


                        if (state == 1) {
                            controller.showDialogMessage_2s("player1 win the game!");
                            controller.disableAllButtons();
                            break;
                        } else if (state == 2) {
                            controller.showDialogMessage_2s("player2 win the game!");
                            controller.disableAllButtons();
                            break;
                        } else if (state == 3) {
                            System.out.println("lose...");
                            controller.showDialogMessage_2s("a tie!");
                            controller.disableAllButtons();
                            break;
                        } else if (state == 0) {
                            controller.showDialogMessage_2s("game continue, your turn");
                        }

                        System.out.println("updating successful ------");

                    } else {
                        controller.disableAllButtons();
                        int flag;

                        Object obj = in.readObject();
                        int[] arr = new int[0];
                        if (obj instanceof String) {
                            System.out.println((String) obj);
                            handleOpponentLeft(stage);
                            return;
                        }else {
                            arr = (int[]) obj;
                        }
                        flag = arr[0];
                        System.out.println("updating------");
                        if (flag != 0) {
                            controller.showDialogMessage_2s("Your opponent deleted (" + arr[1] + "," + arr[2] + "),(" + arr[2 * flag + 1] + "," + arr[2 * flag + 2] + ") successfully");
                            controller.update(arr);
                        } else {
                            controller.showDialogMessage_2s("your opponent has a error match!");
                        }

                        int state = in.readInt();
                        System.out.println("state:" + state);
                        Thread.sleep(1000);

                        if (state == 1) {
                            controller.showDialogMessage_2s("player1 win the game!");
                            controller.disableAllButtons();
                            break;
                        } else if (state == 2) {
                            controller.showDialogMessage_2s("player2 win the game!");
                            controller.disableAllButtons();
                            break;
                        } else if (state == 3) {
                            controller.showDialogMessage_2s("a tie!");
                            controller.disableAllButtons();
                            break;
                        }else if (state == 0) {
                            controller.showDialogMessage_2s("game continue, your turn");
                        }

                        controller.enableAllButtons();

                        record = controller.oneRound();
                        flag = record[0];

                        System.out.println("you delete:");
                        for (int i = 0; i < record.length; i++) {
                            System.out.print(record[i] + " ");
                        }
                        if (flag != 0) {
                            controller.showDialogMessage_2s("You deleted (" + record[1] + "," + record[2] + "),(" + record[2 * flag + 1] + "," + record[2 * flag + 2] + ") successfully");
                        } else {
                            controller.showDialogMessage_2s("error match!");

                        }
                        for (int x = 0; x < 9; x++) {
                            out.writeInt(record[x]);
                            out.flush();
                        }
                        System.out.println("end your round");
                        controller.disableAllButtons();

                        state = in.readInt();
                        System.out.println("state:" + state);
                        Thread.sleep(1000);

                        if (state == 1) {
                            controller.showDialogMessage_2s("player1 win the game!");
                            controller.disableAllButtons();
                            break;
                        } else if (state == 2) {
                            controller.showDialogMessage_2s("player2 win the game!");
                            controller.disableAllButtons();
                            break;
                        } else if (state == 3) {
                            controller.showDialogMessage_2s("a tie!");
                            controller.disableAllButtons();
                            break;
                        }else if (state == 0) {
                            controller.showDialogMessage_2s("game continue, opponent's turn");
                        }

                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                handleServerDisconnection("Server connection lost. Exiting game.", stage);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void checkThread() {
        Map<Thread, StackTraceElement[]> allThreads = Thread.getAllStackTraces();
        System.out.println("--------------查看活跃情况--------------");
        for (Thread thread : allThreads.keySet()) {
            if (thread.getName().startsWith("JavaFX")) {
                System.out.println("线程名称: " + thread.getName() + " 是否活跃: " + thread.isAlive());
            }
        }
    }

    private static void handleServerDisconnection(String message, Stage stage) {
        connected = false;
        Platform.runLater(() -> {
            if (alert != null) {
                alert.close();
            }
            if (stage != null) {
                stage.close();
            }
            if (dialog != null ) {
                dialog = null;
            }
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("player" + player + " Connection Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
            Platform.exit();
        });

        // 输出断开连接消息
        System.out.println("Server disconnected: " + message);
    }

    private static void handleOpponentLeft(Stage stage) {
        connected = false;
        Platform.runLater(() -> {
            if (alert != null) {
                alert.close();
            }
            if (stage != null) {
                stage.close();
            }
            if (dialog != null ) {
                dialog = null;
            }
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("player" + player + " Connection Error");
            alert.setHeaderText(null);
            alert.setContentText("your opponent has left the game");
            alert.showAndWait();
            Platform.exit();
        });

        // 输出断开连接消息
        System.out.println("player left: ");
    }


}