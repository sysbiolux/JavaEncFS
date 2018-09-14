package unilu.encFS.exceptions;

public class InvalidStoreName extends Exception {

	public InvalidStoreName(String storename) {
		// TODO Auto-generated constructor stub
		super(storename + "is not a valid encrypted storage folder");
	}
}
