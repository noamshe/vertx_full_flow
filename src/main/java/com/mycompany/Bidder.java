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
   // private static Logger syslog = org.apache.log4j.Logger.getRootLogger();
    private static SyslogConfigIF conf1 =new TCPNetSyslogConfig() ;
    private static SyslogConfigIF conf2 =new TCPNetSyslogConfig() ;
    private static SyslogConfigIF conf3 =new TCPNetSyslogConfig() ;


    private static SyslogIF[] syslogs =new SyslogIF[3];
        static {




            conf1.setSendLocalName(false);
            conf1.setFacility(1);
            conf1.setSendLocalTimestamp(false);
            conf1.setHost("10.67.144.188");
            conf1.setPort(51515);


            conf2.setSendLocalName(false);
            conf2.setFacility(1);
            conf2.setSendLocalTimestamp(false);
            conf2.setHost("10.67.144.188");
            conf2.setPort(51516);

            conf3.setSendLocalName(false);
            conf3.setFacility(1);
            conf3.setSendLocalTimestamp(false);
            conf3.setHost("10.67.144.188");
            conf3.setPort(51517);

            syslogs[0]=Syslog.createInstance( "tcp1",conf1);
            syslogs[1]=Syslog.createInstance( "tcp2",conf2);
            syslogs[2]=Syslog.createInstance( "tcp3",conf3);

    }



    public void start() {

    final int  inComingPort= container.config().getInteger("port");
     final int flumePort= container.config().getInteger("flume_port");
      final String flumeHost=container.config().getString("flume_host");
        final Random rand =new Random();








        HttpServer listen = vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
            private HttpServerRequest req;


            public void handle(final HttpServerRequest req) {
                //    parse(req);

                HttpServerRequest id = req.bodyHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer buffer) {

                        StringBuilder sb = new StringBuilder();
                        String uuid = "{\"uuid\":\"" + UUID.randomUUID().toString() + "\"";
                        String bidrequest = "\"bid_request\":";
                        String date = "\"date\":\"" + new Date().toString() + "\"}";
                        sb.append(uuid).append(",").append(bidrequest).append(buffer.toString()).append(",").append(date);
                        int logNum=rand.nextInt(3);

                        syslogs[logNum].debug(sb.toString().replace("\n", ""));

                    }


                });

                //req.response().putHeader("Content-Length","2048");
                req.response().setStatusCode(202);
                req.response().end(NO_BID);

            }
        }).listen(inComingPort);



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

