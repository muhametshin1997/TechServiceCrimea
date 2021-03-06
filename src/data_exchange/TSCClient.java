package data_exchange;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class TSCClient implements Runnable
{
    private Socket client;
    private Thread manager;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String hash;
    private OnRequestAcceptedListener onRequestAcceptedListener;

    public TSCClient(Socket client)
    {
        this.client = client;
    }

    private static String readToEndBlock(BufferedReader reader) throws IOException
    {
        String result = "";
        String tmp;
        while (!Thread.interrupted() && !reader.ready())
            try
            {
                Thread.sleep(10);
            } catch (Exception e)
            {

            }
        while (!Thread.interrupted() && (tmp = reader.readLine()) != null && !tmp.equals("---END---"))
            result += tmp;
        return result.replace("\\n", "\n");
    }

    private static void writeWithEndBlock(BufferedWriter writer, String text) throws IOException
    {
        writer.write(String.format("%s\n---END---\n", text.replace("\n", "\\n")));
        writer.flush();
    }

    public boolean isConnected()
    {
        return client.isConnected();
    }

    public void start()
    {
        if (manager == null || !manager.isAlive())
        {
            manager = new Thread(this);
            manager.start();
        }
    }

    @Override
    public void run()
    {
        try
        {
            client.setSoTimeout(100);
            client.setSoLinger(false, 0);
            client.setTcpNoDelay(true);
            reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            do
                try
                {
                    String data = readToEndBlock(reader);
                    if (data.startsWith("hash="))
                    {
                        hash = data.split("=")[1];
                        writeWithEndBlock(writer, String.format("OK, hash=%s", hash));
                    } else if (onRequestAcceptedListener != null)
                        writeWithEndBlock(writer, onRequestAcceptedListener.onRequestAccepted(this, Request.parse(data.replace(Integer.toString((char) 0), ""))));
                    else
                        writeWithEndBlock(writer, "Sorry, I still not ready...");
                } catch (IOException e)
                {
                    System.err.println("TSCClient.{manager}.run(){data exchange loop}->\n" + e.toString());
                }
            while (!Thread.interrupted() && isConnected());
        } catch (IOException e)
        {
            System.err.println("TSCClient.{manager}.run()->\n" + e.toString());
        }
    }

    public void stop()
    {
        if (client != null)
            try
            {
                client.close();
                client = null;
            } catch (IOException e)
            {
                System.err.println("TSCClinet.stop()->\n" + e.toString());
            }
        if (manager != null)
        {
            manager.interrupt();
            manager = null;
        }
    }

    public void setOnRequestAcceptedListener(OnRequestAcceptedListener onRequestAcceptedListener)
    {
        this.onRequestAcceptedListener = onRequestAcceptedListener;
    }

    public interface OnRequestAcceptedListener
    {
        String onRequestAccepted(TSCClient sender, Request request);
    }
}