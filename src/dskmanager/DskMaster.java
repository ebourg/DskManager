package dskmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * un DskManager, personnel � un DskFile
 * @author Joe
 *
 */
public class DskMaster {
	int [] sectorSizes = new int[] {0x80,0x100,0x200,0x400,0x800,0x1000,0x1800};
	// dictionary
//	byte[]usedSectorEntry={};
	// contient aussi C1 C2 C3 C4
	List<DskSector> allSectors = new ArrayList<DskSector>();
	List<Integer> allCatsId= new ArrayList<Integer>();
	List<DskSector> allCatsSector= new ArrayList<DskSector>();
	
	DskType type;

	public DskMaster() {
	}
	
	DskSector find0F(DskTrack track, int sectorId) {
		for (DskSector sector:track.sectors) {
			if ((sector.sectorIdR & 0x0F)==(sectorId & 0x0F)) {
				return sector;
			}
		}
		return null;
	}
	
	/**
	 * Repli allCats au passage
	 * @param entriesSector
	 * @param sectors
	 * @return
	 */
	public List<Integer> findCatsId(byte[] entriesSector) {
		List<Integer> cats= new ArrayList<Integer>();
		int k=2;
		if (type==DskType.SS40) {
			k=2; // min(catId)
		} else if (type==DskType.DOSD2) {
			k=4; // min(catId)
		}
		
		List<DskSector> allCSectors = new ArrayList<DskSector>(allSectors);
		Collections.sort(allCSectors, new Comparator<DskSector>() {
			@Override
			public int compare(DskSector o1, DskSector o2) {
				if (o1.trackC<o2.trackC) {
					return -1;
				}
				if (o1.trackC>o2.trackC) {
					return 1;
				}
				if (o1.sideH<o2.sideH) {
					return -1;
				}
				if (o1.sideH>o2.sideH) {
					return 1;
				}
				if (o1.sectorIdR<o2.sectorIdR) {
					return -1;
				}
				if (o1.sectorIdR>o2.sectorIdR) {
					return 1;
				}
				return 0;
			}
		});

		for (DskSector sector : allCSectors) {
			if (!(sector instanceof DskSectorCatalogs)) {
				int pair=0;
				for (byte b : entriesSector) {
					pair=(pair+1)%2;
					if (type==DskType.DOSD2 && pair==0) {
						//FIXME : peut �tre plus gros que 0xFF normalement, moi je coupe. 
						continue;
					}
					if ((b & 0xff) ==k) {
						cats.add((int)b);
						allCatsId.add((int)b);
					}
				}
				k++;
			}
		}
		if (cats.size()==0) {
			System.out.println("rien dans ce catalog");
		}
		return cats;
	}
	
	/**
	 * Repli allCats au passage
	 * @param entriesSector
	 * @param sectors
	 * @return
	 */
	public List<DskSector> findCatsSector(byte[] entriesSector) {
		List<DskSector> cats= new ArrayList<DskSector>();
		float k=2;
		if (type==DskType.SS40) {
			k=2; // min(catId)
		} else if (type==DskType.DOSD2) {
			k=4; // min(catId)
		}
		
		List<DskSector> allCSectors = new ArrayList<DskSector>(allSectors);
		Collections.sort(allCSectors, new Comparator<DskSector>() {
			@Override
			public int compare(DskSector o1, DskSector o2) {
				if (o1.trackC<o2.trackC) {
					return -1;
				}
				if (o1.trackC>o2.trackC) {
					return 1;
				}
				if (o1.sideH<o2.sideH) {
					return -1;
				}
				if (o1.sideH>o2.sideH) {
					return 1;
				}
				if (o1.sectorIdR<o2.sectorIdR) {
					return -1;
				}
				if (o1.sectorIdR>o2.sectorIdR) {
					return 1;
				}
				return 0;
			}
		});

		for (DskSector sector : allCSectors) {
			if (!(sector instanceof DskSectorCatalogs)) {
				int pair=0;
				for (byte b : entriesSector) {
					pair=(pair+1)%2;
					if (type==DskType.DOSD2 && pair==0) {
						//FIXME : peut �tre plus gros que 0xFF normalement, moi je coupe. 
						continue;
					}
					if ((b & 0xff) == Math.floor(k)) {
						cats.add(sector);
						allCatsSector.add(sector);
					}
				}
				k+=0.5;
			}
		}
		if (cats.size()==0) {
			System.out.println("rien dans ce catalog sectors list");
		}
		return cats;
	}
	
