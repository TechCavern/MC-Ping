package io.github.asyncronous.mcping;

import com.google.gson.Gson;

public final class Test{
    public static void main(String... args)
    throws Exception{
        MCServer server = StandardMCVersions.MC_18.ping("us.playmindcrack.com");
        System.out.println(new Gson().toJson(server));
    }
}