package io.github.asyncronous.mcping;

import java.io.IOException;
import java.net.InetSocketAddress;

public interface Ping{
    public MCServer ping(InetSocketAddress address)
    throws IOException;
    public MCServer ping(String address)
    throws IOException;
}