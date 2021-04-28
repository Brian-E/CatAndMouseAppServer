package com.catandmouseapp;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable
public class Notification {
	@NotPersistent
	public static final String FIELD_INTENDED_PLAYER_ID = "intendedPlayerId";

	@NotPersistent
	public static final String FIELD_CAT_PLAYER_NAME = "catPlayerName";

	@NotPersistent
	public static final String FIELD_MOUSE_PLAYER_NAME = "mousePlayerName";
	
	@NotPersistent 
	public static final String FIELD_NOTIFICATION_TYPE = "notificationType";

	@NotPersistent 
	public static final String FIELD_DESCRIPTION = "description";

	@NotPersistent 
	public static final String FIELD_ACTIVITY_DATE = "activityDate";
	
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private int gameNumber;
    
    @Persistent
    private int gameType;
    
    @Persistent
    private String intendedPlayerId;
    
    @Persistent
    private String catPlayerId;
    
    @Persistent
    private String catPlayerName;
    
    @Persistent
    private String mousePlayerId;
    
    @Persistent
    private String mousePlayerName;
    
    @Persistent
    private int notificationType = 0;
    
    @Persistent
    private long activityDate;
    
    public Notification(String intendedPlayerId, int gameNumber, int gameType, String catPlayerId, String catPlayerName, 
    		String mousePlayerId, String mousePlayerName, int notificationType, long activityDate) {
    	this.intendedPlayerId = intendedPlayerId;
    	this.gameNumber = gameNumber;
    	this.gameType = gameType;
    	this.catPlayerId = catPlayerId;
    	this.catPlayerName = catPlayerName;
    	this.mousePlayerId = mousePlayerId;
    	this.mousePlayerName = mousePlayerName;
    	this.notificationType = notificationType;
    	this.activityDate = activityDate;
    }

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public int getGameNumber() {
		return gameNumber;
	}

	public void setGameNumber(int gameNumber) {
		this.gameNumber = gameNumber;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public int getGameType() {
		return gameType;
	}

	public String getIntendedPlayerId() {
		return intendedPlayerId;
	}

	public void setIntendedPlayerId(String intendedPlayerId) {
		this.intendedPlayerId = intendedPlayerId;
	}

	public void setCatPlayerId(String catPlayerId) {
		this.catPlayerId = catPlayerId;
	}

	public String getCatPlayerId() {
		return catPlayerId;
	}

	public String getCatPlayerName() {
		return catPlayerName;
	}

	public void setCatPlayerName(String catPlayerName) {
		this.catPlayerName = catPlayerName;
	}

	public void setMousePlayerId(String mousePlayerId) {
		this.mousePlayerId = mousePlayerId;
	}

	public String getMousePlayerId() {
		return mousePlayerId;
	}

	public String getMousePlayerName() {
		return mousePlayerName;
	}

	public void setMousePlayerName(String mousePlayerName) {
		this.mousePlayerName = mousePlayerName;
	}

	public int getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(int notificationType) {
		this.notificationType = notificationType;
	}

	public long getActivityDate() {
		return activityDate;
	}

	public void setActivityDate(long activityDate) {
		this.activityDate = activityDate;
	}

}
