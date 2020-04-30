<!DOCTYPE html>
<%@ page import = "rest.snakeApp.*" %>
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
    #wrapper{
        overflow:auto;
    }

    #form_user_pass{
      border-radius: 5px;
      padding: 20px;
      float:right;
    }

    #user_content{
      float:left;
    }

</style>
</head>
<body>

<div class="navbar">
  <a href="app/logout">Logout</a>
  <a href="app/edit">Edit users</a>
</div>
<div id ="wrapper">
  <div id ="user_content"> 
    <h3>Current temperature inside laboratory:<div name="temp_in">${temp_in}</dir> </h3>
    <h3>Current humidity inside laboratory:<div name="humidity_out">${humidity_out}</dir> </h3>
    <h3>Logged users: </h3>
  </div>
  
  <div id="form_user_pass">
    <center>
      <%
        if ((int)request.getAttribute("response_msg") != PassStatus.PASS_IDLE) 
        {
            if ((int)request.getAttribute("response_msg") == PassStatus.PASS_CHANGE_OK) {
                out.println("<h6 style=\"color:green\">Your password has been changed </h6>");
            }else{
                out.println("<h6 style=\"color:red\">Your password hasn't been changed </h6>");
            }
          }
      %>
    <form action="app" method="Post">
      <label for="old_pass">Old password:</label><br>
      <input type="password" id="old_pass" name="old_pass" ><br>
      <label for="new_pass">New password:</label><br>
      <input type="password" id="new_pass" name="new_pass" ><br>
      <label for="new_pass_repeated">Repeat new password:</label><br>
      <input type="password" id="new_pass_repeated" name="new_pass_repeated" ><br>
      <input type="submit" value="Submit">
    </form>
    </center>
  </div>
</div>
</body>
</html>
