package com.probridge.vbox.vmm.wmi.utils;


public interface Notifier<N extends Notifier, T> {
    public void addNotifierListener(NotifierListener<N, T> listener);

    public void removeNotifierListener(NotifierListener<N, T> listener);

    public void fire(T event);
}
