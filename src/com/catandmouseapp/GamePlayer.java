package com.catandmouseapp;

import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
@PersistenceCapable
public class GamePlayer {
	@NotPersistent
	public static final String SEPARATOR="-";
	
	@NotPersistent
	public static final String FIELD_GAME_PLAYER_ID="gamePlayerId";	

	@PrimaryKey
	@Persistent
	private String gamePlayerId;
	
	@Persistent
	private String playerId;
	
	@Persistent
	private int gameNumber;
	
	@Persistent
	private boolean isCat;
	
	public GamePlayer(String playerId, int gameNumber, boolean isCat) {
		this.gamePlayerId = playerId+GamePlayer.SEPARATOR+gameNumber;
		this.playerId = playerId;
		this.gameNumber = gameNumber;
		this.isCat = isCat;
	}

	public String getGamePlayerId() {
		return gamePlayerId;
	}

	public void setGamePlayerId(String gamePlayerId) {
		this.gamePlayerId = gamePlayerId;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getGameNumber() {
		return gameNumber;
	}

	public void setGameNumber(int gameNumber) {
		this.gameNumber = gameNumber;
	}

	public boolean isCat() {
		return isCat;
	}

	public void setCat(boolean isCat) {
		this.isCat = isCat;
	}
	
	public static String createGamePlayerId(String playerId, int iGameNumber) {
		return playerId+GamePlayer.SEPARATOR+iGameNumber;
	}
}
