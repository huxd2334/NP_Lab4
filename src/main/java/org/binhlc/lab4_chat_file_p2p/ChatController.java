package org.binhlc.lab4_chat_file_p2p;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ChatController extends Thread {
    // Define the content_Types variable
    private static final String[] content_Types = {"text/plain", "text/html", "application/json"};
    public static ArrayList<String> allMessages;
    @FXML
    public Label message, connectedToLabel, listeningAtLabel;
    private Main main;
    private Pane root;
    private FXMLLoader fxmlLoader;
    private volatile boolean running = true; // Flag to control thread execution
    @FXML
    private Button submitButton, resetButton;
    @FXML
    private TextField ipAddrField, portField, clientPortField, userNameField;
    private String ipAddressToConnect, portToConnect, portToListen, userName;
    @FXML
    private Label portNoToListenEmptyMessage, portNoEmptyMessage, ipaddrEmptyMessage, userNameEmptyMessage, chatUserNameLabel;
    @FXML
    private TextArea messageSendBox;
    private boolean isValid;
    private Receiver messageReceiver;
    private Sender messageSender;
    private Thread connectionChecker;
    @FXML
    private Button messageSendButton, fileAttachButton;
    @FXML
    private ColorPicker themeChangeButton;
    private FileChooser fileChooser;
    private Stage stage;
    private File file;
    private ScrollPane scrollPane;
    private byte[] byteArray;
    private ArrayList<String> sentMessages, receivedMessages;

    public ChatController() {
    }

    public ChatController(Main main) throws IOException {
        this.main = main;
        root = main.getRoot();
        submitButton = (Button) root.lookup("#submitButton");
        resetButton = (Button) root.lookup("#resetButton");
        ipAddrField = (TextField) root.lookup("#ipAddrField");
        portField = (TextField) root.lookup("#portField");
        clientPortField = (TextField) root.lookup("#clientPortField");
        ipaddrEmptyMessage = (Label) root.lookup("#ipaddrEmptyMessage");
        portNoToListenEmptyMessage = (Label) root.lookup("#portNoToListenEmptyMessage");
        portNoEmptyMessage = (Label) root.lookup("#portNoEmptyMessage");
        message = (Label) root.lookup("#message");
        userNameField = (TextField) root.lookup("#userNameField");
        userNameEmptyMessage = (Label) root.lookup("#userNameEmptyMessage");
        System.out.println(portField);
        ipaddrEmptyMessage.setVisible(false);
        portNoEmptyMessage.setVisible(false);
        portNoToListenEmptyMessage.setVisible(false);
        userNameEmptyMessage.setVisible(false);
        setEventHandlers();
        allMessages = new ArrayList<String>();
        connectionChecker = new Thread(this, "Connection Checker");
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " started....");
        while (running && (!messageReceiver.isConnected() || messageReceiver.getReceiverPort() != -1 || messageSender.getSenderPort() != -1)) {
            System.out.println("Cannot connect. Please check the connection information.");
            try {
                Thread.sleep(1000);
                if (messageReceiver.isConnected() && messageReceiver.getReceiverPort() != -1 && messageSender.getSenderPort() != -1) {
                    System.out.println("Connected: " + messageReceiver.getReceiverPort());
                    System.out.println("Connected to the server. Please enter a message.");
                    showChat();
                    messageReceiver.setRoot(root);
                    messageSender.setRoot(root);
                    messageSender.setSender(userName);
                    messageReceiver.setStage(stage);
                    running = false; // Stop the thread loop
                    System.out.println("Thread Closed");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore the interrupted status
                System.out.println("Thread was interrupted.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stopConnectionChecker() {
        running = false;
    }

    private void showChat() throws IOException {
        fxmlLoader = new FXMLLoader(getClass().getResource("chat-view.fxml"));
        root = fxmlLoader.load();
        if (root == null)
            System.out.println("Root is null");
        System.out.println("Root: " + this.root);
        System.out.println(main);
        if (main != null)
            main.setRoot(this.root);
        initChatScreenNodes();
    }

    private void initChatScreenNodes() {
        listeningAtLabel = (Label) root.lookup("#listeningAtLabel");
        connectedToLabel = (Label) root.lookup("#connectedToLabel");
        messageSendButton = (Button) root.lookup("#messageSendButton");
        messageSendBox = (TextArea) root.lookup("#messageSendBox");
        connectedToLabel.setText("Port kết nối: " + messageSender.getSenderPort());
        listeningAtLabel.setText("Port nhận tin:" + messageReceiver.getReceiverPort());
        fileAttachButton = (Button) root.lookup("#fileAttachButton");
        scrollPane = (ScrollPane) root.lookup("#messageList");
        scrollPane.setStyle("-fx-background:transparent;");
        chatUserNameLabel = (Label) root.lookup("#chatUserNameLabel");
        chatUserNameLabel.setText("Chào " + userName);
        messageSendButton.setOnMouseClicked(new EventHandler<Event>() {
            @Override
            public void handle(Event arg0) {
                String message = messageSendBox.getText();
                try {
                    if (message.length() > 0) {
                        messageSender.sendMessage(message);
                    }
                    if (file != null) {
                        messageSender.sendFile(byteArray, file);
                        file = null;
                    }
                    messageSendBox.setText("");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        fileAttachButton.setOnMouseClicked(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                fileChooser = new FileChooser();
                file = fileChooser.showOpenDialog(stage);
                if (file != null) {
                    String fileName = file.getName();
                    byteArray = new byte[(int) file.length()];

                }
            }
        });


    }


    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void setEventHandlers() {
        submitButton.setOnMouseClicked(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                try {
                    ipAddressToConnect = ipAddrField.getText();
                    portToConnect = portField.getText();
                    portToListen = clientPortField.getText();
                    userName = userNameField.getText();
                    validateForm();
                    if (isValid) {
                        System.out.println("Connecting...");
                        submitButton.setOpacity(0.7);
                        submitButton.setText("Đang kết nối...");
                        messageReceiver = new Receiver(Integer.parseInt(portToListen));
                        messageReceiver.start();
                        messageSender = new Sender(ipAddressToConnect, Integer.parseInt(portToConnect));
                        messageSender.start();
                        connectionChecker.start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        resetButton.setOnMouseClicked(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                try {
                    System.out.println("Reset");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void validateForm() {
        isValid = true;
        if (ipAddressToConnect.isEmpty()) {
            ipaddrEmptyMessage.setVisible(true);
            isValid = false;
        } else {
            ipaddrEmptyMessage.setVisible(false);
        }
        if (portToConnect.isEmpty()) {
            portNoEmptyMessage.setVisible(true);
            isValid = false;
        } else {
            portNoEmptyMessage.setVisible(false);
        }
        if (portToListen.isEmpty()) {
            portNoToListenEmptyMessage.setVisible(true);
            isValid = false;
        } else {
            portNoToListenEmptyMessage.setVisible(false);
        }
        if (userName.isEmpty()) {
            userNameEmptyMessage.setVisible(true);
            isValid = false;
        } else {
            userNameEmptyMessage.setVisible(false);
        }
        System.out.println(isValid);
    }
}
