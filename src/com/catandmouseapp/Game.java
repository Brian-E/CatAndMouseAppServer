package com.catandmouseapp;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;

@PersistenceCapable
public class Game {
	@NotPersistent
	public static final String FIELD_GAME_NUMBER="gameNumber";
	
	@NotPersistent
	public static final String FIELD_GAME_NAME="gameName";
	
	@NotPersistent
	public static final String FIELD_PASSWORD="password";
	
	@NotPersistent
	public static final String FIELD_LOCATION_NAME="locationName";
	
	@NotPersistent
	public static final String FIELD_LATITUDE="latitude";
	
	@NotPersistent
	public static final String FIELD_LONGITUDE="longitude";
	
	@NotPersistent
	public static final String FIELD_RANGE="range";
	
	@NotPersistent
	public static final String FIELD_RATIO="ratio";
	
	@NotPersistent
	public static final String FIELD_START_TIME="startTime";
	
	@NotPersistent
	public static final String FIELD_END_TIME="endTime";	
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private int gameNumber;
	
	@Persistent
	private String gameName;
	
	@Persistent
	private int gameType;
	
	@Persistent
	private boolean isPrivate;
	
	@Persistent
	private String password;
	
	@Persistent
	private String locationName;
	
	@Persistent
	private double latitude;
	
	@Persistent
	private double longitude;
	
	@Persistent
	private long range;
	
	@Persistent
	private int ratio = 10;
	
	@Persistent
	private long startTime;
	
	@Persistent
	private long endTime;
	
	public Game(int gameNumber, String gameName, int gameType, boolean isPrivate, String password, String locationName, double latitude, double longitude, long range, int ratio, long startTime, long endTime) {
		this.gameNumber = gameNumber;
		this.gameName = gameName;
		this.gameType = gameType;
		this.isPrivate = isPrivate;
		this.locationName = locationName;
		this.password = password;
		this.latitude = latitude;
		this.longitude = longitude;
		this.range = range;
		this.ratio = ratio;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Key getKey() {
		return key;
	}

	public int getGameNumber() {
		return gameNumber;
	}

	public void setGameNumber(int gameNumber) {
		this.gameNumber = gameNumber;
	}

	public String getGameName() {
		return gameName;
	}

	public void setGameName(String gameName) {
		this.gameName = gameName;
	}

	public int getGameType() {
		return gameType;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getLocationName() {
		return locationName;
	}

	public long getRange() {
		return range;
	}

	public void setRange(long range) {
		this.range = range;
	}

	public int getRatio() {
		return ratio;
	}

	public void setRatio(int ratio) {
		this.ratio = ratio;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

}
