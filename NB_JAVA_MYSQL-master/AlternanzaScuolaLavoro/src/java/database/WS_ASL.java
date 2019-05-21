/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.sql.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 *
 * @author Raoul Palummo
 */
@WebServlet(name = "WS_ASL", urlPatterns = {"/WS_ASL"})
public class WS_ASL extends HttpServlet {
    final private String driver = "com.mysql.jdbc.Driver";
    final private String dbms_url = "jdbc:mysql://localhost/";
    final private String database = "dbalternanza";
    final private String user = "root";
    final private String password = "";
    private Connection ASL;
    private boolean connected;

    // attivazione servlet (connessione a DBMS)
    public void init() {
        String url = dbms_url + database;
        try {
            Class.forName(driver);
            ASL = DriverManager.getConnection(url, user, password);
            connected = true;
        } catch (SQLException e) {
            connected = false;
        } catch (ClassNotFoundException e) {
            connected = false;
        }
    }

    // disattivazione servlet (disconnessione da DBMS)
    public void destroy() {
        try {
            ASL.close();
        } catch (SQLException e) {
       }
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet WS_ASL</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet WS_ASL at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name;
        String surname;
        String classe;
        String datebirth;
        String placebirth;
//        String number;
//        String description = "";
        String url;
        String[] url_section;
        // verifica stato connessione a DBMS
        if (!connected) {
            response.sendError(500, "DBMS server error!");
            return;
        }
        // estrazione nominativo da URL
        url = request.getRequestURL().toString();
//        if (name == null) {
//            response.sendError(400, "Request syntax error!");
//            return;
//        }
//        if (name.isEmpty()) {
//            response.sendError(400, "Request syntax error!");
//            return;
//        }
        try {
//          String descrizione = request.getParameter("descr");
            String sql = "SELECT Nome, Cognome, Classe, DataNascita, LuogoNascita";
//            if (descrizione != null && descrizione.equals("si"))
//                sql += ", description";
            sql += " FROM alunni"; // WHERE Name = '" + name + "';";
            // ricerca nominativo nel database
            Statement statement = ASL.createStatement();
            ResultSet result = statement.executeQuery(sql);
            if (result.next()) {
                name = result.getString(1);
                surname = result.getString(2);
                classe = result.getString(3);
                datebirth = result.getString(4);
                placebirth = result.getString(5);
//                number = result.getString(2);
//                if (descrizione != null && descrizione.equals("si")) {
//                    description = result.getString(3);
//                }
                    
            } else {
                response.sendError(404, "Entry not found!");
                result.close();
                statement.close();
                return;
            }
            result.close();
            statement.close();
            // scrittura del body della risposta
            response.setContentType("text/xml;charset=UTF-8");
            PrintWriter out = response.getWriter();
            try {
                out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                out.println("<Alunno>");
                out.println("<Nome>");
                out.println(name);
                out.println("</Nome>");
                out.println("<Cognome>");
                out.println(surname);
                out.println("</Cognome>");
                out.println("<Classe>");
                out.println(classe);
                out.println("</Classe>");
                out.println("<DataNascita>");
                out.println(datebirth);
                out.println("</DataNascita>");
                out.println("<LuogoNascita>");
                out.println(placebirth);
                out.println("</LuogoNascita>");
//                out.print("<name>");
//                out.print(name);
//                out.println("</name>");
//                out.print("<number>");
//                out.print(number);
//                out.println("</number>");
//                
//                if (descrizione != null && descrizione.equals("si")) {
//                    out.print("<description>");
//                    out.print(description);
//                    out.println("</description>");
//                    
//                }
                out.println("</Alunno>");
            } finally {
                out.close();
            }
            response.setStatus(200); // OK
        } catch (SQLException e) {
            response.sendError(500, "DBMS server error!");
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // processRequest(request, response);
        String line;

        // verifica stato connessione a DBMS
        if (!connected) {
            response.sendError(500, "DBMS server error!");
            return;
        }
        try {
            // scrittura nel file "entry.xml" del body della richiesta
            BufferedReader input = request.getReader();
            BufferedWriter file = new BufferedWriter(new FileWriter("alunni.xml"));
            while ((line = input.readLine()) != null) {
                file.write(line);
                file.newLine();
            }
            input.close();
            file.flush();
            file.close();
            // estrazione dei valori degli elementi "name" e "number" dal file "entry.xml"
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse("alunni.xml");
            Element root = document.getDocumentElement();
            NodeList list = root.getElementsByTagName("Nome");
            String name = null;
            if (list != null && list.getLength() > 0) {
                name = list.item(0).getFirstChild().getNodeValue();
            }
            list = root.getElementsByTagName("Cognome");
            String surname = null;
            if (list != null && list.getLength() > 0) {
                surname = list.item(0).getFirstChild().getNodeValue();
            }
            list = root.getElementsByTagName("Classe");
            String classe = null;
            if (list != null && list.getLength() > 0) {
                classe = list.item(0).getFirstChild().getNodeValue();
            }
            list = root.getElementsByTagName("DataNascita");
            String datebirth = null;
            if (list != null && list.getLength() > 0) {
                datebirth = list.item(0).getFirstChild().getNodeValue();
            }
            list = root.getElementsByTagName("LuogoNascita");
            String placebirth = null;
            if (list != null && list.getLength() > 0) {
                placebirth = list.item(0).getFirstChild().getNodeValue();
            }
            if (name == null || surname == null) {
                response.sendError(400, "Malformed XML!");
                return;
            }
            if (name.isEmpty() || surname.isEmpty()) {
                response.sendError(400, "Malformed XML!");
                return;
            }
            try {
                // aggiunta voce nel database
                Statement statement = ASL.createStatement();
                if (statement.executeUpdate("INSERT alunni(Nome, Cognome, Classe, DataNascita, LuogoNascita) VALUES('" + name + "', '" + surname + "' '" + classe + "', '" + datebirth + "' '" + placebirth + "');") <= 0) {
                    response.sendError(403, "Name exist!");
                    statement.close();
                    return;
                }
                statement.close();
            } catch (SQLException e) {
                response.sendError(500, "DBMS server error!");
                return;
            }
            response.setStatus(201); // OK
        } catch (ParserConfigurationException e) {
            response.sendError(500, "XML parser error!");
        } catch (SAXException e) {
            response.sendError(500, "XML parser error!");
        }
    }
    
     protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url_name;
        String url;
        String line;
        String[] url_section;

        // verifica stato connessione a DBMS
        if (!connected) {
            response.sendError(500, "DBMS server error!");
            return;
        }
        // estrazione nominativo da URL
        url = request.getRequestURL().toString();
        url_section = url.split("/");
        url_name = url_section[url_section.length - 1];
        if (url_name == null) {
            response.sendError(400, "Request syntax error!");
            return;
        }
        if (url_name.isEmpty()) {
            response.sendError(400, "Request syntax error!");
            return;
        }
        try {
            // scrittura nel file "entry.xml" del body della richiesta
            BufferedReader input = request.getReader();
            BufferedWriter file = new BufferedWriter(new FileWriter("alunni.xml"));
            while ((line = input.readLine()) != null) {
                file.write(line);
                file.newLine();
            }
            input.close();
            file.flush();
            file.close();
            // estrazione dei valori degli elementi "name" e "number" dal file "entry.xml"
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse("alunni.xml");
            Element root = document.getDocumentElement();
            NodeList list = root.getElementsByTagName("Nome");
            String name = null;
            if (list != null && list.getLength() > 0) {
                name = list.item(0).getFirstChild().getNodeValue();
            }
            list = root.getElementsByTagName("Classe");
            String classe = null;
            if (list != null && list.getLength() > 0) {
                classe = list.item(0).getFirstChild().getNodeValue();
            }
            if (name == null || classe == null) {
                response.sendError(400, "Malformed XML!");
                return;
            }
            if (name.isEmpty() || classe.isEmpty()) {
                response.sendError(400, "Malformed XML!");
                return;
            }
            if (!name.equalsIgnoreCase(url_name)) {
                response.sendError(400, "URL name mismtach XML name!");
                return;
            }
            try {
                Statement statement = ASL.createStatement();
                if (statement.executeUpdate("UPDATE alunni SET Classe='" + classe + "'WHERE Name = '" + name + "';") <= 0) {
                    response.sendError(404, "Entry not found!");
                    statement.close();
                    return;
                }
                statement.close();
            } catch (SQLException e) {
                response.sendError(500, "DBMS server error!");
                return;
            }
            response.setStatus(204); // OK
        } catch (ParserConfigurationException e) {
            response.sendError(500, "XML parser error!");
        } catch (SAXException e) {
            response.sendError(500, "XML parser error!");
        }
    }
     
     protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name;
        String url;
        String[] url_section;

        // verifica stato connessione a DBMS
        if (!connected) {
            response.sendError(500, "DBMS server error!");
            return;
        }
        // estrazione nominativo da URL
        url = request.getRequestURL().toString();
        url_section = url.split("/");
        name = url_section[url_section.length - 1];
        if (name == null) {
            response.sendError(400, "Request syntax error!");
            return;
        }
        if (name.isEmpty()) {
            response.sendError(400, "Request syntax error!");
            return;
        }
        try {
            Statement statement = ASL.createStatement();
            if (statement.executeUpdate("DELETE FROM alunni WHERE Name = '" + name + "';") <= 0) {
                response.sendError(404, "Entry not found!");
                statement.close();
                return;
            }
            statement.close();
            response.setStatus(204); // OK
        } catch (SQLException e) {
            response.sendError(500, "DBMS server error!");
            return;
        }
    }


    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}