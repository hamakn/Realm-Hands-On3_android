package io.realm;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: add comment
 * 複数のrealmリスト
 * header/footerについて
 */
public abstract class MultiRealmListAdapter<T> extends BaseAdapter {
    protected LayoutInflater inflater;
    protected List<T> adapterData;
    protected List<OrderedRealmCollection<RealmModel>> realmLists;
    protected Context context;
    private final List<RealmChangeListener> listeners;

    public MultiRealmListAdapter(Context context, List<T> data, OrderedRealmCollection<? extends RealmModel>... realmLists) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context;
        this.adapterData = data;
        this.inflater = LayoutInflater.from(context);

        this.realmLists = new ArrayList<OrderedRealmCollection<RealmModel>>();
        this.listeners = new ArrayList<RealmChangeListener>();
        for (OrderedRealmCollection<?> realms : realmLists) {
            RealmChangeListener<BaseRealm> listener = new RealmChangeListener<BaseRealm>() {
                @Override
                public void onChange(BaseRealm results) {
                    onDataSetChanged();
                }
            };
            this.realmLists.add((OrderedRealmCollection<RealmModel>) realms);
            this.listeners.add(listener);
            addListener(realms, listener);
        }
    }

    public void onDataSetChanged() {
        notifyDataSetChanged();
    }

    private void addListener(OrderedRealmCollection<?> realms, RealmChangeListener<BaseRealm> listener) {
        if (realms instanceof RealmResults) {
            RealmResults realmResults = (RealmResults) realms;
            realmResults.realm.handlerController.addChangeListenerAsWeakReference(listener);
        } else if (realms instanceof RealmList) {
            RealmList realmList = (RealmList) realms;
            realmList.realm.handlerController.addChangeListenerAsWeakReference(listener);
        } else {
            throw new IllegalArgumentException("RealmCollection not supported: " + realms.getClass());
        }
    }

    private void removeListener(OrderedRealmCollection<?> realms, RealmChangeListener<BaseRealm> listener) {
        if (realms instanceof RealmResults) {
            RealmResults realmResults = (RealmResults) realms;
            realmResults.realm.handlerController.removeWeakChangeListener(listener);
        } else if (realms instanceof RealmList) {
            RealmList realmList = (RealmList) realms;
            realmList.realm.handlerController.removeWeakChangeListener(listener);
        } else {
            throw new IllegalArgumentException("RealmCollection not supported: " + realms.getClass());
        }
    }

    @Override
    public int getCount() {
        if (adapterData == null) {
            return 0;
        }
        return adapterData.size();
    }

    @Override
    public T getItem(int position) {
        if (adapterData == null) {
            return null;
        }
        return adapterData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void removeListeners() {
        if (realmLists == null) {
            return;
        }

        for (int i = 0; i < realmLists.size(); i++) {
            removeListener(realmLists.get(i), listeners.get(i));
        }
    }
}