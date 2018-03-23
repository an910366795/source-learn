package com.learning.jdk.util;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author chengan.liang
 * @deprecated:HashMap源码学习,参考自https://github.com/julycoding/The-Art-Of-Programming-By-July/blob/master/ebook/zh/03.01.md
 * @since 2018-03-17 9:19
 */


public class MyHashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, Cloneable, Serializable {

    /**
     * 默认容器大小,16
     */
    static final int DEFAULT_CAPACITY = 1 << 4;

    /**
     * 最大的容器大小，2的30次方
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * 默认的加载因子，0.75，当前容量=当前容器大小*加载因子时会对容器进行扩容
     * 数据表明在0.75的时候各方面的性能最佳
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * 默认桶中的链表转化为红黑树的阀值
     * 因为链表过长时，遍历的时间复杂度为O(n),转化为红黑树之后时间复杂度为O(logn)
     */
    static final int TREEIFY_THRESHOLD = 8;

    /**
     * 默认的红黑树转化为链表的阀值
     */
    static final int UNTREEIFY_THRESHOLD = 6;

    /**
     * 当桶中的bin元素大于TREEIFY_THRESHOLD但容量大小小于MIN_TREEIFY_CAPACITY时
     * 依然使用链表结构，当容器容量大于MIN_TREEIFY_CAPACITY时会将链表转化为红黑树
     */
    static final int MIN_TREEIFY_CAPACITY = 64;

    /**
     * 静态内部类，节点实现类
     */
    static class Node<K, V> implements Map.Entry<K, V> {
        //节点的hash值
        final int hash;
        final K key;
        V value;
        //下一个节点
        Node<K, V> next;

        //默认构造器
        public Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        //重写toString方法
        public final String toString() {
            return key + "=" + value;
        }

