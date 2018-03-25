package com.learning.jdk.util;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author chengan.liang
 * @deprecated:linklist的基本方法的实现，linkLIst源码学习
 * @since 2018-03-24 19:23
 */
public class MyLinkedList<E> extends AbstractSequentialList<E> implements List<E>, Deque<E>, Cloneable, Serializable {

    transient int size = 0;

    /**
     * 头节点
     */
    transient Node<E> first;
    /**
     * 尾节点
     */
    transient Node<E> last;


    /**
     * 无参构造函数
     */
    public MyLinkedList() {
    }

    /**
     * 从一个collection构造一个LinkedLIst
     */
    public MyLinkedList(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    /**
     * 根据元素构造一个节点，并将该节点设置为LinkedList的头节点
     */
    private void linkFirst(E e) {
        final Node<E> f = first;
        final Node<E> newNode = new Node<>(null, e, f);
        first = newNode;
        if (f == null)
            //如果此时linkedList为空，将尾节点设置为当前节点,即首尾都是同一个节点
            last = newNode;
        else
            f.prev = newNode;
        size++;
        modCount++;
    }

    /**
     * 根据元素构造一个节点 ，将将该节点设置为linkedList的尾节点
     */
    private void linkLast(E e) {
        final Node<E> l = last;
        final Node<E> newNode = new Node<>(l, e, null);
        last = newNode;
        if (l == null)
            first = newNode;
        else
            l.next = newNode;
        size++;
        modCount++;
    }


    /**
     * 根据元素构造一个node，将插入到另一个节点之前
     */
    void linkBefore(E e, Node<E> succ) {
        final Node<E> pred = succ.prev;
        final Node<E> newNode = new Node<>(pred, e, succ);
        succ.prev = newNode;
        if (pred == null)
            first = newNode;
        else
            pred.next = newNode;
        size++;
        modCount++;
    }

    /**
     * 移除一个头节点，并返回相应的元素
     */
    private E unLinkFirst(Node<E> f) {
        final E element = f.item;
        final Node<E> next = f.next;
        f.item = null;
        f.next = null;
        first = next;
        if (next == null)
            last = null;
        else
            next.prev = null;
        size--;
        modCount++;
        return element;
    }


    /**
     * 移除一个尾节点并返回相应的元素
     */
    private E unlinkLast(Node<E> l) {

        final E element = l.item;
        Node<E> prev = l.prev;
        l.item = null;
        l.prev = null;
        last = prev;
        if (last == null)
            first = null;
        else
            prev.next = null;
        size--;
        modCount++;
        return element;
    }

    /**
     * 移除一个节点
     */
    E unlink(Node<E> x) {
        final E element = x.item;
        final Node<E> next = x.next;
        final Node<E> prev = x.prev;

        if (prev == null)
            //如果前节点为空，说明该节点为首节点，此时将下一节点设置为首节点
            first = next;
        else {
            prev.next = next;
            x.prev = null;
        }

        if (next == null)
            last = prev;
        else {
            next.prev = prev;
            x.next = null;
        }

        x.item = null;
        size--;
        modCount++;
        return element;
    }


    /**
     * linkerlist的节点，内部类
     */
    private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        public Node(Node<E> prev, E item, Node<E> next) {
            this.item = item;
            this.next = next;
            this.prev = prev;
        }
    }


    /**
     * 获取首节点
     */
    @Override
    public E getFirst() {
        final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException("linkedlist为空");
        return f.item;
    }


    /**
     * 获取尾节点
     */
    @Override
    public E getLast() {
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException("linkedlist为空");
        return l.item;
    }


    /**
     * 移除首节点
     */
    @Override
    public E removeFirst() {
        final Node<E> f = this.first;
        if (f == null)
            throw new NoSuchElementException("linkedlist为空");
        return unLinkFirst(f);
    }


    /**
     * 移除尾节点
     */
    @Override
    public E removeLast() {
        final Node<E> l = this.last;
        if (l == null)
            throw new NoSuchElementException("linkedlist为空");
        return unlinkLast(l);
    }


    /**
     * 从根节点开始添加元素
     *
     * @param e
     */
    @Override
    public void addFirst(E e) {
        linkFirst(e);
    }


    /**
     * 从尾节点添加元素
     */
    @Override
    public void addLast(E e) {
        linkLast(e);
    }

