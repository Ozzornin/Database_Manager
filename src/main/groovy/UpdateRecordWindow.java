import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class UpdateRecordWindow extends JDialog {

    private Connection connection;
    private String tableName;
    private DefaultTableModel tableModel;
    private int selectedRow;
    private Object primaryKeyColumnValue = null;
    private String primaryKeyColumnName = null;
    private Main mainWindow;

    public UpdateRecordWindow(Frame mainWindow, Connection connection, String tableName, int selectedRow) {
        super(mainWindow, "Update Record", true);
        this.connection = connection;
        this.tableName = tableName;
        this.selectedRow = selectedRow;
        this.mainWindow = (Main) mainWindow;


        tableModel = new DefaultTableModel();
        populateTableModel();


        JTable dataTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(dataTable);
        JButton updateButton = new JButton("Update Record");
        updateButton.addActionListener(e -> updateRecord());


        JPanel buttonPanel = new JPanel();
        buttonPanel.add(updateButton);


        setLayout(new BorderLayout());
        add(tableScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);


        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(mainWindow);
        setVisible(true);
    }

    private void populateTableModel() {
        try {

            String selectQuery = "SELECT * FROM " + tableName;
            ResultSet resultSet = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(selectQuery);
            ResultSetMetaData metaData = resultSet.getMetaData();


            int columnCount = metaData.getColumnCount();
            Vector<String> columnNames = new Vector<>();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                if (!Utils.isPrimaryKeyColumn(columnName, connection, tableName))
                    columnNames.add(columnName);
                else primaryKeyColumnName = columnName;
            }


            resultSet.absolute(selectedRow + 1);
            Vector<Object> rowData = new Vector<>();


            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                if (!columnName.equals(primaryKeyColumnName)) {
                    rowData.add(resultSet.getObject(i));
                } else {
                    primaryKeyColumnValue = resultSet.getObject(i);
                }
            }


            tableModel = new DefaultTableModel(new Vector<Vector<Object>>() {{
                add(rowData);
            }}, columnNames);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateRecord() {
        try {

            StringBuilder updateQuery = new StringBuilder("UPDATE " + tableName + " SET ");


            int columnCount = tableModel.getColumnCount();


            for (int i = 0; i < columnCount; i++) {
                String columnName = tableModel.getColumnName(i);
                updateQuery.append(columnName).append(" = ?, ");
            }


            updateQuery.delete(updateQuery.length() - 2, updateQuery.length());


            updateQuery.append(" WHERE ").append(primaryKeyColumnName).append(" = ?");


            try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery.toString())) {

                for (int i = 1; i <= columnCount; i++) {
                    Object cellValue = tableModel.getValueAt(0, i - 1);
                    preparedStatement.setObject(i, cellValue);
                }


                preparedStatement.setObject(columnCount + 1, primaryKeyColumnValue);


                int updatedRows = preparedStatement.executeUpdate();


                if (updatedRows > 0) {

                    this.mainWindow.displayTableData(tableName);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update the record.");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating the record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isPrimaryKeyColumn(String columnName, Connection connection) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet primaryKeyResultSet = metaData.getPrimaryKeys(null, null, tableName);

            while (primaryKeyResultSet.next()) {
                String primaryKeyColumnName = primaryKeyResultSet.getString("COLUMN_NAME");
                if (columnName.equalsIgnoreCase(primaryKeyColumnName)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
