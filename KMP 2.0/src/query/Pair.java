package query;

public class Pair<Left, Right> {

	protected Right right;
	protected Left left;

	public Pair(Left left, Right right) {
		this.left = left;
		this.right = right;
	}
}
