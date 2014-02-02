package com.probridge.vbox.vmm.wmi.utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


/**
 * Code found within the sun's help forum:
 * http://forums.sun.com/thread.jspa?messageID=2886690
 * This class allows users to implement the observer/observable patter with observers registered
 * as {@link WeakReference}
 * @param <N>
 * @param <T>
 */
public class NotifierAdapter<N extends Notifier, T> implements Notifier<N, T> {
    private ArrayList<WeakReference<NotifierListener<N, T>>> listeners = new ArrayList<WeakReference<NotifierListener<N, T>>>();
    private N notifier;

    protected NotifierAdapter() {
        this.notifier = (N) this;
    }

    public NotifierAdapter(N notifier) {
        this.notifier = notifier;
    }

    public void addNotifierListener(NotifierListener<N, T> listener) {
        WeakReference<NotifierListener<N, T>> weakListenerReference = new WeakReference<NotifierListener<N, T>>(
            listener);
        this.listeners.add(weakListenerReference);
    }

    public void removeNotifierListener(NotifierListener<N, T> listener) {
        for (int i = 0; i < this.listeners.size(); i++) {
            WeakReference<NotifierListener<N, T>> weakListenerReference = this.listeners.get(i);
            NotifierListener<N, T> weakListener = weakListenerReference.get();
            if ((weakListener == null) || (weakListener == listener)) {
                this.listeners.remove(weakListenerReference);
            }
        }
    }

    public void fire(T event) {
        ArrayList<WeakReference<NotifierListener<N, T>>> toDelete = new ArrayList<WeakReference<NotifierListener<N, T>>>();
        for (WeakReference<NotifierListener<N, T>> weakListenerReference : listeners) {
            NotifierListener<N, T> weakListener = weakListenerReference.get();
            if (weakListener == null) {
                toDelete.add(weakListenerReference);
            } else {
                weakListener.update(this.notifier, event);
            }
        }
        listeners.removeAll(toDelete);
    }
}