    /**
     * 实现
     *
     * @param o
     * @return
     */
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }


    /**
     * 给定一个元素，判断该元素在linkedlist的位置
     */
    public int indexOf(Object o) {
        int index = 0;
        if (o == null) {
            //如果给定的元素为空
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null)
                    return index;
                index++;
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item.equals(o))
                    return index;
                index++;
            }
        }
        return -1;
    }

    /**
     * 给定元素判断该元素存在于链表中，并返回存在该最尾节点的位置
     */
    public int lastIndexOf(Object o) {
        int index = size;
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.prev) {
                index--;
                if (x.item == null)
                    return index;
            }
        } else {
            for (Node<E> x = first; x != null; x = x.prev) {
                index--;
                if (x.item.equals(o))
                    return index;
            }
        }
        return -1;
    }

    /**
     * 返回首节点上的元素，但是不做移除操作，如果为空返回空
     */
    @Override
    public E peek() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
    }

    /**
     * 返回首节点上的元素，但是不做移除操作，如果为空抛出异常
     */
    @Override
    public E element() {
        return getFirst();
    }

    /**
     * 链表的poll操作，返回并移除首节点，首节点为空则返回空
     */
    @Override
    public E poll() {
        final Node<E> f = first;
        return (f == null) ? null : unLinkFirst(f);
    }

    /**
     * 返回并移除首节点，为空则抛出异常
     */
    @Override
    public E remove() {
        return removeFirst();
    }

    /**
     * 从尾节点开始添加元素
     */
    @Override
    public boolean offer(E e) {
        return add(e);
    }


    /**
     * 从首节点开始添加元素
     */
    @Override
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }


    /**
     * 从尾节点开始添加元素
     */
    @Override
    public boolean offerLast(E e) {
        addLast(e);
        return false;
    }


    /**
     * 同peek()
     *
     * @return
     */
    @Override
    public E peekFirst() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
    }

    /**
     * 同peek()
     */
    @Override
    public E peekLast() {
        final Node<E> l = last;
        return (l == null) ? null : l.item;
    }

    /**
     * 同poll
     *
     * @return
     */
    @Override
    public E pollFirst() {
        final Node<E> f = first;
        return (f == null) ? null : unLinkFirst(f);
    }

    @Override
    public E pollLast() {
        final Node<E> l = last;
        return (l == null) ? null : unlinkLast(l);
    }

    /**
     * 从首节点开始添加元素
     */
    @Override
    public void push(E e) {
        addFirst(e);
    }

    /**
     * 链表pop操作，移除并返回首节点,为空时抛出异常
     */
    @Override
    public E pop() {
        return removeFirst();
    }


    /**
     * 移除元素，从首节点开始遍历
     */
    @Override
    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
    }

    /**
     * 移除元素，从尾节点开始遍历
     */
    @Override
    public boolean removeLastOccurrence(Object o) {
        if (o == null) {
            for (Node<E> l = last; l != null; l = l.prev) {
                if (l == null) {
                    unlink(l);
                    return true;
                }
            }
        } else {
            for (Node<E> l = last; l != null; l = l.prev) {
                if (l.item.equals(o)) {
                    unlink(l);
                    return true;
                }
            }
        }
        return false;
    }

    public ListIterator<E> listIterator(int index) {
        checkPositionIndex(index);
        return new ListItr(index);
    }

    /**
     * ListIterator的内部实现类
     */
    private class ListItr implements ListIterator<E> {
        //上个返回的节点
        private Node<E> lastReturned;
        //下个返回的节点
        private Node<E> next;
        //下个返回的节点位置
        private int nextIndex;
        //链表结构发生改变次数
        private int expectedModCount = modCount;

        ListItr(int index) {
            next = (index == size) ? null : node(index);
            nextIndex = index;
        }

        /**
         * 是否有下一个节点，如果小于链表的长度则有，否则无
         */
        @Override
        public boolean hasNext() {
            return nextIndex < size;
        }

        /**
         * 返回下一个节点的元素
         */
        @Override
        public E next() {
            checkForComodification();
            if (!hasNext())
                throw new NoSuchElementException();
            lastReturned = next;
            next = next.next;
            nextIndex++;
            return lastReturned.item;
        }

        /**
         * 判断是否有前节点
         */
        @Override
        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        /**
         * 返回前一节点
         */
        @Override
        public E previous() {
            checkForComodification();
            if (!hasPrevious())
                throw new NoSuchElementException();
            lastReturned = next = (next == null) ? last : next.prev;
            nextIndex--;
            return lastReturned.item;
        }

        @Override
        public int nextIndex() {
            return nextIndex;
        }

        @Override
        public int previousIndex() {
            return nextIndex - 1;
        }

        @Override
        public void remove() {
            checkForComodification();
            if (lastReturned == null)
                throw new NoSuchElementException();
            Node<E> lastNext = lastReturned.next;
            unlink(lastReturned);
            if (next == lastReturned)
                next = lastNext;
            else
                nextIndex--;
            lastReturned = null;
            expectedModCount++;
        }

        @Override
        public void set(E e) {
            if (lastReturned == null)
                throw new IllegalStateException();
            checkForComodification();
            lastReturned.item = e;
        }

        @Override
        public void add(E e) {
            checkForComodification();
            lastReturned = null;
            if (next == null)
                linkLast(e);
            else
                linkBefore(e, next);
            nextIndex++;
            expectedModCount++;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            while (modCount == expectedModCount && nextIndex < size) {
                action.accept(next.item);
                lastReturned = next;
                next = next.next;
                nextIndex++;
            }
            checkForComodification();
        }

        /**
         * 并发异常，如果在遍历的时候modCount和expectedModCount不相等，说明有多线程对链表进行改变，抛异常
         */
        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }


    /**
     * 返回linkedList的size
     */
    @Override
    public int size() {
        return size;
    }


    /**
     * 添加一个元素，从链表的最后位置添加
     */
    @Override
    public boolean add(E e) {
        linkLast(e);
        return true;
    }

    /**
     * 移除一个元素
     */
    public boolean remove(Object o) {
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }

    /**
     * 从一个位置开始，复制collection的元素到LinkedLIst中
     *
     * @param index
     * @param c
     * @return
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        checkPositionIndex(index);

        //将collection元素转化为数组
        Object[] a = c.toArray();
        //新增元素的个数
        int numNew = a.length;
        if (numNew == 0)
            return false;

        Node<E> pred, succ;
        if (index == size) {
            succ = null;
            pred = last;
        } else {
            succ = node(index);
            pred = succ.prev;
        }

        for (Object o : a) {
            E e = (E) o;
            Node<E> newNode = new Node<>(pred, e, null);
            if (pred == null)
                first = newNode;
            else
                pred.next = newNode;
            pred = newNode;
        }

        if (succ == null)
            last = pred;
        else {
            pred.next = succ;
            succ.prev = pred;
        }
        size++;
        modCount++;
        return true;

    }

    /**
     * 清空链表
     */
    public void clear() {
        for (Node<E> x = first; x != null; ) {
            Node<E> next = x.next;
            x.item = null;
            x.prev = null;
            x.next = null;
            x = next;
        }
        first = last = null;
        size = 0;
        modCount++;
    }

    /**
     * 根据链表中的位置index,返回指定的元素
     */
    public E get(int index) {
        checkPositionIndex(index);
        return node(index).item;
    }

    /**
     * 在指定位置设置新的值（指定位置上的元素必须有值）,并返回旧值
     */
    @Override
    public E set(int index, E element) {
        checkPositionIndex(index);
        Node<E> node = node(index);
        E oldVal = node.item;
        node.item = element;
        return oldVal;
    }

    /**
     * 在指定位置插入新的元素
     */
    @Override
    public void add(int index, E element) {
        checkPositionIndex(index);
        if (index == size)
            linkLast(element);
        else
            linkBefore(element, node(index));

    }

    /**
     * 移除指定位置上的元素
     */
    public E remove(int index) {
        checkPositionIndex(index);
        return unlink(node(index));
    }

    /**
     * 判断指定位置上是否上元素
     */
    private boolean isELementIndex(int index) {
        return index >= 0 && index < size;
    }

    private void checkElementIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException("超出链表长度了");
    }


    /**
     * 返回特定链表位置上的Node
     * 二分查找的思想来进行查找，提高查找效率
     *
     * @param index
     * @return
     */
    private Node<E> node(int index) {
        if (index < (size >> 1)) {
            Node<E> x = first;
            for (int i = 0; i < index; i++)
                x = x.next;
            return x;
        } else {
            Node<E> x = last;
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            return x;
        }
    }


    /**
     * 校验index是否超出链表的长度
     *
     * @param index
     */
    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException("index超出链表长度");
    }

    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }


    @Override
    public Iterator<E> descendingIterator() {
        return null;
    }
}
