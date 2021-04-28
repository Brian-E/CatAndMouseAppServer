package com.catandmouseapp;

import java.util.Date;

import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.GeoPt;

@PersistenceCapable
public class Player {
	@NotPersistent
	public static final String FIELD_PLAYER_ID="playerId";
	
	@NotPersistent
	public static final String FIELD_PLAYER_NAME="playerName";
	
	@NotPersistent
	public static final String FIELD_MAC_ADDR="macAddress";
	
	@NotPersistent
	public static final String FIELD_LOCATION="location";
	
	@NotPersistent
	public static final String FIELD_LAST_ACTIVE_DATE="lastActiveDate";	
	
	@PrimaryKey
	@Persistent
	private String playerId;
	
	@Persistent
	private String playerName;

	@Persistent
	private String macAddress;
	
	@Persistent
	private GeoPt location;
	
	@Persistent
	private long lastActiveDate;
	
	public Player(String playerId, String playerName, String macAddress, GeoPt location) {
		this.playerId = playerId;
		this.playerName = playerName;
		this.macAddress = macAddress;
		this.location = location;
		this.lastActiveDate = System.currentTimeMillis();
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public GeoPt getLocation() {
		return location;
	}

	public void setLocation(GeoPt location) {
		this.location = location;
	}

	public long getLastActiveDate() {
		return lastActiveDate;
	}

	public void setLastActiveDate(long lastActiveDate) {
		this.lastActiveDate = lastActiveDate;
	}

}
