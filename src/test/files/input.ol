// from .embedMe import EmbedMe
// from .otherservice import EmbedInService, EmbedAsService, EmbedMeInterface1, EmbedMeInterface2

interface MyInterface {
	RequestResponse:
		helloReqRes( int )( string )
	OneWay:
		helloOneway( rec1 )
}

// type one: int {
// 	x: one
// }

// type two: int {
// 	x: string {
// 		y: bool {
// 			z: two
// 		}
// 		z: two
// 	}
// }

// type three: int {
// 	x: string {
// 		y: three
// 	}
// 	|
// 	bool {
// 		z: string
// 	}
// }


type input: void {
	x: rec1
	y: rec1
}

type rec1: int {
	x: rec1
}


type rec2: int {
	x: int {
		x: int {
			x: rec2
			y: rec1
		}
	}
}

type huh: int | string

service MyService() {
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

	init {
		a = 10
	}
	
	// main {
	// 	a = 10
	// 	a.x = 10
	// 	a.x.y = 10

	// 	a.x = "hey"

	// 	// i = 0
	// 	// while(i < 4){
	// 	// 	z = a
	// 	// 	a = b
	// 	// 	b = z
	// 	// 	undef ( z )

	// 	// 	i++
	// 	// }

	// }
	
}