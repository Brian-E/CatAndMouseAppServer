package com.catandmouseapp;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CaughtPlayerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(CaughtPlayerServlet.class.getName());

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		doPost(req, resp);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		// Check if this is a valid catch. Produce notification if it is
		String catMacAddress = req.getParameter(Constants.PARM_MAC_ADDR);
		String mousePlayerId = req.getParameter(Constants.PARM_PLAYER_ID);
		String gameNumber = req.getParameter(Constants.PARM_GAME_NUMBER);

		if ((catMacAddress==null || catMacAddress.isEmpty()) || (mousePlayerId==null || mousePlayerId.isEmpty()) || 
				(gameNumber==null || gameNumber.isEmpty())) {
			log.log(Level.WARNING, "missing parms");
			resp.sendError(Constants.ERR_MISSING_PARMS);
			return;
		}

		int iGameNumber = 0;
		try {
			iGameNumber = Integer.parseInt(gameNumber);
		}
		catch (NumberFormatException e) {
			log.log(Level.WARNING, "bad parms");
			resp.sendError(Constants.ERR_BAD_PARMS);
			return;
		}

		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			// Get this cat player id if it exists
			Query query = pm.newQuery(Player.class);
			query.setFilter(Player.FIELD_MAC_ADDR+"=='"+catMacAddress+"'");
			List<Player> playerList = (List<Player>) query.execute();
			if (playerList.size()==1) {
				Player catPlayer = playerList.get(0);
				// See if this player is a cat in the given game
				String gamePlayerId = GamePlayer.createGamePlayerId(catPlayer.getPlayerId(), iGameNumber);
				try {
					GamePlayer catGamePlayer = pm.getObjectById(GamePlayer.class, gamePlayerId);
					if (catGamePlayer.isCat()) {
						// OK, you're busted. Send out a notification if this game exists
						try {
							query = pm.newQuery(Game.class);
							query.setFilter(Game.FIELD_GAME_NUMBER+"=="+iGameNumber);
							List<Game> listGames = (List<Game>) query.execute();
							Game game = listGames.get(0);
							try {
								Player mousePlayer = pm.getObjectById(Player.class, mousePlayerId);
								Notification n = new Notification(Constants.ALL, iGameNumber, game.getGameType(), catPlayer.getPlayerId(), catPlayer.getPlayerName(), 
										mousePlayer.getPlayerId(),mousePlayer.getPlayerName(), Constants.NOTIFICATION_PLAYER_CAUGHT, System.currentTimeMillis());
								//pm.makePersistent(n);
								CMUtil.publishNotification(iGameNumber, n);
								
								// If this is a reverse game, change this mouse back to a cat, and assign
								// a new mouse
								if (game.getGameType()==Constants.GAME_TYPE_REVERSE) {
									String mouseGamePlayerId = GamePlayer.createGamePlayerId(mousePlayer.getPlayerId(), iGameNumber);
									try {
										GamePlayer mouseGamePlayer = pm.getObjectById(GamePlayer.class, mouseGamePlayerId);
										mouseGamePlayer.setCat(true);
										// Send the change notification
										Notification nMouse = new Notification(Constants.ALL, iGameNumber, game.getGameType(), 
												mousePlayer.getPlayerId(),mousePlayer.getPlayerName(), "", "", Constants.NOTIFICATION_STATE_CAT, System.currentTimeMillis());
										CMUtil.publishNotification(iGameNumber, nMouse);
										
										// Establish a new mouse in 10 seconds
										CMUtil.assignCat(iGameNumber, 10000);
									}
									catch (JDOObjectNotFoundException e) {
										// didn't find the damn mouse?
										log.log(Level.WARNING, "Couldn't find the mouse game player in game "+iGameNumber);
										// Establish a new mouse in 10 seconds, probably need it
										CMUtil.assignCat(iGameNumber, 10000);
									}
								}

							}
							catch (JDOObjectNotFoundException e) {
								// This mouse player isn't in the game!  Geez...
								log.log(Level.WARNING, "Mouse playerid "+mousePlayerId+" not found!");
								resp.sendError(Constants.ERR_PLAYER_NOT_FOUND);
								return;
							}
						}
						catch (JDOObjectNotFoundException e) {
							// game doesn't exist?!
							log.log(Level.SEVERE, "Game "+gameNumber+" not found!");
							resp.sendError(Constants.ERR_INVALID_GAME_NUMBER);
							return;
						}
						//					if (game!=null) {
						//						Player mousePlayer = pm.getObjectById(Player.class, mousePlayerId);
						//						if (mousePlayer!=null) {
						//							Notification n = new Notification(Constants.ALL, iGameNumber, game.getGameType(), catPlayer.getPlayerName(), 
						//									mousePlayer.getPlayerName(), Constants.NOTIFICATION_PLAYER_CAUGHT, System.currentTimeMillis());
						//							pm.makePersistent(n);
						//						}
						//						else {
						//							// This mouse player isn't in the game!  Geez...
						//							log.log(Level.WARNING, "Mouse playerid "+mousePlayerId+" not found!");
						//							resp.sendError(HttpServletResponse.SC_BAD_REQUEST, Integer.toBinaryString(Constants.ERR_PLAYER_NOT_FOUND));
						//						}
						//					}
						//					else {
						//						// game doesn't exist?!
						//						log.log(Level.SEVERE, "Game "+gameNumber+" not found!");
						//						resp.sendError(HttpServletResponse.SC_BAD_REQUEST, Integer.toBinaryString(Constants.ERR_INVALID_GAME_NUMBER));
						//					}
					}
					else {
						// Not a cat, or changed back. Too bad for him
						log.log(Level.WARNING, "Gameplayer "+gamePlayerId+" is not a cat");
						resp.sendError(Constants.ERR_PLAYER_NOT_FOUND);
						return;
					}

				}
				catch (JDOObjectNotFoundException e) {
					// Doesn't exist
					log.log(Level.WARNING, "Cat player was not found in game "+iGameNumber);
					resp.sendError(Constants.ERR_PLAYER_NOT_FOUND);
					return;
				}
				//				if (catGamePlayer!=null) {
				//					// Is this guy actually a cat?
				//					if (catGamePlayer.isCat()) {
				//						// OK, you're busted. Send out a notification if this game exists
				//						Game game = pm.getObjectById(Game.class, gameNumber);
				//						if (game!=null) {
				//							Player mousePlayer = pm.getObjectById(Player.class, mousePlayerId);
				//							if (mousePlayer!=null) {
				//								Notification n = new Notification(Constants.ALL, iGameNumber, game.getGameType(), catPlayer.getPlayerName(), 
				//										mousePlayer.getPlayerName(), Constants.NOTIFICATION_PLAYER_CAUGHT, System.currentTimeMillis());
				//								pm.makePersistent(n);
				//							}
				//							else {
				//								// This mouse player isn't in the game!  Geez...
				//								log.log(Level.WARNING, "Mouse playerid "+mousePlayerId+" not found!");
				//								resp.sendError(HttpServletResponse.SC_BAD_REQUEST, Integer.toBinaryString(Constants.ERR_PLAYER_NOT_FOUND));
				//							}
				//						}
				//						else {
				//							// game doesn't exist?!
				//							log.log(Level.SEVERE, "Game "+gameNumber+" not found!");
				//							resp.sendError(HttpServletResponse.SC_BAD_REQUEST, Integer.toBinaryString(Constants.ERR_INVALID_GAME_NUMBER));
				//						}
				//					}
				//					else {
				//						// Not a cat, or changed back. Too bad for him
				//						log.log(Level.WARNING, "Gameplayer "+gamePlayerId+" is not a cat");
				//						resp.sendError(HttpServletResponse.SC_BAD_REQUEST, Integer.toBinaryString(Constants.ERR_PLAYER_NOT_FOUND));
				//					}
				//				}
				//				else {
				//					// Doesn't exist
				//					log.log(Level.WARNING, "Cat player was not found in game "+iGameNumber);
				//					resp.sendError(HttpServletResponse.SC_BAD_REQUEST, Integer.toBinaryString(Constants.ERR_PLAYER_NOT_FOUND));
				//				}
			}
			else if (playerList.size()==0) {
				// no player with this mac address
				log.log(Level.SEVERE, "no player by mac address "+catMacAddress+" exists");
				resp.sendError(Constants.ERR_BAD_MAC_ADDR);
				return;
			}
			else if (playerList.size()>1) {
				// More than one player with this mac address???
				log.log(Level.SEVERE, "More than one player with mac address "+catMacAddress+" discovered");
				resp.sendError(Constants.ERR_MULTIPLE_MAC_ADDR);
				return;
			}
			else {
				// ???
				log.log(Level.SEVERE, "I don't even know how I got here!");
				resp.sendError(Constants.ERR_BAD_MAC_ADDR);
				return;
			}
		}
		finally {
			if (pm!=null)
				pm.close();
		}
	}

}
