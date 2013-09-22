package gLTS;


public class NoStateExistException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	State s;
	public LTS lts;

	public NoStateExistException(State s2, LTS lts)
	{
		this.s = s2;
		this.lts = lts;
	}
}
