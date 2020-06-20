package dskmanager;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;

public class TransferHelper extends TransferHandler {

	private DefaultTableModel model;
	private JTable table;

	public TransferHelper(JTable table) {
		this.model = (DefaultTableModel) table.getModel();
		this.table = table;
		 table.addMouseMotionListener(new MouseMotionListener() {
			    public void mouseDragged(MouseEvent e) {
			        e.consume();
			        JComponent c = (JComponent) e.getSource();
			        exportAsDrag(c, e, TransferHandler.MOVE);
			    }

			    public void mouseMoved(MouseEvent e) {
			    }
			});
	}
    public boolean canImport(TransferHandler.TransferSupport info) {
        // Spammed
//    	System.out.println("canImport?");
        if (!info.isDataFlavorSupported(DataFlavor.stringFlavor) && !info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        	table.setCursor(DragSource.DefaultMoveNoDrop);
            return false;
        }

        JTable.DropLocation dl = (JTable.DropLocation)info.getDropLocation();
        if (dl.getRow() == -1) {
        	table.setCursor(DragSource.DefaultMoveNoDrop);
            return false;
        }
        
        table.setCursor(DragSource.DefaultMoveDrop);
        return true;
    }

    public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop()) {
            return false;
        }
         
        // Check for String flavor
        if (!info.isDataFlavorSupported(DataFlavor.stringFlavor) && !info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            System.out.println("List doesn't accept a drop of this type.");
            return false;
        }
        System.out.println("from Desktop");
        table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        JList.DropLocation dl = (JList.DropLocation)info.getDropLocation();
        DefaultTableModel listModel = model;
        int index = dl.getIndex();
        boolean insert = dl.isInsert();
        // Get the current string under the drop.
        String value = (String)listModel.getValueAt(index,0);

        // Get the string that is being dropped.
        Transferable t = info.getTransferable();
        String data;
        try {
            data = (String)t.getTransferData(DataFlavor.stringFlavor);
        } 
        catch (Exception e) { return false; }
         
        // Display a dialog with the drop information.
        String dropValue = "\"" + data + "\" dropped ";
        if (dl.isInsert()) {
            if (dl.getIndex() == 0) {
                System.out.println(dropValue + "at beginning of list");
            } else if (dl.getIndex() >= model.getRowCount()) {
            	System.out.println(dropValue + "at end of list");
            } else {
                String value1 = (String)model.getValueAt(dl.getIndex() - 1,0);
                String value2 = (String)model.getValueAt(dl.getIndex(),0);
                System.out.println(dropValue + "between \"" + value1 + "\" and \"" + value2 + "\"");
            }
        } else {
        	System.out.println(dropValue + "on top of " + "\"" + value + "\"");
        }
         
/**  This is commented out for the basicdemo.html tutorial page.
         **  If you add this code snippet back and delete the
         **  "return false;" line, the list will accept drops
         **  of type string.
        // Perform the actual import.  
        if (insert) {
            listModel.add(index, data);
        } else {
            listModel.set(index, data);
        }
        return true;
*/
return false;
    }
     
    public int getSourceActions(JComponent c) {
        return COPY;
    }
     
    @Override
    protected Transferable createTransferable(JComponent c) {
    	System.out.println("to Desktop");
    	
//    	JTable list = (JTable)c;
//        int[] values = list.getSelectedRows();
// 
//        StringBuffer buff = new StringBuffer();
//
//        for (int i = 0; i < values.length; i++) {
//            Object val = model.getValueAt(values[i],0);
//            buff.append(val == null ? "" : val.toString());
//            if (i != values.length - 1) {
//                buff.append("\n");
//            }
//        }
        
        List<File> files= new ArrayList<File>();
        files.add(new File("toto.txt"));
		return new FileTransferable(files);
//        return new StringSelection(buff.toString());
    }
	@Override
	protected void exportDone(JComponent c, Transferable t, int act) {
		// spamm� bien pour le cursor
//		
//			try {
//		
//			
//			File data2 = (File) t.getTransferData(DataFlavor.javaFileListFlavor);
//		} catch (UnsupportedFlavorException | IOException e) {
//			e.printStackTrace();
//			try {
//				String data = (String) t.getTransferData(DataFlavor.stringFlavor);
//			} catch (UnsupportedFlavorException | IOException e1) {
//				e1.printStackTrace();
//			}
//		}
				
		
//		System.out.println("exportDone "+act+" "+c);
		if ((act == TransferHandler.MOVE) || (act == TransferHandler.NONE)) {
	       table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	    }
		super.exportDone(c, t, act);
	}
	
	private class FileTransferable implements Transferable {

        private List<File> files;

        public FileTransferable(List<File> files) {
            this.files = files;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.javaFileListFlavor};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(DataFlavor.javaFileListFlavor);
        }

        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return files;
        }
    }

}