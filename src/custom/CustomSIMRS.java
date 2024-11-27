/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package custom;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.akses;
import java.awt.Desktop;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import uz.ncipro.calendar.JDateTimePicker;
import widget.Button;
import widget.ComboBox;
import widget.Tanggal;
import widget.TextArea;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import widget.TextBox;

/**
 *
 * @author krisna
 */
public final class CustomSIMRS {

    private int a, j, i, result = 0;
    private String s, s1, auto, PEMBULATANHARGAOBAT = koneksiDB.PEMBULATANHARGAOBAT();
    private final Connection connect = koneksiDB.condb();
    private final sekuel sek = new sekuel();
    private final java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
    private final DecimalFormat df2 = new DecimalFormat("###,###,###,###,###,###,###");
    private final DecimalFormat df4 = new DecimalFormat("###,###,###,###,###,###,###.#################");
    private final DecimalFormat df5 = new DecimalFormat("###,###,###,###,###,###,###.##");
    private final DecimalFormat df3 = new DecimalFormat("######");
    private final DecimalFormat df6 = new DecimalFormat("######.###");
    private final DecimalFormat df7 = new DecimalFormat("######.#");
    private PreparedStatement ps;
    private ResultSet rs;
    private File file;
    private boolean status = true;
    private final Calendar now = Calendar.getInstance();
    private final int year = (now.get(Calendar.YEAR));
    private String[] nomina = {"", "satu", "dua", "tiga", "empat", "lima", "enam",
        "tujuh", "delapan", "sembilan", "sepuluh", "sebelas"};

    public CustomSIMRS() {
        super();
    };
    