        /**
         * 获取节点Node的hashcode,key和value的hashcode进行'异或'运算后的结果
         */
        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        /**
         * 设置Node的新value，返回旧值
         */
        @Override
        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        /**
         * 重写equals方法，当且仅当节点的key和value相等时这个Node才相等
         */
        public final boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof Map.Entry) {
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                if (Objects.equals(e.getKey(), key) && Objects.equals(e.getValue(), value)) {
                    return true;
                }
            }
            return false;
        }
    }
    /*--------静态工具方法-------*/

    /**
     * 根据key获取这个key的hashcode
     * 1.调用native方法hashcode获取key的hashcode
     * 2.再将进行高低位‘异或’运算（h>>16）以此获取各均匀的hash散列值
     * 3.这样做的目的是因为如果直接用hashcode与容器的容器进行与运算的话，当容器小于16位时仅有低位参与运算
     * ，此时产生hash碰撞的机率就比较大，通过高低位的异或运算可以降低产生hash碰撞的机率
     */
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : ((h = key.hashCode()) ^ (h >> 16));
    }

    /**
     * 返回comparable实现类的当前运行Class对象
     * 1.只有实现了comparable接口或者其继承链上的类实现了comparable接口才会返回运行的Class对象
     * 否则返回null
     */
    static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {
            Class<?> c;
            Type[] ts, as;
            Type t;
            ParameterizedType p;
            //如果x属于字符串，直接返回String.class
            if ((c = x.getClass()) == String.class) {
                return c;
            }
            if ((ts = c.getGenericInterfaces()) != null) {
                for (int i = 0; i < ts.length; i++) {
                    if (((t = ts[i]) instanceof ParameterizedType)
                            && ((p = (ParameterizedType) t).getRawType()) == Comparable.class
                            && (as = p.getActualTypeArguments()) != null
                            && as.length == 1 && as[0] == c) {
                        return c;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 返回两个对象的compare结果，如果这两个对象属于Compare的实现类，否则返回0
     */
    static int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc ? 0 :
                ((Comparable) k).compareTo(x));
    }

    /**
     * 在计算容器大小时，给定一个整数，返回一个最小的2的n次幂的容器大小
     * 此方法确保容器的大小都是2的n次幂
     */
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 12;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /*---------字段-----------*/

    /**
     * Mapr的hash桶，元素存于数组中
     */
    transient Node<K, V>[] table;

    /**
     * Map的entry的Set集合
     */
    transient Set<Map.Entry<K, V>> entrySet;

    /**
     * Map中有key-value的个数
     */
    transient int size;

    /**
     * hashMap结构被更改的次数
     */
    transient int modCount;

    /**
     * 阀值，当容量当前填充的个数大于此阀值时会进行扩容
     */
    int threShold;

    /**
     * table的加载因子
     */
    final float loadFactor;


    /*--------Public 操作----------*/

    /**
     * 构造函数
     *
     * @param initialCapacity 初始化容量大小
     * @param loadFactor      加载因子
     */
    public MyHashMap(int initialCapacity, float loadFactor) {
        //如果初始化大小小于0，抛出illegalArgumentException
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("非法的初始化容量大小,必须是大于0的整数");
        }
        //如果大于最大容量则取最大的容量
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        //加载因子不能为小于0或者非法的
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("非法的加载因子,必须为大于0的float");
        this.loadFactor = loadFactor;
        //计算一个最接近的2次幂作为容器的容量大小
        this.threShold = tableSizeFor(initialCapacity);
    }

    /**
     * 构造函数
     *
     * @param initialCapacity 初始化大小
     */
    public MyHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 空参数的构造函数，此时只将初始化默认的加载因子，当真正添加元素的时候再对容器进行初始化的动作
     */
    public MyHashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
    }

    /**
     * 根据另一个map进行初始化
     */
    public MyHashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putMapEntries(m, false);
    }

    /**
     * 从一个Map将entry复制到当前map中
     *
     * @param m
     * @param evict
     */
    private void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
        //来源map的大小
        int s = m.size();
        //计算出复制后的Map合适的容量
        if (size > 0) {
            //如果此时容器为空
            if (table == null) {
                float ft = ((float) s / loadFactor) + 1.0F;
                int t = (ft < (float) MAXIMUM_CAPACITY) ? (int) ft : MAXIMUM_CAPACITY;
                if (t > threShold)
                    threShold = tableSizeFor(t);
            }
            //如果不为空
            else if (s > threShold)
                //容器重新计算大小
                resize();
            for (Entry<? extends K, ? extends V> e : m.entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                //设置值
                putVal(hash(key), key, value, false, evict);
            }
        }
    }

    /**
     * 实现了Map.put和关联的方法
     *
     * @param hash         key的hash值
     * @param key          键值
     * @param value        value值
     * @param onlyIfAbsent 为true时，不覆盖原有的值
     * @param evict        如果为false,则为创造方式
     * @return 返回一个原有的值，或者返回空如果之前无值
     */
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict) {
        Node<K, V>[] tab;
        //p为设置值之前hash桶同一个位置的节点
        Node<K, V> p;
        //n为当前hash桶的容器大小
        //i为计算（n-1)&hash之后的值，即节点所在hash桶的位置
        int n, i;
        //如果此时hash桶为空，则进行初始化容器大小
        if ((tab = table) == null || (n = tab.length) == 0) {
            //如果之前hash桶为空,resize()初始化大小之后,n为默认的hash桶的值为16
            n = (tab = resize()).length;
            //如果之前hash桶相对应的位置为空，则直接新建节点放到该位置中
            if ((p = tab[i = (n - 1) & hash]) == null)
                tab[i] = newNode(hash, key, value, null);
            else {
                Node<K, V> e;
                K k;
                //判断：如果插入的节点的key值等于之前hash桶对应位置上的key值
                if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k))))
                    e = p;
                else if (p instanceof TreeNode)
                    //如果hash桶上位置上的节点为红黑树结构，插入红黑树结构中
                    e = ((TreeNode) p).putTreeVal(this, tab, hash, key, value);
                else {
                    //如果为链表结构
                    for (int binCount = 0; ; ++binCount) {
                        //循环直到为链表的最后一个节点
                        if ((e = p.next) == null) {
                            p.next = newNode(hash, key, value, null);
                            //如果此时链表长度>=8,将链表结构转化红黑树
                            if (binCount >= TREEIFY_THRESHOLD - 1)
                                treeifyBin(tab, hash);

                        }
                    }
                }

            }
        }
        return null;
    }

    /**
     * 将链表结构转化为红黑树，需要满足以下条件
     * 1.hash桶相同位置上的链表长度大于等于8
     * 2.当前hash桶的大小大于64
     * 如果只满足条件1，则只是将当前容器大小扩容为两倍（调用resize（））
     *
     * @param tab
     * @param hash
     */
    private void treeifyBin(Node<K, V>[] tab, int hash) {
        int n, index;
        Node<K, V> e;
        if (tab == null || (n = tab.length) > MIN_TREEIFY_CAPACITY)
            resize();
        else if ((e = tab[index = (n - 1) & hash]) != null) {
            TreeNode<K, V> hd = null, tl = null;
            do {

            } while ((e = e.next) == null);
        }
    }

    /**
     * 构造一个新的Node节点
     */
    Node<K, V> newNode(int hash, K key, V value, Node<K, V> next) {
        return new Node<>(hash, key, value, next);
    }

    /**
     * 初始化大小或者将容器大小扩容为当前的两倍。
     *
     * @return 返回Map的hash桶
     */
    final Node<K, V>[] resize() {
        //将旧的hash桶数据进行复制
        Node<K, V>[] oldTab = table;
        //旧的容量大小
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        //旧的阀值
        int oldThr = threShold;
        //新的容量大小和阀值
        int newCap, newThr = 0;
        if (oldCap > 0) {
            //如果旧的容量大小大于容量允许的最大值，将容量大小设置为最大值（
            if (oldCap >= MAXIMUM_CAPACITY) {
                threShold = Integer.MAX_VALUE;
                return oldTab;
            }
            //如果旧的容量扩容两倍小于容量最大值且旧的容量大于默认容量值时，将阀值扩容为旧的阀值两倍
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY && oldCap > DEFAULT_CAPACITY) {
                newThr = oldThr << 1;
            }
        } else if (oldThr > 0)
            //如果容量为0，但是阀值不为0，说明创建了hash表但是还没有添加元素，初始化容量等于阀值
            newCap = oldThr;
        else {
            //如果容量和阀值都为0，说明还没有创建hash表，将容量和阀值设置为默认
            newCap = DEFAULT_CAPACITY;
            newThr = (int) (DEFAULT_LOAD_FACTOR * DEFAULT_CAPACITY);
        }
        if (newThr == 0) {
            float ft = (float) newCap * loadFactor;
            newThr = newCap < MAXIMUM_CAPACITY && ft < (float) MAXIMUM_CAPACITY ? (int) ft : Integer.MAX_VALUE;
        }
        threShold = newThr;
        //新的hash桶
        Node<K, V>[] newTab = (Node<K, V>[]) new Node[newCap];
        table = newTab;
        //遍历复制到新的hash桶中
        if (oldTab != null) {
            for (int j = 0; j < oldCap; j++) {
                Node<K, V> e;
                if ((e = oldTab[j]) != null) {
                    //将旧的桶的值设置为空
                    oldTab[j] = null;
                    if (e.next == null)
                        //如果当前节点没有next,说明桶的数组位置只有一个元素，
                        //e.hash &(newCap-1)为计算元素在hash桶的位置
                        newTab[e.hash & (newCap - 1)] = e;
//                    else if (e instanceof TreeNode)
//                        //如果之前的元素为红黑树节点
//                        ((ThreeNode < K, V > e).split(this, newTab, j, oldCap);
                    else {
                        /**
                         * loHead和loTail为原hash桶的链表位置上的数据头和尾
                         * 因为扩容之后，原hash桶位置上的链表上数据，要么在原来的位置，要么在 oldCap(原容量大小)+原来的位置
                         * 1.当(e.hash & oldCap) ==0 时为原来的位置
                         *   原理：计算Node的位置公式为 hash &(n-1)，因为容器的大小总是为2的n次幂，
                         *         所以n-1总能得到低位都为1的二进制数据，比如n=16(进制为010000)时,
                         *         n-1的二进制为（01111）,扩容后的容量为之前的2倍，即n=32(二进制为0100000),
                         *         此时n-1的二进制为（011111），相比原来只在高一位多了个1，这样做的目的是扩容后，
                         *         只要计算新的n-1的最高位数1 & hash相对应位数上的值，如果为0则是原来的位置，如果为1
                         *         则为原来的位置加上旧的容量大小（等同于e.hash & oldCap）
                         *   举例：Node1 的hash 为 0100000100010000 0000101110001011
                         *         Node2 的hash 为 0100000100010000 0000101110011011
                         *         原容量大小为16，通过公式计算两个Node的位置相同，均为数组下标为11的位置,如下：
                         *              Node1:0100000100010000 0000101110001011 & 0100000100010000 0000000000001111 = 01011(11)
                         *              Node2:0100000100010000 0000101110011011 & 0100000100010000 0000000000001111 = 01011(11)
                         *         扩容之后，容量大小为32，通过公式计算两个Node的位置，Node1为11，Node2为27，如下：
                         *              Node1:0100000100010000 0000101110001011 & 0100000100010000 0000000000011111 = 01011(11)
                         *              Node2:0100000100010000 0000101110011011 & 0100000100010000 0000000000011111 = 011011(27)
                         *   所以只要知道扩容后高一位的1& hash对应位置上的数值即可得出链表上的元素在新的hash桶中的位置，即等效于e.hash & oldCap,如下
                         *              Node1:0100000100010000 0000101110001011 & 0100000100010000 000000000010000 = 0
                         *              Node2:0100000100010000 0000101110011011 & 0100000100010000 000000000010000 = 010000(16)
                         *    这样就避免了1.7之前扩容后每个节点都要重新计算hashcode和位置
                         * 2.当(e.hash & oldCap) !=0时为 原来的位置+oldCap(原容量大小)，原理同上
                         */

                        Node<K, V> loHead = null, loTail = null;
                        Node<K, V> hiHead = null, hiTail = null;
                        Node<K, V> next;
                        do {//循环赋值到新的hash桶中
                            next = e.next;
                            //如果hash & 旧的容量大小，说明位置不变
                            if ((e.hash & oldCap) == 0) {
                                //如果链表尾节点为空,说明当前链表为空将头设置为当前节点
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;

                            }
                            //如果hash&oldcap ==1 则新的位置为旧的位置下标+旧的容量大小
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                            if (loTail != null) {
                                loTail.next = null;
                                newTab[j] = loHead;
                            }
                            if (hiTail != null) {
                                hiTail.next = null;
                                newTab[j + oldCap] = hiHead;
                            }
                        } while ((e = next) != null);
                    }
                }
            }
        }
        return newTab;
    }


    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V get(Object key) {
        return null;
    }

    @Override
    public V put(K key, V value) {
        return null;
    }

    @Override
    public V remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<K> keySet() {
        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }

    private <K, V> Node<K, V> replacementNode(Node<K, V> p, Node<K, V> next) {
        return new Node<>(p.hash, p.key, p.value, next);
    }

    //构建一个新的节点
    TreeNode<K, V> newTreeNode(int hash, K key, V value, Node<K, V> next) {
        return new TreeNode<>(hash, key, value, next);
    }

    /**
     * hashMap的红黑树的实现
     */
    static final class TreeNode<K, V> extends Node<K, V> {
        Entry<K, V> before, after;
        //父节点
        TreeNode<K, V> parent;
        //左节点
        TreeNode<K, V> left;
        //右节点
        TreeNode<K, V> right;
        TreeNode<K, V> prev;
        boolean red;

        TreeNode(int hash, K key, V val, Node<K, V> next) {
            super(hash, key, val, next);
        }

        /**
         * 返回当前节点的根节点
         */
        final TreeNode<K, V> root() {
            for (TreeNode<K, V> r = this, p; ; ) {
                //如果节点的父节点为空，说明此节点为根节点,否则将节点的父节点赋值给p,循环直到某个节点的父节点为空为止
                if ((p = r.parent) == null)
                    return r;
                r = p;
            }
        }

        /**
         * 确保给定的红黑树节点为hash桶中的第一个节点，即确保红黑树的根节点在hash桶数组中
         */
        static <K, V> void moveRootToFront(Node<K, V>[] tab, TreeNode<K, V> root) {
            int n;
            if (root != null && tab != null && (n = tab.length) > 0) {
                //算出根节点在hash桶中的位置
                int index = (n - 1) & root.hash;
                //tab数组上的第一个元素就是根节点
                TreeNode<K, V> first = (TreeNode<K, V>) tab[index];
                if (root != first) {
                    Node<K, V> rn;
                    //替换根节点为当前的root
                    tab[index] = root;
                    TreeNode<K, V> rp = root.prev;
                    if ((rn = root.next) != null)
                        ((TreeNode<K, V>) rn).prev = rp;
                    if (rp != null)
                        //如果root存在前节点，则将前节点置为root的next节点
                        rp.next = rn;
                    if (first != null)
                        //将之前在根节点的元素放到root的next节点
                        first.prev = root;
                    root.next = first;
                    root.prev = null;
                }
                //确保红黑树完整性
                assert checkInvariants(root);
            }
        }

        /**
         * find()方法，根据给定的hash 和key 查找树结点
         */
        final TreeNode<K, V> find(int h, Object k, Class<?> kc) {
            TreeNode<K, V> p = this;
            do {
                int ph, dir;
                K pk;
                TreeNode<K, V> pl = p.left, pr = p.right, q;
                if ((ph = p.hash) > h) {
                    //如果此节点的hash大于查找的hash，说明查找的hash在左子树下
                    p = pl;
                } else if (ph < h) {
                    //如果此节点的hash小于查找的hash,说明查找的hash在右子树下
                    p = pr;
                } else if ((pk = p.key) == k || (k != null && k.equals(pk))) {
                    //如果查找的key等于节点的key，直接返回此节点的key
                    return p;
                } else if (pl == null) {
                    //如果hash相等且左节点为空，则从右节点查找
                    p = pr;
                } else if (pr == null) {
                    //如果hash相等且右节点为空，则从左节点查找
                    p = pl;
                } else if ((kc != null || (kc = comparableClassFor(k)) != null) && (dir = compareComparables(kc, k, pk)) != 0)
                    //如果hash相等，且左右节点都不为空，则根据传进来的comparable实现类kc进行比较
                    //比较结果如果小于0则从左子树查找，大于0则从右子树查找
                    p = (dir < 0) ? pl : pr;
                else if ((q = pr.find(h, k, kc)) != null) {
                    //如果上面条件都不满足则要么在右子树中查找，要么在左子树中查找,直到跳出循环
                    return q;
                } else
                    p = pl;
            } while (p != null);
            return null;
        }

        /**
         * 获取节点,从根节点开始查找
         */
        final TreeNode<K, V> getTreeNode(int h, Object k) {
            return ((parent != null) ? root() : this).find(h, k, null);
        }

        /**
         * 同等条件下的插入排序工具类：
         * 当hashcode相等并且不可比较的情况下可以调用此工具类进行插入排序
         */
        static int tieBreakOrder(Object a, Object b) {
            int d;
            /**
             * 如果a或b为空，或者a和b是同种类型的Object，则调用System.identityHashCodeK获取两个对象的定义值
             * System.identityHashCode 是根据对象的内存地址产生hash值的
             */
            if (a == null || b == null || (d = a.getClass().getName().compareTo(b.getClass().getName())) == 0)
                d = (System.identityHashCode(a) <= System.identityHashCode(b)) ? -1 : 1;
            return d;
        }

        /**
         * 将TreeNode链表转化为红黑树结构
         */
        final void treeify(Node<K, V>[] tab) {
            TreeNode<K, V> root = null;
            for (TreeNode<K, V> x = this, next; x != null; x = next) {
                //遍历x.next节点
                next = (TreeNode<K, V>) x.next;
                //将TreeNode的左右节点清空
                x.left = x.right = null;
                //将此节点设置为根节点
                if (root == null) {
                    x.parent = null;
                    x.red = false;
                    root = x;
                } else {
                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    for (TreeNode<K, V> p = root; ; ) {
                        //dir 决定元素所在的位置是在左边还是右边，dir = -1时在左边，dir =1时在右边
                        int dir, ph;
                        K pk = p.key;
                        if ((ph = p.hash) > h)
                            dir = -1;
                        else if (ph < h)
                            dir = 1;
                            //如果hash值与根节点相等（hash碰撞）,调用tieBreakOrder根据内存地址计算出新的hash值对比
                        else if ((kc == null && (kc = comparableClassFor(k)) == null) ||
                                (dir = compareComparables(kc, k, pk)) == 0)
                            dir = tieBreakOrder(k, pk);
                        TreeNode<K, V> xp = p;
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            x.parent = xp;
                            if (dir < 0)
                                xp.left = x;
                            else
                                xp.right = x;
                            //平衡调整
                            root = balanceInsertion(root, x);
                            break;
                        }
                    }
                }
            }
            moveRootToFront(tab, root);
        }

        /**
         * 将Map的红黑树结构转化为链表结构
         */
        final Node<K, V> untreeify(MyHashMap<K, V> map) {
            Node<K, V> hd = null, tl = null;
            for (Node<K, V> q = this; q != null; q = q.next) {
                Node<K, V> p = map.replacementNode(q, null);
                if (tl == null)
                    hd = p;
                else
                    tl.next = p;
                tl = p;
            }
            return hd;
        }

        /**
         * 红黑树的插入新节点时的调用此方法来保证红黑树的特性
         * 一个红黑树插入一个元素经过以下步骤：
         * 1.将一个红黑当做一个二叉查找树，将节点插入
         * 2.将节点设置为红色（为什么不置为黑色，因为置为红色不会违背红黑树任意节点到叶子节点的黑节点数目都是相同的原则）
         * 3.通过旋转和换色等方法修正红黑树，让其保持红黑树的特性
         */
        static <K, V> TreeNode<K, V> balanceInsertion(TreeNode<K, V> root, TreeNode<K, V> x) {

            x.red = true;
            //xp为父节点，xpp为祖父节点，xppl和xppr为叔节点（其中一个和xp相等，取决于x所在的位置）
            for (TreeNode<K, V> xp, xpp, xppl, xppr; ; ) {
                /**
                 * 情况一：如果插入的是根节点(只有根节点的父节点为空)
                 *         只需要修改根节点的颜色即可修复红黑树的性质（根节点为黑色）
                 */
                if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                /**
                 * 情况二：插入的节点的父节点是黑色或者是根节点的子节点
                 *        此种情况并不会破坏红黑树的结构，直接返回，不做调整
                 */
                else if (!xp.red || (xpp = xp.parent) == null)
                    return root;
                /**
                 * PS:情况3，4，6其实是一个完整的插入修复过程
                 * 情况三：当前节点的父节点和叔节点是红色
                 * 对策：将当前节点的父节点和叔叔节点涂黑，祖父节点涂红，
                 *       把当前节点指向祖父节点，从新的当前节点重新开始算法。
                 */

                //父节点在祖父节点的左边的情况
                if (xp == (xppl = xpp.left)) {
                    //情况三
                    if ((xppr = xpp.right) != null && xppr.red) {
                        xppr.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    }
                    /**
                     * 情况四：当前节点的父节点是红色，叔叔节点是黑色，且当前当前节点是其父节点的右节点
                     * 对策：当前节点的父节点做为新的当前节点，以新当前节点为支点左旋
                     */
                    else {
                        if (x == xp.right) {
                            root = rotateLeft(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        /**
                         * 情况五：当前节点的父节点是红色，叔叔节点是黑色，当前节点是其父节点的左节点
                         * 对策：父节点变为黑色，祖父节点变为红色，在祖父节点为支点右旋
                         */
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateRight(root, xpp);
                            }
                        }
                    }


                }
                //父节点在祖父节点的右边的情况
                else {
                    //情况三
                    if (xppl != null && xppl.red) {
                        xppl.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    } else {
                        //与情况四对称，相应地做右旋
                        if (x == xp.left) {
                            root = rotateRight(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        //与情况五对称，相应的做左旋
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateLeft(root, xpp);
                            }
                        }
                    }

                }
            }
        }

        /**
         * 删除节点后的自调整方法,从x节点开始调整
         */
        static <K, V> TreeNode<K, V> balanceDeletion(TreeNode<K, V> root, TreeNode<K, V> x) {
            //xpl和xpr为兄弟节点
            for (TreeNode<K, V> xp, xpl, xpr; ; ) {
                //情况一：如果节点为空或者节点是根节点，直接返回
                if (x == null || x == root)
                    return root;
                    //情况二：如果父节点为空，说明是根节点，将根节点颜色置为黑色
                else if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                //情况三：如果当前节点是红色，直接将红色节点置为黑色后返回
                else if (x.red) {
                    x.red = false;
                    return root;
                }
                //以下几种情况当前节点都为黑色
                //在父节点左边的情况
                else if ((xpl = xp.left) == x) {
                    /**
                     * 情况四：当前节点为黑色且兄弟节点为红色（此时父结点和兄弟结点的子结点分为黑色，
                     *         因为不会出现连续的两个红色节点，否则删除前就不是红黑树了）
                     * 策略：把父结点染成红色，把兄弟结点染成黑色，然后进行左旋转
                     */
                    if ((xpr = xp.right) != null && xpr.red) {
                        xpr.red = false;
                        xp.red = true;
                        root = rotateLeft(root, xp);
                        xpr = (xp = x.parent) == null ? null : xp.right;
                    }
                    if (xpr == null)
                        x = xp;
                    else {
                        TreeNode<K, V> sl = xpr.left, sr = xpr.right;
                        if ((sr == null || !sr.red) && (sl == null || !sl.red)) {
                            xpr.red = true;
                            x = xp;
                        } else {
                            if (sr == null || !sr.red) {
                                if (sl != null)
                                    sl.red = false;
                                xpr.red = true;
                                root = rotateRight(root, xpr);
                                xpr = (xp = x.parent) == null ?
                                        null : xp.right;
                            }
                            if (xpr != null) {
                                xpr.red = (xp == null) ? false : xp.red;
                                if ((sr = xpr.right) != null)
                                    sr.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateLeft(root, xp);
                            }
                            x = root;
                        }
                    }


                }
                //在父节点右边的情况
                else {
                    if (xpl != null && xpl.red) {
                        xpl.red = false;
                        xp.red = true;
                        root = rotateRight(root, xp);
                        xpl = (xp = x.parent) == null ? null : xp.left;
                    }
                    if (xpl == null)
                        x = xp;
                    else {
                        TreeNode<K, V> sl = xpl.left, sr = xpl.right;
                        if ((sl == null || !sl.red) &&
                                (sr == null || !sr.red)) {
                            xpl.red = true;
                            x = xp;
                        } else {
                            if (sl == null || !sl.red) {
                                if (sr != null)
                                    sr.red = false;
                                xpl.red = true;
                                root = rotateLeft(root, xpl);
                                xpl = (xp = x.parent) == null ?
                                        null : xp.left;
                            }
                            if (xpl != null) {
                                xpl.red = (xp == null) ? false : xp.red;
                                if ((sl = xpl.left) != null)
                                    sl.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateRight(root, xp);
                            }
                            x = root;
                        }
                    }
                }
            }
        }

        /**
         * 红黑树的左旋转
         */
        static <K, V> TreeNode<K, V> rotateLeft(TreeNode<K, V> root, TreeNode<K, V> p) {
            TreeNode<K, V> r, pp, rl;
            if (p != null && (r = p.right) != null) {
                if ((rl = p.right = r.left) != null)
                    rl.parent = p;
                if ((pp = r.parent = p.parent) == null)
                    (root = r).red = false;
                else if (pp.left == p)
                    pp.left = r;
                else
                    pp.right = r;
                r.left = p;
                p.parent = r;
            }
            return root;
        }

        /**
         * 红黑树的右旋转
         */
        static <K, V> TreeNode<K, V> rotateRight(TreeNode<K, V> root, TreeNode<K, V> p) {
            TreeNode<K, V> l, pp, lr;
            if (p != null && (l = p.left) != null) {
                if ((lr = p.left = l.right) != null)
                    lr.parent = p;
                if ((pp = l.parent = p.parent) == null)
                    (root = l).red = false;
                else if (pp.right == p)
                    pp.right = l;
                else
                    pp.left = l;
                l.right = p;
                p.parent = l;
            }
            return root;
        }

        /**
         * 递归检查树节点的完整性和正确性
         */
        private static <K, V> boolean checkInvariants(TreeNode<K, V> t) {
            TreeNode<K, V> tp = t.parent, tl = t.left, tr = t.right, tb = t.prev, tn = (TreeNode<K, V>) t.next;
            //前节点不为空时，确保前节点的next节点为当前节点
            if (tb != null && tb.next != t)
                return false;
            //next节点不为空时，确保节点的next节点的前节点为当前节点
            if (tn != null && tn.prev != t)
                return false;
            //父节点不为空时,确保节点为父节点的左节点或者右节点
            if (tp != null && t != tp.left && t != tp.right)
                return false;
            //左节点不为空时，确保左节点的父节点为当前节点且左节点的hash值小于当前节点
            if (tl != null && (tl.parent != t || tl.hash > t.hash))
                return false;
            //右节点不为空时，确保右节点的父节点为当前节点且右节点的hash值大于当前节点
            if (tr != null && (tr.parent != t || tr.hash < t.hash))
                return false;
            //当节点为红色时，确保子节点的颜色都为黑色（红黑树不能有连续的红色节点）
            if (t.red && tl != null && tl.red && tr != null && tr.red)
                return false;
            //递归检查左节点
            if (tl != null && !checkInvariants(tl))
                return false;
            //递归检查右节点
            if (tr != null && !checkInvariants(tr))
                return false;
            return true;
        }

        /**
         * 新增一个树节点
         */
        public Node<K, V> putTreeVal(MyHashMap<K, V> map, Node<K, V>[] tab, int h, K k, V v) {
            Class<?> kc = null;
            //是否进行过树的查找标志，为true时说明已经进行过查找了
            boolean searched = false;
            //如果根节点为空，设置当前节点为根节点
            TreeNode<K, V> root = (parent != null) ? root() : this;
            for (TreeNode<K, V> p = root; ; ) {
                //dir为1时，在节点的右边，为-1时在节点的左边
                int dir, ph;
                K pk;
                if ((ph = p.hash) > h)
                    dir = 1;
                else if (ph < h)
                    dir = -1;
                else if ((pk = p.key) == k || (k != null && k.equals(pk))) {
                    //key值相同的情况，直接返回根节点
                    return p;
                } else if ((kc == null && (kc = comparableClassFor(k)) == null) || (dir = compareComparables(kc, k, pk)) == 0) {
                    //key值不同，但是产生hash碰撞的情况并且无法根据key的class类型比较的情况
                    //查找左右子节点中是否存在相同的树节点，如果存在，直接返回这个树节点，不做调整
                    if (!searched) {
                        TreeNode<K, V> q, ch;
                        searched = true;
                        //左右子树查找
                        if (((ch = p.left) != null && ((q = ch.find(h, k, kc)) != null)) ||
                                ((ch = p.right) != null) && ((q = ch.find(h, k, kc)) != null))
                            return q;
                    }
                    dir = tieBreakOrder(k, pk);
                }

                TreeNode<K, V> xp = p;
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    Node<K, V> xpn = xp.next;
                    //构造一个新的节点
                    TreeNode<K, V> x = map.newTreeNode(h, k, v, xpn);
                    //比较并放置节点的位置
                    if (dir <= 0)
                        xp.left = x;
                    else
                        xp.right = x;
                    xp.next = x;
                    x.parent = x.prev = xp;
                    if (xpn != null)
                        ((TreeNode) xpn).prev = x;
                    //重新调整红黑树,确保红黑树的性质和根节点落在根节点上面
                    moveRootToFront(tab, balanceDeletion(root, x));
                    return null;
                }
            }
        }
    }


    public static void main(String[] args) {

    }
}
