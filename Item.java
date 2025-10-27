public class Item {
    private String name;
    private String category;
    private int quantity;
    private double price;

    public Item(String name, String category, int quantity, double price) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters and setters
    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    // Display item info (console fallback)
    public void display() {
        System.out.printf("%-15s %-15s %-10d %-10.2f\n", name, category, quantity, price);
    }
}
