package org.example.demo;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import static org.example.demo.Game.SetupBoard;

public class ApplicationLauncher extends javafx.application.Application {

    @Override
    public void start(Stage stage) throws IOException {
        launchApp(stage);
    }

    public static void launchApp(Stage stage) throws IOException {

        try  {

            Socket socket = new Socket("localhost", 12345);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            String waitMessage = (String) in.readObject();
            System.out.println("server: " + waitMessage);



            int player;
            Scanner scanner = new Scanner(System.in);

            String playerInfo = (String) in.readObject();
            System.out.println(playerInfo);
            if (playerInfo.split(" ")[3].equals("1.")){
                String size_input = scanner.nextLine();
                out.writeObject(size_input);
                out.flush();
                player = 1;
            }else {
                player = 2;
            }

            GameState gameState = (GameState)in.readObject();

            Controller.game = new Game(gameState.getBoard());
            FXMLLoader fxmlLoader = new FXMLLoader(ApplicationLauncher.class.getResource("board.fxml"));
            VBox root = fxmlLoader.load();
            Controller controller = fxmlLoader.getController();
            controller.createGameBoard();

            Scene scene = new Scene(root);
            stage.setTitle("连连看游戏player" + player);
            stage.setScene(scene);
            stage.show();

            System.out.println("okkk");

            new Thread(() -> {
                try {
                    while (true) {
                        int[] record;
                        if (player == 1) {

                            controller.enableAllButtons();
                            System.out.println("start your round");
                            record = controller.oneRound();
                            int flag = record[0];

                            System.out.println("you delete:");
                            for (int i = 0; i < record.length; i++) {
                                System.out.print(record[i] + " ");
                            }
                            if (flag != 0) {
                                controller.showDialogMessage_2s("You deleted (" + record[1] + "," + record[2] + "),(" + record[2 * flag + 1] + "," + record[2 * flag + 2] + ") successfully");
                            }else {
                                controller.showDialogMessage_2s("error match!");

                            }
                            for (int x = 0; x < 9; x++) {
                                out.writeInt(record[x]);
                                out.flush();
                            }

                            System.out.println("end your round");
                            controller.disableAllButtons();

                            int state = in.readInt();
                            System.out.println("state:"+state);

                            if (state == 1) {
                                controller.showDialogMessage_2s("player1 win the game!");
                                controller.disableAllButtons();
                                break;
                            }else if (state == 2) {
                                controller.showDialogMessage_2s("player2 win the game!");
                                controller.disableAllButtons();
                                break;
                            }else if (state == 3) {
                                controller.showDialogMessage_2s("a tie!");
                                controller.disableAllButtons();
                                break;
                            }

                            System.out.println("waiting the server");
                            int[] arr = (int[]) in.readObject();
                            flag = arr[0];
                            System.out.println("updating------");
                            if (flag != 0) {
                                controller.showDialogMessage_2s("Your opponent deleted (" + arr[1] + "," + arr[2] + "),(" + arr[2*flag+1] + "," + arr[2*flag+2] + ") successfully");
                                controller.update(arr);
                            }
                            else {
                                controller.showDialogMessage_2s("your opponent has a error match!");
                            }

                            state = in.readInt();
                            System.out.println("state:"+state);

                            if (state == 1) {
                                controller.showDialogMessage_2s("player1 win the game!");
                                controller.disableAllButtons();
                                break;
                            }else if (state == 2) {
                                controller.showDialogMessage_2s("player2 win the game!");
                                controller.disableAllButtons();
                                break;
                            }else if (state == 3) {
                                System.out.println("lose...");
                                controller.showDialogMessage_2s("a tie!");
                                controller.disableAllButtons();
                                break;
                            }

                            System.out.println("updating successful ------");

                        } else {
                            controller.disableAllButtons();
                            int flag;
                            int[] arr = (int[]) in.readObject();
                            flag = arr[0];
                            System.out.println("updating------");
                            if (flag != 0) {
                                controller.showDialogMessage_2s("Your opponent deleted (" + arr[1] + "," + arr[2] + "),(" + arr[2*flag+1] + "," + arr[2*flag+2] + ") successfully");
                                controller.update(arr);
                            }
                            else {
                                controller.showDialogMessage_2s("your opponent has a error match!");
                            }

                            int state = in.readInt();
                            System.out.println("state:"+state);

                            if (state == 1) {
                                controller.showDialogMessage_2s("player1 win the game!");
                                controller.disableAllButtons();
                                break;
                            }else if (state == 2) {
                                controller.showDialogMessage_2s("player2 win the game!");
                                controller.disableAllButtons();
                                break;
                            }else if (state == 3) {
                                controller.showDialogMessage_2s("a tie!");
                                controller.disableAllButtons();
                                break;
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
                            }else {
                                controller.showDialogMessage_2s("error match!");

                            }
                            for (int x = 0; x < 9; x++) {
                                out.writeInt(record[x]);
                                out.flush();
                            }

                            state = in.readInt();
                            System.out.println("state:"+state);
                            if (state == 1) {
                                controller.showDialogMessage_2s("player1 win the game!");
                                controller.disableAllButtons();
                                break;
                            }else if (state == 2) {
                                controller.showDialogMessage_2s("player2 win the game!");
                                controller.disableAllButtons();
                                break;
                            }else if (state == 3) {
                                controller.showDialogMessage_2s("a tie!");
                                controller.disableAllButtons();
                                break;
                            }

                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static int[] getBoardSizeFromUser() {
        return new int[]{9, 6};  // 默认棋盘大小
    }
}
