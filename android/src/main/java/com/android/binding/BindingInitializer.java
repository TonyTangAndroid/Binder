package com.android.binding;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.binding.Binder;
import com.binding.annotations.SubscriptionsFactory;

import java.lang.reflect.Constructor;

import io.reactivex.functions.Consumer;

/**
 * a function that initializes the Binding operation
 * <p>
 * Created by Ahmed Adel Ismail on 1/31/2018.
 */
class BindingInitializer implements Consumer<Object> {

    @Override
    public void accept(Object owner) throws Exception {
        if (isAnnotatedWithSubscriptionFactory(owner)) {
            BindersCache.put(owner, bind(owner));
        }
    }

    private boolean isAnnotatedWithSubscriptionFactory(Object owner) {
        return owner.getClass().isAnnotationPresent(SubscriptionsFactory.class);
    }

    private Binder<Object> bind(Object owner) throws Exception {
        return Binder.bind(owner)
                .to(subscriptionsFactory(owner, subscriptionFactoryClass(owner)));
    }

    @NonNull
    private Object subscriptionsFactory(Object owner, Class<?> factoryClass) throws Exception {
        if (owner instanceof SubscriptionFactoryProvider) {
            return ((SubscriptionFactoryProvider) owner).subscriptionFactory();
        } else if (ViewModel.class.isAssignableFrom(factoryClass)) {
            return ViewModelProviders.of(ownerActivity(owner))
                    .get(viewModelClass(factoryClass));
        } else if (factoryClass.isAnnotationPresent(SharedSubscriptionFactory.class)) {
            return reuseOrCreateSubscriptionsFactory(factoryClass);
        } else {
            return createNewSubscriptionsFactory(factoryClass);
        }
    }

    @NonNull
    private Class<?> subscriptionFactoryClass(Object owner) {
        return owner.getClass().getAnnotation(SubscriptionsFactory.class).value();
    }

    private FragmentActivity ownerActivity(Object owner) {
        return (owner instanceof Fragment)
                ? ((Fragment) owner).getActivity()
                : (FragmentActivity) owner;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends ViewModel> viewModelClass(Class<?> subscriptionFactory) {
        return (Class<? extends ViewModel>) subscriptionFactory;
    }

    private Object reuseOrCreateSubscriptionsFactory(Class<?> factoryClass) throws Exception {
        Object factory = BindersCache.getSubscriptionsFactoryOrNull(factoryClass);
        if (factory == null) {
            factory = createNewSubscriptionsFactory(factoryClass);
        }
        return factory;
    }

    private Object createNewSubscriptionsFactory(Class<?> factoryClass) throws Exception {
        Constructor constructor = factoryClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
}
