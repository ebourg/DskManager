package dskmanager;

import java.awt.BorderLayout;

import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

public class DskManagerEditor extends JFrame {

	JPanel bottomMenu = new JPanel();
	private JButton buttonNew = new JButton("New");
	private JButton buttonLoad = new JButton("Load");
	private JTable table;
	private static DskManagerEditor jFrame;
	public DskManagerEditor() {
		super("CPC Dsk Manager");
		
		setLayout(new BorderLayout());
		
		DefaultTableModel model = new DefaultTableModel();
		table=new JTable(model);
		JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		table.setTransferHandler(new TransferHelper(table));
        table.setDragEnabled(true);
        table.setDropMode(DropMode.USE_SELECTION);
//        table.setRowSelectionAllowed(true);
//        table.setCellSelectionEnabled(true);
		
		add(scrollPane,BorderLayout.CENTER);
		add(table.getTableHeader(), BorderLayout.NORTH);
		add(bottomMenu,BorderLayout.SOUTH);
		bottomMenu.setLayout(new BorderLayout());
		bottomMenu.add(buttonNew,BorderLayout.WEST);
		bottomMenu.add(buttonLoad,BorderLayout.EAST);
		
		model.addColumn("Filename");
		model.addColumn("Size");
		
		model.addRow(new Object[]{"TRON.BAS", "2KB"});
		model.addRow(new Object[]{"FRUITY.BAS", "3KB"});
	
		model.removeRow(0);
	}
	
	public static void main(String[] args) {
		jFrame = new DskManagerEditor();
		jFrame.setSize(300, 200);
		jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		jFrame.setVisible(true);
	}

}

