import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Use system look & feel for nicer native-ish look if available
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new InventoryGUI());
    }
}
