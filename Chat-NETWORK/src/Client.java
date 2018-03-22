import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Client extends JFrame {

    BufferedReader in;
    PrintWriter out;
    private JButton chatroomButton;
    private JScrollPane userScrollPane;
    private JScrollPane messageScrollPane;
    private JTextArea messageArea;
    private JTextField messageField;
    private JButton privateMessage;
    private JButton sendButton;
    private JTable userTable;

    private void initComponents() {

        messageField = new javax.swing.JTextField();
        messageScrollPane = new javax.swing.JScrollPane();
        userScrollPane = new javax.swing.JScrollPane();
        messageArea = new javax.swing.JTextArea();
        userTable = new javax.swing.JTable();
        sendButton = new javax.swing.JButton();
        privateMessage = new javax.swing.JButton();
        chatroomButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        messageField.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                out.println(messageField.getText());
                messageField.setText("");
            }
        });

        messageArea.setColumns(20);
        messageArea.setRows(5);
        messageScrollPane.setViewportView(messageArea);
        messageArea.setEditable(false);

        userTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {

                },
                new String [] {
                        "Online Users"
                }
        ) {
            boolean[] canEdit = new boolean [] {
                    false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });

        userScrollPane.setViewportView(userTable);

        sendButton.setText("Send");
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                out.println(messageField.getText());
                messageField.setText("");
            }
        });

        privateMessage.setText("Private Message");
        privateMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String user = (String)userTable.getModel().getValueAt(userTable.getSelectedRow(), 0);

                out.println("PRIVATE_MESSAGE " + user + " " + messageField.getText());
                messageField.setText("");

            }
        });

        chatroomButton.setText("Chatroom [S O O N]");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(messageField)
                                        .addComponent(messageScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(userScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                        .addComponent(sendButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(privateMessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(chatroomButton, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE))
                                .addContainerGap(25, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(54, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(messageScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 372, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(userScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(privateMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(chatroomButton, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(messageField)
                                        .addComponent(sendButton, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE))
                                .addGap(23, 23, 23))
        );

        pack();
    }


    public Client() {
        // Layout GUI
        initComponents();
    }

    private String getUserName() {
        return JOptionPane.showInputDialog(
                this,
                "Choose a screen name:",
                "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);
    }

    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = "127.0.0.1";
        Socket socket = new Socket(serverAddress, 49152);
        in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        String userName = null;
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();

        String[] names = null;

        // Process all messages from server, according to the protocol.
        while (true) {
            String line = in.readLine();
            if (line.startsWith("GET_NAME")) {
                userName = getUserName();
                out.println(userName);
            } else if (line.startsWith("NAME_OK")) {
                model.addRow(new String[]{userName});

            } else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");
            }

            if (line.startsWith("NAME_CLIENTS")) {
                String[] temp = line.split(" ");

                for (int i = 1; i < temp.length; i++) {
                    boolean isToAdd = true;
                    for (int j = 0; j < model.getRowCount(); j++) {
                        String compare = (String)model.getValueAt(j,0);

                        if (compare.equalsIgnoreCase(temp[i])) {
                            isToAdd = false;
                            break;
                        }

                    }

                    if (isToAdd) {
                        model.addRow(new String[] {temp[i]});
                    }
                }
            }
            if (line.startsWith("DISCONNECT")){
                String[] temp = line.split(" ");
                System.out.println("dc " + temp[1]);

                for (int j = 0; j < model.getRowCount(); j++){
                    System.out.println(model.getValueAt(j, 0));
                    String compare = (String) model.getValueAt(j, 0);

                    if (compare.equalsIgnoreCase(temp[1]))
                        model.removeRow(j);
                }
            }

            out.println("GET_NAME_CLIENTS");
        }
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.setVisible(true);
        client.run();
    }
}