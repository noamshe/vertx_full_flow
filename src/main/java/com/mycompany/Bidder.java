package com.mycompany;


//import org.apache.log4j.Logger;
//import org.productivity.java.syslog4j.Syslog;
//import org.productivity.java.syslog4j.SyslogConstants;
//import org.productivity.java.syslog4j.SyslogIF;


import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;


import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.Logger;
import org.productivity.java.syslog4j.Syslog;
import org.productivity.java.syslog4j.SyslogConfigIF;
import org.productivity.java.syslog4j.SyslogIF;
import org.productivity.java.syslog4j.impl.net.tcp.TCPNetSyslogConfig;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;

import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.http.*;

import net.minidev.json.*;
import org.vertx.java.core.net.NetClient;
import org.vertx.java.core.net.NetSocket;
import org.vertx.java.core.streams.Pump;
import org.vertx.java.platform.Verticle;

import org.vertx.java.core.buffer.Buffer;


import java.util.Date;
import java.util.Random;
import java.util.UUID;



public class Bidder extends Verticle {

    //private static  Logger logger = org.slf4j.LoggerFactory.getLogger(Bidder.class);
    private static final String NO_BID="{\n" +
            "  \n" +
            "  \"string\": \"no bid\"\n" +
            "}";



    private static final int NUM_OF_LOGGER_CONNECTION=Conf.getConfig().getInt("num_of_logger_connection");
    private static final int SYSLOG_MESSAGE_LENGTH=Conf.getConfig().getInt("syslog_message_length");
    private static final int SYSLOG_QUEUE_SIZE=Conf.getConfig().getInt("syslog_queue_size");
    private static final int SYSLOG_LOOP_INTERVAL=Conf.getConfig().getInt("syslog_loop_interval");

    private static final int LISTEN_PORT=Conf.getConfig().getInt("listen_port");
    private static final int FLUME_PORT=Conf.getConfig().getInt("flume_port");
    private static final String FLUME_HOST=Conf.getConfig().getString("flume_host");


    private static SyslogIF[] syslogs= new SyslogIF[NUM_OF_LOGGER_CONNECTION];

    static {


            for(int i=0;i<NUM_OF_LOGGER_CONNECTION; i++) {
                TCPNetSyslogConfig conf =new TCPNetSyslogConfig();
                conf.setMaxQueueSize(SYSLOG_QUEUE_SIZE);
                conf.setMaxMessageLength(SYSLOG_MESSAGE_LENGTH);
                conf.setThreadLoopInterval(SYSLOG_LOOP_INTERVAL);
                conf.setHost(FLUME_HOST);
                conf.setPort(FLUME_PORT);
                syslogs[i]=Syslog.createInstance("tcp"+i,conf);

            }

        }



    public void start() {

        final Random rand =new Random();

        HttpServer listen = vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
            private HttpServerRequest req;


            public void handle(final HttpServerRequest req) {
                //    parse(req);

                HttpServerRequest id = req.bodyHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer buffer) {

                        StringBuilder sb = new StringBuilder();
                        StringBuilder uuid = new StringBuilder("{\"uuid\":\"").append( UUID.randomUUID().toString()).append("\"");
                        StringBuilder bidRequest =  new StringBuilder("\"bid_request\":");
                        StringBuilder date = new StringBuilder("\"date\":\"").append (new Date().toString()).append( "\"}");
                        sb.append(uuid).append(",").append(bidRequest).append(buffer.toString()).append(",").append(date);

                        syslogs[rand.nextInt(NUM_OF_LOGGER_CONNECTION)].debug(sb.toString().replace("\n", ""));

                    }


                });


                req.response().setStatusCode(202);
                req.response().end(NO_BID);

            }
        }).listen(LISTEN_PORT);



    }


/*
    private void parse(final HttpServerRequest req)
    {
        HttpServerRequest id = req.bodyHandler(new Handler<Buffer>() {
            @Override
            public void handle(Buffer buffer) {
                String contentType = req.headers().get("Content-Type");
                if ("application/xml".equals(contentType)) {
                    try {

                        VTDGen vg = new VTDGen();
                        vg.setDoc(buffer.getBytes());
                        vg.parse(false);
                        VTDNav vn = vg.getNav();
                        if (vn.matchElement("id")) {


                        }

                    } catch (Exception e) {
                        System.out.println("exception occurred ==>" + e);
                    }
                }
                if ("application/json".equals(contentType)) {
                    try {

                        if (!socket.writeQueueFull()) {
                            StringBuilder sb = new StringBuilder();
                            String uuid = "{\"uuid\":\"" + UUID.randomUUID().toString() + "\"";
                            String bidrequest = "\"bid_request\":";
                            String date = "\"date\":\"" + new Date().toString() + "\"}";
                            sb.append(uuid).append(",").append(bidrequest).append(buffer.toString()).append(",").append(date);


                            socket.write(sb.toString().replace("\n", "") + "\n");


                        } else {
                            socket.pause();
                            socket.drainHandler(new VoidHandler() {
                                public void handle() {
                                    socket.resume();
                                }
                            });

                        }


                        /*

                        String s = buffer.toString();
                        Object obj = JSONValue.parse(s);
                        JSONObject jobj=(JSONObject)obj;
                        jobj=(JSONObject)jobj.get("site");
                        jobj=(JSONObject)jobj.get("publisher");




                        syslog.debug(String.valueOf(jobj.get("id")));


                    } catch (Exception e) {
                        System.out.println("exception occurred ==>" + e);
                    }


                }

            }

        });

    }
  */



}

