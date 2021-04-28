package com.catandmouseapp;

import java.io.Serializable;

import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class ComposedIdKey implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 6788239534778930856L;
	public String playerId;
    public int gameNumber;

    /**
     *  Default constructor.
     */
    public ComposedIdKey ()
    {
    }

    /**
     * String constructor.
     */
    public ComposedIdKey(String playerName, int gameNumber) 
    {
        //field1
        this.playerId = playerName;
        //field2
        this.gameNumber = gameNumber;
    }

    public String getPlayerId() {
		return playerId;
	}

	public int getGameNumber() {
		return gameNumber;
	}

	/**
     * Implementation of equals method.
     */
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (!(obj instanceof ComposedIdKey))
        {
            return false;
        }
        ComposedIdKey c = (ComposedIdKey)obj;

        return playerId.equals(c.playerId) && gameNumber==c.gameNumber;
    }

    /**
     *  Implementation of hashCode method that supports the
     *  equals-hashCode contract.
     */
    public int hashCode ()
    {
        return this.playerId.hashCode() ^ gameNumber;
    }

    /**
     *  Implementation of toString that outputs this object id's PK values.
     */
    public String toString ()
    {
        return this.playerId + this.gameNumber;
    }
}
