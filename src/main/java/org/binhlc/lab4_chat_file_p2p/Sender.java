package org.binhlc.lab4_chat_file_p2p;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;

public class Sender extends Thread {
    private final String destinationIP;
    private final int destinationPort;
    private final ArrayList<String> messageList;
    private Socket client;
    private Pane root;
    @FXML
    private ScrollPane messageListView;
    private String messageToSend, sender;

    public Sender(String destinationIP, int destinationPort) {
        this.destinationIP = destinationIP;
        this.destinationPort = destinationPort;
        this.messageList = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            System.out.println("Connecting to " + destinationIP + " on port " + destinationPort);
            client = null;
            while (client == null) {
                System.out.println("Waiting for client");
                try {
                    client = new Socket(destinationIP, destinationPort);


                } catch (IOException e) {
                    System.out.println("Port Not Found. Retrying....");
                    Thread.sleep(1000);
                }
            }
            System.out.println("From Client : Just connected to " + client.getRemoteSocketAddress());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) throws IOException {
        this.messageToSend = message;
        if (messageToSend.length() > 50) {
            int len = messageToSend.length();
            String partA = "", partB = "";
            for (int i = 0; i < len; i += 50) {
                if (i > 0) {
                    partA = messageToSend.substring(0, i);
                    partB = messageToSend.substring(i + 1, len);
                    messageToSend = partA + "\n" + partB;
                }
            }
        }
        messageList.add(messageToSend);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("isFile", false);
        jsonObject.put("isColor", false);
        jsonObject.put("message", messageToSend);
        jsonObject.put("sender", sender);
        OutputStream outToServer = client.getOutputStream();
        DataOutputStream out = new DataOutputStream(outToServer);
        out.writeUTF(jsonObject.toString());
        //out.writeBoolean(false);

        Platform.runLater(new Runnable() {

            @Override
            public void run() {

                Label messageLabel = new Label(messageToSend);
                Label senderLabel = new Label("Bạn");
                ChatController.allMessages.add(sender + " : " + messageToSend);
                senderLabel.setFont(new Font(10));
                senderLabel.setStyle("-fx-padding:2;-fx-background-color:#2c3e50;");
                senderLabel.setTextFill(Color.WHITE);
                senderLabel.setVisible(false);

                messageLabel.setOnMouseEntered(new EventHandler<Event>() {

                    @Override
                    public void handle(Event event) {
                        senderLabel.setVisible(true);
                        messageLabel.setOpacity(0.9);
                    }
                });

                messageLabel.setOnMouseExited(new EventHandler<Event>() {

                    @Override
                    public void handle(Event event) {
                        senderLabel.setVisible(false);
                        messageLabel.setOpacity(1.0);
                    }
                });
                messageLabel.setCursor(Cursor.HAND);
                messageLabel.setFont(new Font(15));
                messageLabel.setStyle("-fx-background-color:#ecf0f1;-fx-padding:10;-fx-background-radius:8;");
                messageLabel.setTextFill(Color.BLACK);
                VBox messageInfo = new VBox(messageLabel, senderLabel);
                BorderPane borderPane = new BorderPane();
                borderPane.setRight(messageInfo);
                Receiver.vbox.getChildren().add(borderPane);
                messageListView.setContent(Receiver.vbox);
            }
        });


    }

    public void sendFile(byte[] byteArray, File file) throws IOException {
        //this.messageToSend = message;
        System.out.println("Sending file....");
//
        FileInputStream fileInputStream = new FileInputStream(file);
        int i;
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        bufferedInputStream.read(byteArray, 0, byteArray.length);
        String fileName = file.getName();
        OutputStream outToServer = client.getOutputStream();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("isFile", true);
        jsonObject.put("isColor", false);
        jsonObject.put("name", fileName);
        jsonObject.put("sender", sender);
        jsonObject.put("fileStream", Base64.getEncoder().encodeToString(byteArray));
        DataOutputStream out = new DataOutputStream(outToServer);
        out.writeUTF(jsonObject.toString());
        out.flush();
        Platform.runLater(new Runnable() {

            @Override
            public void run() {

                Label messageLabel = new Label(fileName + " is sent");
                messageLabel.setFont(new Font(15));
                messageLabel.setStyle("-fx-background-color:#2ecc71;-fx-padding:10;-fx-background-radius:8;");
                messageLabel.setTextFill(Color.WHITE);
                Label senderLabel = new Label("You");
                senderLabel.setFont(new Font(10));
                senderLabel.setStyle("-fx-padding:2;-fx-background-color:#2c3e50;");
                senderLabel.setTextFill(Color.WHITE);
                senderLabel.setVisible(false);

                messageLabel.setOnMouseEntered(new EventHandler<Event>() {

                    @Override
                    public void handle(Event event) {
                        senderLabel.setVisible(true);
                        messageLabel.setOpacity(0.9);
                    }
                });

                messageLabel.setOnMouseExited(new EventHandler<Event>() {

                    @Override
                    public void handle(Event event) {
                        senderLabel.setVisible(false);
                        messageLabel.setOpacity(1.0);
                    }
                });
                VBox messageInfo = new VBox(messageLabel, senderLabel);
                BorderPane borderPane = new BorderPane();
                borderPane.setRight(messageInfo);
                Receiver.vbox.getChildren().add(borderPane);
                messageListView.setContent(Receiver.vbox);

            }
        });
    }


    public int getSenderPort() {
        if (client != null)
            return client.getPort();
        return -1;
    }

    public ArrayList<String> getMessageList() {
        return messageList;
    }

    public void setRoot(Pane root) {
        this.root = root;
        messageListView = (ScrollPane) root.lookup("#messageList");
        messageListView.setFitToWidth(true);
        messageListView.setStyle("-fx-control-inner-background: black;");
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
