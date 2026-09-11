package com.jagrosh.discordipc.entities.pipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.entities.Callback;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.serialize.PacketDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public class UnixPipe extends Pipe {

    private static final Logger LOGGER = LogManager.getLogger(UnixPipe.class);
    private final Process process;
    private final DataInputStream in;
    private final OutputStream out;

    UnixPipe(IPCClient ipcClient, HashMap<String, Callback> callbacks, String location) throws IOException {
        super(ipcClient, callbacks);
        this.process = new ProcessBuilder("nc", "-U", location).start();
        this.in = new DataInputStream(process.getInputStream());
        this.out = process.getOutputStream();
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
        out.flush();
    }

    @Override
    public Packet read() throws IOException {
        
        if (status == PipeStatus.DISCONNECTED || status == PipeStatus.CLOSED) {
             throw new IOException("Disconnected!");
        }

        int opCodeInt;
        try {
            opCodeInt = Integer.reverseBytes(in.readInt());
        } catch (IOException e) {
             if (status == PipeStatus.DISCONNECTED || status == PipeStatus.CLOSED) {
                 throw new IOException("Pipe closed");
             }
             throw e;
        }

        int len = Integer.reverseBytes(in.readInt());
        byte[] d = new byte[len];
        
        in.readFully(d);

        Packet.OpCode op = Packet.OpCode.values()[opCodeInt];
        
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Packet.class, new PacketDeserializer(op))
                .create();
        JsonObject jsonObject = gson.fromJson(new String(d), JsonObject.class);
        Packet p = gson.fromJson(jsonObject, Packet.class);

        LOGGER.debug(String.format("Received packet: %s", p.toString()));
        
        if(listener != null) {
            listener.onPacketReceived(ipcClient, p);
        }
        
        return p;
    }

    @Override
    public void close() throws IOException {
        LOGGER.debug("Closing IPC pipe...");
        try {
            send(Packet.OpCode.CLOSE, new JsonObject(), null);
        } catch (Exception ignored) {}
        
        status = PipeStatus.CLOSED;
        
        try {
            in.close();
        } catch (IOException ignored) {}
        try {
            out.close();
        } catch (IOException ignored) {}
        
        process.destroy();
    }
}
