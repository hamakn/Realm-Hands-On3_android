package io.realm.handson3.twitter;


import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import io.realm.handson3.twitter.entity.Tweet;

import static org.junit.Assert.assertEquals;

public class RealmTestExample {
    private List<Tweet> mTweets;

    private Tweet buildTweetFromJSON(JSONObject jsonObject) {
        Tweet tweet = new Tweet();
        try {
            tweet.setId(jsonObject.getLong("id"));
            tweet.setText(jsonObject.getString("text"));
        } catch (JSONException e) {
        }
        return tweet;
    }

    @Before
    public void setUp() throws Exception, JSONException, ParseException {
        // test環境でrealmを取得する方法がなさそう...
        /*
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(realmConfiguration);
        Realm realm = Realm.getDefaultInstance();
        */

        final InputStream is = RealmTestExample.class.getClassLoader().getResourceAsStream("tweets.json");
        final String jsonText = IOUtils.toString(is);
        final JSONArray jsonArray = new JSONArray(jsonText);
        mTweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject jsonObject = jsonArray.getJSONObject(i);
            // realm instanceが作れないのでできない
            //final Tweet tweet = realm.createObjectFromJson(Tweet.class, jsonObject);
            final Tweet tweet = buildTweetFromJSON(jsonObject);
            mTweets.add(tweet);
        }
    }

    @Test
    public void test() throws Exception {
        assertEquals(4, 2 + 2);
        assertEquals(mTweets.get(0).getId(), 1);
        assertEquals(mTweets.get(0).getText(), "hoge");
    }
}
