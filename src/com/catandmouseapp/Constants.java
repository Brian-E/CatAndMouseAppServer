package com.catandmouseapp;

public class Constants {
	public static final String ALL="ALL";
	
	// General -1 to -10
	public static final int ERR_MISSING_PARMS = 601;
	public static final int ERR_BAD_PARMS = 602;
	public static final int ERR_NO_LOCATION_DATA = 603;
	public static final int ERR_INVALID_GAME_NUMBER = 604;
	public static final int ERR_INVALID_PLAYER_ID = 605;
	public static final int ERR_BAD_MAC_ADDR = 606;
	public static final int ERR_MULTIPLE_MAC_ADDR = 607;
	public static final int ERR_PLAYER_NOT_FOUND = 608;
	
	// Class specific -11 to ....
	public static final int ERR_LOGIN_EXISTING_DIFF_MAC_ADDR = 611;
	public static final int ERR_JOIN_GAME_PLAYER_NOT_LOGGED_IN = 612;
	public static final int ERR_JOIN_GAME_NO_PASSWORD = 614;
	public static final int ERR_JOIN_GAME_BAD_PASSWORD = 615;
	public static final int ERR_JOIN_GAME_TOO_EARLY = 616;
	public static final int ERR_JOIN_GAME_TOO_LATE = 617;
	public static final int ERR_JOIN_GAME_OUTSIDE_PERIMETER = 618;
	public static final int ERR_CREATE_GAME_NAME_EXISTS = 619;
	
	public static final String PARM_PLAYER_ID="playerId";
	public static final String PARM_PLAYER_NAME="playerName";
	public static final String PARM_MAC_ADDR="macAddress";
	public static final String PARM_GAME_NUMBER="gameNumber";
	public static final String PARM_GAME_NAME="gameName";
	public static final String PARM_PASSWORD="password";
	public static final String PARM_LATITUDE="latitude";
	public static final String PARM_LONGITUDE="longitude";
	public static final String PARM_GAME_PLAYER_ID="gamePlayerId";
	public static final String PARM_LAST_UPDATE_TIME="lastUpdateTime";
	public static final String PARM_GAME_BEAN="gameBean";
	public static final String PARM_NOTIFICATION_BEAN="notificationBean";
	public static final String PARM_CENTER_LATITUDE="centerLatitude";
	public static final String PARM_CENTER_LONGITUDE="centerLongitude";
	public static final String PARM_CORNER_LATITUDE="cornerLatitude";
	public static final String PARM_CORNER_LONGITUDE="cornerLongitude";
	
	public static final long TIME_MILLISECONDS_5_MIN=300000;
	public static final long TIME_MILLISECONDS_30_SEC=30000;
	public static final long TIME_MILLISECONDS_1_MIN=60000;
	public static final long TIME_PLAYER_INACTIVITY_MILLISECONDS=TIME_MILLISECONDS_5_MIN;
	
	// Notifications
	public static int NOTIFICATION_PLAYER_CAUGHT = 0;
	public static int NOTIFICATION_STATE_CAT = 1;
	public static int NOTIFICATION_STATE_MOUSE = 2;
	public static int NOTIFICATION_GAME_OVER = 3;
	public static int NOTIFICATION_INACTIVTY_LOGOUT = 4;
	
	// Game types
	public static final int GAME_TYPE_NORMAL = 0;
	public static final int GAME_TYPE_REVERSE = 1;

	// PUBNUB
	public static final String PUBNUB_PUBLISH_KEY="pub-5901b2b5-5dd6-48bb-986d-ee604931f07a";
	public static final String PUBNUB_SUBSCRIBE_KEY="sub-903f254c-920c-11e0-8fea-29df83201866";
	public static final String PUBNUB_SECRET_KEY="sec-efd833df-5f86-4af1-9c4d-2170f8273996";	
}
