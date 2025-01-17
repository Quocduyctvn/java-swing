package KyTucXa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;


import Data.DatabaseConnection;

import java.awt.*;
import java.awt.Font;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.itextpdf.text.*;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;

public class QL_DatPhong extends JFrame {
    private JTable tablePhong;
    private DefaultTableModel tableModel;
    private JPanel panelPagination;
    private int currentPage = 1;

    public QL_DatPhong() {
        setTitle("Quản lý Đặt Phòng KTX");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Tạo bảng hiển thị danh sách phòng
        tableModel = new DefaultTableModel(new String[]{"Phòng", "Hạng phòng", "Số SV tối đa", "Tổng SV hiện tại"}, 0);
        tablePhong = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tablePhong);

        // Tạo label tiêu đề
        JLabel lblTitle = new JLabel("Danh Sách Phòng KTX", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));

        // Tạo panel tiêu đề
        JPanel panelHeader = new JPanel(new BorderLayout());
        JPanel titlePanel = new JPanel();
        titlePanel.add(lblTitle);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        panelHeader.add(titlePanel, BorderLayout.NORTH);

        // Tạo các nút chức năng
        JButton btnThemSV = new JButton("Thêm SV");
        JButton btnChiTietSV = new JButton("Xem Chi Tiết");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(btnChiTietSV);
        buttonPanel.add(btnThemSV);

        panelHeader.add(buttonPanel, BorderLayout.SOUTH);

        // Panel phân trang
        panelPagination = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnPrev = new JButton("<");
        JButton btnNext = new JButton(">");
        JLabel lblPageInfo = new JLabel("Trang: " + currentPage);

        panelPagination.add(btnPrev);
        panelPagination.add(lblPageInfo);
        panelPagination.add(btnNext);

        // Layout chính
        setLayout(new BorderLayout());
        add(panelHeader, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelPagination, BorderLayout.SOUTH);

        // Load dữ liệu phòng lên bảng
        loadDanhSachPhong();

        // Xử lý sự kiện nút
        btnThemSV.addActionListener(e -> themSinhVien());
        btnChiTietSV.addActionListener(e -> xemChiTietSinhVien());
        btnPrev.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                lblPageInfo.setText("Trang: " + currentPage);
                loadDanhSachPhong();
            }
        });
        btnNext.addActionListener(e -> {
            currentPage++;
            lblPageInfo.setText("Trang: " + currentPage);
            loadDanhSachPhong();
        });
    }

    
    private void loadDanhSachPhong() {
        String query = """
                SELECT p.name AS ten_phong, 
                       h.name AS hang_phong, 
                       h.so_luong_sv AS so_sv_toi_da, 
                       p.so_luong_sv AS so_sv_hien_tai
                FROM phong p
                JOIN hang_phong h ON p.id_hang_phong = h.id;
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            tableModel.setRowCount(0);
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

        try (Connection conn = DatabaseConnection.getConnection()) {
            List<String> sinhViens = layDanhSachSinhVien(conn);

            // Hiển thị danh sách sinh viên
            String selectedStudent = (String) JOptionPane.showInputDialog(
                    this, "Chọn sinh viên:", "Chọn sinh viên",
                    JOptionPane.QUESTION_MESSAGE, null, sinhViens.toArray(), sinhViens.get(0));

            if (selectedStudent != null) {
                String maSinhVien = selectedStudent.split(" - ")[0];
                if (kiemTraSinhVienCoPhong(conn, maSinhVien)) {
                    JOptionPane.showMessageDialog(this, "Sinh viên đã có phòng!");
                    return;
                }

                // Thực hiện thêm sinh viên
                int months = chonThoiGian(); // Chọn thời gian
                if (months > 0) {
                    // Hiển thị hộp thoại yêu cầu xác nhận
                    int confirm = JOptionPane.showConfirmDialog(
                            this, 
                            "Bạn có chắc chắn muốn thêm sinh viên vào phòng với thời gian " + months + " tháng?", 
                            "Xác nhận", 
                            JOptionPane.YES_NO_OPTION, 
                            JOptionPane.QUESTION_MESSAGE
                    );

                    // Nếu người dùng chọn "Yes"
                    if (confirm == JOptionPane.YES_OPTION) {
                         themSVVaoPhong(conn, tenPhong, maSinhVien, months); // Gọi phương thức thêm sinh viên vào phòng
                         loadDanhSachPhong(); // Tải lại danh sách phòng
                         JOptionPane.showMessageDialog(this, "Thêm sinh viên thành công!");
                        
                    } else {
                        // Nếu người dùng chọn "No" hoặc đóng hộp thoại, không làm gì thêm
                        JOptionPane.showMessageDialog(this, "Thêm sinh viên đã bị hủy bỏ.");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn thời gian hợp lệ.");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm sinh viên: " + ex.getMessage());
        }
    }

    private List<String> layDanhSachSinhVien(Connection conn) throws SQLException {
        List<String> sinhViens = new ArrayList<>();
        String query = "SELECT id, ten_sinh_vien FROM sinh_vien";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                sinhViens.add(rs.getString("id") + " - " + rs.getString("ten_sinh_vien"));
            }
        }
        return sinhViens;
    }

    private boolean kiemTraSinhVienCoPhong(Connection conn, String maSinhVien) throws SQLException {
        String query = """
                SELECT 1 
                FROM dat_phong 
                WHERE id_sinh_vien = ? 
                  AND trang_thai = 1 
                  AND NOW() BETWEEN ngay_vao AND (ngay_vao + INTERVAL tong_thoi_gian MONTH);
            """;
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, maSinhVien);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();  // Nếu có kết quả, trả về true
            }
        }
    }




 private void themSVVaoPhong(Connection conn, String tenPhong, String maSinhVien, int months) throws SQLException {
        float pricePerMonth = layGiaPhong(conn, tenPhong);
        float totalFee = pricePerMonth * months;

        // Thêm thông tin đặt phòng
        String query = """
                INSERT INTO dat_phong (id_phong, id_sinh_vien, ngay_vao, so_luong_sv, tong_tien, tong_thoi_gian, trang_thai)
                VALUES ((SELECT id FROM phong WHERE name = ?), ?, NOW(), 1, ?, ?, 1);
            """;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, tenPhong);
            stmt.setString(2, maSinhVien);
            stmt.setFloat(3, totalFee);
            stmt.setInt(4, months);
            stmt.executeUpdate();
        }

        String updateQuery = "UPDATE phong SET so_luong_sv = so_luong_sv + 1 WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setString(1, tenPhong);
            stmt.executeUpdate();
        }

        // Tạo hóa đơn và file PDF
        String pdfPath = taoHoaDonVaLuuPDF(conn, maSinhVien, tenPhong, months, totalFee);
        if (pdfPath != null) {
            JOptionPane.showMessageDialog(this, "Chi tiết Hóa đơn lưu tại: " + pdfPath);
        }
    }

 private String taoHoaDonVaLuuPDF(Connection conn, String maSinhVien, String tenPhong, int months, float totalFee) {
	    // Đường dẫn thư mục lưu hóa đơn 
	    String pdfFolderPath = "src/File/hoa_don";
	    String pdfPath = pdfFolderPath + "/" + maSinhVien + "_" + System.currentTimeMillis() + ".pdf";

	    try {
	        // Tạo thư mục nếu chưa tồn tại
	        File folder = new File(pdfFolderPath);
	        if (!folder.exists()) {
	            folder.mkdirs(); // Tạo thư mục nếu nó chưa tồn tại
	        }

	        // Tạo file PDF
	        Document document = new Document();  // This is the iText Document
	        PdfWriter.getInstance(document, new FileOutputStream(pdfPath));
	        document.open();
	        BaseFont font = BaseFont.createFont("c:/windows/fonts/times.ttf", 
                    BaseFont.IDENTITY_H, 
                    BaseFont.EMBEDDED);


	        // Thêm tiêu đề
	        Paragraph title = new Paragraph("HÓA ĐƠN ĐẶT PHÒNG KÝ TÚC XÁ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
	        title.setAlignment(Element.ALIGN_CENTER);
	        document.add(title);
	        document.add(new Paragraph("\n")); // Dòng trống

	        // Lấy thông tin chi tiết sinh viên từ bảng sinh_vien
	        String tenSinhVien = "";
	        String lopSinhVien = "";
	        String khoaSinhVien = "";
	        String querySinhVien = "SELECT ten_sinh_vien, ten_lop, mo_ta FROM sinh_vien " +
	                               "JOIN lop ON sinh_vien.id_lop = lop.id WHERE sinh_vien.id = ?";
	        try (PreparedStatement stmt = conn.prepareStatement(querySinhVien)) {
	            stmt.setString(1, maSinhVien);
	            try (ResultSet rs = stmt.executeQuery()) {
	                if (rs.next()) {
	                    tenSinhVien = rs.getString("ten_sinh_vien");
	                    lopSinhVien = rs.getString("ten_lop");
	                    khoaSinhVien = rs.getString("mo_ta");
	                }
	            }
	        }

	        // Thêm thông tin sinh viên
	        document.add(new Paragraph("Thông tin sinh viên:"));
	        document.add(new Paragraph("  - Mã sinh viên: " + maSinhVien));
	        document.add(new Paragraph("  - Tên sinh viên: " + tenSinhVien));
	        document.add(new Paragraph("  - Lớp: " + lopSinhVien));
	        document.add(new Paragraph("  - Khoa: " + khoaSinhVien));
	        document.add(new Paragraph("\n")); // Dòng trống

	        // Lấy thông tin phòng từ bảng phong
	        String loaiPhong = "";
	        float giaPhong = 0;
	        String queryPhong = "SELECT h.name AS loai_phong, h.gia AS gia_phong FROM phong p " +
	                            "JOIN hang_phong h ON p.id_hang_phong = h.id WHERE p.name = ?";
	        try (PreparedStatement stmt = conn.prepareStatement(queryPhong)) {
	            stmt.setString(1, tenPhong);
	            try (ResultSet rs = stmt.executeQuery()) {
	                if (rs.next()) {
	                    loaiPhong = rs.getString("loai_phong");
	                    giaPhong = rs.getFloat("gia_phong");
	                }
	            }
	        }

	        // Thêm thông tin phòng
	        document.add(new Paragraph("Thông tin phòng:"));
	        document.add(new Paragraph("  - Tên phòng: " + tenPhong));
	        document.add(new Paragraph("  - Loại phòng: " + loaiPhong));
	        document.add(new Paragraph("  - Giá phòng mỗi tháng: " + giaPhong + " VND"));
	        document.add(new Paragraph("\n")); // Dòng trống

	        // Thêm thông tin hóa đơn
	        document.add(new Paragraph("Chi tiết hóa đơn:"));
	        document.add(new Paragraph("  - Thời gian thuê: " + months + " tháng"));
	        document.add(new Paragraph("  - Tổng tiền: " + totalFee + " VND"));
	        document.add(new Paragraph("  - Ngày tạo hóa đơn: " + java.time.LocalDate.now()));
	        document.add(new Paragraph("\n")); // Dòng trống

	        // Thêm ghi chú
	        document.add(new Paragraph("Ghi chú:"));
	        document.add(new Paragraph("  - Quý khách vui lòng giữ lại hóa đơn này để đối chiếu khi cần thiết."));
	        document.add(new Paragraph("  - Nếu có thắc mắc, vui lòng liên hệ với ban quản lý ký túc xá."));
	        document.add(new Paragraph("\n")); // Dòng trống

	        document.close();

	        // Lưu thông tin vào bảng hoa_don
	        String insertQuery = """
	        	    INSERT INTO hoa_don (id, duong_dan, gia, trang_thai, id_dat_phong)
	        	    VALUES (?, ?, ?, 1, (
	        	        SELECT dp.id FROM dat_phong dp
	        	        JOIN phong p ON dp.id_phong = p.id
	        	        WHERE dp.id_sinh_vien = ? AND p.name = ? LIMIT 1
	        	    ));
	        	""";

	        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
	            stmt.setString(1, maSinhVien + "_" + System.currentTimeMillis());
	            stmt.setString(2, pdfPath);
	            stmt.setFloat(3, totalFee);
	            stmt.setString(4, maSinhVien);
	            stmt.setString(5, tenPhong);
	            stmt.executeUpdate();
	        }

	        return pdfPath;
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(null, "Lỗi khi tạo hóa đơn PDF: " + e.getMessage());
	        return null;
	    }
	}


 
    private float layGiaPhong(Connection conn, String tenPhong) throws SQLException {
        String query = """
                SELECT h.gia
                FROM phong p
                JOIN hang_phong h ON p.id_hang_phong = h.id
                WHERE p.name = ?;
            """;
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, tenPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getFloat("gia");
            }
        }
        return 0;
    }

 
    private int chonThoiGian() {
        String[] options = {"3 tháng", "6 tháng", "1 năm", "2 năm", "3 năm", "4 năm"};
        int selectedOption = JOptionPane.showOptionDialog(
                this, 
                "Chọn thời gian ở:", 
                "Chọn thời gian", 
                JOptionPane.DEFAULT_OPTION, 
                JOptionPane.INFORMATION_MESSAGE, 
                null, 
                options, 
                options[0]
        );
        
        // Kiểm tra xem người dùng có chọn lựa hay không
        if (selectedOption == -1) {
            return -1;  // Nếu người dùng hủy bỏ
        }

        // Trả về số tháng tương ứng với lựa chọn
        switch (selectedOption) {
            case 0: // 3 tháng
                return 3;
            case 1: // 6 tháng
                return 6;
            case 2: // 1 năm
                return 12;
            case 3: // 2 năm
                return 24;
            case 4: // 3 năm
                return 36;
            case 5: // 4 năm
                return 48;
            default:
                return -1;  // Trường hợp không hợp lệ
        }
    }

    
    
private void xemChiTietSinhVien() {
    int selectedRow = tablePhong.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng để xem chi tiết sinh viên!");
        return;
    }

    String tenPhong = tableModel.getValueAt(selectedRow, 0).toString();
    String query = """
            SELECT sv.id, sv.ten_sinh_vien, dp.ngay_vao, dp.tong_thoi_gian 
            FROM sinh_vien sv
            JOIN dat_phong dp ON sv.id = dp.id_sinh_vien
            WHERE dp.id_phong = (SELECT id FROM phong WHERE name = ?)
            AND dp.trang_thai = 1;  -- Điều kiện lọc trạng thái = 1
        """;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setString(1, tenPhong);
        try (ResultSet rs = stmt.executeQuery()) {
            // Tạo bảng để hiển thị chi tiết sinh viên
            DefaultTableModel studentTableModel = new DefaultTableModel(new String[]{"Mã SV", "Tên", "Ngày vào", "Thời gian ở / Tháng"}, 0);
            JTable studentTable = new JTable(studentTableModel);
            JScrollPane studentScrollPane = new JScrollPane(studentTable);

            while (rs.next()) {
                studentTableModel.addRow(new Object[]{
                        rs.getString("id"),
                        rs.getString("ten_sinh_vien"),
                        rs.getDate("ngay_vao"),
                        rs.getInt("tong_thoi_gian")
                });
            }

            // Tạo panel để chứa bảng và các nút chức năng
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(studentScrollPane, BorderLayout.CENTER);

            // Nút sửa và xóa
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton btnSua = new JButton("Sửa Đặt Phòng");
            JButton btnXoa = new JButton("Xóa Đặt Phòng");
            buttonPanel.add(btnSua);
            buttonPanel.add(btnXoa);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            // Mở cửa sổ mới để hiển thị bảng sinh viên và các nút chức năng
            JFrame detailFrame = new JFrame("Chi Tiết Sinh Viên");
            detailFrame.setSize(600, 400);
            detailFrame.setLocationRelativeTo(this);
            detailFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            detailFrame.add(panel);
            detailFrame.setVisible(true);

            // Xử lý sự kiện sửa
            btnSua.addActionListener(e -> {
                int selectedStudentRow = studentTable.getSelectedRow();
                if (selectedStudentRow == -1) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên để sửa thông tin!");
                    return;
                }
                String maSinhVien = studentTableModel.getValueAt(selectedStudentRow, 0).toString();
                String tenSinhVien = studentTableModel.getValueAt(selectedStudentRow, 1).toString();
                int thoiGianO = Integer.parseInt(studentTableModel.getValueAt(selectedStudentRow, 3).toString());
                suaDatPhong(maSinhVien, tenSinhVien, thoiGianO);
            });

            // Xử lý sự kiện xóa
            btnXoa.addActionListener(e -> {
                int selectedStudentRow = studentTable.getSelectedRow();
                if (selectedStudentRow == -1) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên để xóa!");
                    return;
                }
                String maSinhVien = studentTableModel.getValueAt(selectedStudentRow, 0).toString();
                xoaDatPhong(maSinhVien);
            });
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách sinh viên: " + ex.getMessage());
    }
}

    
    
    private void suaDatPhong(String maSinhVien, String tenSinhVien, int thoiGianO) {
        // Mở hộp thoại cho phép người dùng nhập lại thời gian ở
        String newThoiGian = JOptionPane.showInputDialog(this, "Nhập thời gian ở mới (tháng):", thoiGianO);
        if (newThoiGian != null && !newThoiGian.isEmpty()) {
            try {
                int newThoiGianO = Integer.parseInt(newThoiGian);
                if (newThoiGianO > 0) {
                    // Cập nhật thông tin thời gian ở của sinh viên
                    String query = """
                            UPDATE dat_phong 
                            SET tong_thoi_gian = ?
                            WHERE id_sinh_vien = ?;
                        """;
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setInt(1, newThoiGianO);
                        stmt.setString(2, maSinhVien);
                        stmt.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Cập nhật thông tin thành công!");
                        loadDanhSachPhong(); // Cập nhật lại danh sách phòng
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Thời gian phải lớn hơn 0!");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi sửa thông tin: " + ex.getMessage());
            }
        }
    }

    
private void xoaDatPhong(String maSinhVien) {
    int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa đặt phòng của sinh viên này?", "Xóa đặt phòng", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu transaction

            // Cập nhật trạng thái đặt phòng thành trạng_thai = 3 (ví dụ: trạng thái 'đã hủy' hoặc 'đã xóa')
            String updateDatPhongQuery = """
                    UPDATE dat_phong 
                    SET trang_thai = 3
                    WHERE id_sinh_vien = ?;
                """;
            try (PreparedStatement stmt = conn.prepareStatement(updateDatPhongQuery)) {
                stmt.setString(1, maSinhVien);
                int rowsUpdated = stmt.executeUpdate();

                if (rowsUpdated > 0) {
                    // Nếu thành công, cập nhật số lượng sinh viên trong phòng giảm đi 1
                    String updatePhongQuery = """
                            UPDATE phong
                            SET so_luong_sv = so_luong_sv - 1
                            WHERE id = (SELECT id_phong FROM dat_phong WHERE id_sinh_vien = ? LIMIT 1);
                        """;
                    try (PreparedStatement stmtPhong = conn.prepareStatement(updatePhongQuery)) {
                        stmtPhong.setString(1, maSinhVien);
                        stmtPhong.executeUpdate();

                        conn.commit(); // Commit transaction
                        JOptionPane.showMessageDialog(this, "Xóa đặt phòng thành công!");
                        loadDanhSachPhong(); // Cập nhật lại danh sách phòng
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy đặt phòng của sinh viên này!");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thay đổi trạng thái đặt phòng: " + ex.getMessage());
        }
    }
}


private static void capNhatTrangThaiDatPhong() {
    String query = """
            UPDATE dat_phong 
            SET trang_thai = 2  
            WHERE trang_thai = 1  -- Đang trong trạng thái đặt phòng
            AND (ngay_vao + INTERVAL tong_thoi_gian MONTH) < NOW();  -- Kiểm tra nếu đã hết thời gian 
        """;

    // Sử dụng try-with-resources để đảm bảo đóng kết nối và statement
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        int rowsUpdated = stmt.executeUpdate(); // Cập nhật dữ liệu vào cơ sở dữ liệu
        System.out.println("Cập nhật thành công " + rowsUpdated + " dòng.");
        
    } catch (SQLException e) {
        e.printStackTrace(); // In ra thông báo lỗi nếu có ngoại lệ
    }
}

public static void batDauCapNhat() {
    // Cập nhật mỗi 24 giờ
    Timer timer = new Timer(24 * 60 * 60 * 1000, e -> capNhatTrangThaiDatPhong());
    timer.start();
}

public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new QL_DatPhong().setVisible(true));
    batDauCapNhat(); // Gọi phương thức để bắt đầu cập nhật
}

}
