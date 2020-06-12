package dskmanager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class DskSectorCatalogs extends DskSector {
	
	List<DskSectorCatalog> cats = new ArrayList<DskSectorCatalog>();
	public DskSectorCatalogs(DskMaster master,int track, int sectorId) {
		super(master,track,sectorId);
	}

	/**
	 * 
	 * @param fos
	 * @param fileName
	 * @return > zero si pas assez de jocker FIXME
	 * @throws IOException
	 */
	public void scanCatalog(FileChannel channel, String fileName, List<DskSector> listSector) throws IOException {
		// catEntry is not data's target of entry.
		DskSectorCatalog cat = new DskSectorCatalog(master);
		cat.sectors=listSector;
		cat.filename=fileName;
		if (cat.sectors.size()>0x10) {
			System.out.println("�a ne tient pas dans C1, faudra utiliser C2-C4");
		}
		cat.scan(channel.position(0x200+cats.size()*0x20), fileName);
		cats.add(cat);
	}

	
	public String toString() {
		String s="DskSectorCatalogs "+String.format("#%02X", sectorIdR)+" with "+cats.size()+" cats\n";
		for (DskSectorCatalog cat:cats) {
			s+="cat "+cat.toString();
		}
		return s+super.toString();
	}

	public void scanCatalog() throws IOException {
		// fill cats from data
		ByteArrayInputStream bis=new ByteArrayInputStream(data);
		DskSectorCatalog cat = new DskSectorCatalog(master);
		for (int c=0;c<data.length/8;c++) {
			cat.scan(bis);
			cats.add(cat);
		}
		
	}
}
