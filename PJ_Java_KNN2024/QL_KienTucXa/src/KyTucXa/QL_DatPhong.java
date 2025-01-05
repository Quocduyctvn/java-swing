package KyTucXa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import Data.DatabaseConnection;

import java.awt.*;
import java.sql.*;

// Class chính để quản lý đặt phòng
public class QL_DatPhong extends JFrame {
    private JTable tablePhong;
    private DefaultTableModel tableModel;

    public QL_DatPhong() {
        setTitle("Quản lý Đặt Phòng KTX");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Tạo bảng hiển thị danh sách phòng
        tableModel = new DefaultTableModel(new String[]{"Phòng", "Hạng phòng", "Số SV tối đa", "Tổng SV hiện tại"}, 0);
        tablePhong = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tablePhong);

        // Tạo các nút chức năng
        JButton btnThemSV = new JButton("Thêm SV");
        JButton btnSuaDatPhong = new JButton("Sửa Đặt Phòng");
        JButton btnXoaDatPhong = new JButton("Xóa Đặt Phòng");

        // Panel chứa các nút
        JPanel panelButtons = new JPanel(new FlowLayout());
        panelButtons.add(btnThemSV);
        panelButtons.add(btnSuaDatPhong);
        panelButtons.add(btnXoaDatPhong);

        // Layout chính
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(panelButtons, BorderLayout.SOUTH);

        // Load dữ liệu phòng lên bảng
        loadDanhSachPhong();

        // Xử lý sự kiện nút
        btnThemSV.addActionListener(e -> themSinhVien());
        btnSuaDatPhong.addActionListener(e -> suaDatPhong());
        btnXoaDatPhong.addActionListener(e -> xoaDatPhong());
    }

    private void loadDanhSachPhong() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            String query = """
                SELECT p.name AS ten_phong, 
                       h.name AS hang_phong, 
                       h.so_luong_sv AS so_sv_toi_da, 
                       p.so_luong_sv AS so_sv_hien_tai
                FROM phong p
                JOIN hang_phong h ON p.id_hang_phong = h.id;
            """;

            ResultSet rs = stmt.executeQuery(query);
            tableModel.setRowCount(0); // Xóa dữ liệu cũ
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("ten_phong"),
                    rs.getString("hang_phong"),
                    rs.getInt("so_sv_toi_da"),
                    rs.getInt("so_sv_hien_tai")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách phòng: " + ex.getMessage());
        }
    }

    private void themSinhVien() {
        int selectedRow = tablePhong.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng để thêm sinh viên!");
            return;
        }

        String tenPhong = tableModel.getValueAt(selectedRow, 0).toString();
        int soSvHienTai = Integer.parseInt(tableModel.getValueAt(selectedRow, 3).toString());
        int soSvToiDa = Integer.parseInt(tableModel.getValueAt(selectedRow, 2).toString());

        if (soSvHienTai >= soSvToiDa) {
            JOptionPane.showMessageDialog(this, "Phòng đã đầy, không thể thêm sinh viên!");
            return;
        }

        String maSinhVien = JOptionPane.showInputDialog(this, "Nhập mã sinh viên:");
        if (maSinhVien != null && !maSinhVien.trim().isEmpty()) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("""
                     INSERT INTO dat_phong (id_phong, id_sinh_vien, so_luong_sv, trang_thai)
                     VALUES ((SELECT id FROM phong WHERE name = ?), ?, ?, 1);
                 """)) {
                pstmt.setString(1, tenPhong);
                pstmt.setString(2, maSinhVien);
                pstmt.setInt(3, soSvHienTai + 1);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Thêm sinh viên thành công!");
                loadDanhSachPhong();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm sinh viên: " + ex.getMessage());
            }
        }
    }

    private void suaDatPhong() {
        JOptionPane.showMessageDialog(this, "Chức năng sửa thông tin đặt phòng chưa được triển khai!");
    }

    private void xoaDatPhong() {
        int selectedRow = tablePhong.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng để xóa đặt phòng!");
            return;
        }

        String tenPhong = tableModel.getValueAt(selectedRow, 0).toString();
        String maSinhVien = JOptionPane.showInputDialog(this, "Nhập mã sinh viên để xóa đặt phòng:");

        if (maSinhVien != null && !maSinhVien.trim().isEmpty()) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("""
                     DELETE FROM dat_phong
                     WHERE id_phong = (SELECT id FROM phong WHERE name = ?)
                     AND id_sinh_vien = ?;
                 """)) {
                pstmt.setString(1, tenPhong);
                pstmt.setString(2, maSinhVien);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Xóa đặt phòng thành công!");
                loadDanhSachPhong();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa đặt phòng: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            QL_DatPhong form = new QL_DatPhong();
            form.setVisible(true);
        });
    }
}
