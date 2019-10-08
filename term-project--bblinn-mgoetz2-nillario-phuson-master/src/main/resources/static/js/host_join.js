let loaded = false;
let maxPlayers = 8

let numAI = 0;
let myPlayerType;
let AI_width = 5

const PlayerType = {
	HOST: 0,
	JOIN: 1
}

let buttonDiv  = document.createElement("div");
function display_start_button(bool) {
	buttonDiv.classList.add("center")
	buttonDiv.classList.add("green")
	buttonDiv.style.fontSize = "30px"
	buttonDiv.style.top = "200px"
	canvasDiv.appendChild(buttonDiv)
	let start;

	start = document.getElementById("startButton");
	if (start === null && bool) {
		start = document.createElement("button")
		start.innerHTML = "START GAME!";
		start.id = "startButton"
		start.style.borderRadius = "15px"
		start.style.width = "250px"
		start.style.height = "100px"
		start.onclick = function() {
			load_game();
		}
	}
	if (start !== null) {
		if (!bool) {
			buttonDiv.removeChild(start)
		} else {
			buttonDiv.appendChild(start)
		}
	}
}

function display_game_id(newId,playType) {
	let hostDiv = document.createElement("div")
	hostDiv.classList.add("center")
	hostDiv.classList.add("green")
	hostDiv.style.fontSize = "30px"


	canvasDiv.appendChild(hostDiv)
	hostDiv.appendChild(document.createTextNode(`Your Game ID = ${newId}`))
	hostDiv.appendChild(document.createElement("br"))
		hostDiv.appendChild(document.createElement("br"))
	loaded = true;
}

function create_host_table(type,newId) {
	$(content).empty()
	let hostDiv = document.createElement("div");
	hostDiv.classList.add("center")
	hostDiv.classList.add("green")
	hostDiv.classList.add("host_table")
	hostDiv.style.fontSize = "50px"
	hostDiv.style.color = "black"
	hostDiv.id = "hostDiv"

	hostDiv.appendChild(document.createElement("p"))
	hostDiv.appendChild(document.createTextNode(`You have selected ${type} mode`))
	let line = document.createElement("hr")
	//line.style.width = "7px"
	line.color = "black"
	hostDiv.appendChild(line)
	hostDiv.appendChild(document.createElement("p"))

	content.appendChild(hostDiv)
}

function receive_host_join(data) {
	totalUsers = data.num;
	myGroupId = data.groupId;
	if (myPlayerType === PlayerType.JOIN) {
		create_host_table("JOIN",myGroupId)
	}
}

function host_game() {
	myGroupId = Math.random().toString(36).substr(2, 5);
	myPlayerType = PlayerType.HOST;
	joinWithID_click(myGroupId,PlayerType.HOST)

	create_host_table("HOST",myGroupId)
	display_game_id(myGroupId,PlayerType.HOST)
	let hostDiv = document.createElement("div")
	hostDiv.classList.add("center")
	hostDiv.classList.add("green")
	hostDiv.style.fontSize = "40px"
	hostDiv.style.top = "130px"
	canvasDiv.appendChild(hostDiv)

	let AI = document.createElement("input",{type:"number"})
	AI.style.width = "50px"
	AI.style.height = "50px"
	AI.min = 0;
	AI.max = 4;
	AI.placeholder = 0;
	AI.step = 1;
	AI.size = AI_width;
	AI.id = "AI"
	AI.oninput = function() {
		let value = parseInt(AI.value)
		if (!isNaN(value)) {
			numAI = value;
			let total = numAI + totalUsers;
			let numValid = total > 1 && total <= maxPlayers
			if (numValid) {
				display_start_button(true)
			} else {
				display_start_button(false)
			}
		} else {
			display_start_button(false)
		}
	}
	hostDiv.appendChild(document.createElement("br"))
	hostDiv.append("Number of AI players: ")
	hostDiv.appendChild(AI)
	hostDiv.appendChild(document.createElement("br"))
	hostDiv.appendChild(document.createElement("br"))
}

function join_game(){
	myPlayerType = PlayerType.JOIN

	$(content).empty()
	let inputDiv = document.createElement("div");
	inputDiv.classList.add("center")
	inputDiv.classList.add("green")
	inputDiv.classList.add("larger")
	inputDiv.appendChild(document.createElement("br"))
	inputDiv.appendChild(document.createTextNode("Enter the ID of the game you want to "+
		"join and press ENTER!"))
	inputDiv.appendChild(document.createElement("br"));
	inputDiv.appendChild(document.createElement("br"))
	inputDiv.appendChild(document.createElement("br"))

	let input = document.createElement("input");
	input.type = "text"
	input.id = "joinTextBox"
	input.style.height = "30px"
	input.style.width = "200px"
	input.onkeypress = function(event) {
		if (event.charCode === 13) {
			joinWithID_click(this.value,PlayerType.JOIN)
		}
	}

	inputDiv.appendChild(input)
	content.appendChild(inputDiv)
}

function joinWithID_click(value,player){
	params = {"reqType":REQ_TYPE.JOIN,"id":myId,"groupId":value,"player":player};
	conn.send(JSON.stringify(params))
}

function load_game() {
	initialize_group();
    start_new_game();
}

function initialize_group() {
	params = {"reqType":REQ_TYPE.LINK,
		"groupId":myGroupId,
		"ids":[myId],
		"gameType":GAME_TYPES.KLONDIKE,
		"AI":numAI};
	conn.send(JSON.stringify(params))
}
