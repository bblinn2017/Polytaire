let CANVAS_WIDTH = window.innerWidth;
let CANVAS_HEIGHT = window.innerHeight;
let CANVAS_CENTER = CANVAS_WIDTH / 2; //horizontal center
let color = "black"

// Global reference to the content div
let content;

// Global reference to the group id counter
let nextGroupId = 0;
let slotsCtx;
// Global reference to canvas div
let canvasDiv;

//holds display of opponent's X locs
let opponentXLocs = [];
let opponent_box_width = 0;
const GAME_TYPES = {
	KLONDIKE: 0
}

let scoreInterval;

// If the game is initialized
let init = false;

let setup = false;

// This handles resizing of the window
function resize_handler() {
    CANVAS_WIDTH = window.innerWidth;
    CANVAS_HEIGHT = window.innerHeight;
    CANVAS_CENTER = CANVAS_WIDTH/2;

    if (init) {
    	drawStartingSetup()
    	cards = cardsDict.getAll()
    	for (let i = 0; i < cards.length; i++) {
    		cards[i].updateImage()
    	}

    	set_images();
    }
}

function load_content(url,fx) {
    $.get(url, function(response) {
        content.innerHTML = response;

				if (fx !== undefined) {
					fx()
				}
    })
}

function changeField(elem,value) {
	elem.setAttribute("value",value)
	elem.innerHTML = `${elem.getAttribute("text")}${value}`
}

/*
	When the document is ready, this runs.
*/
$(document).ready(() => {

    // Set up the content
    content = document.getElementById("content")
    canvasDiv = document.getElementById("myCanvas")
    load_content("/homepage")

    // Setup live solitaire
    setup_live_solitaire();

});

/*
 * Setup location of decks
 */
function drawStartingSetup(){

	let slotDiv = document.getElementById("slots");
	$(slotDiv).empty();


	let slots = document.createElement("canvas")
	slots.width = CANVAS_WIDTH;
	slots.height= CANVAS_HEIGHT;
	slotsCtx = slots.getContext("2d")
	slotsCtx.strokeStyle = color;

	imgSources = ["images/Diamond.png","images/Heart.png","images/Club.png","images/Spade.png"]

	// Draw tableau foundations and stock
	for(let i = 0; i < 7; i++){
		let x = CANVAS_CENTER - centerOffset + i*cardSpaceWidth;

		// stock & foundation
		if (i !== 2) {
			slotsCtx.rect(x, foundation_height, cardWidth, cardHeight);
		}

		// tableau
		slotsCtx.rect(x, tableau_height, cardWidth, cardHeight);
	}

	canvasDiv.onclick = function(event) {
		let x = event.x;
		let y = event.y;
		let offsetY = $(canvasDiv).offset().top
		let withinY = y - offsetY >= foundation_height && y - offsetY <= foundation_height+cardHeight;
		let withinX = x >= CANVAS_CENTER - centerOffset && x <= CANVAS_CENTER - centerOffset + cardWidth
		let stock0Empty = cardsDict.getDeckLen(PILE_TYPE.STOCK,0) === 0;
		let stock1Empty = cardsDict.getDeckLen(PILE_TYPE.STOCK,1) === 0;

		if (withinX && withinY && stock0Empty && !stock1Empty) {
			cardInfo = [PILE_TYPE.STOCK,1,0];
			destInfo = [PILE_TYPE.STOCK,0]
			check_move(cardInfo,destInfo)
		}
	}

	//draw opponent boards
	for(let i = 0; i < totalUsers-1; i++){
		let x = (CANVAS_WIDTH / (totalUsers)) * i + space;
		opponent_box_width = CANVAS_WIDTH / ((totalUsers))
		opponentOffset = (opponent_box_width / 2) - (3 * opponentCardSpaceWidth)
		opponentXLocs[i] = x;
		slotsCtx.rect(x, opponent_height, opponent_box_width, CANVAS_HEIGHT - opponent_height);
	}

	slotsCtx.stroke();
	let sImage = document.createElement("img")
	sImage.src = slots.toDataURL();
	sImage.style = "position:absolute"

	slotDiv.appendChild(sImage)

	// foundation images
	for (let i = 0; i < 4; i++) {
		let x = CANVAS_CENTER - centerOffset + (i+3)*cardSpaceWidth;
		let size = cardWidth/2;

		let imgSrc = imgSources[i];
		let newImg = new Image(size,size);
		newImg.src = imgSrc;

		let style = "position:absolute;"
		style += `top:${foundation_height+cardHeight/2-size/2}px;`
		style += `left:${x+size/2}px`
		newImg.style = style


		newImg.top = "10px";

		slotDiv.appendChild(newImg)
	}

	//put myID on div
	let idDiv = document.getElementById("myID")
	let idText = document.createTextNode("Your ID is: " + myId)
	idDiv.style.fontSize = '30px';
	idDiv.appendChild(idText)
}
