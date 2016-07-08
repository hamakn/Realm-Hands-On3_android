package io.realm.handson3.twitter;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.realm.MultiRealmListAdapter;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.handson3.twitter.databinding.ListitemRetweetBinding;
import io.realm.handson3.twitter.databinding.ListitemTweetBinding;
import io.realm.handson3.twitter.databinding.TweetHeaderBinding;
import io.realm.handson3.twitter.entity.Tweet;

public class TimelineFragment extends ListFragment {
    private final int VIEW_TYPE_TWEET = 0;
    private final int VIEW_TYPE_RETWEET = 1;
    private final int VIEW_TYPE_HEADER = 2;
    private final int VIEW_COUNT = 3;
    private Realm realm;
    RealmResults<Tweet> mTweets;

    private final int ITEM_TYPE_TWEET = 0;
    private final int ITEM_TYPE_HEADER = 2;

    private class TimelineItem {
        private Tweet tweet;
        private int viewType;
        private String label;

        public TimelineItem(Tweet tweet, int viewType, String label) {
            this.tweet = tweet;
            this.viewType = viewType;
            this.label = label;
        }

        public Tweet getTweet() {
            return tweet;
        }

        public int getViewType() {
            return viewType;
        }

        public String getLabel() {
            return label;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //noinspection ConstantConditions
        final ListView listView = getListView();
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        //listView.setDivider(null);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final TimelineItem item = (TimelineItem) listView.getItemAtPosition(position);
                if (item.getViewType() != ITEM_TYPE_TWEET) {
                    return;
                }

                final Tweet tweet = item.getTweet();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        tweet.setFavorited(!tweet.isFavorited());
                    }
                });
            }
        });

        realm = Realm.getDefaultInstance();
        mTweets = buildTweetList(realm);

        final List<TimelineItem> timeline = buildTimeline(mTweets);

        final MultiRealmListAdapter<TimelineItem> adapter = new MultiRealmListAdapter<TimelineItem>(getContext(), timeline, mTweets) {
            @Override
            public void onDataSetChanged() {
                adapterData = buildTimeline(mTweets);
                notifyDataSetChanged();
            }

            @Override
            public int getViewTypeCount() {
                return VIEW_COUNT;
            }

            @Override
            public int getItemViewType(int position) {
                TimelineItem item = getItem(position);
                Tweet tweet = item.getTweet();
                if (tweet == null) {
                    return VIEW_TYPE_HEADER;
                } else if (tweet.getText().startsWith("RT @")) {
                    return VIEW_TYPE_RETWEET;
                } else {
                    return VIEW_TYPE_TWEET;
                }
            }

            @Override
            public boolean isEnabled(int position) {
                TimelineItem item = getItem(position);
                if (item.getViewType() == ITEM_TYPE_TWEET) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final TimelineItem item = getItem(position);
                Tweet tweet = item.getTweet();

                switch (getItemViewType(position)) {
                    case VIEW_TYPE_HEADER:
                        convertView = getHeaderView(item, position, convertView, parent);
                        break;

                    case VIEW_TYPE_TWEET:
                        convertView = getTweetView(tweet, position, convertView, parent);
                        break;

                    case VIEW_TYPE_RETWEET:
                        convertView = getRetweetView(tweet, position, convertView, parent);
                        break;

                    default:
                        break;
                }

                if (tweet != null) {
                    listView.setItemChecked(position, tweet.isFavorited());
                }
                return convertView;
            }

            private View getHeaderView(TimelineItem item, int position, View convertView, ViewGroup parent) {
                TweetHeaderBinding binding;
                if (convertView == null) {
                    binding = DataBindingUtil.inflate(inflater, R.layout.tweet_header, parent, false);
                    convertView = binding.getRoot();
                    convertView.setTag(binding);
                } else {
                    binding = (TweetHeaderBinding) convertView.getTag();
                }

                binding.textView.setText(item.getLabel());

                return convertView;
            }

            private View getTweetView(Tweet tweet, int position, View convertView, ViewGroup parent) {
                ListitemTweetBinding binding;
                if (convertView == null) {
                    binding = DataBindingUtil.inflate(inflater, R.layout.listitem_tweet, parent, false);
                    convertView = binding.getRoot();
                    convertView.setTag(binding);
                } else {
                    binding = (ListitemTweetBinding) convertView.getTag();
                }

                binding.setTweet(tweet);
                Picasso.with(context)
                        .load(tweet.getIconUrl())
                        .into(binding.image);

                return convertView;
            }

            private View getRetweetView(Tweet tweet, int position, View convertView, ViewGroup parent) {
                ListitemRetweetBinding binding;
                if (convertView == null) {
                    binding = DataBindingUtil.inflate(inflater, R.layout.listitem_retweet, parent, false);
                    convertView = binding.getRoot();
                    convertView.setTag(binding);
                } else {
                    binding = (ListitemRetweetBinding) convertView.getTag();
                }

                binding.setTweet(tweet);
                Picasso.with(context)
                        .load(tweet.getIconUrl())
                        .into(binding.rtImage);

                return convertView;
            }
        };

        setListAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MultiRealmListAdapter<?>) getListAdapter()).removeListeners();
        realm.close();
        realm = null;
    }

    @NonNull
    protected RealmResults<Tweet> buildTweetList(Realm realm) {
        return realm.where(Tweet.class).findAllSorted("createdAt", Sort.DESCENDING);
    }

    @NonNull
    protected List<TimelineItem> buildTimeline(RealmResults<Tweet> tweets) {
        final List<TimelineItem> timeline = new ArrayList<>();

        for (int i = 0; i < tweets.size(); i++) {

            if (i > 0 && i % 10 == 0) {
                TimelineItem item2 = new TimelineItem(null, ITEM_TYPE_HEADER, String.valueOf(i));
                timeline.add(item2);
            }

            Tweet tweet = tweets.get(i);
            TimelineItem item = new TimelineItem(tweet, ITEM_TYPE_TWEET, null);
            timeline.add(item);
        }

        return timeline;
    }
}
