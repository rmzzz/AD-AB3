package ab3.impl.KuparSiarheyeuIsmailov;

public class Dijkstra {
    static class Node {
        int id;
        int distance;
        Node prev;
        boolean selected;

        public Node(int id, int distance) {
            this.id = id;
            this.distance = distance;
        }
    }
    final int[][] adjacencyCostMatrix;
    final int size;

    public Dijkstra(int[][] adjacencyCostMatrix) {
        this.adjacencyCostMatrix = adjacencyCostMatrix;
        size = adjacencyCostMatrix.length;
    }

    public int getFarthestVertexDistance(int startingVertex) {
        Node[] nodes = new Node[size];
        for (int i = 0; i < size; i++) {
            nodes[i] = new Node(i, Integer.MAX_VALUE);
        }

        Node s = new Node(startingVertex, 0);
        s.prev = s;
        s.selected = true;

        boolean[] rand = new boolean[size];
        expand(s, nodes, rand);

        while(!isEmpty(rand)) {
            int nearestIdx = -1;
            for(int i = 0; i < size; i++) {
                if(rand[i] && (nearestIdx == -1 || nodes[i].distance < nodes[nearestIdx].distance)) {
                    nearestIdx  = i;
                }
            }
            rand[nearestIdx] = false;

            expand(nodes[nearestIdx], nodes, rand);
        }

        int farthestDistance = 0;
        for(int i = 0; i < size; i++) {
            if(nodes[i].distance > farthestDistance) {
                farthestDistance = nodes[i].distance;
            }
        }
        return farthestDistance;
    }

    void expand(Node v, Node[] nodes, boolean[] rand) {
        for (int i = 0; i < size; i++) {
            int c = adjacencyCostMatrix[v.id][i];
            if(c != 0) {
                if(!nodes[i].selected && v.distance + c < nodes[i].distance) {
                    nodes[i].prev = v;
                    nodes[i].distance = v.distance + c;
                    rand[i] = true;
                }
            }
        }
    }

    boolean isEmpty(boolean[] rand) {
        for (boolean b : rand) {
            if(b)
                return false;
        }
        return true;
    }
}
