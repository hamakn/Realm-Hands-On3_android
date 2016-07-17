package io.realm.handson3.twitter;


import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import io.realm.MockRealm;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.handson3.twitter.entity.Tweet;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Realm.class, RealmResults.class, TweetUtil.class})
public class RealmTestExample {
    private List<Tweet> mTweets;
    private Realm mTestRealm;
    private RealmResults mRealmResults;

    private Tweet buildTweetFromJSON(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.setId(jsonObject.getLong("id"));
        tweet.setText(jsonObject.getString("text"));
        return tweet;
    }

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception, JSONException, ParseException {
        // load json
        final InputStream is = RealmTestExample.class.getClassLoader().getResourceAsStream("tweets.json");
        final String jsonText = IOUtils.toString(is);
        final JSONArray jsonArray = new JSONArray(jsonText);
        mTweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject jsonObject = jsonArray.getJSONObject(i);
            final Tweet tweet = buildTweetFromJSON(jsonObject);
            mTweets.add(tweet);
        }

        // load yaml
        Yaml yaml = new Yaml();
        mTweets = yaml.loadAs(ClassLoader.getSystemResourceAsStream("tweets.yml"), List.class);

        // mock realm
        mTestRealm = MockRealm.mockRealm();
        mRealmResults = MockRealm.mockRealmResult(mTestRealm, mTweets);
    }

    @Test
    public void test() throws Exception {
        assertEquals(4, 2 + 2);

        assertEquals(mTweets.size(), 2);
        assertEquals(mTweets.get(0).getText(), "hoge");

        mockStatic(TweetUtil.class);
        doReturn(mRealmResults).when(TweetUtil.class, "buildTweetList", mTestRealm);

        RealmResults<Tweet> tweets = TweetUtil.buildTweetList(mTestRealm);
        assertEquals(tweets.size(), 2);
        assertEquals(tweets.get(0).getText(), "hoge");
        assertEquals(tweets.get(1).getText(), "fuga");
        assertEquals(tweets.get(0).getIconUrl(), "http://hoge.test/1.jpg");

        for (int i = 0; i < tweets.size(); i++) {
            System.out.println("for OK");
        }
        for (Tweet tweet : tweets) {
            System.out.println("for-each OK");
        }
    }
}
