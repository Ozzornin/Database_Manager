import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AddRecordWindow extends JDialog {

    private Connection connection;
    private String tableName;
    private DefaultTableModel tableModel;
    private Main mainWindow;

    public AddRecordWindow(Frame parent, Connection connection, String tableName) {
        super(parent, "Add Record", true);
        this.connection = connection;
        this.tableName = tableName;
        this.mainWindow = (Main) parent;


        tableModel = new DefaultTableModel(new Object[]{}, 1);


        getColumnNames();


        JTable dataTable = new JTable(tableModel);


        JScrollPane tableScrollPane = new JScrollPane(dataTable);


        JButton addButton = new JButton("Add Record");
        addButton.addActionListener(e -> addRecord());


        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);


        setLayout(new BorderLayout());
        add(tableScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);


        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void getColumnNames() {
        try {

            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, null);

            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                if (!Utils.isPrimaryKeyColumn(columnName, connection, tableName))
                    tableModel.addColumn(columnName);
            }

            columns.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addRecord() {
        try {

            StringBuilder insertQuery = new StringBuilder("INSERT INTO " + tableName + " (");

            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                String collName = tableModel.getColumnName(i);
                insertQuery.append(collName + ", ");
            }
            insertQuery.delete(insertQuery.length() - 2, insertQuery.length());
            insertQuery.append(")");
            insertQuery.append(" VALUES (");

            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                insertQuery.append("?, ");
            }


            insertQuery.delete(insertQuery.length() - 2, insertQuery.length());
            insertQuery.append(")");


            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery.toString())) {

                for (int i = 1; i <= tableModel.getColumnCount(); i++) {
                    Object cellValue = tableModel.getValueAt(0, i - 1);
                    preparedStatement.setObject(i, cellValue);
                }


                int insertedRows = preparedStatement.executeUpdate();


                if (insertedRows > 0) {

                    dispose();
                    mainWindow.displayTableData(tableName);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add the record.");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding the record: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
