import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

class LocationKDTree {
    private static final int K = 3; // 3-d tree
    private final Node tree;

    public LocationKDTree(final List<Location> locations) {
        final List<Node> nodes = new ArrayList<Node>(locations.size());
        for (final Location location : locations) {
            nodes.add(new Node(location));
        }
        tree = buildTree(nodes, 0);
    }

    
    public Location findNearest(final double latitude, final double longitude) {
        final Node node = findNearest(tree, new Node(latitude, longitude), 0);
        return node == null ? null : node.location;
    }

    private static Node findNearest(final Node current, final Node target, final int depth) {
        final int axis = depth % K;
        final int direction = getComparator(axis).compare(target, current);
        final Node next = (direction < 0) ? current.left : current.right;
        final Node other = (direction < 0) ? current.right : current.left;
        Node best = (next == null) ? current : findNearest(next, target, depth + 1);
        if (current.euclideanDistance(target) < best.euclideanDistance(target)) {
            best = current;
        }
        if (other != null) {
            if (current.verticalDistance(target, axis) < best.euclideanDistance(target)) {
                final Node possibleBest = findNearest(other, target, depth + 1);
                if (possibleBest.euclideanDistance(target) < best.euclideanDistance(target)) {
                    best = possibleBest;
                }
            }
        }
        return best;
    }

    
    private static Node buildTree(final List<Node> items, final int depth) {
        if (items.isEmpty()) {
            return null;
        }

        Collections.sort(items, getComparator(depth % K));
        final int index = items.size() / 2;
        final Node root = items.get(index);
        root.left = buildTree(items.subList(0, index), depth + 1);
        root.right = buildTree(items.subList(index + 1, items.size()), depth + 1);
        return root;
    }

    private static class Node {
        Node left;
        Node right;
        Location location;
        final double[] point = new double[K];

        Node(final double latitude, final double longitude) {
            point[0] = (double) (cos(toRadians(latitude)) * cos(toRadians(longitude)));
            point[1] = (double) (cos(toRadians(latitude)) * sin(toRadians(longitude)));
            point[2] = (double) (sin(toRadians(latitude)));
        }

        Node(final Location location) {
            this(location.latitude, location.longitude);
            this.location = location;
        }

        double euclideanDistance(final Node that) {
            final double x = this.point[0] - that.point[0];
            final double y = this.point[1] - that.point[1];
            final double z = this.point[2] - that.point[2];
            return x * x + y * y + z * z;
        }

        double verticalDistance(final Node that, final int axis) {
            final double d = this.point[axis] - that.point[axis];
            return d * d;
        }
    }

    private static Comparator<Node> getComparator(final int i) {
        return NodeComparator.values()[i];
    }

    private static enum NodeComparator implements Comparator<Node> {
        x {
            
            public int compare(final Node a, final Node b) {
                return Double.compare(a.point[0], b.point[0]);
            }
        },
        y {
            
            public int compare(final Node a, final Node b) {
                return Double.compare(a.point[1], b.point[1]);
            }
        },
        z {
            
            public int compare(final Node a, final Node b) {
                return Double.compare(a.point[2], b.point[2]);
            }
        }
    }
}