	/**
	 * recherche un cat libre et pas dans C1-C4
	 * @return
	 */
	public NewFreeCatResult nextFreeCat() {
		NewFreeCatResult cats= new NewFreeCatResult();
		// catId � 2 car les cats C1(k==0) et C2(k==0) sont fig� pour le CAT
		if (type==DskType.SS40) {
			cats.catId=2; // min(catId)
		} else if (type==DskType.DOSD2) {
			cats.catId=4; // min(catId)
		}
		int catIdModulo2=2;
		
		List<DskSector> allCSectors = new ArrayList<DskSector>(allSectors);
		Collections.sort(allCSectors, new Comparator<DskSector>() {
			@Override
			public int compare(DskSector o1, DskSector o2) {
				if (o1.trackC<o2.trackC) {
					return -1;
				}
				if (o1.trackC>o2.trackC) {
					return 1;
				}
				if (o1.sideH<o2.sideH) {
					return -1;
				}
				if (o1.sideH>o2.sideH) {
					return 1;
				}
				if (o1.sectorIdR<o2.sectorIdR) {
					return -1;
				}
				if (o1.sectorIdR>o2.sectorIdR) {
					return 1;
				}
				return 0;
			}
		});
		
		
		for (int i=0;i<allCSectors.size();i++) {
			DskSector sector= allCSectors.get(i);
			if (!(sector instanceof DskSectorCatalogs)) {
				if (!allCatsId.contains(cats.catId)) {
//					allCats.put(k,sector);
					allCatsId.add((int)cats.catId);
					
					// et le suivant est un DskSectorCatalogs
					DskSector nextSector=allCSectors.get(i+1);
					if (nextSector instanceof DskSectorCatalogs) {
						// avoir confiance au tri de la liste allCSectors
						System.out.println("galere");
					}

					allCatsSector.add(allCSectors.get(i));
					// et le suivant 1 catsId <=> 2 catsSector
					allCatsSector.add(allCSectors.get(i+1));
					if (type==DskType.DOSD2) {
						allCatsSector.add(allCSectors.get(i+2));
						allCatsSector.add(allCSectors.get(i+3));
//						allCatsSector.add(allCSectors.get(i+4));
//						allCatsSector.add(allCSectors.get(i+5));
//						allCatsSector.add(allCSectors.get(i+6));
//						allCatsSector.add(allCSectors.get(i+7));
					}
					cats.catSectors.add(allCSectors.get(i));
					// et le suivant 1 catsId <=> 2 catsSector
					cats.catSectors.add(allCSectors.get(i+1));
					if (type==DskType.DOSD2) {
						cats.catSectors.add(allCSectors.get(i+2));
						cats.catSectors.add(allCSectors.get(i+3));
//						cats.catSectors.add(allCSectors.get(i+4));
//						cats.catSectors.add(allCSectors.get(i+5));
//						cats.catSectors.add(allCSectors.get(i+6));
//						cats.catSectors.add(allCSectors.get(i+7));
					}
					return cats;
				}
				
				catIdModulo2++;
				if (catIdModulo2%2==0) {
					cats.catId++;
				}
			}
		}
		return cats;
	}
	
	
	public String arrayToString(byte[] bufferHeader) {
		
		StringBuilder sb = new StringBuilder();
		for (byte b: bufferHeader) {
			char ch = (char)b;
			sb.append(ch);
		}

		return sb.toString();
	}
	
	public String cpcname2realname(String cpcname) {
    	String realname=cpcname.substring(0,8)+"."+cpcname.substring(8,11);
    	realname=realname.replaceAll(" ", "");
    	return realname;
    }

	public String realname2cpcname(String realname) {
    	String cpcname = realname.toUpperCase();
    	if (cpcname.contains(".")) {
            int point = cpcname.indexOf(".");
            String filename = cpcname.substring(0, point);
            filename = filename + "        ";
            filename = filename.substring(0, 8);
            String extension = cpcname.substring(point + 1,
                    cpcname.length());
            extension = extension + "   ";
            extension = extension.substring(0, 3);

            cpcname = filename + extension;
        } else {
            cpcname = cpcname + "        " + "   ";
            cpcname = cpcname.substring(0, 8 + 3);
        }
    	return cpcname;
    }

	
	
	
	public boolean catalogToCreate(int trackC, int sideH,int sectorIdR) {
		if (type==DskType.SS40) {
			if (trackC==0 && sideH==0 && (sectorIdR & 0x0F)<=4) {
				return true;
			}
		} else if (type==DskType.DOSD2) {
			if (trackC==0 && sideH==0) {
				return true;
			}
			if (trackC==0 && sideH==1 && (sectorIdR & 0x0F)<=7) {
				return true;
			}
		}
		return false;
	}

	public List<DskSectorCatalogs> buildCatalogs(List<DskTrack> tracks) {
		List<DskSectorCatalogs>catalogs= new ArrayList<DskSectorCatalogs>(); 
		if (type==DskType.SS40) {
			DskTrack track0 = tracks.get(0);
			catalogs.add((DskSectorCatalogs) find0F(track0,0xC1));
			catalogs.add((DskSectorCatalogs) find0F(track0,0xC2));
			catalogs.add((DskSectorCatalogs) find0F(track0,0xC3));
			catalogs.add((DskSectorCatalogs) find0F(track0,0xC4));
		} else if (type==DskType.DOSD2) {
			DskTrack track0 = tracks.get(0);
			DskTrack track0side1 = tracks.get(1);
			catalogs.add((DskSectorCatalogs) find0F(track0,0x21));
			catalogs.add((DskSectorCatalogs) find0F(track0,0x22));
			catalogs.add((DskSectorCatalogs) find0F(track0,0x23));
			catalogs.add((DskSectorCatalogs) find0F(track0,0x24));
			catalogs.add((DskSectorCatalogs) find0F(track0,0x25));
			catalogs.add((DskSectorCatalogs) find0F(track0,0x26));
			catalogs.add((DskSectorCatalogs) find0F(track0,0x27));
			catalogs.add((DskSectorCatalogs) find0F(track0,0x28));
			catalogs.add((DskSectorCatalogs) find0F(track0,0x29));
			catalogs.add((DskSectorCatalogs) find0F(track0side1,0x21));
			catalogs.add((DskSectorCatalogs) find0F(track0side1,0x22));
			catalogs.add((DskSectorCatalogs) find0F(track0side1,0x23));
			catalogs.add((DskSectorCatalogs) find0F(track0side1,0x24));
			catalogs.add((DskSectorCatalogs) find0F(track0side1,0x25));
			catalogs.add((DskSectorCatalogs) find0F(track0side1,0x26));
			catalogs.add((DskSectorCatalogs) find0F(track0side1,0x27));
		}
		return catalogs;
	}

}
