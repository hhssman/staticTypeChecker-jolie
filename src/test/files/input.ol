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

	main {
		a = 10
		a.x = "hey"
		a.z = true

		i = 0
		while(i < 4){
			a = "hey2"
			a.x = 10
			a.y = 20
			undef ( a.z )

			i++
		}

	}
	
}