import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionWindow extends JFrame {
    private JTextField urlField;
    private JTextField userField;
    private JPasswordField passwordField;

    public ConnectionWindow() {
        setTitle("Database Connection");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);


        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(new JLabel("Database URL:"), constraints);

        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        urlField = new JTextField("jdbc:mysql://localhost:3306/java_db");
        panel.add(urlField, constraints);


        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(new JLabel("Username:"), constraints);

        constraints.gridx = 1;
        userField = new JTextField("root");
        panel.add(userField, constraints);


        constraints.gridx = 0;
        constraints.gridy = 2;
        panel.add(new JLabel("Password:"), constraints);

        constraints.gridx = 1;
        passwordField = new JPasswordField("root");
        panel.add(passwordField, constraints);


        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.NONE;
        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String url = urlField.getText();
                String user = userField.getText();
                String password = new String(passwordField.getPassword());
                Connection connection = isValidConnection(url, user, password);
                if (connection != null) {
                    dispose();
                    openMainWindow(connection);
                } else {
                    JOptionPane.showMessageDialog(ConnectionWindow.this, "Invalid connection details");
                }
            }
        });
        panel.add(connectButton, constraints);

        add(panel);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private Connection isValidConnection(String url, String user, String password) {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, password);
            return conn;
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    private void openMainWindow(Connection connection) {
        JFrame mainWindow = new Main(connection);
        mainWindow.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConnectionWindow());
    }
}