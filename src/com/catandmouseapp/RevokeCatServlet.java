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

public class RevokeCatServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(RevokeCatServlet.class.getName());

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		doPost(req, resp);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		// Check if the gameplayer is still a cat. If he is, revoke him and call assign cat
		String gamePlayerId = req.getParameter(Constants.PARM_GAME_PLAYER_ID);
		if (gamePlayerId==null || gamePlayerId.isEmpty()) {
			resp.sendError(Constants.ERR_BAD_PARMS);
		}

		// Try and get this game player
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			GamePlayer gamePlayer = pm.getObjectById(GamePlayer.class, gamePlayerId);
			// Get player id
			try {
				Player player = pm.getObjectById(Player.class, gamePlayer.getPlayerId());
				// Still in the game.  Is he a cat or a mouse?
				try {
					Query query = pm.newQuery(Game.class);
					query.setFilter(Game.FIELD_GAME_NUMBER+"=="+gamePlayer.getGameNumber());
					List<Game> gameList =  (List<Game>) query.execute();
					if (gameList.size()>1) {
						// messed up!
						log.log(Level.SEVERE, "multiple games with same game number!");
						return;
					}
					else if (gameList.size()==1) {
						Game game = gameList.get(0);
						if ((game.getGameType()==Constants.GAME_TYPE_NORMAL && gamePlayer.isCat()) ||
								(game.getGameType()==Constants.GAME_TYPE_REVERSE && !gamePlayer.isCat())) {
							// This guy is still 'it'; change that
							int notificationType = game.getGameType()==Constants.GAME_TYPE_NORMAL ? Constants.NOTIFICATION_STATE_MOUSE : Constants.NOTIFICATION_STATE_CAT;
							String catPlayerId = game.getGameType()==Constants.GAME_TYPE_NORMAL ? "" : player.getPlayerId();
							String catPlayerName = game.getGameType()==Constants.GAME_TYPE_NORMAL ? "" : player.getPlayerName();
							String mousePlayerId = game.getGameType()==Constants.GAME_TYPE_NORMAL ? player.getPlayerId() : "";
							String mousePlayerName = game.getGameType()==Constants.GAME_TYPE_NORMAL ? player.getPlayerName() : "";
							Notification n = new Notification(Constants.ALL, game.getGameNumber(), game.getGameType(), catPlayerId, catPlayerName, 
									mousePlayerId, mousePlayerName, 
									notificationType, System.currentTimeMillis());
							//pm.makePersistent(n);
							CMUtil.publishNotification(game.getGameNumber(), n);

							gamePlayer.setCat(!gamePlayer.isCat());

							// Assign a new one
							CMUtil.assignCat(game.getGameNumber(), Constants.TIME_MILLISECONDS_30_SEC/2); // 15 second delay seems sufficient
						}
					}
				}
				catch (JDOObjectNotFoundException e) {
					// Where's the fuckin game?!
					log.log(Level.SEVERE, "Game number "+gamePlayer.getGameNumber()+" not found!");
				}
			}
			catch (JDOObjectNotFoundException e) {
				log.log(Level.WARNING, "Player is null!");
			}
		}
		catch (JDOObjectNotFoundException e) {
			log.log(Level.WARNING, "GamePlayer is null!");
		}
		finally {
			if (pm!=null)
				pm.close();
		}
	}

}
