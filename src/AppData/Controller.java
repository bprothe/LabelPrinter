package AppData;

import java.net.InetAddress;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.print.*;
import javafx.scene.control.*;
import javax.imageio.ImageIO;
import javax.imageio.spi.RegisterableService;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaPrintableArea;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.io.*;
import java.util.Optional;
import java.util.Scanner;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.sourceforge.barbecue.*;
import net.sourceforge.barbecue.output.OutputException;


public class Controller {

    public File csv;
    public Button LPKPrint;
    public TextField LPKInput;
    public TextField LPKCount;
    public TextField UPCInput;
    public Button NextBatch;
    public Text LastLPK;
    public Button CustomPrint;
    public TextField CustomInput;
    public TextField CustomCount;
    public MenuButton CustomSize;
    public Button UPCPrint;
    public TextField UPCCount;
    public MenuButton CSVSize;
    public Button CSVImport;
    public Button CSVPrint;
    public Button CSVDownload;
    public Text CSVReady;
    public CheckBox DoubleSided;
    public Text CountErrorLPK;
    public Button LoadBatch;
    public Text BlankErrorLPK;
    public Text CountErrorUPC;
    public Text CountErrorCustom;
    public PrintService printer;
    public MenuItem customSizeLG;
    public MenuItem customSizeSM;
    public String fileName = "PrintLabel.pj";
    public CheckBox TCLabel;

