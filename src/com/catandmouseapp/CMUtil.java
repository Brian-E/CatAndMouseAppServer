package com.catandmouseapp;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import pubnub.Pubnub;

import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.gson.Gson;

public class CMUtil {
	private static final Logger log = Logger.getLogger(CMUtil.class.getName());
	//private static final Logger log = Logger.getLogger(CMUtil.class.getName());
	
	public static void assignCat(int gameNumber) {
		CMUtil.assignCat(gameNumber, 0);
	}
	
	public static void assignCat(int gameNumber, long delay) {
		Queue queue = QueueFactory.getDefaultQueue();
		TaskOptions to = TaskOptions.Builder.withUrl("/admin/assigncat").method(Method.POST);//       ?"+Constants.PARM_GAME_NUMBER+"="+gameNumber).method(Method.POST);
		to.param(Constants.PARM_GAME_NUMBER, Integer.toString(gameNumber));
		if (delay > 0)
			to.countdownMillis(delay);
		queue.add(to);		
	}
	
	public static void revokeCat(String gamePlayerId) {
		Queue queue = QueueFactory.getDefaultQueue();
		TaskOptions to = TaskOptions.Builder.withUrl("/admin/revokecat").method(Method.POST); //      ?"+Constants.PARM_GAME_PLAYER_ID+"="+gamePlayerId).method(Method.POST);
		to.param(Constants.PARM_GAME_PLAYER_ID, gamePlayerId);
		to.countdownMillis(Constants.TIME_MILLISECONDS_5_MIN+Constants.TIME_MILLISECONDS_30_SEC); // 5 minutes 30 seconds
		queue.add(to);		
	}
	
	public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 6378137; // meters
		double dLat = Math.toRadians(lat2-lat1);
		double dLng = Math.toRadians(lng2-lng1);
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
		Math.sin(dLng/2) * Math.sin(dLng/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double dist = earthRadius * c;

		return dist;
	}
	
	public static long getPlayerCount(int gameNumber) {
		long playerCount = 0;
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery(GamePlayer.class);
			query.setFilter(Game.FIELD_GAME_NUMBER+"=="+gameNumber);
			List<GamePlayer> gamePlayers = (List<GamePlayer>) query.execute();
			playerCount = gamePlayers.size();
		}
		catch (JDOObjectNotFoundException e) {
			// No big deal
		}		
		finally {
			if (pm!=null)
				pm.close();
		}
		
		return playerCount;
	}
	
	public static void publishNotification(int gameNumber, Notification n) {
		Pubnub pn = new Pubnub(Constants.PUBNUB_PUBLISH_KEY, Constants.PUBNUB_SUBSCRIBE_KEY, Constants.PUBNUB_SECRET_KEY, false );
		NotificationBean nb = new NotificationBean(n.getGameNumber(), n.getGameType(),
				n.getIntendedPlayerId(), n.getCatPlayerId(), n.getCatPlayerName(), 
				n.getMousePlayerId(), n.getMousePlayerName(),
				n.getNotificationType(), n.getActivityDate());
		String notification = new Gson().toJson(nb);

	    JSONObject message = new JSONObject();
	    try { 
	    	message.put( Constants.PARM_NOTIFICATION_BEAN, notification ); 
	    }
	    catch (org.json.JSONException jsonError) {
	    	log.log(Level.SEVERE, "Error creating Pubnub msg: "+jsonError.toString());
	    }

	    // Publish Message
	    JSONArray info = pn.publish(
	        Integer.toString(gameNumber), // Channel Name
	        message        // JSON Message
	    );

	    // Print Response from PubNub JSONP REST Service
	    log.log(Level.INFO, info.toString());
	}
	
	public static void sendLeaderboardMail(String gameName) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        
        String msgBody = "Create a leaderboard for game named "+gameName;
		
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("BrianEmond71@gmail.com", "Cat & Mouse Admin"));
            msg.addRecipient(Message.RecipientType.TO,
                             new InternetAddress("brianemond71@gmail.com", "Brian Emond"));
            msg.setSubject(msgBody);
            msg.setText(msgBody);
            Transport.send(msg);
    
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error sending email for leaderboard "+gameName+". Exception: "+e.toString());
        }
	}
	
}
