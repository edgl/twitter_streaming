package com.twitter.streaming.kafka;

import java.io.IOException;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class AppTwitter4J {

	private static final StanfordCoreNLP pipeline;
	static {
		Properties properties = new Properties();
		try {
			properties.load(AppTwitter4J.class.getClassLoader().getResourceAsStream("nlp.properties"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		pipeline = new StanfordCoreNLP(properties);
	}
	
	public static void main(String[] args) throws Exception {
		
		final ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setDebugEnabled(true)
		.setOAuthConsumerKey(args[0])
		.setOAuthConsumerSecret(args[1])
		.setOAuthAccessToken(args[2])
		.setOAuthAccessTokenSecret(args[3]);
		
		final TwitterFactory factory = new TwitterFactory(builder.build());
		final Twitter twitter = factory.getInstance();
		
		ResponseList<Status> statuses = twitter.getUserTimeline("briefingcom");
		
		for(Status status : statuses) {
			System.out.println(status.getText() + " | SENTIMENT SCORE: " + findSentiment(status.getText()));
		}
	}
	
	private static int findSentiment(String tweet) {
		int mainSentiment = 0;
		
		int longest = 0;
		Annotation ann = pipeline.process(tweet);
		for(CoreMap sentence : ann.get(CoreAnnotations.SentencesAnnotation.class)) {
			
			Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
			int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
			String partText = sentence.toString();
			if(partText.length() > longest) {
				mainSentiment = sentiment;
				longest = partText.length();
			}
			
		}
		
		return longest;
	}

}
