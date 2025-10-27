import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

public class InventoryGUI extends JFrame {
    private Inventory inventory;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel totalValueLabel;

    // Fun pastel colors
    private final Color PINK = new Color(255, 182, 193);
    private final Color LILAC = new Color(200, 162, 200);
    private final Color MINT = new Color(189, 252, 201);
    private final Color PEACH = new Color(255, 218, 185);
    private final Color PALE_BLUE = new Color(200, 225, 255);
    private final Color LOW_STOCK = new Color(255, 200, 200);

    public InventoryGUI() {
        inventory = new Inventory();
        setTitle("🌈 Maison Inventory — Inventory Stock Monitoring");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Left panel with colorful buttons
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(8, 1, 8, 8));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        leftPanel.setBackground(PALE_BLUE);

        JButton addBtn = styledButton("➕ Add Item", PINK);
        JButton sellBtn = styledButton("💸 Sell Item", LILAC);
        JButton restockBtn = styledButton("🔁 Restock", MINT);
        JButton searchBtn = styledButton("🔍 Search Item", PEACH);
        JButton categoryBtn = styledButton("📂 Show by Category", PINK);
        JButton sortBtn = styledButton("↕️ Sort Inventory", LILAC);
        JButton refreshBtn = styledButton("🔄 Refresh", MINT);
        JButton exitBtn = styledButton("🚪 Exit", PEACH);

        leftPanel.add(addBtn);
        leftPanel.add(sellBtn);
        leftPanel.add(restockBtn);
        leftPanel.add(searchBtn);
        leftPanel.add(categoryBtn);
        leftPanel.add(sortBtn);
        leftPanel.add(refreshBtn);
        leftPanel.add(exitBtn);

