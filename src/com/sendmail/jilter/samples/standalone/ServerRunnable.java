/*
 * Copyright (c) 2001-2004 Sendmail, Inc. All Rights Reserved
 */

package com.sendmail.jilter.samples.standalone;

import com.sendmail.jilter.JilterHandler;
import com.sendmail.jilter.JilterProcessor;

import java.io.IOException;

import java.nio.ByteBuffer;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Category;

/**
 * Sample implementation of a handler for a socket based Milter protocol connection.
 */

class ServerRunnable implements Runnable
{
    private static Category log = Category.getInstance(ServerRunnable.class.getName());

    private SocketChannel socket = null;
    private JilterProcessor processor = null;

    /**
     * Constructor.
     * 
     * @param socket the incoming socket from the MTA.
     * @param handler the handler containing callbacks for the milter protocol.
     */
    public ServerRunnable(SocketChannel socket, JilterHandler handler)
        throws IOException
    {
        this.socket = socket;
        this.socket.configureBlocking(true);
        this.processor = new JilterProcessor(handler);
    }

    public void run()
    {
        ByteBuffer dataBuffer = ByteBuffer.allocateDirect(4096);

        try
        {
            while (this.processor.process(this.socket, (ByteBuffer) dataBuffer.flip()))
            {
                dataBuffer.compact();
                log.debug("Going to read");
                if (this.socket.read(dataBuffer) == -1)
                {
                    log.debug("socket reports EOF, exiting read loop");
                    break;
                }
                log.debug("Back from read");
            }
        }
        catch (IOException e)
        {
            log.debug("Unexpected exception, connection will be closed", e);
        }
        finally
        {
            log.debug("Closing processor");
            this.processor.close();
            log.debug("Processor closed");
            try
            {
                log.debug("Closing socket");
                this.socket.close();
                log.debug("Socket closed");
            }
            catch (IOException e)
            {
                log.debug("Unexpected exception", e);
            }
        }
    }
}