    public Controller() {
        //10.52.24.77
        Printer choosePrinter = null;
        ChoiceDialog dialog = new ChoiceDialog(Printer.getDefaultPrinter(), Printer.getAllPrinters());
        dialog.setHeaderText("Choose the printer!");
        dialog.setContentText("Choose a printer from available printers");
        dialog.setTitle("Printer Choice");
        Optional<Printer> opt = dialog.showAndWait();
        if (opt.isPresent()) {
            choosePrinter = opt.get();
        }
        else
            Platform.exit();
        PrintService[] psList = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService ps : psList) {
            if (ps.getName().equals(choosePrinter.getName())) {
                printer = ps;
            }
        }
        StreamPrintService sps;

    }

    private String readConfig(String tag) {
        tag = "#"+tag+":";
        //System.out.println(tag);
        try {
            File myFile = new File("config.ini");
            Scanner fin = new Scanner(myFile);
            String contents = fin.nextLine();
            contents = contents.substring(contents.indexOf(tag));
            contents = contents.substring(tag.length(), contents.indexOf(";"));
            //system.out.println(contents);
            fin.close();
            return contents;
        } catch (FileNotFoundException e) {
            //system.out.println("An error occurred.");
            e.printStackTrace();
        }
        return "";
    }
    public PrintRequestAttributeSet create4X1(String labelText, int count) throws BarcodeException, OutputException, IOException {
        Barcode b = BarcodeFactory.createCode128(labelText);
        PrinterJob printerJob = PrinterJob.createPrinterJob(Printer.getDefaultPrinter());
        PrinterAttributes printerAttributes = printerJob.getPrinter().getPrinterAttributes();
        b.setResolution(printerAttributes.getDefaultPrintResolution().getFeedResolution());
        b.setFont(null);
        b.setSize(500, 500);
        b.setBarHeight(100);
        b.setBarWidth(2);
        BufferedImage image = new BufferedImage(800, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_RESOLUTION_VARIANT, RenderingHints.VALUE_RESOLUTION_VARIANT_BASE);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.rotate(Math.toRadians(180), image.getWidth()/2, image.getHeight()/2);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(5.0f));
        g2d.setFont(Font.decode("arial-50"));
        if(labelText.length()<12) {
            g2d.drawString(labelText, 20, 120);
            b.draw(g2d, 450, 50);
        } else if (labelText.length()<14) {
            g2d.drawString(labelText, 20, 120);
            b.draw(g2d, 370, 50);
        } else {
            b.setFont(Font.decode("arial-25"));
            b.draw(g2d, 20, 50);
        }
        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        pras.add(new Copies(count));
        pras.add(new MediaPrintableArea(0, 0, 4, 1, MediaPrintableArea.INCH));
        File outputfile = new File(fileName);
        ImageIO.write(image, "png", outputfile);
        return pras;
    }
    public PrintRequestAttributeSet createUPC(String labelText, int count) throws BarcodeException, OutputException, IOException {
        Barcode b;
        if(TCLabel.isSelected())
            b = BarcodeFactory.createCode128("T"+labelText);
        else
            b = BarcodeFactory.createCode128(labelText);
        PrinterJob printerJob = PrinterJob.createPrinterJob(Printer.getDefaultPrinter());
        PrinterAttributes printerAttributes = printerJob.getPrinter().getPrinterAttributes();
        b.setResolution(printerAttributes.getDefaultPrintResolution().getFeedResolution());
        b.setFont(Font.decode("arial-25"));
        b.setSize(500, 500);
        b.setBarHeight(100);
        b.setBarWidth(2);
        BufferedImage image = new BufferedImage(800, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.rotate(Math.toRadians(180), image.getWidth()/2, image.getHeight()/2);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(5.0f));
        b.draw(g2d, 70, 50);
        if(TCLabel.isSelected()) {
            b = BarcodeFactory.createCode128("C" + labelText);
            b.setResolution(printerAttributes.getDefaultPrintResolution().getFeedResolution());
            b.setFont(Font.decode("arial-25"));
            b.setSize(500, 500);
            b.setBarHeight(100);
            b.setBarWidth(2);
            b.draw(g2d, 450, 50);
        }
        if(DoubleSided.isSelected())
            b.draw(g2d, 450, 50);
        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        pras.add(new Copies(count));
        pras.add(new MediaPrintableArea(0, 0, 4, 1, MediaPrintableArea.INCH));
        File outputfile = new File(fileName);
        ImageIO.write(image, "png", outputfile);
        return pras;
    }
    public PrintRequestAttributeSet create4X6(String labelText, int count) throws BarcodeException, IOException, OutputException, PrinterException {
        Barcode b = BarcodeFactory.createCode128(labelText);
        b.setResolution(50);
        b.setFont(null);
        b.setSize(500, 400);
        b.setBarHeight(250);
        BufferedImage image = new BufferedImage(800, 1200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_RESOLUTION_VARIANT, RenderingHints.VALUE_RESOLUTION_VARIANT_BASE);
        g2d.rotate(Math.toRadians(90), image.getWidth()/2, image.getHeight()/2);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(15.0f));
        if(labelText.contains("LPK")) {
            g2d.setFont(Font.decode("arial-200"));
            b.setBarWidth(6);
            g2d.drawString(labelText, -170, 500);
            b.draw(g2d, -240, 700);
            g2d.drawLine(650, 940, 990, 940);
        } else if(labelText.length() < 12){
            g2d.setFont(Font.decode("arial-200"));
            b.setBarWidth(6);
            g2d.drawString(labelText, -170, 500);
            b.draw(g2d, -240, 700);
        } else {
            g2d.setFont(Font.decode("arial-80"));
            b.setBarWidth(4);
            g2d.drawString(labelText, -170, 500);
            b.draw(g2d, -240, 700);
        }
        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        pras.add(new Copies(count));
        pras.add(new MediaPrintableArea(0, 0, 4, 6, MediaPrintableArea.INCH));
        File outputfile = new File(fileName);
        ImageIO.write(image, "png", outputfile);
        return pras;
    }

    public void printImage (PrintRequestAttributeSet pras) throws Exception {
        DocPrintJob job = printer.createPrintJob();
        FileInputStream fin = new FileInputStream(fileName);
        
        Doc doc = new SimpleDoc(fin, DocFlavor.INPUT_STREAM.PNG, null);
        job.print(doc, pras);
        fin.close();
    }

    public void printCSV() throws Exception {
        if(CSVReady.getFill() == Paint.valueOf("Green")) {
            Scanner scan = new Scanner(csv);
            String line;
            scan.nextLine();
            while (scan.hasNextLine()) {
                line = scan.nextLine();
                while(line.contains(",")) {
                    line = line.replace(",", "");
                }
                if(CSVSize.getText().equals("4X6")) {
                    printImage(create4X6(line, 1));
                } else {
                    printImage(create4X1(line, 1));
                }
            }
        }

    }
    public void customPrint() throws Exception {
        int count = 1;
        boolean canPrint = true;
        CountErrorCustom.setVisible(false);
        if(!CustomInput.getText().isEmpty()) {
            if(!CustomCount.getText().isEmpty()) {
                if(!isNumeric(CustomCount.getText())) {
                    canPrint = false;
                    CountErrorCustom.setVisible(true);
                }
                else
                    count = Integer.parseInt(CustomCount.getText());
            }
            if(canPrint) {
                if (CustomSize.getText().equals("4X6")) {
                    printImage(create4X6(CustomInput.getText(), count));
                } else {
                    printImage(create4X1(CustomInput.getText(), count));
                }
            }
        }
    }
    public void printUPC() throws Exception {
        CountErrorUPC.setVisible(false);
        if(isNumeric(UPCInput.getText())) {
            if (TCLabel.isSelected() && isNumeric(UPCCount.getText())) {
                int upc = Integer.parseInt(UPCInput.getText());
                for (int i=0; i< Integer.parseInt(UPCCount.getText()); i++) {
                    printImage(createUPC(String.valueOf(upc+i), 1));
                }
            }
            if (UPCCount.getText().isEmpty() && !TCLabel.isSelected())
                printImage(createUPC(UPCInput.getText(), 1));
            else if(isNumeric(UPCCount.getText()) && !TCLabel.isSelected()) {
                printImage(createUPC(UPCInput.getText(), Integer.parseInt(UPCCount.getText())));
            }
        }
        else {
            CountErrorUPC.setVisible(true);
        }
    }
    public void printLPK() throws Exception {
        int count = -1;
        final int defaultCount = 1;
        boolean canPrint = true;
        BlankErrorLPK.setVisible(false);
        CountErrorLPK.setVisible(false);
        int lpkFromConfig = getLastLPK();
        int lpkFromField = 1;
        if(LPKCount.getText().isEmpty()) {
            count = defaultCount;
        }
        else {
            if(isNumeric(LPKCount.getText())) {
                count = Integer.parseInt(LPKCount.getText());
            }
            else {
                canPrint = false;
                CountErrorLPK.setVisible(true);
            }
        }

        if(LPKInput.getText().isEmpty()) {
            canPrint = false;
            BlankErrorLPK.setVisible(true);
        }
        else {
            if(isNumeric(LPKInput.getText())) {
                lpkFromField = Integer.parseInt(LPKInput.getText());
            }
            else {
                canPrint = false;
                BlankErrorLPK.setVisible(true);
            }
        }

        if (canPrint) {
            if (lpkFromConfig > lpkFromField) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Duplicate LPK error");
                alert.setHeaderText("The entered LPK ("+lpkFromField+") is smaller than the LPK number indicated in records. ("+lpkFromConfig+")");
                alert.setContentText("Would you like to proceed?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    //system.out.println("Printing from Input box rather than config");
                } else {
                    BlankErrorLPK.setVisible(true);
                    canPrint = false;
                }
            }
        }
        if (canPrint) {
            CountErrorLPK.setVisible(false);
            int lpk = lpkFromField;
            printImage(create4X6("LPK"+lpk, 1));
            for(int i=0; i<count-1; i++){
                lpk = lpk + 1;
                printImage(create4X6("LPK"+lpk, 1));
            }
            //system.out.println("Last one printed " + lpk);
            updateConfig(lpk);
        }
    }

    public void updateConfig(int last) throws IOException {
        File file = new File("config.ini");
        file.delete();
        FileWriter myFile = new FileWriter("config.ini");
        String newConfig = "#RECENT_LPK:" + last + ";\n" +
                "-----------------------------------------------\n" +
                "This Barcode Labeling Software has been written and designed by Brandon Prothe.\n" +
                "-----------------------------------------------\n" +
                "The intention of this program is to make it easier for people to print barcodes onto Zebra Label Printers.\n" +
                "This is free to use and not to be sold. This program has four primary functions.\n" +
                "To create LPK labels (4X6) to store pallets.\n" +
                "To create UPC labels (4X1) to put on merchandise.\n" +
                "To create custom labels (4X6 & 4X1) for use in whatever needed.\n" +
                "To create a list of labels/barcodes imported from a CSV.";
        myFile.write(newConfig);
        myFile.close();
    }

    public void importCSV() {
        try {
            File myFile;
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open CSV File");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "\\Downloads"));
            myFile = fileChooser.showOpenDialog(new Stage());
            if(myFile.getName().contains("csv")) {
                CSVReady.setFill(Paint.valueOf("GREEN"));
                CSVReady.setText("Ready");
                csv = myFile;
                CSVImport.setDisable(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void downloadCSV() throws IOException {
        FileWriter myFile = new FileWriter(System.getProperty("user.home")+"\\Downloads\\LabelTemplate.csv");
        myFile.write("Prefix,Label,Suffix\n");
        myFile.close();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Template Downloaded");
        alert.setHeaderText("LabelTemplate.csv is in downloads folder");
        alert.showAndWait();

    }
    public void clearCSV() {
        CSVReady.setFill(Paint.valueOf("RED"));
        CSVReady.setText("Not Ready");
        csv = null;
        CSVImport.setDisable(false);
    }
    public void loadBatch() {
        if(getLastLPK() == 0)
            LastLPK.setText("No LPK Found");
        else
            LastLPK.setText("LPK"+readConfig("RECENT_LPK"));
    }
    public void nextBatch() {
        LPKInput.setText(String.valueOf((getLastLPK()+1)));
    }
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    public int getLastLPK() {
        Integer data = Integer.parseInt(readConfig("RECENT_LPK"));
        return data;
    }
    public void CSVSizeLG() {
        CSVSize.setText("4X6");
    }
    public void CSVSizeSM() {
        CSVSize.setText("4X1");
    }
    public void CustomSizeLG() {
        CustomSize.setText("4X6");
    }
    public void CustomSizeSM() {
        CustomSize.setText("4X1");
    }
    public void OpenLPK() {
        LastLPK.setText("LPK"+ getLastLPK());
    }

    public void doublesideClick() {
        TCLabel.setSelected(false);
    }

    public void tcClick() {
        DoubleSided.setSelected(false);
    }
}

