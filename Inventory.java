import java.util.ArrayList;
import java.io.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Inventory {
    private ArrayList<Item> items;
    private final String FILE_NAME = "inventory.txt";

    public Inventory() {
        items = new ArrayList<>();
        loadInventory();
    }

    // Add new item (used by GUI)
    public void addItem(String name, String category, int quantity, double price) {
        items.add(new Item(name, category, quantity, price));
        saveInventory();
    }

    // Sell item, return true if successful, false if not enough or not found
    public boolean sellItem(String name, int sellQty) {
        for (Item item : items) {
            if (item.getName().equalsIgnoreCase(name)) {
                if (sellQty <= item.getQuantity()) {
                    item.setQuantity(item.getQuantity() - sellQty);
                    saveInventory();
                    return true;
                } else {
                    return false; // insufficient stock
                }
            }
        }
        return false; // not found
    }

    // Restock item, return true if found and restocked
    public boolean restockItem(String name, int addQty) {
        for (Item item : items) {
            if (item.getName().equalsIgnoreCase(name)) {
                item.setQuantity(item.getQuantity() + addQty);
                saveInventory();
                return true;
            }
        }
        return false;
    }

    // Search by name (case-insensitive). Returns the Item or null.
    public Item searchItem(String name) {
        for (Item item : items) {
            if (item.getName().equalsIgnoreCase(name)) return item;
        }
        return null;
    }

    // Return read-only list to GUI
    public ArrayList<Item> getItems() {
        return new ArrayList<>(items);
    }

    // Display by category: returns map category -> list
    public Map<String, ArrayList<Item>> getCategoryMap() {
        Map<String, ArrayList<Item>> categoryMap = new HashMap<>();
        for (Item item : items) {
            categoryMap.putIfAbsent(item.getCategory(), new ArrayList<>());
            categoryMap.get(item.getCategory()).add(item);
        }
        return categoryMap;
    }

    // Sort inventory based on choice
    // 1 = qty asc, 2 = qty desc, 3 = price asc, 4 = price desc
    public void sortInventory(int choice) {
        switch (choice) {
            case 1 -> items.sort(Comparator.comparingInt(Item::getQuantity));
            case 2 -> items.sort(Comparator.comparingInt(Item::getQuantity).reversed());
            case 3 -> items.sort(Comparator.comparingDouble(Item::getPrice));
            case 4 -> items.sort(Comparator.comparingDouble(Item::getPrice).reversed());
            default -> {}
        }
        saveInventory();
    }

    // Total inventory value
    public double calculateTotalValue() {
        double total = 0;
        for (Item item : items) total += item.getQuantity() * item.getPrice();
        return total;
    }

    // Save & Load (CSV simple format: name,category,quantity,price)
    private void saveInventory() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Item item : items) {
                pw.println(escape(item.getName()) + "," + escape(item.getCategory()) + "," + item.getQuantity() + "," + item.getPrice());
            }
        } catch (IOException e) {
            System.err.println("Error saving inventory: " + e.getMessage());
        }
    }

    private void loadInventory() {
        items.clear();
        File f = new File(FILE_NAME);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    String name = unescape(parts[0]);
                    String category = unescape(parts[1]);
                    int qty = Integer.parseInt(parts[2]);
                    double price = Double.parseDouble(parts[3]);
                    items.add(new Item(name, category, qty, price));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading inventory: " + e.getMessage());
        }
    }

    // helpers to allow commas in text fields
    private String escape(String s) {
        return s.replace("|", "||").replace(",", "|,");
    }

    private String unescape(String s) {
        return s.replace("|,", ",").replace("||", "|");
    }
}
