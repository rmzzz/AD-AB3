package ab3.impl.KuparSiarheyeuIsmailov;

import ab3.Ab3;

public class Ab3Impl implements Ab3 {

	/*

	     (1)
	  (2)   (3)
	(4)(5) (6)(7)

	LWR: 4 2 5 1 6 3 7
	WLR: 1 2 4 5 3 6 7

	     (1)
	  (2)   (3)
	(4)       (7)

	LWR: 4 2 1 3 7
	WLR: 1 2 4 3 7

	 */
	@Override
	public Tree reconstructTree(int[] inorder, int[] preorder) {
		// LWR-(inorder) und WLR-(preorder)
		// YOUR CODE HERE
		Tree tree = new Tree();
		tree.size = inorder.length;

		tree.root = reconstructNode(inorder, 0, inorder.length - 1, preorder, 0, preorder.length - 1);

		return tree;
	}

	Node reconstructNode(int[] inorder, int iFrom, int iTo, int[] preorder, int pFrom, int pTo) {
		if (pFrom < 0 || pTo < 0 || pTo >= preorder.length || iFrom > iTo || pFrom > pTo)
			return null;

		int key = preorder[pFrom];
		Node node = new Node();
		node.key = key;

		int posInorderRoot = indexOf(key, inorder, iFrom, iTo);

		int leftSize = posInorderRoot - iFrom;
		node.left = reconstructNode(inorder, iFrom, posInorderRoot - 1,
				preorder, pFrom + 1, pFrom + leftSize);

		node.right = reconstructNode(inorder, posInorderRoot + 1, iTo,
				preorder, pFrom + leftSize + 1, pTo);

		return node;
	}

	static int indexOf(int key, int[] array, int from, int to) {
		for (int i = from; i <= to; i++) {
			if (array[i] == key) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public int farthestVertex(int startingVertex, int[][] adjacencyCostMatrix) {
		Dijkstra d = new Dijkstra(adjacencyCostMatrix);

		// YOUR CODE HERE
		return d.getFarthestVertexDistance(startingVertex);
	}


	@Override
	public byte[] lzwEncode(byte[] data, int bits) {
		// YOUR CODE HERE
		return LzwCodec.encode(data, bits);
	}

	@Override
	public byte[] lzwDecode(byte[] data, int bits) {
		// YOUR CODE HERE
		return LzwCodec.decode(data, bits);
	}
}
