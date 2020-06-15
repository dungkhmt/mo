package com.socolabs.mo.components.algorithms.rtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public class BTree {

    public static final int MAX_DEGREE = 5;
    public static final int FULL_KEYS = MAX_DEGREE * 2 - 1;

    class BTreeNode {
        int[] keys;
        int nbKeys;
        BTreeNode[] children;
        boolean isLeaf;

        public BTreeNode(boolean isLeaf) {
            this.isLeaf = isLeaf;
            keys = new int[MAX_DEGREE * 2 - 1];
            children = new BTreeNode[MAX_DEGREE * 2];
            nbKeys = 0;
        }

        public int findKey(int k) {
            int idx=0;
            while (idx < nbKeys && keys[idx] < k) {
                idx++;
            }
            return idx;
        }

        public int getPredecessor(int idx) {
            BTreeNode c = children[idx];
            while (!c.isLeaf) {
                c = c.children[c.nbKeys];
            }
            return c.keys[c.nbKeys - 1];
        }

        public int getSuccesor(int idx) {
            BTreeNode c = children[idx];
            while (!c.isLeaf) {
                c = c.children[0];
            }
            return c.keys[0];
        }
    }

    private BTreeNode root;
    private int checkVal;
    private boolean isError;
    public ArrayList<Integer> lst;
    public int maxDepth;

    public BTree() {
        root = null;
    }

    public void traverse() {
        if (root != null) {
            checkVal = -1;
            isError = false;
            lst = new ArrayList<>();
            maxDepth = 0;
            traverse(root, 0);
            System.out.println();
            if (isError) {
                System.exit(-1);
            }
        }
    }

    private void traverse(BTreeNode p, int depth) {
        if (p != null) {
            if (p.isLeaf) {
                if (maxDepth == 0) {
                    maxDepth = Math.max(maxDepth, depth);
                } else {
                    if (maxDepth != depth) {
                        System.exit(-2);
                    }
                }
            }
            for (int i = 0; i <= p.nbKeys; i++) {
                traverse(p.children[i], depth + 1);
                if (i < p.nbKeys) {
                    if (checkVal > p.keys[i]) {
                        isError = true;
                    }
                    checkVal = p.keys[i];
                    lst.add(p.keys[i]);
                    System.out.print(p.keys[i] + " ");
                }
            }
        }
    }

    public void add(int k) {
        if (root == null) {
            root = new BTreeNode(true);
            root.keys[root.nbKeys++] = k;
        } else {
            if (root.nbKeys == FULL_KEYS) {
                BTreeNode newNode = split(root);
                BTreeNode newRoot = new BTreeNode(false);
                newRoot.keys[0] = root.keys[MAX_DEGREE - 1];
                newRoot.children[0] = root;
                newRoot.children[1] = newNode;
                newRoot.nbKeys = 1;
                root = newRoot;
                if (root.keys[0] < k) {
                    add(root.children[1], k);
                } else {
                    add(root.children[0], k);
                }
            } else {
                add(root, k);
            }
        }
    }

    private void add(BTreeNode p, int k) {
        int i = p.nbKeys - 1;
        if (p.isLeaf) {
            while (i >= 0 && p.keys[i] > k) {
                p.keys[i + 1] = p.keys[i];
                i--;
            }
            p.keys[i + 1] = k;
            p.nbKeys++;
        } else {
            while (i >= 0 && p.keys[i] > k) {
                i--;
            }
            i++;
            if (p.children[i].nbKeys == FULL_KEYS) {
                BTreeNode newNode = split(p.children[i]);
                for (int j = p.nbKeys; j > i; j--) {
                    p.children[j + 1] = p.children[j];
                    p.keys[j] = p.keys[j - 1];
                }
                p.keys[i] = p.children[i].keys[MAX_DEGREE - 1];
                p.children[i + 1] = newNode;
                p.nbKeys++;
                if (p.keys[i] < k) {
                    i++;
                }
            }
            add(p.children[i], k);
        }
    }

    private BTreeNode split(BTreeNode p) {
        BTreeNode q = new BTreeNode(p.isLeaf);
        for (int i = 0; i < MAX_DEGREE - 1; i++) {
            q.keys[i] = p.keys[i + MAX_DEGREE];
        }
        for (int i = 0; i < MAX_DEGREE; i++) {
            q.children[i] = p.children[i + MAX_DEGREE];
        }
        q.nbKeys = MAX_DEGREE - 1;
        p.nbKeys = MAX_DEGREE - 1;
        return q;
    }

    public void remove(int k) {
        if (root == null) {
            return;
        }
        remove(root, k);
        if (root.nbKeys == 0) {
            if (root.isLeaf) {
                root = null;
            } else {
                root = root.children[0];
            }
        }
    }

    private void remove(BTreeNode p, int k) {
        int idx = p.findKey(k);
        if (idx < p.nbKeys && p.keys[idx] == k) {
            if (p.isLeaf) {
                for (int i = idx; i < p.nbKeys - 1; i++) {
                    p.keys[i] = p.keys[i + 1];
                }
                p.nbKeys--;
            } else {
                if (p.children[idx].nbKeys >= MAX_DEGREE) {
                    int pred = p.getPredecessor(idx);
                    p.keys[idx] = pred;
                    remove(p.children[idx], pred);
                } else if (p.children[idx + 1].nbKeys >= MAX_DEGREE) {
                    int succ = p.getSuccesor(idx + 1);
                    p.keys[idx] = succ;
                    remove(p.children[idx + 1], succ);
                } else {
                    merge(p, idx);
                    remove(p.children[idx], k);
                }
            }
        } else {
            if (!p.isLeaf) {
                boolean flag = idx == p.nbKeys;
                if (p.children[idx].nbKeys < MAX_DEGREE) {
                    fill(p, idx);
                }
                if (flag && idx > p.nbKeys) {
                    remove(p.children[idx - 1], k);
                } else {
                    remove(p.children[idx], k);
                }
            }
        }
    }

    private void fill(BTreeNode p, int idx) {
        if (idx != 0 && p.children[idx - 1].nbKeys >= MAX_DEGREE) {
            borrowFromPrev(p, idx);
        } else if (idx < p.nbKeys && p.children[idx + 1].nbKeys >= MAX_DEGREE) {
            borrowFromNext(p, idx);
        } else {
            if (idx < p.nbKeys) {
                merge(p, idx);
            } else {
                merge(p, idx - 1);
            }
        }
    }

    private void borrowFromPrev(BTreeNode p, int idx) {
        BTreeNode l = p.children[idx - 1];
        BTreeNode r = p.children[idx];
        for (int i = r.nbKeys; i > 0; i--) {
            r.keys[i] = r.keys[i - 1];
        }
        for (int i = r.nbKeys; i >= 0; i--) {
            r.children[i + 1] = r.children[i];
        }
        r.keys[0] = p.keys[idx - 1];
        r.children[0] = l.children[l.nbKeys];
        p.keys[idx - 1] = l.keys[l.nbKeys - 1];
        l.nbKeys--;
        r.nbKeys++;
    }

    private void borrowFromNext(BTreeNode p, int idx) {
        BTreeNode l = p.children[idx];
        BTreeNode r = p.children[idx + 1];
        l.keys[l.nbKeys] = p.keys[idx];
        l.children[l.nbKeys + 1] = r.children[0];
        p.keys[idx] = r.keys[0];
        for (int i = 1; i < r.nbKeys; i++) {
            r.keys[i - 1] = r.keys[i];
        }
        for (int i = 0; i < r.nbKeys; i++) {
            r.children[i] = r.children[i + 1];
        }
        l.nbKeys++;
        r.nbKeys--;
    }

    private void merge(BTreeNode p, int idx) {
        BTreeNode l = p.children[idx];
        BTreeNode r = p.children[idx + 1];

        l.keys[MAX_DEGREE - 1] = p.keys[idx];
        for (int i = 0; i < r.nbKeys; i++) {
            l.keys[i + MAX_DEGREE] = r.keys[i];
        }
        for (int i = 0; i <= r.nbKeys; i++) {
            l.children[i + MAX_DEGREE] = r.children[i];
        }
        l.nbKeys += r.nbKeys + 1;

        for (int i = idx + 1; i < p.nbKeys; i++) {
            p.keys[i - 1] = p.keys[i];
            p.children[i] = p.children[i + 1];
        }
        p.nbKeys--;
    }

    public static void main(String[] args) {
        BTree btree = new BTree();
        Random rand = new Random();
        ArrayList<Integer> lst = new ArrayList<>();
        long startM = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            int x = rand.nextInt(1000000);
            btree.add(x);
//            lst.add(x);
        }
        Collections.sort(lst);
        for (int i = 0; i < 500000; i++) {
            int x = rand.nextInt(1000000);
//            boolean c = lst.contains(x);
//            int oldLen = lst.size();
//            int idx = lst.indexOf(x);
//            if (0 <= idx && idx < lst.size() && lst.get(idx) == x) {
//                lst.remove(idx);
//            }
//            if (!c && oldLen != lst.size()) {
//                System.exit(-1);
//            }
            btree.remove(x);
//            btree.traverse();
//            for (int j = 0; j < lst.size(); j++) {
////                System.out.println(btree.lst.get(j) + " " + lst.get(j));
//                int x1 = btree.lst.get(j);
//                int y = lst.get(j);
//                if (x1 != y) {
//                    System.out.println(btree.lst.get(j) + ":" + lst.get(j));
//                    System.exit(-1);
//                }
//            }
        }
        long endM = System.currentTimeMillis();
        btree.traverse();
//        for (int i = 0; i < lst.size(); i++) {
//            System.out.println(btree.lst.get(i) + " " + lst.get(i));
//            int x = btree.lst.get(i);
//            int y = lst.get(i);
//            if (x != y) {
//                System.out.println(btree.lst.get(i) + ":" + lst.get(i));
//                System.exit(-1);
//            }
//        }
        System.out.println("running time = " + (endM - startM));
        System.out.println(btree.maxDepth);
        System.out.println(lst.size());
    }
}
