package dskmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class DskManager {
	
	
	private static DskManager instance=null;
	public static DskManager getInstance(){
		if (instance==null) {instance=new DskManager();}
		return instance;
	}
	
	
	public DskFile newDsk(File currentDir, String dskName) throws IOException{
		DskFile dskFile=new DskFile(currentDir, dskName);
		dskFile.master=new DskMaster();
		FileOutputStream fos= new FileOutputStream(dskFile.file);
		dskFile.scan(fos);
		dskFile.master.allSectors.clear();
		for (int i=0; i<dskFile.nbTracks; i++) {
			DskTrack dskTrack = new DskTrack(dskFile.master, i);
			dskFile.tracks.add(dskTrack);
			dskTrack.scan(fos);
			
			for (int j=0;j<dskTrack.nbSectors;j++) {
				if (dskFile.master.sectorId[j] <= 0xC4) {
					DskSectorCatalogs sector = new DskSectorCatalogs(dskFile.master, i, dskFile.master.sectorId[j]);
					sector.scan(fos);
					dskTrack.sectors.add(sector);
					dskFile.master.allSectors.add(sector);
				} else {
					DskSector sector = new DskSector(dskFile.master, i, dskFile.master.sectorId[j]);
					sector.scan(fos);
					dskTrack.sectors.add(sector);
					dskFile.master.allSectors.add(sector);
				}
					
			}
			//garbage "0" at end of Track-Info
			int garbage=0x1D-dskTrack.nbSectors;
			for (int j=0;j<garbage;j++) {
				for (int k=0;k<8;k++) {
					fos.write(0);
				}
			}
			
			// garbage "E5" as data of each sector
			for (int j=0;j<dskTrack.nbSectors;j++) {
				dskTrack.sectors.get(j).data=new byte[dskFile.master.sectorSizes[dskTrack.sectorSize]];
				for (int k=0;k<dskFile.master.sectorSizes[dskTrack.sectorSize];k++) {
					dskTrack.sectors.get(j).data[k]=((Integer)dskTrack.fillerByte).byteValue();
				}
				dskTrack.sectors.get(j).scanData(fos);
			}
			
			
			
		}
		fos.close();
		// cats : on attache les secteurs point� par la liste de sector cat
		DskTrack track0 = dskFile.tracks.get(0);
		DskSectorCatalogs sectorCatalogC1 = (DskSectorCatalogs) dskFile.master.find(track0,0xC1);
		sectorCatalogC1.scanCatalog();
		DskSectorCatalogs sectorCatalogC2 = (DskSectorCatalogs) dskFile.master.find(track0,0xC2);
		sectorCatalogC2.scanCatalog();
		DskSectorCatalogs sectorCatalogC3 = (DskSectorCatalogs) dskFile.master.find(track0,0xC3);
		sectorCatalogC3.scanCatalog();
		DskSectorCatalogs sectorCatalogC4 = (DskSectorCatalogs) dskFile.master.find(track0,0xC4);
		sectorCatalogC4.scanCatalog();
		return dskFile;
	}
	
	public DskFile loadDsk(File currentDir, String dskName) throws IOException {
		DskFile dskFile=new DskFile(currentDir, dskName);
		FileInputStream fis = new FileInputStream(dskFile.file);
		dskFile.scan(fis);
		dskFile.master.allSectors.clear();
		for (int i=0; i<dskFile.nbTracks; i++) {
			DskTrack dskTrack= new DskTrack(dskFile.master,i);
			dskFile.tracks.add(dskTrack);
			System.out.println("avant scan sdkTrack : "+fis.getChannel().position());
			dskTrack.scan(fis);
			for (int j=0;j<dskTrack.nbSectors;j++) {
				if (dskFile.master.sectorId[j] <= 0xC4) {
					DskSector sector = new DskSectorCatalogs(dskFile.master,i, dskFile.master.sectorId[j]);
					System.out.println("avant scan sector : "+fis.getChannel().position());
					sector.scan(fis);
					dskTrack.sectors.add(sector);
					dskFile.master.allSectors.add(sector);
				} else {
					DskSector sector = new DskSector(dskFile.master,i, dskFile.master.sectorId[j]);
					System.out.println("avant scan sector : "+fis.getChannel().position());
					sector.scan(fis);
					dskTrack.sectors.add(sector);
					dskFile.master.allSectors.add(sector);
				}
					
			}
			System.out.println("garbage 0 debut : "+fis.getChannel().position());
			fis.skip(160);//0x100-0x60-dskTrack.nbSectors*8); // skip 0x00
			
			System.out.println("garbage 0 fin : "+fis.getChannel().position());
			for (DskSector sector : dskTrack.sectors) {
				System.out.println("avant scanData sector : "+fis.getChannel().position());
				sector.scanData(fis);
			}
			System.out.print("haouh");
		}
		fis.close();
		// cats : on attache les secteurs point� par la liste de sector cat
		DskTrack track0 = dskFile.tracks.get(0);
		DskSectorCatalogs sectorCatalogC1 = (DskSectorCatalogs) dskFile.master.find(track0,0xC1);
		sectorCatalogC1.scanCatalog();
		DskSectorCatalogs sectorCatalogC2 = (DskSectorCatalogs) dskFile.master.find(track0,0xC2);
		sectorCatalogC2.scanCatalog();
		DskSectorCatalogs sectorCatalogC3 = (DskSectorCatalogs) dskFile.master.find(track0,0xC3);
		sectorCatalogC3.scanCatalog();
		DskSectorCatalogs sectorCatalogC4 = (DskSectorCatalogs) dskFile.master.find(track0,0xC4);
		sectorCatalogC4.scanCatalog();
		
		return dskFile;
	}

	public void addFile(DskFile dskFile, File currentDir, String fileName, boolean generateAMSDOSHeader) throws IOException {
		DskTrack track0 = dskFile.tracks.get(0);
		System.out.println("R�cup�ration de C1-C4");
		DskSectorCatalogs sectorCatalogC1 = (DskSectorCatalogs) dskFile.master.find(track0,0xC1);
		System.out.println(sectorCatalogC1);
		DskSectorCatalogs sectorCatalogC2 = (DskSectorCatalogs) dskFile.master.find(track0,0xC2);
		DskSectorCatalogs sectorCatalogC3 = (DskSectorCatalogs) dskFile.master.find(track0,0xC3);
		DskSectorCatalogs sectorCatalogC4 = (DskSectorCatalogs) dskFile.master.find(track0,0xC4);
		// search entry free space
		RandomAccessFile fos = new RandomAccessFile(dskFile.file, "rw");
//		fos.getChannel().position(0x100); //header
		//Track-info
//		; // first Track-info
		int nbEntry = (int)(dskFile.file.length()/(16*1024)); // each 16KB
		int lastEntry = (int)(dskFile.file.length()%(16*1024));

		FileInputStream fis=new FileInputStream(dskFile.file);
		List<DskSector> listSector=new ArrayList<DskSector>();
		
//		allCats=dskFile.master.searchAllCat(dskFile.master.allSectors);
		for (int i=0;i<=nbEntry;i++) {
			if (i<nbEntry || (i==nbEntry && lastEntry <i)) {
				byte [] data=new byte[Math.min(512,fis.available())];
				fis.read(data);
				DskSector d=dskFile.master.nextFreeSector();
				d.data=data;
				listSector.add(d);
			}
		}
		
		sectorCatalogC1.scanCatalog(fos.getChannel().position(0x200),fileName);
		System.out.println("Apr�s : "+sectorCatalogC1);
		fis.close();
		
		
//		dskFile=new DskFile(currentDir, fileName);

		// garbage
//		for (int k=0;k<0x200-0x160;k++) {
//			fos.write(0);					
//		}
		
		for (int j=0;j<track0.nbSectors;j++) {
			for (int k=0;k<dskFile.master.sectorSizes[0x02];k++) {
				fos.write(track0.fillerByte);
			}
		}
		
		fos.close();
		
	}
	
}
