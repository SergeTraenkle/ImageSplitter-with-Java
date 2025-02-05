package PNG_Dateien_halbieren;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageSplitter {
    public static void main(String[] args) {
        // Look and Feel für bessere Darstellung
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Erstelle Hauptfenster
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Wähle Eingabeordner
        JFileChooser inputChooser = new JFileChooser();
        inputChooser.setDialogTitle("Wählen Sie den Ordner mit den PNG-Dateien");
        inputChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (inputChooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(frame, "Kein Eingabeordner ausgewählt. Programm wird beendet.");
            System.exit(0);
        }

        // Wähle Ausgabeordner für linke Hälften
        JFileChooser leftOutputChooser = new JFileChooser();
        leftOutputChooser.setDialogTitle("Wählen Sie den Ordner zum Speichern der linken Hälften");
        leftOutputChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (leftOutputChooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(frame, "Kein Ausgabeordner für linke Hälften ausgewählt. Programm wird beendet.");
            System.exit(0);
        }

        // Wähle Ausgabeordner für rechte Hälften
        JFileChooser rightOutputChooser = new JFileChooser();
        rightOutputChooser.setDialogTitle("Wählen Sie den Ordner zum Speichern der rechten Hälften");
        rightOutputChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (rightOutputChooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(frame, "Kein Ausgabeordner für rechte Hälften ausgewählt. Programm wird beendet.");
            System.exit(0);
        }

        File inputDir = inputChooser.getSelectedFile();
        File leftOutputDir = leftOutputChooser.getSelectedFile();
        File rightOutputDir = rightOutputChooser.getSelectedFile();

        // Zähler für die Statistik
        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Verarbeite alle PNG-Dateien im Eingabeordner
        File[] files = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        
        if (files == null || files.length == 0) {
            JOptionPane.showMessageDialog(frame, "Keine PNG-Dateien im ausgewählten Ordner gefunden.");
            System.exit(0);
        }

        // Fortschrittsanzeige erstellen
        JProgressBar progressBar = new JProgressBar(0, files.length);
        JDialog progressDialog = new JDialog(frame, "Verarbeite Bilder...", true);
        progressDialog.add(progressBar);
        progressDialog.setSize(300, 75);
        progressDialog.setLocationRelativeTo(null);

        // Verarbeitung in separatem Thread starten
        new Thread(() -> {
            for (File file : files) {
                try {
                    // Bild einlesen
                    BufferedImage originalImage = ImageIO.read(file);
                    
                    // Bildmaße ermitteln
                    int width = originalImage.getWidth();
                    int height = originalImage.getHeight();

                    // Linke Hälfte ausschneiden
                    BufferedImage leftHalf = originalImage.getSubimage(0, 0, width/2, height);
                    
                    // Rechte Hälfte ausschneiden
                    BufferedImage rightHalf = originalImage.getSubimage(width/2, 0, width/2, height);

                    // Neue Dateinamen erstellen
                    String leftFileName = "left_" + file.getName();
                    String rightFileName = "right_" + file.getName();
                    
                    File leftOutputFile = new File(leftOutputDir, leftFileName);
                    File rightOutputFile = new File(rightOutputDir, rightFileName);

                    // Bilder speichern
                    ImageIO.write(leftHalf, "PNG", leftOutputFile);
                    ImageIO.write(rightHalf, "PNG", rightOutputFile);
                    
                    // Zähler erhöhen
                    processedCount.incrementAndGet();
                    progressBar.setValue(processedCount.get());

                } catch (IOException e) {
                    errorCount.incrementAndGet();
                    System.err.println("Fehler bei der Verarbeitung von " + file.getName() + ": " + e.getMessage());
                }
            }

            // Fortschrittsdialog schließen
            progressDialog.dispose();

            // Abschlussmeldung anzeigen
            String message = String.format("Verarbeitung abgeschlossen!\n" +
                    "Erfolgreich verarbeitete Bilder: %d\n" +
                    "Fehler aufgetreten bei: %d Bildern", 
                    processedCount.get(), errorCount.get());
            
            JOptionPane.showMessageDialog(frame, message, "Fertig", JOptionPane.INFORMATION_MESSAGE);
            
            System.exit(0);
        }).start();

        // Fortschrittsdialog anzeigen
        progressDialog.setVisible(true);
    }
}
