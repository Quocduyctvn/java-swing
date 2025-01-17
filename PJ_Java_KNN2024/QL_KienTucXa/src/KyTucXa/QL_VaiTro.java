package KyTucXa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import DTOs.QuyenDAO;
import DTOs.VaiTroDAO;
import Data.DatabaseConnection;
import Data.Quyen;
import Data.VaiTro;

public class QL_VaiTro extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, perButton;
    private JLabel totalLabel;
    private JButton nextButton, prevButton;
    private JLabel pageLabel;

    private int currentPage = 1;
    private final int rowsPerPage = 10;
    private List<VaiTro> dataList;

    public QL_VaiTro() {
        setTitle("Quản lý Vai Trò");
        initComponents();
        loadVaiTroData();

        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Bảng và Cột
        String[] columnNames = { "ID", "STT", "Tên Vai Trò", "Mô Tả", "Ngày tạo" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Panel trên cùng
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Quản lý danh sách Vai Trò", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("Thêm Vai Trò");
        editButton = new JButton("Sửa");
        deleteButton = new JButton("Xóa");
        perButton = new JButton("Chi tiết vai trò");
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(perButton);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Panel dưới cùng
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalLabel = new JLabel("Danh sách này có tổng cộng: 0 bản ghi");
        prevButton = new JButton("<<");
        nextButton = new JButton(">>");
        pageLabel = new JLabel("Trang " + currentPage);

        prevButton.addActionListener(e -> showPage(currentPage - 1));
        nextButton.addActionListener(e -> showPage(currentPage + 1));

        bottomPanel.add(totalLabel);
        bottomPanel.add(prevButton);
        bottomPanel.add(pageLabel);
        bottomPanel.add(nextButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // Các sự kiện nút
        addButton.addActionListener(e -> addVaiTro());
        editButton.addActionListener(e -> editVaiTro());
        deleteButton.addActionListener(e -> deleteVaiTro());
        perButton.addActionListener(e -> manageRolePermissions());
    }

    private void loadVaiTroData() {
        dataList = VaiTroDAO.getAllVaiTro(); // Replace with actual DAO for VaiTro
        int totalItems = dataList.size();
        int totalPages = (int) Math.ceil((double) totalItems / rowsPerPage);

        int startIndex = (currentPage - 1) * rowsPerPage;
        int endIndex = Math.min(startIndex + rowsPerPage, totalItems);

        tableModel.setRowCount(0);
        for (int i = startIndex; i < endIndex; i++) {
            VaiTro vt = dataList.get(i);
            tableModel.addRow(new Object[] { vt.getId(), i + 1, vt.getTenVaiTro(), vt.getMoTa(), vt.getNgayTao() });
        }

        totalLabel.setText("Danh sách này có tổng cộng: " + totalItems + " bản ghi");
        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(currentPage < totalPages);
        pageLabel.setText("Trang " + currentPage);
    }

    private void showPage(int page) {
        if (page > 0) {
            currentPage = page;
            loadVaiTroData();
        }
    }

    private void addVaiTro() {
        // Create input fields for role name and description
        JTextField nameField = new JTextField();
        JTextField descriptionField = new JTextField();

        // Create a panel for the input fields
        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Tên vai trò:"));
        panel.add(nameField);
        panel.add(new JLabel("Mô tả:"));
        panel.add(descriptionField);

        // Show the input dialog
        int option = JOptionPane.showConfirmDialog(this, panel, "Thêm Vai Trò", JOptionPane.OK_CANCEL_OPTION);

        // If the user clicks OK, add the new role
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String description = descriptionField.getText().trim();

            // Validate input
            if (name.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tên vai trò và mô tả.");
                return;
            }

            VaiTro newVaiTro = new VaiTro();
            newVaiTro.setTenVaiTro(name);
            newVaiTro.setMoTa(description);
            newVaiTro.setNgayTao(new Timestamp(System.currentTimeMillis())); // Set the creation date

            // Add the new VaiTro to the database
            boolean isAdded = VaiTroDAO.addVaiTro(newVaiTro);
            if (isAdded) {
                JOptionPane.showMessageDialog(this, "Thêm vai trò thành công.");
                loadVaiTroData(); // Reload the data
            } else {
                JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi thêm vai trò.");
            }
        }
    }


    private void editVaiTro() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một vai trò để chỉnh sửa.");
            return;
        }

        VaiTro selectedVaiTro = dataList.get(selectedRow);
        JTextField nameField = new JTextField(selectedVaiTro.getTenVaiTro());
        JTextField descriptionField = new JTextField(selectedVaiTro.getMoTa());

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Tên vai trò:"));
        panel.add(nameField);
        panel.add(new JLabel("Mô tả:"));
        panel.add(descriptionField);

        int option = JOptionPane.showConfirmDialog(this, panel, "Chỉnh sửa Vai Trò", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            selectedVaiTro.setTenVaiTro(nameField.getText());
            selectedVaiTro.setMoTa(descriptionField.getText());

            VaiTroDAO.updateVaiTro(selectedVaiTro);

            loadVaiTroData();
        }
    }

    private void deleteVaiTro() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một vai trò để xóa.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa vai trò này?", "Xóa Vai Trò",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            VaiTro selectedVaiTro = dataList.get(selectedRow);

            boolean isDeleted = VaiTroDAO.deleteVaiTro(selectedVaiTro.getId());
            if (isDeleted) {
                JOptionPane.showMessageDialog(this, "Xóa vai trò thành công.");
                loadVaiTroData();
            } else {
                JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi xóa vai trò.");
            }
        }
    }
    
    
	private void manageRolePermissions() {
	    // Lấy chỉ số hàng đã chọn trong bảng
	    int selectedRow = table.getSelectedRow();
	    if (selectedRow == -1) {
	        JOptionPane.showMessageDialog(this, "Vui lòng chọn một vai trò để quản lý quyền.");
	        return;
	    }
	
	    // Lấy thông tin vai trò đã chọn từ danh sách
	    VaiTro selectedVaiTro = dataList.get(selectedRow);
	
	    // Tạo đối tượng QuyenDAO để tương tác với cơ sở dữ liệu
	    QuyenDAO quyenDAO = new QuyenDAO(DatabaseConnection.getConnection());
	
	    // Lấy tất cả các quyền từ cơ sở dữ liệu
	    List<Quyen> allPermissions = quyenDAO.getAllQuyen();
	    List<Quyen> selectedPermissions = quyenDAO.getPermissionsByVaiTroId(selectedVaiTro.getId());
	
	    // Tạo khung hiển thị
	    JDialog dialog = new JDialog(this, "Quản lý Quyền Vai Trò", true);
	    dialog.setSize(800, 500);
	    dialog.setLocationRelativeTo(this);
	
	    // Sử dụng JScrollPane để hiển thị danh sách checkbox
	    JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10)); // Hiển thị hai cột
	    Map<Integer, JCheckBox> checkBoxMap = new HashMap<>();
	
	    for (Quyen permission : allPermissions) {
	        JCheckBox checkBox = new JCheckBox(permission.getTenQuyen());
	        boolean isSelected = selectedPermissions.stream().anyMatch(p -> p.getId() == permission.getId());
	        checkBox.setSelected(isSelected);
	
	        checkBoxMap.put(permission.getId(), checkBox);
	        panel.add(checkBox);
	    }
	
	    JScrollPane scrollPane = new JScrollPane(panel);
	    dialog.add(scrollPane, BorderLayout.CENTER);
	
	    // Thêm nút Lưu và Hủy
	    JPanel buttonPanel = new JPanel();
	    JButton saveButton = new JButton("Lưu");
	    JButton cancelButton = new JButton("Hủy");
	
	    saveButton.addActionListener(e -> {
	        List<Integer> selectedPermissionIds = new ArrayList<>();
	        for (Map.Entry<Integer, JCheckBox> entry : checkBoxMap.entrySet()) {
	            if (entry.getValue().isSelected()) {
	                selectedPermissionIds.add(entry.getKey());
	            }
	        }
	
	        boolean success = quyenDAO.updatePermissionsForVaiTro(selectedVaiTro.getId(), selectedPermissionIds);
	        if (success) {
	            JOptionPane.showMessageDialog(dialog, "Cập nhật quyền thành công.");
	        } else {
	            JOptionPane.showMessageDialog(dialog, "Cập nhật quyền thất bại.");
	        }
	        dialog.dispose();
	    });
	
	    cancelButton.addActionListener(e -> dialog.dispose());
	
	    buttonPanel.add(saveButton);
	    buttonPanel.add(cancelButton);
	    dialog.add(buttonPanel, BorderLayout.SOUTH);
	
	    // Hiển thị hộp thoại
	    dialog.setVisible(true);
	}
	
	public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QL_VaiTro());
    }
}
