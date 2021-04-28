package com.catandmouseapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AssignCatServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(AssignCatServlet.class.getName());

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		doPost(req, resp);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		// get game number from the request
		int gameNumber = 0;
		String strGameNumber = req.getParameter(Constants.PARM_GAME_NUMBER);
		if (strGameNumber!=null && strGameNumber.length()>0) {
			try {
				gameNumber = Integer.parseInt(strGameNumber);
			}
			catch (NumberFormatException e) {
				log.warning("Bad game number supplied: "+strGameNumber);
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, Integer.toString(Constants.ERR_BAD_PARMS));
				return;
			}
		}
		else {
			log.warning("No game number supplied");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, Integer.toString(Constants.ERR_BAD_PARMS));
			return;
		}

		// Collect all the players for this game and determine if we have the right ratio for a new cat or mouse
		if (gameNumber>0) {
			PersistenceManager pm = PMF.get().getPersistenceManager();
			try {
				Query query = pm.newQuery(Game.class);
				query.setFilter(Game.FIELD_GAME_NUMBER+"=="+gameNumber);
				List<Game> gameList = (List<Game>) query.execute();
				Game game;
				if (gameList!=null && gameList.size()==1) {
					game = gameList.get(0);
				}
				else {
					// WTF?
					log.log(Level.SEVERE, "No game found or multiple games found for game number:"+gameNumber);
					return;
				}

				query = pm.newQuery(GamePlayer.class);
				query.setFilter(Game.FIELD_GAME_NUMBER+"=="+gameNumber);

				List<GamePlayer> playerList = (List<GamePlayer>) query.execute();
				List<GamePlayer> mouseList = new ArrayList<GamePlayer>();
				if (playerList.size()>0) {
					// how many cats do we currently have?
					int iCats = 0;
					for (int i=0; i < playerList.size(); i++) {
						GamePlayer player = playerList.get(i);
						if ((game.getGameType()==Constants.GAME_TYPE_NORMAL && player.isCat()) ||
								(game.getGameType()==Constants.GAME_TYPE_REVERSE && !player.isCat()))
							iCats++;
						else
							mouseList.add(player);
					}

					// Compare our cat number to the player number and if its > ratio we need another cat or mouse
					int iCatsNeeded = playerList.size()/(game.getRatio()); // Testing line
					//TODO: int iCatsNeeded = playerList.size()/(game.getRatio()+1); // use this once we're GA
					if (iCatsNeeded > iCats) {
						log.log(Level.INFO, "Assigning "+(iCatsNeeded-iCats)+(game.getGameType()==Constants.GAME_TYPE_NORMAL ? " cats" : " mice"));
						// we need a cat or mouse; randomly assign one of these jamokes and update em
						for (; iCats < iCatsNeeded; iCats++) {
							Random random = new Random(System.currentTimeMillis());
							GamePlayer player = mouseList.get(random.nextInt(mouseList.size()));
							// Does this player exist?  Would be weird if he didnt
							try {
								Player catPlayer = pm.getObjectById(Player.class, player.getPlayerId());
								player.setCat(game.getGameType()==Constants.GAME_TYPE_NORMAL); // change is persisted at closing

								// Notify players
								String catPlayerId = game.getGameType()==Constants.GAME_TYPE_NORMAL ? catPlayer.getPlayerId() : "";
								String catPlayerName = game.getGameType()==Constants.GAME_TYPE_NORMAL ? catPlayer.getPlayerName() : "";
								String mousePlayerId = game.getGameType()==Constants.GAME_TYPE_REVERSE ? catPlayer.getPlayerId() : "";
								String mousePlayerName = game.getGameType()==Constants.GAME_TYPE_REVERSE ? catPlayer.getPlayerName() : "";
								int notificationType = game.getGameType()==Constants.GAME_TYPE_NORMAL ? Constants.NOTIFICATION_STATE_CAT : Constants.NOTIFICATION_STATE_MOUSE;
								Notification n = new Notification(Constants.ALL, player.getGameNumber(), game.getGameType(), catPlayerId, catPlayerName, 
										mousePlayerId, mousePlayerName, notificationType, System.currentTimeMillis());
								//pm.makePersistent(n);
								//pm.makePersistent(player);
								CMUtil.publishNotification(gameNumber, n);

								// Set our revokation task in case this client disappears
								CMUtil.revokeCat(player.getGamePlayerId());
							}
							catch (JDOObjectNotFoundException e) {
								// player is in a game but not in the player db?!?!
								log.log(Level.SEVERE, "Player "+player.getPlayerId()+" is in a game but not in the player db!");
								//I guess call assign cat again
								CMUtil.assignCat(gameNumber);
							}
							
							// remove player from mouse list before looping
							mouseList.remove(player);
						}
						//						if (catPlayer!=null) {
						//							player.setCat(game.getGameType()==Constants.GAME_TYPE_NORMAL); // change is persisted at closing
						//							
						//							// Notify players
						//							String catPlayerName = game.getGameType()==Constants.GAME_TYPE_NORMAL ? catPlayer.getPlayerName() : "";
						//							String mousePlayerName = game.getGameType()==Constants.GAME_TYPE_REVERSE ? catPlayer.getPlayerName() : "";
						//							int notificationType = game.getGameType()==Constants.GAME_TYPE_NORMAL ? Constants.NOTIFICATION_STATE_CAT : Constants.NOTIFICATION_STATE_MOUSE;
						//							Notification n = new Notification(Constants.ALL, player.getGameNumber(), game.getGameType(), catPlayerName, mousePlayerName, 
						//									notificationType, System.currentTimeMillis());
						//							pm.makePersistent(n);
						//							
						//							// Set our revokation task in case this client disappears
						//							CMUtil.revokeCat(player.getGamePlayerId());
						//						}
						//						else {
						//							// player is in a game but not in the player db?!?!
						//							log.log(Level.SEVERE, "Player "+player.getPlayerId()+" is in a game but not in the player db!");
						//							//I guess call assign cat again
						//							CMUtil.assignCat(gameNumber);
						//						}
						//						
					}
				}
			}
			finally {
				if (pm!=null)
					pm.close();
			}
		}
	}
}
