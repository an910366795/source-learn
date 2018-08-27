package com.learning.RxJava;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;

import java.sql.SQLOutput;

/**
 * @Description:RxJava Zip的使用
 * @author: chengan.liang
 * @Date: 2018/5/29
 */
public class ZipDemo {


    private Observable<Integer> getIntegerObservable() {

        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                for (int i = 0; i < 20; i++) {
                    e.onNext(i);
                }
            }
        });
    }

    private Observable<String> getStringObservable() {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                for (int i = 0; i < 21; i++) {
                    if (i % 2 == 0) {
                        e.onNext("【" + i + "】");
                    }
                }
            }
        });
    }

    private void testZip() {
        Observable.zip(getIntegerObservable(), getStringObservable(), new BiFunction<Integer, String, Object>() {
            @Override
            public Object apply(Integer integer, String s) throws Exception {
                return "接收integer{" + integer + "}接收string{" + s + "}";
            }
        }).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                System.out.println(o.toString());
            }
        });
    }

    public static void main(String[] args) {
        ZipDemo zipDemo = new ZipDemo();
        zipDemo.testZip();
    }


}
