import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Main extends JFrame {

    private DatabaseMetaData metaData;
    private JList<String> tableList;
    private JTable dataTable;
    private JScrollPane tableScrollPane;
    private Connection connection;

    public Main(Connection connection) {
        this.connection = connection;
        setTitle("Database GUI");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        tableList = new JList<>();

        tableList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedTable = tableList.getSelectedValue();
                if (selectedTable != null) {
                    displayTableData(selectedTable);
                }
            }
        });

        dataTable = new JTable(new DefaultTableModel());

        tableScrollPane = new JScrollPane(dataTable);
        getContentPane().setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Tables:"), BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(tableList), BorderLayout.CENTER);

        getContentPane().add(leftPanel, BorderLayout.WEST);
        getContentPane().add(tableScrollPane, BorderLayout.CENTER);

        populateTableList();
        addCrudButtons();
        setVisible(true);
    }

    private void populateTableList() {
        try {
            String dbName = connection.getCatalog();
            if (dbName != null) {
                List<String> tableNames = getTableNames(connection, dbName);
                tableList.setListData(tableNames.toArray(new String[0]));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(Main.this, "Cannot get database info");
            e.printStackTrace();
        }
    }

    private List<String> getTableNames(Connection connection, String dbName) {
        List<String> tableNames = new ArrayList<>();
        try {
            metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(dbName, null, "%", new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                tableNames.add(tableName);
            }
            tables.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(Main.this, "Cannot get database info");
            e.printStackTrace();
        }

        return tableNames;
    }

    public void displayTableData(String tableName) {
        try {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM " + tableName);
            ResultSetMetaData metaData = resultSet.getMetaData();

            int columnCount = metaData.getColumnCount();
            Vector<String> columnNames = new Vector<>();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }

            Vector<Vector<Object>> data = new Vector<>();
            while (resultSet.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(resultSet.getObject(i));
                }
                data.add(row);
            }

            DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
            dataTable.setModel(tableModel);
            tableScrollPane.setViewportView(dataTable);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(Main.this, "Error retrieving data from the table");
            e.printStackTrace();
        }
    }

    private void addCrudButtons() {
        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");

        addButton.addActionListener(e -> addRecord());
        updateButton.addActionListener(e -> updateRecord());
        deleteButton.addActionListener(e -> deleteRecord());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addRecord() {
        String tableName = tableList.getSelectedValue();
        if (tableName != null) {
            AddRecordWindow addRecordWindow = new AddRecordWindow(Main.this, connection, tableName);
        } else {
            JOptionPane.showMessageDialog(Main.this, "Please select a table.");
        }
    }

    private void updateRecord() {
        String tableName = tableList.getSelectedValue();
        int selectedRow = dataTable.getSelectedRow();

        if (tableName != null && selectedRow != -1) {
            new UpdateRecordWindow(this, connection, tableName, selectedRow);
        } else {
            JOptionPane.showMessageDialog(Main.this, "Please select a table and a record to update.");
        }
    }

    private void deleteRecord() {
        String tableName = tableList.getSelectedValue();
        int selectedRow = dataTable.getSelectedRow();

        if (tableName != null && selectedRow != -1) {
            int option = JOptionPane.showConfirmDialog(Main.this,
                    "Are you sure you want to delete this record?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                try {
                    String primaryKeyColumnName = dataTable.getModel().getColumnName(0);
                    Object primaryKeyValue = dataTable.getValueAt(selectedRow, 0);

                    if (primaryKeyValue != null) {
                        String deleteQuery = "DELETE FROM " + tableName + " WHERE " + primaryKeyColumnName + " = ?";

                        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
                            preparedStatement.setObject(1, primaryKeyValue);
                            int deletedRows = preparedStatement.executeUpdate();

                            if (deletedRows > 0) {
                                displayTableData(tableName);
                            } else {
                                JOptionPane.showMessageDialog(Main.this, "Failed to delete the record.");
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(Main.this, "Cannot determine the primary key value.");
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(Main.this, "Error deleting the record: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            JOptionPane.showMessageDialog(Main.this, "Please select a table and a record to delete.");
        }
    }
}
