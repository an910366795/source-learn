package com.learning.RxJava;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

/**
 * @Description:RxJava map的使用
 * @author: chengan.liang
 * @Date: 2018/5/29
 */
public class MapDemo {


    private void testMap() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                for (int i = 0; i < 20; i++) {
                    e.onNext(i);
                    Thread.sleep(1000);
                }
            }
        }).map(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) throws Exception {
                return "转化【" + integer + "】";
            }
        }).subscribe(new Observer<String>() {

            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onNext(String s) {
                System.out.println("接收事件：" + s);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("发生异常");
            }

            @Override
            public void onComplete() {
                System.out.println("事件完成");
            }
        });
    }

    public static void main(String[] args) {
        MapDemo mapDemo = new MapDemo();
        mapDemo.testMap();
    }

}
