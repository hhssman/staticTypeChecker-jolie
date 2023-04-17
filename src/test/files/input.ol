// from .embedMe import EmbedMe
// from .otherservice import EmbedInService, EmbedAsService, EmbedMeInterface1, EmbedMeInterface2

interface MyInterface {
	RequestResponse:
		helloReqRes( int )( string )
	OneWay:
		helloOneway( rec )
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
	x: rec
	y: rec
}

type rec: int {
	x: rec
}

type huh: int | string

service MyService(x: rec) {
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

	main {
		// a = 10
		// b = "hey"
		// c = true

		a = 10L
		b = "hey"
		c = a / b

		// i = 0
		// while(i < 4){
		// 	x.y << x

		// 	i++
		// }

	}
	
}