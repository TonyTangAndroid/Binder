package com.binder.app;

import androidx.lifecycle.MutableLiveData;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.android.LiteCycle;
import com.binding.annotations.SubscribeTo;
import com.binding.annotations.SubscriptionsFactory;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.properties.Property;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.Subject;

@SubscriptionsFactory(ViewModel.class)
public class MainActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.act_text);
    }

    @SubscribeTo("updateData")
    void updateAll(Subject<Boolean> updateData) {
        LiteCycle.with(updateData)
                .forLifeCycle(this)
                .onResumeInvoke(subject -> subject.onNext(true))
                .observe();
    }


    @SubscribeTo("stringSubject")
    Disposable stringSubscriber(Subject<String> subject) {
        return subject.share()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(textView::setText)
                .subscribe(v -> Log.e("MainActivity", "stringSubject : " + v));
    }

    @SubscribeTo("intSubject")
    Disposable intSubscriber(Subject<Integer> subject) {
        return subject.share()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(v -> Log.e("MainActivity", "intSubject : " + v));
    }

    @SubscribeTo("stringLiveData")
    void stringLiveDataSubscriber(MutableLiveData<String> liveData) {
        liveData.observe(this,
                text -> Log.e("MainActivity", "liveData : " + text));
    }

    @SubscribeTo("stringProperty")
    Disposable stringPropertySubscriber(Property<String> property) {
        return property.asObservable()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(v -> Log.e("MainActivity", "stringProperty : " + v));
    }

    @SubscribeTo("ownerName")
    void updateOwnerName(Subject<String> ownerName) {
        ownerName.onNext(getClass().getSimpleName());
    }

}
