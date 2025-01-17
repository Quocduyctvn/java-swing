package KyTucXa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import Data.DatabaseConnection;
import java.util.ArrayList;
import java.util.List;

public class QL_HoaDonDatPhong extends JFrame {
    private JTable tableHoaDon;
    private DefaultTableModel tableModel;
    private JPanel panelPagination;
    private int currentPage = 1;

    public QL_HoaDonDatPhong() {
        setTitle("Quản lý Hóa Đơn Đặt Phòng");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Tạo bảng hiển thị danh sách hóa đơn
        tableModel = new DefaultTableModel(new String[]{"Mã HĐ", "Mã SV", "Tên SV", "Phòng", "Tổng tiền", "Ngày tạo"}, 0);
        tableHoaDon = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tableHoaDon);

        // Tạo label tiêu đề
        JLabel lblTitle = new JLabel("Danh Sách Hóa Đơn Đặt Phòng", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));

        // Tạo các nút chức năng
        JButton btnXemChiTiet = new JButton("Xem Chi Tiết");
        JButton btnXoaHoaDon = new JButton("Xóa Hóa Đơn");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(btnXemChiTiet);
        buttonPanel.add(btnXoaHoaDon);

        // Tạo panel phân trang
        panelPagination = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnPrev = new JButton("<");
        JButton btnNext = new JButton(">");
        JLabel lblPageInfo = new JLabel("Trang: " + currentPage);

        panelPagination.add(btnPrev);
        panelPagination.add(lblPageInfo);
        panelPagination.add(btnNext);

        // Layout chính
        setLayout(new BorderLayout());
        add(lblTitle, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.WEST);
        add(panelPagination, BorderLayout.SOUTH);

        // Load dữ liệu hóa đơn
        loadDanhSachHoaDon();

        // Sự kiện nút
        btnXemChiTiet.addActionListener(e -> xemChiTietHoaDon());
        btnXoaHoaDon.addActionListener(e -> xoaHoaDon());
        btnPrev.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                lblPageInfo.setText("Trang: " + currentPage);
                loadDanhSachHoaDon();
            }
        });
        btnNext.addActionListener(e -> {
            currentPage++;
            lblPageInfo.setText("Trang: " + currentPage);
            loadDanhSachHoaDon();
        });
    }

    private void loadDanhSachHoaDon() {
        String query = """
                SELECT hd.id, sv.id AS ma_sv, sv.ten_sinh_vien, p.name AS ten_phong, hd.gia, hd.ngay_tao
                FROM hoa_don hd
                JOIN dat_phong dp ON hd.id_dat_phong = dp.id
                JOIN sinh_vien sv ON dp.id_sinh_vien = sv.id
                JOIN phong p ON dp.id_phong = p.id
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("id"),
                        rs.getString("ma_sv"),
                        rs.getString("ten_sinh_vien"),
                        rs.getString("ten_phong"),
                        rs.getFloat("gia"),
                        rs.getDate("ngay_tao")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách hóa đơn: " + ex.getMessage());
        }
    }

    private void xemChiTietHoaDon() {
        int selectedRow = tableHoaDon.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn để xem chi tiết!");
            return;
        }

        String maHoaDon = tableModel.getValueAt(selectedRow, 0).toString();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT duong_dan FROM hoa_don WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, maHoaDon);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String pdfPath = rs.getString("duong_dan");
                        moFileHoaDon(pdfPath);
                    } else {
                        JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn này!");
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi mở hóa đơn: " + e.getMessage());
        }
    }

    private void moFileHoaDon(String pdfPath) {
        try {
            File file = new File(pdfPath);
            if (file.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                } else {
                    JOptionPane.showMessageDialog(this, "Hệ thống không hỗ trợ mở file!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "File không tồn tại tại: " + pdfPath);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể mở file: " + e.getMessage());
        }
    }


    private void xoaHoaDon() {
        int selectedRow = tableHoaDon.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn để xóa!");
            return;
        }

        String maHoaDon = tableModel.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa hóa đơn này?", "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String deleteQuery = "DELETE FROM hoa_don WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                    stmt.setString(1, maHoaDon);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Xóa hóa đơn thành công!");
                    loadDanhSachHoaDon();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa hóa đơn: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QL_HoaDonDatPhong().setVisible(true));
    }
}
