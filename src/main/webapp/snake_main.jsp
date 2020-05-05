<!DOCTYPE html>
<%@ page import = "rest.snakeApp.*" %>
<%@ page import = "rest.privilege" %>
<%@ page import = "rest.userDescriptor" %>
<%@ page import = "rest.systemCore.cleanersDescriptor" %>
<%@ page import = "rest.sensor" %>
<%@ page import = "java.util.ArrayList" %>
<%@ page import = "java.text.SimpleDateFormat" %>
<%
  userDescriptor u =(userDescriptor)request.getAttribute("u_info");  
  ArrayList<userDescriptor> users = (ArrayList<userDescriptor>)request.getAttribute("active_users");
  ArrayList<sensor> sensors= (ArrayList<sensor>)request.getAttribute("active_sensors");
  cleanersDescriptor cleaners= (cleanersDescriptor)request.getAttribute("cleaning_info");

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

    #lab:link, #lab:visited {
      background-color: #f44336;
      color: white;
      padding: 14px 25px;
      text-align: center;
      text-decoration: none;
      display: inline-block;
    }

    #lab:hover, #lab:active {
      background-color: red;
    }

</style>
</head>
<body onload="setup_page_refresh()">

<div class="navbar">
  <a href="/rest/app/logout">Logout</a>
  <% if (u.getprivilege() == privilege.ADMIN) { %>
    <a href="/rest/users">Edit users</a>
    <a href="/rest/mac">Edit macs</a>
  <% }%>
</div>
<div id ="wrapper">
  <div id ="user_content"> 
    <%
      out.println("<h3>Hi <div name=\"user_info\">" + u.getname() + " " + u.getsurname() + "</dir> </h3>");
    %>
    
    <h3>Users responsible for cleaning this week:</h3>
    <table>
    <tr>
    <td>Login</td> 
    <td>Name</td> 
    <td>Surname</td> 
    </tr>
    <%
      for(int i =0; i < cleaners.staff.length; i++)
      {
        out.println("<tr>");
          out.println("<td>" + cleaners.staff[i].getuserlogin()+ "</td>");
          out.println("<td>" + cleaners.staff[i].getname() + "</td>");
          out.println("<td>" + cleaners.staff[i].getsurname() + "</td>");
        out.println("</tr>");
      }
      out.println("<h5> Next cleaners draw will be avaliable after" + formatter.format(cleaners.nextDrawDate) + "</h5>");
    %>
    </table>
    </br>
    <h3>Logged users: </h3>
      <table>
      <tr>
      <td>Name</td> 
      <td>Surname</td> 
      <td>Nick</td> 
      <td>Active since</td> 
      </tr>
      <%
        for(int i =0; i < users.size(); i++)
        {
          out.println("<tr>");
            out.println("<td>" + users.get(i).getname() + "</td>");
            out.println("<td>" + users.get(i).getsurname() + "</td>");
            out.println("<td>" + users.get(i).getnick()+ "</td>");
            out.println("<td>" + formatter.format(users.get(i).getcreated()) + "</td>");
          out.println("</tr>");
        }
      %>
      </table>
    <h3>Sensors: </h3>
      <table id ="sensors">
      <tr>
      <td>Name</td> 
      <td>Type</td> 
      <td>Value</td> 
      </tr>
      <%
        for(int i =0; i < sensors.size(); i++)
        {
          out.println("<tr>");
            out.println("<td>" + sensors.get(i).sensor_name + "</td>");
            switch(sensors.get(i).type)
            {
              case 0:
                out.println("<td> TEMPERATURE</td>");
                break;
              case 1:
                out.println("<td> HUMIDITY </td>");
                break;
              case 2:
                out.println("<td>OPEN WINDOW SENSOR</td>");
                break;
            };
            out.println("<td>" + sensors.get(i).value+ "</td>");
          out.println("</tr>");
        }
      %>
      </table>
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

      </br>
      <a id="lab" href="/rest/app/unlock"> Open lab</a>
      <a id="lab" href="/rest/app/lock"> Close lab</a>
      <a id="lab" href="/rest/app/logs/download"> Download logs</a>

    </center>
  </div>
</div>
<script>

function createTable(sensors)
{
  var table = document.createElement("TABLE");
  table.setAttribute("id", "sensors");

  var t_val = ["Name", "Type", "Value"];
  createRow(table, t_val);

  for(var i = 0; i < sensors.length; i++){
      var sensor= sensors[i];
      t_val = [];
      for (var key in sensor){
          t_val[t_val.length] = sensor[key];
      }
    createRow(table, t_val);
  }
  var table_old = document.getElementById("sensors");
  table_old.parentNode.replaceChild(table, table_old);
}

function createRow(table, t_params) {
  var i;
  var row = document.createElement("TR");
  for (i = 0; i <t_params.length; i++) {
    var cell= document.createElement("TD");
    var c_text = document.createTextNode(t_params[i]);
    if(i == 1)
    {
      var sensor_type = t_params[i];
      if(sensor_type == "0")
        c_text.nodeValue = "TEMPERATURE";
      else if(sensor_type == "1")
        c_text.nodeValue = "HUMIDITY";
      else if(sensor_type == "2")
        c_text.nodeValue = "OPEN WINDOW SENSOR";
    }
    cell.appendChild(c_text);
    row.appendChild(cell);
  }
  table.appendChild(row);
}


  function loadSensors() {
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
      if (this.readyState == 4 && this.status == 200) {
        measurements = this.responseText;
        const arr= JSON.parse(measurements);
        createTable(arr);
      }
    };
    xhttp.open("GET", "/rest/sensors/measurements", true);
    xhttp.send();
  }

  function setup_page_refresh() {
    setInterval(function(){ loadSensors(); }, 3000);
  }

</script>
</body>
</html>
