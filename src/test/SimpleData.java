package test;

import org.influxdb.dto.*;
import prime_tass.connect.client_api.ConnectionClientAPI;
import prime_tass.connect.client_api.interf.INetworkStatusInterface;
import p.pttable.client_api.data.PTTable_Key;
import p.pttable.client_api.data.PTTable_Data;
import p.pttable.client_api.PTTableClientAPI;
import p.pttable.client_api.data.PTTable;
import p.pttable.client_api.interf.IPTTableDataListener;
import prime_tass.connect.*;
import prime_tass.connect.client_api.ConnectionClientAPI.TYPE;

import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import lombok.extern.log4j.*;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.*;


public class SimpleData
{

    private static String dbname="Forex";
    public SimpleData()
    {
    }

    public static void main(String[] args)throws Exception
    {

//        public InfluxDB influxDB;

        INetworkStatusInterface g = new INetworkStatusInterface()
        {
            public void connectionEstablished(){}
            {
                System.out.println("Connection established.");
            }

            public void networkActivity()
            {
                System.out.println("Network activity.");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }

            public void wrongCredentials()
            {
                System.out.println("Server rejects supplied credentials.");
            }

            public void disconnectedFromServer()
            {
                System.out.println("Disconnected From Server");
            }

            public void serverAcceptedCredentials()
            {
                System.out.println("serverAcceptedCredentials");
            }

            public void serverRefusedConnection(String string)
            {
                System.out.println("serverRefusedConnection "+string);
            }

            public void connectionSuspended()
            {
                System.out.println("connectionSuspended");
            }

            public void connectionResumed()
            {
                System.out.println("connectionResumed");
            }
        };

       InfluxDB influxDB = InfluxDBFactory.connect("http://192.168.1.101:8083/", "inter2", "19876");
        Pong response = influxDB.ping();
        if (response.getVersion().equalsIgnoreCase("unknown")) {
            System.out.println("Error pinging server.");
            return;
        } else   { System.out.println(response.getVersion());}


        influxDB.setDatabase("MyDB");
//                    influxDB.close();

        /*influxDB.write(Point.measurement("cpu")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                //.tag("dc",23)
                .tag("free","90")
                .tag("hostname","localhost")
                .tag("total","12")
                .tag("used","323")
                                                //.addField("JobsInSystem", jobs)
                //.addField("CurrUnprotStorageUsed", unprotStore)
                //.addField("PercentSystemASPUsed", aspUsed)
                .build());

        System.out.psdsdrintln("Wrote to influxDB");*/

        Query query = new Query("SELECT free FROM cpu", "MyDB");
          QueryResult result = influxDB.query(query);
        System.out.println(result.getResults().toString());

        query = new Query("SELECT idle FROM MyDB", "MyDB");
        influxDB.query(query);

        influxDB.enableBatch(2000, 100, TimeUnit.MILLISECONDS);
        influxDB.write(Point.measurement("cpu")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("idle", 90L)
                .addField("user", 9L)
                .addField("system", 1L)
                .build());
        Thread.sleep(200);

        /*Query query = new Query("SELECT * FROM MyDB", "MyDB");
        QueryResult queryResult = influxDB.query(query);*/



       //  try {

 //       } catch (Exception e ) {

    //        System.out.println("Error connecting influxDB: "+e);



        //}
       /* try {
            influxDB.createDatabase("Forex");
        } catch (Exception e ) {

            System.out.println("Error creating DB Forex: "+e);}*/


    /*BatchPoints batchPoints = BatchPoints
                .database("MyDB")
                .retentionPolicy("defaultPolicy")
                .build();

        Point point1 = Point.measurement("memory")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("name", "server1")
                .addField("free", 4743656L)
                .addField("used", 1015096L)
                .addField("buffer", 1010467L)
                .build();

        Point point2 = Point.measurement("memory")
                .time(System.currentTimeMillis() - 100, TimeUnit.MILLISECONDS)
                .addField("name", "server1")
                .addField("free", 4743696L)
                .addField("used", 1016096L)
                .addField("buffer", 1008467L)
                .build();

        batchPoints.point(point1);
        batchPoints.point(point2);
        influxDB.write(batchPoints);
        influxDB.flush();*/


        ConnectionClientAPI cc = new ConnectionClientAPI(g);
        
        try {
        	/*
        	cc.assignNewConnectCredentials("guest", "guest",
                                "terminal.1prime.ru", TYPE.INFO, 6014,
                                60000, 60000, 100);
            */cc.assignNewConnectCredentials("a.korzhenyak", "123456",
                    "terminal.1prime.ru", TYPE.INFO, 6014,
                    60000, 60000, 100);

        	
        	/*
        	cc.assignNewConnectCredentials("fasttest", "*****1008",
                    "**********", TYPE.INFO, 6015,
                    60000, 60000, 100);
        	*/
        	
        } catch (BadParametersException ex)
        {
            System.out.println("Some parameters is unacceptable: "+ex);
        }


        IPTTableDataListener<PTTable_Key,PTTable_Data> dl = new IPTTableDataListener<PTTable_Key,PTTable_Data> ()
        {
             public void dataEvent(PTTable_Key key, PTTable_Data data)
             {
                 System.out.println("arrived row update: key="+key);
                 for(String skey:data.getKeys())                 {
                     System.out.println(skey+"="+new String(data.getCell(skey)));                     
                 }                
             }

             public String getId()
             {
                 return "listener";
             }

             public void flush()
             {
                 System.out.println("flush");
             }

        };


     
        
         PTTableClientAPI api = new PTTableClientAPI(dl);     
         PTTable table = new PTTable();
   
         table.addRow("EUR.FR");
         table.addRow("JPY.FR");
         table.addCol("Ask");
         table.addCol("Bid");
         table.addCol("Time");
       
         api.assignNewTable(table);
         cc.assignSink(api.getNetworkInterface());

    }   
}
