module org.binhlc.lab4_chat_file_p2p {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;


    opens org.binhlc.lab4_chat_file_p2p to javafx.fxml;
    exports org.binhlc.lab4_chat_file_p2p;
}