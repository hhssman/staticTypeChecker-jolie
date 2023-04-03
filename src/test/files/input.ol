from .types import imp
from .embedMe import EmbedMe
// from .otherservice import EmbedInService, EmbedAsService, EmbedMeInterface1, EmbedMeInterface2

interface MyInterface {
	RequestResponse:
		helloReqRes( int )( string )
	OneWay:
		helloOneway( int )
}

type Y: string {
	x: Y
}

type X: int {
	x: X
}

type yo: int {
	x: bool
}

service MyService(param: yo) {
	inputPort in {
		Location: "socket://localhost:8080"
		Protocol: http { format = "json" }
		Interfaces: MyInterface 
	}
	
	outputPort out {
		Location: "socket://localhost:8081"
		Protocol: http { format = "json" }
		Interfaces: MyInterface
	}

	embed EmbedMe(param) as E

	main {
		x = 10
		x.x = "hey"
		// i = 0

		// while(i < 3){
		// 	x.y = x
		// 	i++
		// }
	}
	
}