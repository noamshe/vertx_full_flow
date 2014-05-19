package com.mycompany;


//import org.apache.log4j.Logger;
//import org.productivity.java.syslog4j.Syslog;
//import org.productivity.java.syslog4j.SyslogConstants;
//import org.productivity.java.syslog4j.SyslogIF;


import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;

import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.http.*;
import com.nesscomputing.syslog4j.*;
import net.minidev.json.*;
import org.vertx.java.core.net.NetClient;
import org.vertx.java.core.net.NetSocket;
import org.vertx.java.platform.Verticle;

import org.vertx.java.core.buffer.Buffer;


import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;


public class Bidder extends Verticle {


    private static final String NO_BID="{\n" +
            "  \n" +
            "  \"string\": \"no bid\"\n" +
            "}";
   // private static Logger syslog = org.apache.log4j.Logger.getRootLogger();
/*
    private static final SyslogIF syslog = Syslog.getInstance("tcp");
        static {



        syslog.getConfig().setHost("10.0.2.178");
        syslog.getConfig().setPort(51515);
        syslog.getConfig().setSendLocalName(false);
        syslog.getConfig().setSendLocalTimestamp(false);
        syslog.getConfig().setMaxMessageLength(10000);
        syslog.getConfig().setIncludeIdentInMessageModifier(false);
        syslog.getConfig().setUseStructuredData(false);


    }
*/
   private static NetSocket socket =null;


    public void start() {

    int inComingPort= container.config().getInteger("port");

        HttpServer listen = vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
            private HttpServerRequest req;


            public void handle(HttpServerRequest req) {
                parse(req);
                //req.response().putHeader("Content-Length","2048");
                req.response().setStatusCode(202);
                req.response().end(NO_BID);
            }
        }).listen(inComingPort);
        int flumePort= container.config().getInteger("flume_port");
        String flumeHost=container.config().getString("flume_host");
        NetClient client =vertx.createNetClient();
        client.setReconnectAttempts(1000);

        client.setReconnectInterval(500);
        client.setSendBufferSize(4*1024);
        client.connect(flumePort, flumeHost, new AsyncResultHandler<NetSocket>() {
            public void handle(AsyncResult<NetSocket> asyncResult) {
                if (asyncResult.succeeded()) {
                    container.logger().info("We have connected! Socket is " + asyncResult.result());
                    socket = asyncResult.result();
                } else {
                    asyncResult.cause().printStackTrace();
                }

                //socket.write("maybe");


            }
        });


    }



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
                            StringBuilder sb =new StringBuilder();
                            String uuid = "{\"uuid\":\""+ UUID.randomUUID().toString()+"\"";
                            String bidrequest= "\"bid_request\":";
                            String date = "\"date\":\""+ new Date().toString()+"\"}";
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
                        */

                    } catch (Exception e) {
                        System.out.println("exception occurred ==>" + e);
                    }



                }

            }

        });

    }




}

