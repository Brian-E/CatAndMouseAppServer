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

public class JoinGameServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(JoinGameServlet.class.getName());

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		doPost(req, resp);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		String playerId = req.getParameter(Constants.PARM_PLAYER_ID);
		String gameNumber = req.getParameter(Constants.PARM_GAME_NUMBER);
		String password = req.getParameter(Constants.PARM_PASSWORD);
		boolean bJoinPlayer = true;

		if ((playerId==null || playerId.isEmpty()) || (gameNumber==null || gameNumber.isEmpty())) {
			log.log(Level.WARNING, "Bad join game parameters");
			resp.sendError(Constants.ERR_MISSING_PARMS); // bad parameters
			bJoinPlayer = false;
			return;
		}

		int iGameNumber;
		try {
			iGameNumber = Integer.parseInt(gameNumber);
		}
		catch (NumberFormatException e) {
			log.log(Level.WARNING, "Non-numeric game number");
			resp.sendError(Constants.ERR_BAD_PARMS);
			bJoinPlayer = false;
			return;
		}

		// Get details about the game
		PersistenceManager pm = PMF.get().getPersistenceManager();
		// Is the player logged in?
		try {
			Player player = pm.getObjectById(Player.class, playerId);

			try {
				// Is this a valid game?
				Query query = pm.newQuery(Game.class);
				query.setFilter(Game.FIELD_GAME_NUMBER+"=="+iGameNumber);
				List<Game> listGames = (List<Game>) query.execute();
				Game game = null;
				if (listGames.size()>0)
					game = listGames.get(0);

				// Is this a private game?
				if (game.isPrivate()) {
					// Did they supply a valid password
					if (password==null || password.isEmpty()) {
						log.log(Level.WARNING, "No password supplied for private game");
						resp.sendError(Constants.ERR_JOIN_GAME_NO_PASSWORD);
						bJoinPlayer = false;
						return;
					}

					if (!password.equals(game.getPassword())) {
						log.log(Level.WARNING, "Wrong password supplied for private game");
						resp.setStatus(Constants.ERR_JOIN_GAME_BAD_PASSWORD);
						resp.sendError(Constants.ERR_JOIN_GAME_BAD_PASSWORD);	
						bJoinPlayer = false;
						return;
					}
				}

				// Are there time parameters for this game?
				if (game.getStartTime()!=0 || game.getEndTime()!=0) {
					long currentTime = System.currentTimeMillis();
					if (game.getStartTime()>0 && currentTime < game.getStartTime()) {
						// game hasn't started yet
						log.log(Level.WARNING, "Join game attempt for game "+gameNumber+" that hasn't started yet");
						resp.setStatus(Constants.ERR_JOIN_GAME_TOO_EARLY);			
						resp.sendError(Constants.ERR_JOIN_GAME_TOO_EARLY);			
						bJoinPlayer = false;
						return;
					}
					if (game.getEndTime()>0 && currentTime > game.getEndTime()) {
						// Game is over. Report error

						log.log(Level.WARNING, "Join game attempt for game "+gameNumber+" that has ended");
						resp.sendError(Constants.ERR_JOIN_GAME_TOO_LATE);
						bJoinPlayer = false;
						return;
					}
				}

				// Distance constraints?
				if (game.getLatitude()!=0 || game.getLongitude()!=0) {
					// See if we're in the requested diameter. Get players location
					// valid location?
					if (player.getLocation().getLatitude()==0 && player.getLocation().getLongitude()==0) {
						// Player needs updated location
						bJoinPlayer = false;
						resp.sendError(Constants.ERR_NO_LOCATION_DATA);
						log.log(Level.WARNING, "No location data for player in location-based game");
						return;
					}
					else if (CMUtil.distFrom(player.getLocation().getLatitude(), player.getLocation().getLongitude(), 
							game.getLatitude(), game.getLongitude()) > game.getRange()){
						// Too far!
						bJoinPlayer = false;
						resp.sendError(Constants.ERR_JOIN_GAME_OUTSIDE_PERIMETER);
						log.log(Level.WARNING, "Join game attempt for user outside the distance range");
						return;
					}
				}

				if (bJoinPlayer) {
					// update his activity date
					player.setLastActiveDate(System.currentTimeMillis());
					pm.makePersistent(player);

					// Well, if we made it here, the player is all good. Put em in
					GamePlayer gamePlayer = new GamePlayer(playerId, iGameNumber, game.getGameType()==Constants.GAME_TYPE_REVERSE);

					// Is he already in?
					try {
						GamePlayer loggedInPlayer = pm.getObjectById(GamePlayer.class, gamePlayer.getGamePlayerId());
					}
					catch (JDOObjectNotFoundException e) {
						pm.makePersistent(gamePlayer);			
					}
				}
				//				if (loggedInPlayer==null)
				//					pm.makePersistent(gamePlayer);			

			}
			catch (JDOObjectNotFoundException e) {
				log.log(Level.WARNING, "Game is not valid");
				resp.sendError(Constants.ERR_INVALID_GAME_NUMBER);
				return;
			}
		}
		catch (JDOObjectNotFoundException e) {
			log.log(Level.WARNING, "Player is not logged in");
			resp.sendError(Constants.ERR_JOIN_GAME_PLAYER_NOT_LOGGED_IN);	
			return;
		}
		finally {
			if (pm!=null)
				pm.close();
		}

		if (bJoinPlayer) {
		// 	Call assign cats. 
			CMUtil.assignCat(iGameNumber,5000);
		}
	}

}
