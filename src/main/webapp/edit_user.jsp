<!DOCTYPE html>
<%@ page import = "rest.snakeApp.*" %>
<%@ page import = "rest.privilege" %>
<%@ page import = "rest.userManagerServlet" %>
<%@ page import = "rest.userDescriptor" %>
<%@ page import = "java.util.ArrayList" %>
<%@ page import = "java.text.SimpleDateFormat" %>
<%@ page import = "javax.servlet.jsp.PageContext" %>
<%
  int op_status =(int)request.getAttribute("status");
  userDescriptor curr_u =(userDescriptor)request.getAttribute("current_user");
  userDescriptor edited_u =(userDescriptor)request.getAttribute("edited_user");
  String[] privOptions = {"", ""};
  privOptions[edited_u.getprivilege()] = "selected=\"selected\"";
  pageContext.setAttribute("edited_id",edited_u.getid());
  pageContext.setAttribute("edited_login",edited_u.getuserlogin());
  pageContext.setAttribute("priv_option_0",privOptions[0]);
  pageContext.setAttribute("priv_option_1",privOptions[1]);
  pageContext.setAttribute("edited_pin",edited_u.getpin());
  pageContext.setAttribute("edited_name",edited_u.getname());
  pageContext.setAttribute("edited_surname",edited_u.getsurname());
  pageContext.setAttribute("edited_nick",edited_u.getnick());
  pageContext.setAttribute("edited_expire",edited_u.getaccountexpire());
  pageContext.setAttribute("edited_rfid",edited_u.getrfid());
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
      out.println("<h3>Hi <div name=\"user_info\">" + curr_u.getname() + " " + curr_u.getsurname() + "</dir> </h3>");
      if (op_status != userManagerServlet.EditStatus.IDLE) 
      {
          if ((int)op_status == userManagerServlet.EditStatus.OK) {
              out.println("<h6 style=\"color:green\">Action performed successfuly</h6>");
          }else{
              out.println("<h6 style=\"color:red\">Action has failed</h6>");
          }
      }
    %>
  <h3>Add/Edit user form</h3>
  <form class="form-inline" action="/rest/users" method="post">
    
    <label for="userid">Id:</label>
    <input type="text" id="userid" placeholder="User id" name="userid" value="${edited_id}">
    
    <label for="login">Login:</label>
    <input type="text" id="login" placeholder="User login" name="login" value="${edited_login}">

    <label for="pwd">Password:</label>
    <input type="password" id="pwd" placeholder="User password" name="password">
    
    <label for="priv">Privilege:</label>
    <select id="priv" id="priv" placeholder="User privilege" name="privilege">
      <option value="0" ${priv_option_0} >User</option>
      <option value="1" ${priv_option_1} >Admin</option>
    </select>
    
    <label for="pin">Pin:</label>
    <input type="text" id="pin" placeholder="User pin" name="pin" value="${edited_pin}">
    
    <label for="name">Name:</label>
    <input type="text" id="name" placeholder="User name" name="name" value="${edited_name}">
    
    <label for="surname">Surname:</label>
    <input type="text" id="surname" placeholder="User surname" name="surname" value="${edited_surname}">
    
    <label for="nick">Nick:</label>
    <input type="text" id="nick" placeholder="User nick" name="nick" value="${edited_nick}">
    
    <label for="expire">Account expire date:</label>
    <input type="text" id="expire" placeholder="Expire date" name="expire" value="${edited_expire}">
    
    <label for="rfid">Rfid:</label>
    <input type="text" id="rfid" placeholder="User rfid" name="rfid" value="${edited_rfid}">
    
    <button type="submit">Submit</button>
    <div id="description">
      <h5>Adding rules</h5>
      <p>To add user set id filed to <b>-1</b>, fill rest of fields as you wish.</p>
      <p>Compulsory fileds are: Login, Password, Privilege, Pin, Account expire date.</p>

      <h5>Updating rules</h5>
      <p>To update user choose user from below table and use edit button belonging to him/her.</p>

      <p>Compulsory fields are: Login, Privilege, Pin, account expire date. <b>Mind you</b> that you can't edit user's password.</p>
      <h5>General rules</h5>
      <p>Login and pin fileds have to be unique</p>
      <p>Pin's length has to be at least 4 signs</p>
      <p>Account expire data format is 	&lt;yyyy-mm-dd&gt;</p>
      <p>RFID is encoded as hexadecimal string, so allowed signs are: [0-9A-F]. Typing anything else causes to undefined behaviour</p>
      <p>For both situations use <b>Submit button</b> to finalize action.</p>
    </div>
  </form>
    <h3>Members of skn mos: </h3>
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
            if( users.get(i).getprivilege() == 0)
              out.println("<td>User account</td>");
            else
              out.println("<td>Admin account</td>");
            out.println("<td>" + users.get(i).getpin() + "</td>");
            out.println("<td>" + users.get(i).getname() + "</td>");
            out.println("<td>" + users.get(i).getsurname() + "</td>");
            out.println("<td>" + users.get(i).getnick()+ "</td>");
            out.println("<td>" + users.get(i).getaccountexpire()+ "</td>");
            out.println("<td>" + users.get(i).getrfid()+ "</td>");
            out.println("<td><a href=\"/rest/users/" +users.get(i).getid()+"\">edit</a></td>");
            out.println("<td><a href=\"/rest/users/remove/" +users.get(i).getid()+"\">remove</a></td>");
          out.println("</tr>");
        }
      %>
      </table>
  </div>
  
</body>
</html>
