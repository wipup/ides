package ports.soc.ides.controller.helper;

import java.io.File;

import ports.soc.ides.model.DataModel;

public class FileWrapper extends DataModel {

	public static final String[] UNITS = new String[] {"bytes", "KB", "MB", "GB", "TB"};
	
	private static final long serialVersionUID = -8369008969737187700L;

	private File file;
	
	public FileWrapper(File f) {
		this.file = f;
	}
	
	public String getName(){
		return file.getName();
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public File getFile() {
		return file;
	}
	
	@Override
	public String toString() {
		return file.getName() + " " + getSize();
	}
	
	@Override
	public String printDetail() {
		return file.toString();
	}
	
	public String getSize() {		
		long length = file.length();
		if (length <= 0) {
			return "0 byte";
		}
		int unit = 0;
		int power = (int)Math.floor(Math.log(length) / Math.log(1024));
		unit = power;
		if (unit > UNITS.length) {
			unit = UNITS.length - 1;
		}
		return Math.round(length / Math.pow(1024, power)) + " " + UNITS[unit];
	}

}
