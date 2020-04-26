<!DOCTYPE html>
<%@ page import = "rest.snakeApp.*" %>
<%@ page import = "rest.privilege" %>
<%@ page import = "rest.userDescriptor" %>
<%@ page import = "java.util.ArrayList" %>
<%@ page import = "java.text.SimpleDateFormat" %>
<%
  userDescriptor u =(userDescriptor)request.getAttribute("u_info");  
  ArrayList<userDescriptor> users = (ArrayList<userDescriptor>)request.getAttribute("users");
  SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
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

</style>
</head>
<body>

<div class="navbar">
  <a href="logout">Logout</a>
  <% if (u.getprivilege() == privilege.ADMIN) { %>
    <a href="users">Edit users</a>
  <% }%>

</div>
  <div id ="user_content"> 
    <%
      out.println("<h3>Hi <div name=\"user_info\">" + u.getname() + " " + u.getsurname() + "</dir> </h3>");
    %>
    <h3>Current temperature inside laboratory:<div name="temp_in">${temp_in}</dir></h3>
    <h3>Current humidity inside laboratory:<div name="humidity_out">${humidity_out}</dir> </h3>
    <h3>Logged users: </h3>
      <table>
        <tr>
          <td>Id</td> 
          <td>Login</td> 
          <td>Privilege</td> 
          <td>Pin</td> 
          <td>Name</td> 
          <td>Surname</td> 
          <td>Nick</td> 
          <td>Account expiration date</td> 
          <td>RFID</td> 
          <td>Edit</td> 
          <td>Remove</td> 
        </tr>
      <%
        for(int i =0; i < users.size(); i++)
        {
        out.println("<tr>");
          out.println("<td>" + users.get(i).getid() + "</td>");
          out.println("<td>" + users.get(i).getuserlogin() + "</td>");
          out.println("<td>" + users.get(i).getprivilege() + "</td>");
          out.println("<td>" + users.get(i).getpin() + "</td>");
          out.println("<td>" + users.get(i).getname() + "</td>");
          out.println("<td>" + users.get(i).getsurname() + "</td>");
          out.println("<td>" + users.get(i).getnick()+ "</td>");
          out.println("<td>" + users.get(i).getaccountexpire()+ "</td>");
          out.println("<td>" + users.get(i).getrfid()+ "</td>");
          out.println("<td><a href=\"remove/" +users.get(i).getid()+"\">remove</a></td>");
          out.println("<td><a href=\"edit/" +users.get(i).getid()+"\">edit</a></td>");
        out.println("</tr>");
        }
      %>
      </table>
  </div>
  
</body>
</html>
