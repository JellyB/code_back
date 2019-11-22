package interview.common;

/**
 * Author: xuhuiqiang
 * Time: 2018-07-05  11:28 .
 */
public class HownetParameter {
    /**
     * 知网中的关系符号
     */
    public static final String RELATIONAL_SYMBOL = ",~^#%$*+&@?!";
    /**
     * 知网中的特殊符号，虚词，或具体词
     */
    public static final String SPECIAL_SYMBOL = "{";
    /**
     * 两个无关义原之间的默认距离
     */
    public static final int DEFAULT_PRIMITIVE_DIS = 20;
    /**
     * sim(p1,p2) = alpha/(d+alpha)
     */
    public static final double ALPHA = 1.6;
    /**
     * 计算实词的相似度，参数，基本义原权重
     */
    public static final double BETA1 = 0.5;
    /**
     * 计算实词的相似度，参数，其他义原权重
     */
    public static final double BETA2 = 0.2;
    /**
     * 计算实词的相似度，参数，具体词权重
     */
    public static final double BETA3 = 1;
    /**
     * 计算实词的相似度，参数，关系义原 义原权重
     */
    public static final double BETA4 = 0.17;
    /**
     * 计算实词的相似度，参数，关系义原 具体词权重
     */
    public static final double BETA5 = 0.17;
    /**
     * 计算实词的相似度，参数，关系符号义原 义原权重
     */
    public static final double BETA6 = 0.13;
    /**
     * 计算实词的相似度，参数，关系符号义原 具体词权重
     */
    public static final double BETA7 = 0.13;
    /**
     * 具体词与义原的相似度一律处理为一个比较小的常数. 具体词和具体词的相似度，如果两个词相同，则为1，否则为0.
     */
    public static final double GAMMA = 0.2;
    /**
     * 将任一非空值与空值的相似度定义为一个比较小的常数
     */
    public static final double DELTA = 0.2;
}
