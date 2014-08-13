package io.github.asyncronous.mcping;

public final class MCServer{
    public final int pingVersion;
    public final int protocolVersion;
    public final int maxPlayers;
    public final int players;
    public final String motd;
    public final String gameVersion;

    protected MCServer(int pingVersion, int protocolVersion, int maxPlayers, int players, String motd, String gameVersion){
        this.pingVersion = pingVersion;
        this.protocolVersion = protocolVersion;
        this.maxPlayers = maxPlayers;
        this.players = players;
        this.motd = motd;
        this.gameVersion = gameVersion;
    }
}