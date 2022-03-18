package com.codepath.apps.restclienttemplate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.facebook.stetho.inspector.jsonrpc.JsonRpcException
import okhttp3.Headers
import org.json.JSONException

class TimelineActivity : AppCompatActivity() {

    lateinit var client: TwitterClient

    lateinit var rvTweets : RecyclerView

    lateinit var adapter : TweetsAdapter

    lateinit var swipeContainer: SwipeRefreshLayout

    val tweets = ArrayList<Tweet>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        client = TwitterApplication.getRestClient(this)
        rvTweets = findViewById(R.id.rvTweets)
        adapter = TweetsAdapter(tweets)

        swipeContainer = findViewById(R.id.swipeContainer)

        swipeContainer.setOnRefreshListener {
            // Your code to refresh the list here.
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            populateHomeTimeline()
        }

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light)

        rvTweets.layoutManager = LinearLayoutManager(this)
        rvTweets.adapter = adapter

        populateHomeTimeline()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.compose){
            val intent = Intent(this, ComposeActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == RESULT_OK && requestCode == REQUEST_CODE){
            val tweet = data?.getParcelableExtra("tweet") as Tweet
            tweets.add(0,tweet)
            adapter.notifyItemInserted(0)
            rvTweets.smoothScrollToPosition(0)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun populateHomeTimeline(){
        client.getHomeTimeline(object : JsonHttpResponseHandler(){

            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.i(TAG, "onSuccess!")

                try {
                    adapter.clear()
                    val jsonArray = json.jsonArray
                    val listOfNewTweetsRetrieved = Tweet.fromJsonArray(jsonArray)
                    tweets.addAll(listOfNewTweetsRetrieved)
                    adapter.notifyDataSetChanged()
                    swipeContainer.setRefreshing(false)
                } catch (e : JSONException){
                   Log.i(TAG, "JsonException!! $e")
                }

            }


            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                Log.i(TAG, "Failed!")
            }



        })
    }

    companion object{
        val TAG = "TimelineActivity"
        val REQUEST_CODE = 10
    }
}