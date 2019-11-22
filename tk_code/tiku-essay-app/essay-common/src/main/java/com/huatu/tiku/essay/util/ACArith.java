package com.huatu.tiku.essay.util;


import com.huatu.tiku.essay.vo.resp.Mate;

import java.util.*;

/**
 * @Author ZhenYang
 * @Date Created in 2018/2/24 10:20
 * @Description
 */

public class ACArith {

    private String text = "";
    private List<String> pattens = new ArrayList<>();
    TreeNode root;

    public ACArith(String text, List<String> pattens) {
        this.text = text;
        this.pattens = pattens;
        buildGotoTree(); // goto表和output表
        addFailure(); // failure表
        printTree();
    }


    /**
     * 构建goto表和output表
     */
    public void buildGotoTree() {
        int i = 1;
        root = new TreeNode(null, ' ');
        for (int j = 0; j < pattens.size(); j++) {
            String word = pattens.get(j);
            TreeNode temp = root;
            // 判断节点是否存在，存在转移，不存在添加
            for (char ch : word.toCharArray()) {
                TreeNode innerTem = temp.getSonNode(ch);
                if (innerTem == null) {
                    TreeNode newNode = new TreeNode(temp, ch);
                    newNode.setStatus(i++);
                    temp.addSonNode(newNode);
                    innerTem = newNode;
                }
                temp = innerTem;
            }
            temp.addResult(word);
            temp.setSort(j);
        }
    }

    /**
     * 构建failure表
     * 遍历所有节点, 设置失败节点 原则: 节点的失败指针在父节点的失败指针的子节点中查找 最大后缀匹
     */
    public void addFailure() {
        //过程容器
        ArrayList<TreeNode> mid = new ArrayList<TreeNode>();

        for (TreeNode node : root.getSons()) {
            node.setFailure(root);
            for (TreeNode treeNode : node.getSons()) {
                mid.add(treeNode);
            }
        }

        while (mid.size() > 0) {
            ArrayList<TreeNode> temp = new ArrayList<TreeNode>();
            for (TreeNode node : mid) {
                TreeNode r = node.getParent().getFailure();
                while (r != null && r.getSonNode(node.getCh()) == null) {
                    r = r.getFailure();
                }

                if (r == null) {
                    node.setFailure(root);
                } else {
                    node.setFailure(r.getSonNode(node.getCh()));
                    for (String result : node.getFailure().getResults()) {
                        node.addResult(result);
                    }
                }
                temp.addAll(node.getSons());
            }
            mid = temp;
        }
    }

    public void printTree() {
        List<TreeNode> nodesList = new ArrayList<TreeNode>();
        List<TreeNode> nodes = Arrays.asList(root);
        while (nodes.size() > 0) {
            ArrayList<TreeNode> temp = new ArrayList<TreeNode>();
            for (TreeNode node : nodes) {
                temp.addAll(node.getSons());
                nodesList.add(node);
            }
            nodes = temp;
        }
        Collections.sort(nodesList, (a, b) -> a.getStatus().compareTo(b.getStatus()));
    }

    public  static Map<Integer,List<Mate>> acSearch(String text, List<String> pattens) {
        ACArith ac = new ACArith(text,pattens);
//        List<Mate> mates = new ArrayList<>();
        Map<Integer,List<Mate>> mateMap = new LinkedHashMap<>();
        int index = 0;
        TreeNode mid = ac.root;
        while (index < ac.text.length()) {
            TreeNode temp = null;

            while (temp == null) {
                temp = mid.getSonNode(ac.text.charAt(index));
                if (mid == ac.root) {
                    break;
                }
                if (temp == null) {
                    mid = mid.getFailure();
                }
            }
            if (temp != null) mid = temp;

            List<String> results = mid.getResults();
//            List<Mate> mates1
            for (int i = 0; i < results.size(); i++) {
                Mate mate = new Mate(index - results.get(i).length() + 1, index, results.get(i),mid.getSort());
                List<Mate> mateList = mateMap.get(mid.getSort());
                if(null == mateList){
                    mateList = new ArrayList<>();
                    mateList.add(mate);
                    mateMap.put(mid.getSort(),mateList);
                }else{
                    mateList.add(mate);
                }
//                mates.add(new Mate(index - results.get(i).length() + 1, index, results.get(i), mid.getSort()));
            }
            index++;
        }
        return mateMap;
    }
}

