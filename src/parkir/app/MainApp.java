package parkir.app;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import parkir.dao.Database;
import parkir.exception.DataAccessException;
import parkir.ui.LoginFrame;
import parkir.ui.Tema;

/**
 * Titik masuk aplikasi desktop.
 *
 * <p>Menyiapkan tampilan, membuat skema basis data bila belum ada, lalu
 * menampilkan layar login pada Event Dispatch Thread.</p>
 */
public final class MainApp {

    private MainApp() {
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
            UIManager.put("swing.boldMetal", Boolean.FALSE);
        } catch (Exception e) {
            // Bila Metal tidak tersedia, gunakan tampilan bawaan sistem.
        }
        Tema.pasang();

        try {
            Database.inisialisasi();
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(null,
                    "Gagal menyiapkan basis data:\n" + e.getMessage(),
                    "Eparking", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
