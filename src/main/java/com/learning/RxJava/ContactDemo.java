package com.learning.RxJava;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @Description:RxJava contact的使用
 * @author: chengan.liang
 * @Date: 2018/5/29
 */
public class ContactDemo {

    private void testContact() {
        Observable.concat(Observable.just(1, 2, 3), Observable.just(4, 5, 67))
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        System.out.println(integer);
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    public static void main(String[] args) {
        ContactDemo contactDemo = new ContactDemo();
        contactDemo.testContact();
    }


}
