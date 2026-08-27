import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Main {

    private static JTable table;
    private static JComboBox<String> tableCombo;

    public static void main(String[] args) {

        // ===== LOOK & FEEL =====
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        JFrame frame = new JFrame("Travel Agency ");
        frame.setSize(1100, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // ===== TOP PANEL =====
        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableCombo = new JComboBox<>();
        loadTableNames();

        JButton loadBtn = new JButton("Load");
        JButton insertBtn = new JButton("Insert Customer");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton tripInfoBtn = new JButton("Trip Info");

        loadBtn.setBackground(new Color(52, 152, 219));
        insertBtn.setBackground(new Color(46, 204, 113));
        deleteBtn.setBackground(new Color(231, 76, 60));
        tripInfoBtn.setBackground(new Color(155, 89, 182));

        loadBtn.setForeground(Color.WHITE);
        insertBtn.setForeground(Color.WHITE);
        deleteBtn.setForeground(Color.WHITE);
        tripInfoBtn.setForeground(Color.WHITE);

        topPanel.add(new JLabel("Table:"));
        topPanel.add(tableCombo);
        topPanel.add(loadBtn);
        topPanel.add(insertBtn);
        topPanel.add(deleteBtn);
        topPanel.add(tripInfoBtn);

        // ===== TITLE =====
        JLabel title = new JLabel("Database Table View");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== TABLE =====
        table = new JTable();
        table.setRowHeight(24);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 14));

        table.setDefaultRenderer(Object.class,
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table, Object value, boolean isSelected,
                            boolean hasFocus, int row, int column) {

                        Component c = super.getTableCellRendererComponent(
                                table, value, isSelected, hasFocus, row, column);

                        if (!isSelected) {
                            c.setBackground(row % 2 == 0
                                    ? new Color(245, 245, 245)
                                    : Color.WHITE);
                        }
                        return c;
                    }
                });

        DefaultTableModel empty = new DefaultTableModel();
        empty.addColumn("Info");
        empty.addRow(new Object[]{"Select a table and press Load"});
        table.setModel(empty);

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(title, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);

        // ===== ACTIONS =====
        loadBtn.addActionListener(e -> loadTable());
        insertBtn.addActionListener(e -> insertCustomer());
        deleteBtn.addActionListener(e -> deleteSelected());
        tripInfoBtn.addActionListener(e -> showTripInfo());

        frame.setVisible(true);
    }

    // ================= LOAD TABLE =================
    private static void loadTable() {
        String tableName = (String) tableCombo.getSelectedItem();

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM " + tableName)) {

            ResultSetMetaData meta = rs.getMetaData();
            DefaultTableModel model = new DefaultTableModel();

            for (int i = 1; i <= meta.getColumnCount(); i++)
                model.addColumn(meta.getColumnName(i));

            while (rs.next()) {
                Object[] row = new Object[meta.getColumnCount()];
                for (int i = 1; i <= meta.getColumnCount(); i++)
                    row[i - 1] = rs.getObject(i);
                model.addRow(row);
            }

            table.setModel(model);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    // ================= INSERT CUSTOMER =================
    private static void insertCustomer() {

        JTextField id = new JTextField();
        JTextField name = new JTextField();
        JTextField lname = new JTextField();
        JTextField email = new JTextField();
        JTextField phone = new JTextField();
        JTextField address = new JTextField();
        JTextField birth = new JTextField();

        Object[] fields = {
                "ID:", id,
                "Name:", name,
                "Last Name:", lname,
                "Email:", email,
                "Phone:", phone,
                "Address:", address,
                "Birth Date (YYYY-MM-DD):", birth
        };

        if (JOptionPane.showConfirmDialog(null, fields,
                "Insert Customer",
                JOptionPane.OK_CANCEL_OPTION)
                != JOptionPane.OK_OPTION) return;

        if (id.getText().isEmpty() || name.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "ID and Name are required");
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO customer VALUES (?,?,?,?,?,?,?)")) {

            ps.setInt(1, Integer.parseInt(id.getText()));
            ps.setString(2, name.getText());
            ps.setString(3, lname.getText());
            ps.setString(4, email.getText());
            ps.setString(5, phone.getText());
            ps.setString(6, address.getText());
            ps.setDate(7, Date.valueOf(birth.getText()));

            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Customer inserted");
            loadTable();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    // ================= DELETE =================
    private static void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        String tableName = (String) tableCombo.getSelectedItem();
        Object id = table.getValueAt(row, 0);

        if (JOptionPane.showConfirmDialog(null,
                "Delete selected record?",
                "Confirm",
                JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {

            st.executeUpdate(
                    "DELETE FROM " + tableName +
                    " WHERE " + table.getColumnName(0) + "=" + id);

            loadTable();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    // ================= TRIP INFO =================
    private static void showTripInfo() {

        String trId = JOptionPane.showInputDialog("Trip ID:");
        if (trId == null) return;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT tr_id, tr_departure, tr_return, tr_status " +
                     "FROM trip WHERE tr_id=?")) {

            ps.setInt(1, Integer.parseInt(trId));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(null,
                        "Trip ID: " + rs.getInt(1) +
                        "\nDeparture: " + rs.getDate(2) +
                        "\nReturn: " + rs.getDate(3) +
                        "\nStatus: " + rs.getString(4));
            } else {
                JOptionPane.showMessageDialog(null, "Trip not found");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    // ================= LOAD TABLE NAMES =================
    private static void loadTableNames() {

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT table_name FROM information_schema.tables " +
                     "WHERE table_schema = DATABASE()");
             ResultSet rs = ps.executeQuery()) {

            tableCombo.removeAllItems();
            while (rs.next())
                tableCombo.addItem(rs.getString(1));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Error loading table names:\n" + ex.getMessage());
        }
    }
}
