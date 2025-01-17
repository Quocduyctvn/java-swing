package KyTucXa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class Form_Main extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JButton previousButton, nextButton;
    private int currentPage = 1;
    private final int rowsPerPage = 10; // Số hàng hiển thị mỗi trang

    public Form_Main() {
        setTitle("Hệ thống quản lý ký túc xá");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel trái chứa danh mục
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(200, getHeight()));
        
        // Thêm tiêu đề "KIẾN TÚC XÁ" ở trên cùng
        JLabel titleLabel = new JLabel("KIẾN TÚC XÁ");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));  // Chữ đậm, to
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Khoảng cách trên dưới

        leftPanel.add(titleLabel);

        // Thêm các nút danh mục vào panel trái
        String[] categories = {"Quản lý Đặt phòng","Quản lý Dãy phòng", "Quản lý Hạng phòng", "Quản lý Khu vực/ Tầng", "Quản lý Lớp học", "Quản lý Nhân viên", "Quản lý Phòng học", "Quản lý Sinh viên", "Quản lý Tiện nghi", "Quản lý Vai trò"};
        for (String category : categories) {
            JButton button = new JButton(category);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // Đặt chiều rộng cố định cho các nút
            button.setPreferredSize(new Dimension(1200, 40)); // Chiều rộng là 500, chiều cao là 40
            button.setMaximumSize(new Dimension(1200, 40)); // Đảm bảo chiều rộng không thay đổi
            
         

            leftPanel.add(button);
            

            // Gắn ActionListener cho từng nút
            if (category.equals("Quản lý Đặt phòng")) {
                button.addActionListener(e -> openQLDatPhong());
            }
            if (category.equals("Quản lý Dãy phòng")) {
                button.addActionListener(e -> openQLDayForm());
            }
            if (category.equals("Quản lý Hạng phòng")) {
                button.addActionListener(e -> openQLHangPhong());
            }
            if (category.equals("Quản lý Khu vực/ Tầng")) {
                button.addActionListener(e -> openQLKhuVuc());
            }
            if (category.equals("Quản lý Lớp học")) {
                button.addActionListener(e -> openQLLop());
            }
            if (category.equals("Quản lý Nhân viên")) {
                button.addActionListener(e -> openQLNhanVien());
            }
            if (category.equals("Quản lý Phòng học")) {
                button.addActionListener(e -> openQLPhongHoc());
            }
            if (category.equals("Quản lý Sinh viên")) {
                button.addActionListener(e -> openQLSinhVien());
            }
            if (category.equals("Quản lý Tiện nghi")) {
                button.addActionListener(e -> openQLTienNghi());
            }
            if (category.equals("Quản lý Vai trò")) {
                button.addActionListener(e -> openQLVaiTro());
            }
        }
  

     // Panel phải chứa bảng dữ liệu
        JPanel rightPanel = new JPanel(new BorderLayout());

        // Thêm thông tin liên hệ/slogan ở dưới cùng bên phải
        JPanel infoPanel = new JPanel(new GridBagLayout()); // Căn giữa
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER; // Căn giữa

        // Tạo label với chữ in hoa và to
        JLabel contactLabel = new JLabel("LIÊN HỆ: 0123-456-789 | EMAIL: KYTUCXA@UNIVERSITY.EDU.VN");
        contactLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Chữ to, in đậm
        contactLabel.setBorder(BorderFactory.createEmptyBorder(140, 0, 0, 0)); 

        gbc.gridy++;
        JLabel sloganLabel = new JLabel("\"NƠI KHỞI ĐẦU CHO NHỮNG ƯỚC MƠ\"");
        sloganLabel.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 16)); // Chữ to, in nghiêng

        // Thêm vào panel
        infoPanel.add(contactLabel, gbc);
        gbc.gridy++;
        infoPanel.add(sloganLabel, gbc);

        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Thêm khoảng cách

        // Thêm infoPanel vào dưới cùng bên phải
        rightPanel.add(infoPanel, BorderLayout.NORTH);


   

        // Thêm panel trái và phải vào form chính
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        // Tải dữ liệu trang đầu tiên
        loadPage(currentPage);

        setSize(1200, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    

    private void openQLDatPhong() {
    	QL_DatPhong frame = new QL_DatPhong();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }
    
    // Hàm mở form QL_Day
    private void openQLDayForm() {
        QL_Day frame = new QL_Day();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }
    private void openQLHangPhong() {
        QL_HangPhong frame = new QL_HangPhong();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }
    private void openQLKhuVuc() {
        new QL_KhuVuc(); // Khởi tạo và mở form QL_KhuVuc
        QL_Day frame = new QL_Day();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }
    private void openQLLop() {
    	QL_Lop frame = new QL_Lop();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }
    private void openQLSinhVien() {
        QL_SinhVien frame = new QL_SinhVien();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }
    private void openQLNhanVien() {
        QL_NhanVien frame = new QL_NhanVien();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }
    
    private void openQLPhongHoc() {
        QL_PhongHoc frame = new QL_PhongHoc();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    private void openQLTienNghi() {
        QL_TienNghi frame = new QL_TienNghi();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }
    private void openQLVaiTro() {
        QL_VaiTro frame = new QL_VaiTro();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    // Hàm tải dữ liệu theo trang
    private void loadPage(int page) {
        
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Form_DangNhap()); // Hiển thị Form_DangNhap đầu tiên
    }
}