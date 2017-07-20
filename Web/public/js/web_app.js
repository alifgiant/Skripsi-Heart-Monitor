/**
 * Socket IO config
 */
var socket = io('http://localhost:3000');
var device_id = 'temp';

socket.on('connect', function(){
  console.log('socket connected');
});

socket.on('disconnect', function(){
  console.log('socket disconnect');
});

function addListener(topic, callback){  
  socket.on(topic, callback);
}

function closeListener(device_id, callback){
  socket.off(device_id + '/filtered');
  socket.off(device_id + '/bpm');
}

function setupPatientDetail(){
  /**
   * Heart Signal Chart
   * Socket IO config
   */

  var maxLength = 100;
  var data = new Array(maxLength).fill(0);  

  // set initial 0 to chart
  var res = [];
  for (var i = 0; i < data.length; ++i) {
    res.push([i, data[i]]);
  }

  var plot = $.plot($('#heart-chart'), [res], {
    grid: {
      borderWidth: 0
    },
    series: {
      shadowSize: 0, // Drawing is faster without shadows
      color: "#00c0ef"
    },      
    yaxis: {     
      min:-0.2, max: 0.2,
      show: false
    },
    xaxis: {        
      show: false
    }
  });  

  // Fetch data from server
  device_id = $('#patient-device-id').text().split(' ')[2];  // ["Device", "Id:", '001']  

  // listener for chart
  addListener(device_id+'/filtered', (payload) => {    
    if (data.length > 0) data = data.slice(1);
    data.push(payload);

    // Zip the generated y values with the x values
    var res = [];
    for (var i = 0; i < data.length; ++i) {
      res.push([i, data[i]]);
    }    

    plot.setData([res]);
    // plot.setupGrid(); //only necessary if your new data will change the axes or grid
    plot.draw();  
  });

  // listener for bpm
  var bpm_holder = $('#patient-bpm');
  addListener(device_id+'/bpm', (payload) => {    
    bpm_holder.text('BPM: ' + (Math.round(payload * 100) / 100));
  });
}

function setupPatientList(patient_id) {
  const main = $('#main-content');
  main.load('/dashboard/patient/monitoring?id='+patient_id, setupPatientDetail);

  // handle 1..2..3..4
}

function setupPatientAdd() {  
  const doctor_username =  $('#username');
  const patient_username =  $('#add-patient-username');
  $.post( "/api/doctor/" + doctor_username.text() + "/data/add", {username: patient_username.val()}, function( data ) {
    console.log(data);
    if(data){
      switch (data.status){
        case 'success':
        patient_username.val('');
        alert('patient add success');
        break;
        case 'failed':
        patient_username.val('');
        alert('patient not exist');
        break;
      }
    }else
    alert('patient not exist');
  });
}

function handleSideBarClick(menu, point) {
  // remove last selection
  $('.sidebar-menu').find('li').removeClass('active');

  // mark current selection  
  $(point).addClass('active');  

  const doctor_username =  $('#username');

  const main = $('#main-content');

  switch (menu){ // menu
    case 'menu-dashboard':
      main.load('/dashboard/content?user=' + doctor_username.text());
      closeListener(device_id);  // close any possible socket
      break;
    case 'menu-patient-list':
      main.load('/dashboard/patient/list?user=' + doctor_username.text());
      closeListener(device_id);  // close any possible socket
      break;
    case 'menu-patient-add':
      main.load('/dashboard/patient/add');
      closeListener(device_id);  // close any possible socket
      break;
    case 'menu-record':
      main.load('/dashboard/record?user=' + doctor_username.text());
      closeListener(device_id);  // close any possible socket
      break;
  }
}

function loadRecord() {
  // remove last selection
  $('.sidebar-menu').find('li').removeClass('active');

  const main = $('#main-content');
  closeListener(device_id);

  const doctor_username =  $('#username');
  main.load('/dashboard/record?user=' + doctor_username.text());
  $('#menu-record').addClass('active');
}

function setupSideBar() {  
  const main = $('#main-content');
  const doctor_username =  $('#username');

  main.load('/dashboard/content?user=' + doctor_username.text());
  $('#menu-dashboard').addClass('active');
}

function test(){
  console.log('test');
}

$(document).ready(function () {
  setupSideBar();

  // loadContent(pathname);

  // console.log(sidebar_menus.length);
  // console.log('path', pathname);
});