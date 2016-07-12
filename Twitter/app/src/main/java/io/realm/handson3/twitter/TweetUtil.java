package io.realm.handson3.twitter;

import android.support.annotation.NonNull;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.handson3.twitter.entity.Tweet;

public class TweetUtil {
    @NonNull
    static public RealmResults<Tweet> buildTweetList(Realm realm) {
        return realm.where(Tweet.class).findAllSorted("createdAt", Sort.DESCENDING);
    }
}
