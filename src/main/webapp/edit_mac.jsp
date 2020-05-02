<!DOCTYPE html>
<%@ page import = "rest.snakeApp.*" %>
<%@ page import = "rest.privilege" %>
<%@ page import = "rest.macManagerServlet" %>
<%@ page import = "rest.macDescriptor" %>
<%@ page import = "java.util.ArrayList" %>
<%@ page import = "java.text.SimpleDateFormat" %>
<%@ page import = "javax.servlet.jsp.PageContext" %>
<%
  int op_status =(int)request.getAttribute("status");
  macDescriptor edited_mac =(macDescriptor)request.getAttribute("edited_mac");
  pageContext.setAttribute("edited_id",edited_mac.getid());
  pageContext.setAttribute("edited_login",edited_mac.getuserlogin());
  pageContext.setAttribute("edited_mac",edited_mac.getmac());
  ArrayList<macDescriptor>macs= (ArrayList<macDescriptor>)request.getAttribute("macs");
%>

<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
<style>
    body {
      font-family: Arial, Helvetica, sans-serif;
      background-color: #f2f2f2;
    }

    .navbar {
      overflow: hidden;
      background-color: #333;
    }

    .navbar a {
      float: left;
      font-size: 16px;
      color: white;
      text-align: center;
      padding: 14px 16px;
      text-decoration: none;
    }

    .dropdown {
      float: left;
      overflow: hidden;
    }

    .dropdown .dropbtn {
      font-size: 16px;  
      border: none;
      outline: none;
      color: white;
      padding: 14px 16px;
      background-color: inherit;
      font-family: inherit;
      margin: 0;
    }

    .navbar a:hover, .dropdown:hover .dropbtn {
      background-color: red;
    }

    .dropdown-content {
      display: none;
      position: absolute;
      background-color: #f9f9f9;
      min-width: 160px;
      box-shadow: 0px 8px 16px 0px rgba(0,0,0,0.2);
      z-index: 1;
    }

    .dropdown-content a {
      float: none;
      color: black;
      padding: 12px 16px;
      text-decoration: none;
      display: block;
      text-align: left;
    }

    .dropdown-content a:hover {
      background-color: #ddd;
    }

    .dropdown:hover .dropdown-content {
      display: block;
    }
    input[type=submit] {
      background-color: #4CAF50;
      color: white;
      padding: 12px 20px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }

    input[type=submit]:hover {
      background-color: #45a049;
    }

    table {
      font-family: arial, sans-serif;
      border-collapse: collapse;
      width: 100%;
    }

    td, th {
      border: 1px solid #dddddd;
      text-align: left;
      padding: 8px;
    }

    tr:nth-child(even) {
      background-color: #dddddd;
    }
    body {font-family: Arial, Helvetica, sans-serif;}
    * {box-sizing: border-box;}

    .form-inline {  
      display: flex;
      flex-flow: row wrap;
      align-items: center;
    }

    .form-inline label {
      margin: 5px 10px 5px 0;
    }

    .form-inline input {
      vertical-align: middle;
      margin: 5px 10px 5px 0;
      padding: 10px;
      background-color: #fff;
      border: 1px solid #ddd;
    }

    .form-inline button {
      padding: 10px 20px;
      background-color: dodgerblue;
      border: 1px solid #ddd;
      color: white;
      cursor: pointer;
    }

    .form-inline button:hover {
      background-color: royalblue;
    }

    @media (max-width: 800px) {
      .form-inline input {
        margin: 10px 0;
      }
      
      .form-inline {
        flex-direction: column;
        align-items: stretch;
      }
    }

</style>
</head>
<body>

<div class="navbar">
  <a href="logout">Logout</a>
  <a href="/rest/app">App</a>
</div>
  <div id ="user_content"> 
    <%
      if (op_status != macManagerServlet.EditStatus.IDLE) 
      {
          if ((int)op_status == macManagerServlet.EditStatus.OK) {
              out.println("<h6 style=\"color:green\">Action performed successfuly</h6>");
          }else{
              out.println("<h6 style=\"color:red\">Action has failed</h6>");
          }
      }
    %>
  <h3>Add/Edit user form</h3>
  <div id="user_form">
    <form class="form-inline" action="/rest/mac" method="post">
      
      <label for="userid">Id:</label>
      <input type="text" id="userid" placeholder="User id" name="userid" value="${edited_id}">
      
      <label for="login">Login:</label>
      <input type="text" id="login" placeholder="User login" name="login" value="${edited_login}">

      <label for="mac">Mac address:</label>
      <input type="text" id="mac" placeholder="User mac address" pattern="[0-9A-F]{12}" name="mac" value="${edited_mac}">

      <button type="submit">Submit</button>
    </form>
  </div>
    <div id="description">
      <h5>Adding rules</h5>
      <p>To add mac set id filed to <b>-1</b>, fill rest of fields as you wish.</p>
      <p>All fileds are compulsory</p>

      <h5>Updating rules</h5>
      <p>To update mac choose entry from below table and use edit button belonging to mac.</p>
      <p>All fileds are compulsory</p>
      <h5>General rules</h5>
      <p>Mac address has to be in format &lt;[0-9A-Fa-f]{12}&gt; </p>
    </div>

    <h3>Known user's macs:</h3>
      <table>
        <tr>
          <td>Id</td> 
          <td>Login</td> 
          <td>Mac address</td> 
          <td>Edit</td> 
          <td>Remove</td> 
        </tr>
      <%
        for(int i =0; i <macs.size(); i++)
        {
          out.println("<tr>");
            out.println("<td>" + macs.get(i).getid() + "</td>");
            out.println("<td>" + macs.get(i).getuserlogin() + "</td>");
            out.println("<td>" + macs.get(i).getmac()+ "</td>");
            out.println("<td><a href=\"/rest/mac/" +macs.get(i).getid()+"\">edit</a></td>");
            out.println("<td><a href=\"/rest/mac/remove/" +macs.get(i).getid()+"\">remove</a></td>");
          out.println("</tr>");
        }
      %>
      </table>
  </div>
  
</body>
</html>