    /*
    Method Kirim berkas
    */
    @SuppressWarnings("empty-statement")
    private void KirimOtomatisBerkas(String filename) {
        String SFTPHOST = koneksiDB.HOSTHYBRIDWEB();
        int SFTPPORT = Integer.parseInt(koneksiDB.PORTSFTPUPLOADBERKAS());
        String SFTPUSER = koneksiDB.USERSFTPUPLOADBERKAS();
        String SFTPPASS = koneksiDB.PASSWORDSFTPUPLOADBERKAS();
        String WORKINGDIRSFTP = koneksiDB.WORKINGDIRSFTPUPLOADBERKAS();
        Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
            session.setPassword(SFTPPASS);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            System.out.println("Host connected.");
            channel = session.openChannel("sftp");
            channel.connect();
            System.out.println("sftp channel opened and connected.");
            channelSftp = (ChannelSftp) channel;
            channelSftp.cd(WORKINGDIRSFTP);
            File fa = new File(filename);
            channelSftp.put(new FileInputStream(fa), fa.getName());
            System.out.println("File transfered successfully to host.");
        } catch (JSchException | SftpException | FileNotFoundException ex) {
            System.out.println("Exception found while tranfer the response." + ex);
        } finally {
            channelSftp.exit();
            System.out.println("sftp Channel exited.");
            channel.disconnect();
            System.out.println("Channel disconnected.");
            session.disconnect();
            System.out.println("Host Session disconnected.");
        }
        File Obj = new File(filename);
        Obj.delete();
        JOptionPane.showMessageDialog(null, "Berkas berhasil di UPLOAD ..!!");
    }

    /*
    Method Khsusus dipakai untuk membuat file PDF kemudian memanggil 
    Method KirimOtomatisBerkas(filename) untuk di upload ke webapps
    */
    @SuppressWarnings("empty-statement")
    public void MyReportPDFLaporanOperasiBaru(String reportName, String reportDirName, String judul, String namapasien, String nm_file, Map parameters) {
        Properties systemProp = System.getProperties();

        // Ambil current dir
        String currentDir = systemProp.getProperty("user.dir");

        File dir = new File(currentDir);

        File fileRpt;
        String fullPath = "";
        if (dir.isDirectory()) {
            String[] isiDir = dir.list();
            for (String iDir : isiDir) {
                fileRpt = new File(currentDir + File.separatorChar + iDir + File.separatorChar + reportDirName + File.separatorChar + reportName);
                if (fileRpt.isFile()) { // Cek apakah file RptMaster.jasper ada
                    fullPath = fileRpt.toString();
                    System.out.println("Found Report File at : " + fullPath);
                } // end if
            } // end for i
        } // end if

        try {
            try ( Statement stm = connect.createStatement()) {
                try {
                    String namafile = "./" + reportDirName + "/" + reportName;
//                    String returned_value = (String) parameters.put("nmpasien", file);
                    String returned_value = NamaPasienOperasi(namapasien);
                    JasperReport jasperReport = (JasperReport) JRLoader.loadObjectFromFile(namafile);
                    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connect);
                    JasperExportManager.exportReportToPdfFile(jasperPrint, dir + "/report/" + returned_value + "_" + nm_file);

                    String filename = dir + "/report/" + returned_value + "_" + nm_file;
                    
                    KirimOtomatisBerkas(filename);
                } catch (JRException rptexcpt) {
                    System.out.println("Report Can't view because : " + rptexcpt);
                    JOptionPane.showMessageDialog(null, "Report Can't view because : " + rptexcpt);
                }
            }
        } catch (HeadlessException | SQLException e) {
            System.out.println(e);
        }
    }

    /*
    Method ini digunakan untuk membuat file PDF yang data parameternya berupa query database
    1. Data pemberian obat
    2. dll
    */
    public void MyReportQryPDFBaru(String reportName, String reportDirName, String judul, String qry, String namapasien, String norm, String nm_file, Map parameters) {
        Properties systemProp = System.getProperties();

        // Ambil current dir
        String currentDir = systemProp.getProperty("user.dir");

        File dir = new File(currentDir);

        File fileRpt;
        String fullPath = "";
        if (dir.isDirectory()) {
            String[] isiDir = dir.list();
            for (String iDir : isiDir) {
                fileRpt = new File(currentDir + File.separatorChar + iDir + File.separatorChar + reportDirName + File.separatorChar + reportName);
                if (fileRpt.isFile()) { // Cek apakah file RptMaster.jrxml ada
                    fullPath = fileRpt.toString();
                    System.out.println("Found Report File at : " + fullPath);
                } // end if
            } // end for i
        } // end if

        try {
            ps = connect.prepareStatement(qry);
            try {
                rs = ps.executeQuery();
                JRResultSetDataSource rsdt = new JRResultSetDataSource(rs);
                String namafile = "./" + reportDirName + "/" + reportName;
                String returned_value = NamaPasien(namapasien) + "_" + norm;
                JasperReport jasperReport = (JasperReport) JRLoader.loadObjectFromFile(namafile);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, rsdt);
//                String spri = (String) parameters.put("spri", file);
                JasperExportManager.exportReportToPdfFile(jasperPrint, dir + "/report/" + returned_value + "_" + nm_file);

                String filename = dir + "/report/" + returned_value + "_" + nm_file;
                
                KirimOtomatisBerkas(filename);
                
            } catch (NumberFormatException | JRException rptexcpt) {
                System.out.println("Report Can't view because : " + rptexcpt);
                JOptionPane.showMessageDialog(null, "Report Can't view because : " + rptexcpt);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (HeadlessException | SQLException e) {
            System.out.println(e);
        }
    }
    
     /*
    Method yang dipakai untuk membuat file PDF dari
    1. resume ralan
    2. resume ranap
    3. laporan hasil lab
    4. laporan hasil radiologi
    5. SEP Ralan dan SEP Ranap
    6. SPRI
    7. dll
    */
    @SuppressWarnings("empty-statement")
    public void MyReportPDFBaru(String reportName, String reportDirName, String judul, String namapasien, String norm, String nm_file, Map parameters) {
        Properties systemProp = System.getProperties();

        // Ambil current dir
        String currentDir = systemProp.getProperty("user.dir");

        File dir = new File(currentDir);

        File fileRpt;
        String fullPath = "";
        if (dir.isDirectory()) {
            String[] isiDir = dir.list();
            for (String iDir : isiDir) {
                fileRpt = new File(currentDir + File.separatorChar + iDir + File.separatorChar + reportDirName + File.separatorChar + reportName);
                if (fileRpt.isFile()) { // Cek apakah file RptMaster.jasper ada
                    fullPath = fileRpt.toString();
                    System.out.println("Found Report File at : " + fullPath);
                } // end if
            } // end for i
        } // end if

        try {
            try ( Statement stm = connect.createStatement()) {
                try {
                    String namafile = "./" + reportDirName + "/" + reportName;
                    String returned_value = NamaPasien(namapasien) + "_" + norm;
//                    String returned_value = (String) parameters.put("nmpasien", file) + "_" + parameters.put("norm", file);
                    JasperReport jasperReport = (JasperReport) JRLoader.loadObjectFromFile(namafile);
                    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connect);
//                    String resume = nm_file;
//                    String resume = (String) parameters.put("resume", file);
                    JasperExportManager.exportReportToPdfFile(jasperPrint, dir + "/report/" + returned_value + "_" + nm_file);

                    String filename = dir + "/report/" + returned_value + "_" + nm_file;
                    
                    KirimOtomatisBerkas(filename);
                    
                } catch (JRException rptexcpt) {
                    System.out.println("Report Can't view because : " + rptexcpt);
                    JOptionPane.showMessageDialog(null, "Report Can't view because : " + rptexcpt);
                }
            }
        } catch (HeadlessException | SQLException e) {
            System.out.println(e);
        }
    }

    /*
    Method untuk mengambil nama pasien operasi untuk dijadikan sebagai nama file PDF
    */
    private String NamaPasienOperasi(String nampasien) {
        String nmpasien = nampasien;
        String namapasien = nmpasien.replace(",", "");
        String namepasien = namapasien.replace(" ", "_");
        return namepasien;
    }

    /*
    Method untuk mengambil nama pasien yang memilik spasi untuk dijadikan sebagai nama file PDF
    */
    private String NamaPasien(String nampasien) {
        String namapasien = nampasien.replace(" ", "_");
        return namapasien;
    }

    /*
    Method untuk menyimpan nama file di tabel berkas khusus laporan operasi
    */
    public void SimpanBerkasKeTabelBerkasOperasi(String no_rawat, String kode_berkas, String namapasien, String namafile) {
        sek.menyimpan3("berkas_digital_perawatan", "?,?,?", "No.Rawat", 3, new String[]{
            no_rawat, kode_berkas, "pages/upload/" + NamaPasienOperasi(namapasien) + "_" + namafile
        });
    }

    /*
    Method untuk simpan nama file di tabel berkas perawtan
    */
    public void SimpanBerkasKeTabelBerkas(String no_rawat, String kode_berkas, String namapasien, String norm, String namafile) {
        sek.menyimpan3("berkas_digital_perawatan", "?,?,?", "No.Rawat", 3, new String[]{
            no_rawat, kode_berkas, "pages/upload/" + NamaPasien(namapasien) + "_" + norm + "_" + namafile
        });
    }
    
    /*
    Method Mengambil data SOAP atau CPPT sebagai resume rawat jalan
    */
    public void SimpanResumeDariCPPT(int index){
        
    }

}