        // Right panel with table
        String[] columns = {"Item Name", "Category", "Quantity", "Price", "Value"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(32); // bigger rows
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        // total value label
        totalValueLabel = new JLabel("Total Value: ₹0.00");
        totalValueLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        totalValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        totalValueLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 20));

        add(leftPanel, BorderLayout.WEST);
        add(tableScroll, BorderLayout.CENTER);
        add(totalValueLabel, BorderLayout.SOUTH);

        // Button actions
        addBtn.addActionListener(_ -> openAddDialog());
        sellBtn.addActionListener(_ -> openSellDialog());
        restockBtn.addActionListener(_ -> openRestockDialog());
        searchBtn.addActionListener(_ -> openSearchDialog());
        categoryBtn.addActionListener(_ -> showByCategory());
        sortBtn.addActionListener(_ -> openSortDialog());
        refreshBtn.addActionListener(_ -> refreshTable());
        exitBtn.addActionListener(_ -> System.exit(0));

        // Table renderer for low stock
        table.setDefaultRenderer(Object.class, new TableCellRenderer() {
            private final JLabel lbl = new JLabel();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                lbl.setOpaque(true);
                lbl.setText(value == null ? "" : value.toString());
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

                int qty = 0;
                try {
                    qty = Integer.parseInt(table.getValueAt(row, 2).toString());
                } catch (Exception ignored) {}
                lbl.setBackground(qty < 5 ? LOW_STOCK : Color.white);
                if (isSelected) lbl.setBackground(lbl.getBackground().darker());
                return lbl;
            }
        });

        refreshTable();
        setVisible(true);
    }

    private JButton styledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(bg);
        b.setFont(new Font("SansSerif", Font.BOLD, 16));
        b.setBorder(BorderFactory.createLineBorder(Color.gray, 1, true));
        return b;
    }

    // Add item dialog
    private void openAddDialog() {
        JPanel p = new JPanel(new GridLayout(0, 1, 6, 6));
        JTextField nameF = new JTextField();
        JTextField categoryF = new JTextField();
        JTextField qtyF = new JTextField();
        JTextField priceF = new JTextField();

        p.add(new JLabel("Item name:"));
        p.add(nameF);
        p.add(new JLabel("Category:"));
        p.add(categoryF);
        p.add(new JLabel("Quantity:"));
        p.add(qtyF);
        p.add(new JLabel("Price (e.g., 99.50):"));
        p.add(priceF);

        int option = JOptionPane.showConfirmDialog(this, p, "Add New Item", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String name = nameF.getText().trim();
                String category = categoryF.getText().trim();
                int qty = Integer.parseInt(qtyF.getText().trim());
                double price = Double.parseDouble(priceF.getText().trim());
                if (name.isEmpty() || category.isEmpty() || qty < 0 || price < 0) throw new Exception();
                inventory.addItem(name, category, qty, price);
                JOptionPane.showMessageDialog(this, "🎉 Item added!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input — check fields and try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Sell dialog with low-stock alert
    private void openSellDialog() {
        String name = JOptionPane.showInputDialog(this, "Enter item name to sell:");
        if (name == null || name.trim().isEmpty()) return;
        String qtyStr = JOptionPane.showInputDialog(this, "Enter quantity to sell:");
        if (qtyStr == null) return;
        try {
            int qty = Integer.parseInt(qtyStr.trim());
            Item item = inventory.searchItem(name.trim());
            if (item == null) {
                JOptionPane.showMessageDialog(this, "Item not found!", "Not found", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (qty <= 0) throw new Exception();
            boolean success = inventory.sellItem(name.trim(), qty);
            if (success) {
                String msg = "Sold " + qty + " units of " + item.getName() + ".";
                if (inventory.searchItem(name.trim()).getQuantity() < 5) {
                    msg += "\n⚠️ Low stock!";
                    JOptionPane.showMessageDialog(this,
                            "⚠️ Low Stock Alert!\n" + item.getName() + " has only " + inventory.searchItem(name.trim()).getQuantity() + " left!",
                            "Low Stock Warning",
                            JOptionPane.WARNING_MESSAGE);
                }
                JOptionPane.showMessageDialog(this, msg, "Sold", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Insufficient stock!", "Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Restock dialog with low-stock alert
    private void openRestockDialog() {
        String name = JOptionPane.showInputDialog(this, "Enter item name to restock:");
        if (name == null || name.trim().isEmpty()) return;
        String qtyStr = JOptionPane.showInputDialog(this, "Enter quantity to add:");
        if (qtyStr == null) return;
        try {
            int qty = Integer.parseInt(qtyStr.trim());
            if (qty <= 0) throw new Exception();
            boolean success = inventory.restockItem(name.trim(), qty);
            if (success) {
                Item item = inventory.searchItem(name.trim());
                JOptionPane.showMessageDialog(this, "Restocked successfully!", "Restocked", JOptionPane.INFORMATION_MESSAGE);
                if (item.getQuantity() < 5) {
                    JOptionPane.showMessageDialog(this,
                            "⚠️ Low Stock Alert!\n" + item.getName() + " has only " + item.getQuantity() + " left!",
                            "Low Stock Warning",
                            JOptionPane.WARNING_MESSAGE);
                }
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Item not found!", "Not found", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Search dialog
    private void openSearchDialog() {
        String name = JOptionPane.showInputDialog(this, "Enter item name to search:");
        if (name == null || name.trim().isEmpty()) return;
        Item item = inventory.searchItem(name.trim());
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Item not found!", "Not found", JOptionPane.WARNING_MESSAGE);
        } else {
            String info = String.format("Name: %s\nCategory: %s\nQuantity: %d\nPrice: ₹%.2f\nValue: ₹%.2f",
                    item.getName(), item.getCategory(), item.getQuantity(), item.getPrice(), item.getQuantity() * item.getPrice());
            JOptionPane.showMessageDialog(this, info, "Item Found", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Show by category with combined low-stock alert
    private void showByCategory() {
        Map<String, ArrayList<Item>> map = inventory.getCategoryMap();
        if (map.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inventory is empty.", "Empty", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Low-stock alert
        StringBuilder lowStockAlert = new StringBuilder();
        for (Item it : inventory.getItems()) {
            if (it.getQuantity() < 5) {
                lowStockAlert.append("⚠️ ").append(it.getName()).append(" — only ").append(it.getQuantity()).append(" left!\n");
            }
        }
        if (lowStockAlert.length() > 0) {
            JOptionPane.showMessageDialog(this, lowStockAlert.toString(), "Low Stock Alert", JOptionPane.WARNING_MESSAGE);
        }

        // Display by category
        StringBuilder sb = new StringBuilder();
        for (String cat : map.keySet()) {
            sb.append("↳ ").append(cat).append(":\n");
            for (Item it : map.get(cat)) {
                sb.append("   - ").append(it.getName())
                        .append(" | qty: ").append(it.getQuantity())
                        .append(" | price: ₹").append(String.format("%.2f", it.getPrice())).append("\n");
            }
            sb.append("\n");
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setFont(new Font("Monospaced", Font.BOLD, 14));
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Inventory by Category", JOptionPane.INFORMATION_MESSAGE);
    }

    // Sort dialog
    private void openSortDialog() {
        String[] options = {"Quantity (asc)", "Quantity (desc)", "Price (low→high)", "Price (high→low)"};
        int choice = JOptionPane.showOptionDialog(this, "Choose sort method:", "Sort Inventory",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice >= 0 && choice <= 3) {
            inventory.sortInventory(choice + 1);
            refreshTable();
            JOptionPane.showMessageDialog(this, "Sorted: " + options[choice], "Sorted", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Refresh table
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Item it : inventory.getItems()) {
            double value = it.getQuantity() * it.getPrice();
            tableModel.addRow(new Object[]{it.getName(), it.getCategory(), it.getQuantity(), String.format("₹%.2f", it.getPrice()), String.format("₹%.2f", value)});
        }
        totalValueLabel.setText("Total Inventory Value: ₹" + String.format("%.2f", inventory.calculateTotalValue()));
    }
}
