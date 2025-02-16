package org.binhlc.lab4_chat_file_p2p;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {
    private Pane root;
    private Parent chatView;
    private Scene scene;
    private ChatController chatController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            root = FXMLLoader.load(getClass().getResource("hello-view.fxml"));
            if (root == null) {
                System.out.println("Root is null");
            }
            System.out.println(root.lookup("#anotherButton"));
            chatView = FXMLLoader.load(getClass().getResource("chat-view.fxml"));
            scene = new Scene(root, 600, 650);
            stage.setScene(scene);
            stage.setTitle("Welcome");
            stage.setResizable(false);
            stage.show();
            chatController = new ChatController(this);
            chatController.setStage(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Pane getRoot() {
        return root;
    }

    public void setRoot(Pane root) {
        this.root = root;
        scene.setRoot(root);
    }
}
