package com.company;
import java.sql.*;
import java.io.*;
import java.util.*;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
public class Main {

    private static void FileWrite(ResultSet resS){

        try {
            FileWriter fwr = new FileWriter("File.csv");
//fwr.append(cq);
//System.setOut(new java.io.PrintStream(System.out, true, "Cp866"));

            ResultSetMetaData rsmd = resS.getMetaData();
            int colnumbers = rsmd.getColumnCount();
            while (resS.next()) {
                //for (int i = 1; i < colnumbers; i++)
                //{
                Integer id = resS.getInt(1);
                String sName = resS.getString(2);
                String agName = resS.getString(3);
                fwr.write(id.toString() + ";" + sName + ";" + agName + "\r\n");
                //}
            }
            fwr.close();
        }
        catch (Exception e){
        }
    }

    private static void FileWriterXML(Statement stmXML) throws Exception {
        try {
            String qrAgent = "select ag.id,ag.sname from agents ag";
            ResultSet rsXML = stmXML.executeQuery(qrAgent);


            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document document = documentBuilder.newDocument();
            Element rootElement = document.createElement("BaseClients");
            document.appendChild(rootElement);

            HashMap hashMap = new HashMap();

            while (rsXML.next()){
                hashMap.put(rsXML.getInt(1),rsXML.getString(2));
            }
            Set set = hashMap.entrySet();
            Iterator i = set.iterator();

            while (i.hasNext()){
                Map.Entry me = (Map.Entry)i.next();

                Element agent = document.createElement("Agent");
                rootElement.appendChild(agent);

                Attr attr = document.createAttribute("Name");

                attr.setValue(me.getValue().toString());
                agent.setAttributeNode(attr);

                String qrClient = "select cl.id,cl.sname from  client cl where cl.agentid =" + me.getKey().toString() ;
                rsXML = stmXML.executeQuery(qrClient);

                    while (rsXML.next()){


                        Element client = document.createElement("Client");
                        Attr attr1 = document.createAttribute("ID");
                        attr1.setValue(rsXML.getString(1));
                        client.setAttributeNode(attr1);
                        client.appendChild(document.createTextNode(rsXML.getString(2)));
                        agent.appendChild(client);
                    }


            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File("clients.xml"));

            transformer.transform(domSource, streamResult);
            System.out.println("Файл сохранен!");

        }catch (ParserConfigurationException pce)
        {
            System.out.println(pce.getLocalizedMessage());
            pce.printStackTrace();
        }catch (Exception e ) {
        } finally {
            if (stmXML != null) { stmXML.close(); }
        }

    }



    public static void main(String[] args) throws Exception {

        try {
            Class.forName("org.firebirdsql.jdbc.FBDriver");
        } catch (Exception e) {
            System.err.println("Unable to load driver: " + e);
        }
        Properties props = new Properties();
        props.setProperty("user", "SYSDBA");
        props.setProperty("password", "masterkey");
        props.setProperty("encoding", "WIN1251");

        Connection con = DriverManager.getConnection("jdbc:firebirdsql:127.0.0.1/3050:base",props);
        Statement stmt = null;
        String query = "select cl.id,cl.sname,ag.sname from  client cl left join agents ag on cl.agentid = ag.id";

        try {
            stmt = con.createStatement();
            FileWriterXML(stmt);
            /*ResultSet rs = stmt.executeQuery(query);
            FileWrite(rs); //запись результата в CSV файл
            */
        }
        catch (Exception e ) {
        } finally {
            if (stmt != null) { stmt.close(); }
        }

    }
}
