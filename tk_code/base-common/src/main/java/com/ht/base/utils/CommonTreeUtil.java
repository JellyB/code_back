package com.ht.base.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by lijun on 2018/5/16
 */
@Data
@AllArgsConstructor
public class CommonTreeUtil {

    private Integer id;//ID
    private String code;//code
    private String title;//名称
    private String fullTitle;//全名称
    private Integer sortNum;//排序号
    private List<CommonTreeUtil> children;//子节点
    private Integer parentId;//父界点名称

    public CommonTreeUtil(Integer treeId, String treeCode, String treeTitle, String treeFullTile, Integer sortNum, Integer parentId) {
        this.id = treeId;
        this.code = treeCode;
        this.title = treeTitle;
        this.fullTitle = treeFullTile;
        this.sortNum = sortNum;
        this.parentId = parentId;
    }

    public static List<CommonTreeUtil> transListToTreeData(List<TreeNodeInterface> lst) {
        List<CommonTreeUtil> menuList = new ArrayList<>();
        //排序
        Collections.sort(lst, getSortNumComparator());
        //数据组装
        lst.forEach(node -> {
            if (null != node.getTreeParentId() || 0 == node.getTreeParentId()) {
                CommonTreeUtil menu = new CommonTreeUtil(
                        node.getTreeId(), node.getTreeCode(),
                        node.getTreeTitle(), node.getTreeTitle(), node.getSortNum(), null
                );
                menu.setChildren(getChild(node.getTreeId(), node.getTreeTitle(), lst));
                menuList.add(menu);
            }
        });
        return menuList;
    }

    //获取子节点
    private static List<CommonTreeUtil> getChild(Integer id, String fullTitle, List<TreeNodeInterface> lst) {
        List<CommonTreeUtil> menuChildList = new ArrayList<>();
        for (TreeNodeInterface node : lst) {
            if (null != node.getTreeParentId() && node.getTreeParentId().equals(id)) {
                String selfFullTitle = fullTitle + "/" + node.getTreeTitle();
                CommonTreeUtil menuChild = new CommonTreeUtil(
                        node.getTreeId(), node.getTreeCode(),
                        node.getTreeTitle(), selfFullTitle, node.getSortNum(), id
                );
                menuChild.setChildren(getChild(menuChild.getId(), fullTitle, lst));
                menuChildList.add(menuChild);
            }
        }
        if (menuChildList.size() == 0) {
            return null;
        }
        return menuChildList;
    }

    /**
     * 从当前等级查询到顶级 并按序返回
     *
     * @param id   id
     * @param list 所有数据集合
     * @return
     */
    public static <T extends TreeNodeInterface> List getParentUtilRoot(final Integer id, final List<T> list) {
        BiPredicate<T, Integer> isNode = (node, testId) -> testId.equals(node.getTreeId());
        Function<T, Integer> getNextNode = (node) -> node.getTreeParentId();
        List dataFromNode = getDataFromNode(id, list, isNode, getNextNode);
        return dataFromNode;
    }

    /**
     * 从当前节点查询到最底层节点 并按序返回
     *
     * @param id   当前节点ID
     * @param list 所有数据
     * @return
     */
    public static <T extends TreeNodeInterface> List getChildrenUtilLast(final Integer id, final List<T> list) {
        Optional<T> base = list.parallelStream().filter(node -> node.getTreeId().equals(id)).findAny();
        ArrayList<Object> dataFromNode = new ArrayList<>();
        if (base.isPresent()) {
            BiPredicate<T, Integer> isNode = (node, testId) -> testId.equals(node.getTreeParentId());
            Function<T, Integer> getNextNode = (node) -> node.getTreeId();
            dataFromNode.addAll(getDataFromNode(id, list, isNode, getNextNode));
            dataFromNode.add(0, base.get());
        }
        return dataFromNode;

    }

    private static <T extends TreeNodeInterface> List getDataFromNode(
            final Integer id,
            final List<T> list,
            BiPredicate<T, Integer> isNode,
            Function<T, Integer> getNextNode) {
        final List<T> result = new ArrayList();
        Consumer<Integer>[] consumers = new Consumer[1];

        consumers[0] = (beginId) -> {
            Optional<T> data = list.parallelStream().filter(node -> isNode.test(node, beginId)).findAny();
            if (data.isPresent()) {
                result.add(data.get());
                consumers[0].accept(getNextNode.apply(data.get()));
            }
        };
        consumers[0].accept(id);
        return result;
    }

    private static Comparator getSortNumComparator() {
        return new Comparator<TreeNodeInterface>() {
            @Override
            public int compare(TreeNodeInterface o1, TreeNodeInterface o2) {
                int i = compareByInteger(o1.getSortNum(), o2.getSortNum());
                if (i == 0) {
                    return compareByInteger(o1.getTreeId(), o2.getTreeId());
                }
                return i;
            }

            public int compareByInteger(Integer o1, Integer o2) {
                if (o1 == null && o2 == null) {
                    return 0;
                }
                //sortNum为空表示最小, 返回-1
                if (o1 == null) {
                    return -1;
                }
                if (o2 == null) {
                    return 1;
                }
                return o1 - o2;
            }
        };
    }
}
