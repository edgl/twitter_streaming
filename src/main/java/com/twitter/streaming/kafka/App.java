package com.twitter.streaming.kafka;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

public class App {

	public static void main(String[] args) {
		
		try {
			App.run(args[0], args[1], args[2], args[3]);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void run(String consumerKey, String consumerSecret, String token, String secret) throws InterruptedException {
		BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(100000);
		BlockingQueue<Event> msgEvents = new LinkedBlockingQueue<Event>(100000);
		
		StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
		
		endpoint.stallWarnings(false);
		
		Authentication auth = new OAuth1(consumerKey, consumerSecret, token, secret );
		
		BasicClient client = new ClientBuilder()
				.name("SampleExampleClient")
				.hosts(Constants.USERSTREAM_HOST)
				.endpoint(endpoint)
				.authentication(auth)
				.processor(new StringDelimitedProcessor(msgQueue))
				.build();
		
		client.connect();
		
		for (int msgRead = 0; msgRead < 1000; msgRead++) {
			if(client.isDone()) {
				System.out.println("Client connection closed unexpectedly: " + client.getExitEvent().getMessage());
				break;
			}
			
		}
		
		String msg = msgQueue.poll(5, TimeUnit.SECONDS);
		if(msg == null ){
			System.out.println("Did not receive a message in 5 seconds");
		}
		else {
			System.out.println(msg);
		}
		
		client.stop();
		
		System.out.printf("The client read %d messages!\n", client.getStatsTracker().getNumMessages());
		
	}
}
