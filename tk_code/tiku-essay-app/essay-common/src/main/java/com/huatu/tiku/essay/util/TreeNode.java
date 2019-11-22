package com.huatu.tiku.essay.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * @author zhouwei
 * @Description: TreeNode
 * @create 2018-03-29 下午3:26
 **/
class TreeNode {
    private TreeNode parent;
    private TreeNode failure;
    private char ch;
    private List<TreeNode> sons;
    private Hashtable<Character, TreeNode> sonsHash;
    private List<String> results;
    private int depth = 0;
    private Integer status = 0;
    private Integer sort;

    public TreeNode(TreeNode parent, char ch) {
        this.parent = parent;
        this.ch = ch;
        results = new ArrayList<String>();
        sonsHash = new Hashtable<Character, TreeNode>();
        sons = new ArrayList<TreeNode>();
        if (parent != null)
            depth = parent.getDepth() + 1;
    }

    public void addResult(String result) {
        if (!results.contains(result)) results.add(result);
    }

    public void addSonNode(TreeNode node) {
        sonsHash.put(node.ch, node);
        sons.add(node);
    }

    public TreeNode setFailure(TreeNode failure) {
        this.failure = failure;
        return this.failure;
    }

    public TreeNode getSonNode(char ch) {
        return sonsHash.get(ch);
    }

    public TreeNode getParent() {
        return parent;
    }

    public char getCh() {
        return ch;
    }

    public List<TreeNode> getSons() {
        return sons;
    }

    public List<String> getResults() {
        return results;
    }

    public int getDepth() {
        return depth;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public TreeNode getFailure() {
        return failure;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
