package com.learning.RxJava;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import java.util.Date;

/**
 * @Description:RxJava create()的使用
 * @author: chengan.liang
 * @Date: 2018/5/29
 */
public class CreateDemo {

    /**
     * 1. Observable：发射源，英文释义“可观察的”，在观察者模式中称为“被观察者”或“可观察对象”；
     * 2. Observer：接收源，英文释义“观察者”，没错！就是观察者模式中的“观察者”，可接收Observable、Subject发射的数据；
     * 3. Subject：Subject是一个比较特殊的对象，既可充当发射源，也可充当接收源
     * 4. Subscriber：“订阅者”，也是接收源，那它跟Observer有什么区别呢？Subscriber实现了Observer接口，
     * 比Observer多了一个最重要的方法unsubscribe( )，用来取消订阅，当你不再想接收数据了，
     * 可以调用unsubscribe( )方法停止接收，Observer 在 subscribe() 过程中,最终也会被转换成 Subscriber 对象，
     * 一般情况下，建议使用Subscriber作为接收源；
     * 5. Subscription ：Observable调用subscribe( )方法返回的对象，同样有unsubscribe( )方法，可以用来取消订阅事件；
     */

    public void testObserver() {

        Observable.create(new ObservableOnSubscribe<Long>() {//创建发射源
            @Override
            public void subscribe(ObservableEmitter<Long> e) throws Exception {


                long time;
                do {
                    time = new Date().getTime();
                    //发射事件1
                    e.onNext(time);
                    if (time % 15 == 0) {
                        throw new RuntimeException("发射源异常");
                    }
                } while (time % 3 != 0);

                //完成事件
                e.onComplete();
            }
        }).subscribe(new Observer<Long>() {//创建接收源

            private Disposable mDisposable;

            @Override
            public void onSubscribe(Disposable disposable) {
                mDisposable = disposable;
            }

            @Override
            public void onNext(Long l) {
                //当前发射源发送事件时，接收器接收并进行处理
                System.out.println("接收到事件：【" + l + "】");
                if (l % 15 ==0) {
                    System.out.println("时间被15整除，停止接收事件");
                    mDisposable.dispose();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                String message = throwable.getMessage();

                //当发射源产生错误时
                System.out.println("事件异常:【" + message + "】");
            }

            @Override
            public void onComplete() {
                //当发射源complete时
                System.out.println("事件完成");
            }
        });
    }

    public static void main(String[] args) {
        CreateDemo createDemo = new CreateDemo();
        createDemo.testObserver();
    }


}
