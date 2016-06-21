package io.realm.handson3.twitter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.handson3.twitter.entity.Tweet;

public class TimelineFragment extends ListFragment {

    private final int VIEW_TYPE_TWEET = 0;
    private final int VIEW_TYPE_RETWEET = 1;
    private final int VIEW_COUNT = 2;

    private Realm realm;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //noinspection ConstantConditions
        final ListView listView = getListView();
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Tweet tweet = (Tweet) listView.getItemAtPosition(position);
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        tweet.setFavorited(!tweet.isFavorited());
                    }
                });
            }
        });

        realm = Realm.getDefaultInstance();

        final RealmResults<Tweet> tweets = buildTweetList(realm);
        final RealmBaseAdapter<Tweet> adapter = new RealmBaseAdapter<Tweet>(getContext(), tweets) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final Tweet tweet = getItem(position);

                if (convertView == null) {
                    switch (getItemViewType(position)) {
                        case VIEW_TYPE_TWEET:
                            convertView = inflater.inflate(R.layout.listitem_tweet, parent, false);
                            break;
                        case VIEW_TYPE_RETWEET:
                            convertView = inflater.inflate(R.layout.listitem_retweet, parent, false);
                            break;
                        default:
                            break;
                    }
                }

                // TODO 余裕があればViewHolderパターンを適用してください
                ((TextView) convertView.findViewById(R.id.screen_name)).setText(tweet.getScreenName());
                ((TextView) convertView.findViewById(R.id.text)).setText(tweet.getText());

                Picasso.with(context)
                        .load(tweet.getIconUrl())
                        .into((ImageView) convertView.findViewById(R.id.image));

                listView.setItemChecked(position, tweet.isFavorited());
                return convertView;
            }

            @Override
            public int getViewTypeCount() {
                return VIEW_COUNT;
            }

            @Override
            public int getItemViewType(int position) {
                Tweet tweet = getItem(position);
                if (tweet.getText().startsWith("RT @")) {
                    return VIEW_TYPE_RETWEET;
                } else {
                    return VIEW_TYPE_TWEET;
                }
            }
        };

        setListAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((RealmBaseAdapter<?>) getListAdapter()).updateData(null);
        realm.close();
        realm = null;
    }

    @NonNull
    protected RealmResults<Tweet> buildTweetList(Realm realm) {
        return realm.where(Tweet.class).findAllSorted("createdAt", Sort.DESCENDING);
    }
}
