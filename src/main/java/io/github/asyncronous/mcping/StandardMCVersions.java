package io.github.asyncronous.mcping;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public enum StandardMCVersions
implements Ping{
    MC_14(){
        @Override
        public MCServer ping(InetSocketAddress address)
        throws IOException{
            try(Socket socket = new Socket()){
                socket.setSoTimeout(TIMEOUT);
                socket.setKeepAlive(false);
                socket.connect(address, TIMEOUT);

                try(InputStream in = socket.getInputStream();
                    OutputStream out = socket.getOutputStream()){

                    out.write(new byte[]{
                            (byte) 0xFE,
                            (byte) 0x01
                    });

                    int pID = in.read();
                    if(pID == -1){
                        throw new IOException("Premature end of stream");
                    }

                    if(pID != 0xFF){
                        throw new IOException("Invalid Packet ID (" + pID + ")");
                    }

                    int len = in.read();
                    if(pID == -1){
                        throw new IOException("Premature end of stream");
                    }

                    if(len ==0){
                        throw new IOException("Invalid String length");
                    }

                    byte[] bits = new byte[len];
                    if(in.read(bits, 0, len) != len){
                        throw new IOException("Premature end of Stream");
                    }

                    String[] data = new String(bits).split("\00");
                    int pingVersion = Integer.parseInt(data[0].substring(1));
                    int protocVersion = Integer.parseInt(data[1]);
                    String gameVersion = data[2];
                    String motd = data[3];
                    int playersOnline = Integer.parseInt(data[4]);
                    int maxPlayers = Integer.parseInt(data[5]);

                    return new MCServer(pingVersion, protocVersion, maxPlayers, playersOnline, motd, gameVersion);
                }
            }
        }
    },
    MC_18(){
        private int read(InputStream in)
        throws IOException{
            int i = 0;
            int j = 0;
            while (true) {
                int k = in.read();
                i |= (k & 0x7F) << j++ * 7;
                if (j > 5) {
                    throw new RuntimeException("VarInt too big");
                }
                if ((k & 0x80) != 128){
                    break;
                }
            }
            return i;
        }

        private void write(OutputStream out, int i)
        throws IOException{
            while(true)
            {
                if((i & 0xFFFFFF80) == 0){
                    out.write(i);
                    return;
                }

                out.write(i & 0x7F | 0x80);
                i >>>= 7;
            }
        }

        @Override
        public MCServer ping(InetSocketAddress address)
        throws IOException{
            try(Socket socket = new Socket()){
                socket.setSoTimeout(TIMEOUT);
                socket.connect(address, TIMEOUT);

                try(DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    DataOutputStream handshake = new DataOutputStream(bos)){

                    handshake.writeByte(0x00);
                    write(handshake, 4);
                    write(handshake, address.getHostString().length());
                    handshake.writeBytes(address.getHostString());
                    handshake.writeShort(address.getPort());
                    write(handshake, 1);
                    write(out, bos.size());
                    out.write(bos.toByteArray());
                    out.writeByte(0x01);
                    out.writeByte(0x00);
                    read(in);
                    int id = read(in);

                    if (id == -1) {
                        throw new IOException("Premature end of stream.");
                    }

                    if (id != 0x00) {
                        throw new IOException("Invalid packetID");
                    }

                    int length = read(in);

                    if (length == -1) {
                        throw new IOException("Premature end of stream.");
                    }

                    if (length == 0) {
                        throw new IOException("Invalid string length.");
                    }

                    byte[] bits = new byte[length];
                    in.readFully(bits);
                    StatusResponse resp = GSON.fromJson(new String(bits), StatusResponse.class);
                    return new MCServer(174, Integer.valueOf(resp.version.protocol), resp.players.max, resp.players.online, resp.description, resp.version.name);
                }
            }
        }
    };

    @Override
    public MCServer ping(String address)
    throws IOException{
        return this.ping(new InetSocketAddress(InetAddress.getByName(address), 25565));
    }

    private static final Gson GSON = new Gson();
    public static final int TIMEOUT = 7000;

    private final class StatusResponse{
        private final String description;
        private final String favicon;
        private final int time;
        private final Version version;
        private final Players players;

        private StatusResponse(String description, String favicon, int time, Version version, Players players){
            this.description = description;
            this.favicon = favicon;
            this.time = time;
            this.version = version;
            this.players = players;
        }
    }

    private final class Players{
        private final int max;
        private final int online;
        private final Player[] sample;

        private Players(int max, int online, Player[] sample){
            this.max = max;
            this.online = online;
            this.sample = sample;
        }
    }

    private final class Player{
        private final String name;
        private final String id;

        private Player(String name, String id){
            this.name = name;
            this.id = id;
        }
    }

    private final class Version{
        private final String name;
        private final String protocol;

        private Version(String name, String protocol){
            this.name = name;
            this.protocol = protocol;
        }
    }
}