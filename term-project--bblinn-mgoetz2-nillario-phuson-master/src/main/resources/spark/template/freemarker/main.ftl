<!DOCTYPE html>
  <head>
    <meta charset="utf-8">
    <title>${title}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link rel="icon" type="image/png" href="/polytaire_icon.ico">
    <link rel="stylesheet" href="/css/normalize.css">
    <link rel="stylesheet" href="/css/html5bp.css">
    <link href="https://fonts.googleapis.com/css?family=Short+Stack" rel="stylesheet">
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
    <link rel="stylesheet" href="/css/solitaire.css">

  </head>
  <body onresize="resize_handler()">
	<div class = "center">
		<div class = "title">
      <h1 id = "name">
		<img src="images/Hearts.png" width="80px" height="50px">
		<img src="images/Diamonds.png" width="80px" height="50px">
		<img src="images/Hearts.png" width="80px" height="50px">
		<b>Polytaire</b>
		<img src="images/Diamonds.png" width="80px" height="50px">
    <img src="images/Hearts.png" width="80px" height="50px">
		<img src="images/Diamonds.png" width="80px" height="50px">
      </h1>
    </div>
	</div>

	<div id="content"></div>

  <div id="myID" class="myID"></div>
	<div id="myCanvas" class="myCanvas" style="position:relative">
	</div>

  <div id="opponentText" class="opponentText hidden">  </div>

  <div id="opponentTableauNum" class="opponentTableauNum"> </div>
  <script src="js/jquery-3.1.1.js"></script>
	<script src="js/canvas.js"></script>
	<script src="js/card.js"></script>
	<script src="js/websockets.js"></script>
	<script src="js/host_join.js"></script>
	<script src="js/powerup.js"></script>
  </body>
</html>
