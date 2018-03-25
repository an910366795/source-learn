package com.learning.desing.mode;

/**
 * 单例设计模式
 * 参考自：
 * 1.https://mp.weixin.qq.com/s?__biz=MzI4Njc5NjM1NQ==&mid=2247483786&idx=1&sn=072991244ec2f259c7049bc80317a728&scene=21#wechat_redirect
 * 2.https://design-patterns.readthedocs.io/zh_CN/latest/creational_patterns/singleton.html
 * 3.《并发编程艺术》3.8 双重检查锁定的与延迟初始化
 * <p>
 * <p>
 * 定义：单例设计模式保证系统中一个类只有一个实例对象
 * 要点：1.某个类只能有一个实例
 * 2.必须自行创建这个实例
 * 3.必须向整个系统提供这个实例
 * <p>
 * 实现：1.私有化构造函数
 * 2.提供一个自身的静态私有变量；
 * 3.对外提供一个公有的静态工厂方法
 */

class Singleton {

    private Singleton() {
    }

/*--------------------饿汉式------------------------*/
    /**
     * 1.饿汉式
     * 特点：在类加载的时候进行初始化
     * 缺点：如果该单例一直没有被用到，会浪费一定的资源
     */
    /*private static Singleton instance = new Singleton();
    public static Singleton getInstance() {
        return instance;
    }*/
/*--------------------饿汉式------------------------*/

/*--------------------懒汉式-----------------------*/

    /**
     * 2.懒汉式
     * 特点，在实例被调用的时候进行初始化，即懒加载
     * 缺点：懒加载的模式可能导致多线程下的不同步造成的问题
     */


    //2.1 懒汉式--线程安全问题
    /*
     private static Singleton instance;
     public static Singleton getInstance() {
        *//*
            此处可能产生线程安全问题，如果两个线程同时调用getInstance，
            其中一个线程判断instance ==null后cpu执行权给到了第二个线程，
            此时instance并未实例化，也会判断instance==null，两个线程会
            同时进行实例化，违背了单例设计模式，故不推荐使用此种方法
         *//*
        if (instance == null)
            instance = new Singleton();
        return instance;
    }*/

    //2.2 懒汉式--同步对外获取实例方法
    // 此种方式简单，但是效率较低，因为多个线程只能同时有一个线程可以调用获取实例的方法,也不推荐

    /*
     private static Singleton instance;
     public static synchronized Singleton getInstance() {
        if (instance == null)
            instance = new Singleton();
        return instance;
    }*/

    //2.3 懒汉式--双重判断的同步方式
    /*
     private static Singleton instance;
     public static Singleton getInstance() {
        *//*
            此种方式相对比2.2效率更高，但是由于重排序的原因，可能会产生线程A判断为空对其进行初始化，但是未初始化完成
            线程B判断instance不为空（因为已经分配了内存空间，但实例化未完成）,此时访问了instance造成对象未初始化的问题
            具体分析参考《并发编程艺术》P69，如下：
            1.A线程判断instance为空，并且对A进行初始化（instance = new Singleton()）
            2.instace初始化为的过程：
                1.分配对象的内存地址
                2.初始化对象
                3.设置instance指向刚分配的内存地址
                由于java的重排序，可能将2和3步骤进行调换，即分配对象的内存地址-->将instance指向刚分配的内存地址-->初始化对象
            在将instance指向刚分配的内存地址的时候线程B调用getInstance()方法，此时instance == null 等于false,程序直接返回instance
            线程B调用instance的方法，但是此时线程A最后一步的初始化对象还没完成，因此会产生问题，也不推荐使用
         *//*
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null)
                    instance = new Singleton();
            }
        }
        return instance;
    }*/

    //2.4 懒汉式--基于volatile的双重锁方式
    /*private volatile static Singleton instance;
    public static Singleton getInstance() {
        //这种方式通过禁止2和3的重排序，保证了线程的安全
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null)
                    instance = new Singleton();
            }
        }
        return instance;
    }*/


    //2.5 懒汉式--基于静态内部类的解决方案
    private static class InstanceHolder {
        private static Singleton instance = new Singleton();
    }
    public static Singleton getInstance() {
        return InstanceHolder.instance;
    }

/*--------------------懒汉式-----------------------*/

}